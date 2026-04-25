<#
.SYNOPSIS
    Verify and sync GoMule's translations/ JSON files against the active D2R
    mod (MDK V3) and vanilla D2R lng dumps.

.DESCRIPTION
    GoMule loads a fixed set of *.json translation files on startup (see
    src/main/java/gomule/translations/TranslationsLoader.java).
    Resolution priority (mirrors D2R MPQ load order):
      1. Mod source        (MDK V3 MPQ data/local/lng/strings)
      2. Vanilla D2R dump  (Casc Viewer extract)

    custom-gomule.json is GoMule-specific and is NEVER overwritten.

    Compatibility notes (verified 2026-04-25):
      - GoMule's MapBasedTranslations.loadTranslations reads ONLY 'Key' + 'enUS'
        from each JSON entry; other locale fields (zhCN / zhTW / deDE / ...)
        are silently ignored. Source files keep them — Jackson tolerates extras.
      - JSON is an array of {id, Key, enUS, ...}. Both MDK and vanilla use this
        format. Jackson handles UTF-8 BOM transparently.
      - Some translation files (skills.json, ui-controller.json) are not in
        MDK lng — fall back to vanilla.

    By default the script REPORTS only. Use -Apply to overwrite.

.PARAMETER Apply
    Actually copy mismatched files. Without -Apply only prints a diff report.

.PARAMETER Mod
    Mod lng dir. Default: MDK V3.

.PARAMETER Vanilla
    Vanilla D2R lng dump. Default: Casc Viewer extract.

.EXAMPLE
    pwsh ./tools/sync-translations.ps1
    pwsh ./tools/sync-translations.ps1 -Apply
#>
[CmdletBinding()]
param(
    [switch]$Apply,
    [string]$Mod     = 'D:\BlizGames\Diablo II Resurrected\mods\D2RMMMDKV3\D2RMMMDKV3.mpq\data\local\lng\strings',
    [string]$Vanilla = 'D:\260305\D2RM_Ladiks Casc Viewer\Work\data\data\local\lng\strings',
    [string]$Dst     = ''
)

$ErrorActionPreference = 'Stop'

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
if (-not $Dst) {
    $Dst = Join-Path $ScriptDir '..\src\main\resources\d2Files\D2R_1.0\translations'
}

# Auto-discovered list of files GoMule actually loads.
$loader  = Join-Path $ScriptDir '..\src\main\java\gomule\translations\TranslationsLoader.java'
$needed  = Select-String -Path $loader -Pattern 'translations/([a-zA-Z0-9_-]+)\.json' -AllMatches |
    ForEach-Object { $_.Matches } | ForEach-Object { $_.Groups[1].Value } |
    Sort-Object -Unique |
    Where-Object { $_ -ne 'custom-gomule' }   # protect GoMule-only file

if (-not (Test-Path $Mod))     { throw "Mod lng dir not found: $Mod" }
if (-not (Test-Path $Vanilla)) { throw "Vanilla lng dir not found: $Vanilla" }
if (-not (Test-Path $Dst))     { throw "GoMule translations dir not found: $Dst" }

$results = foreach ($name in $needed) {
    $modFile = Get-ChildItem $Mod     -Filter "$name.json" -EA SilentlyContinue | Select-Object -First 1
    $vanFile = Get-ChildItem $Vanilla -Filter "$name.json" -EA SilentlyContinue | Select-Object -First 1
    $dstFile = Get-ChildItem $Dst     -Filter "$name.json" -EA SilentlyContinue | Select-Object -First 1

    $src    = if ($modFile) { $modFile } elseif ($vanFile) { $vanFile } else { $null }
    $origin = if ($modFile) { 'mod' } elseif ($vanFile) { 'vanilla' } else { 'NONE' }

    if (-not $src) {
        [pscustomobject]@{ Name=$name; Status='NO-SOURCE'; Origin=$origin; Src=0; Dst=$(if($dstFile){$dstFile.Length}else{0}) }
        continue
    }
    if (-not $dstFile) {
        [pscustomobject]@{ Name=$name; Status='ADD';       Origin=$origin; Src=$src.Length; Dst=0 }
        continue
    }
    $hs = (Get-FileHash $src.FullName     -Algorithm SHA1).Hash
    $hd = (Get-FileHash $dstFile.FullName -Algorithm SHA1).Hash
    if ($hs -eq $hd) {
        [pscustomobject]@{ Name=$name; Status='OK';        Origin=$origin; Src=$src.Length; Dst=$dstFile.Length }
    } else {
        [pscustomobject]@{ Name=$name; Status='DIFF';      Origin=$origin; Src=$src.Length; Dst=$dstFile.Length }
    }
}

$results | Format-Table -AutoSize

$todo = $results | Where-Object Status -in 'DIFF','ADD'
$bad  = $results | Where-Object Status -eq  'NO-SOURCE'

if ($bad)  { Write-Warning "Files with no source ($($bad.Count)): $(($bad.Name) -join ', ')" }

if (-not $todo) {
    Write-Host "`n✓ translations/ is in sync with mod+vanilla sources." -ForegroundColor Green
    exit 0
}

if (-not $Apply) {
    Write-Host "`n$(($todo).Count) file(s) out of sync. Re-run with -Apply to fix." -ForegroundColor Yellow
    exit 1
}

foreach ($row in $todo) {
    $srcDir = if ($row.Origin -eq 'mod') { $Mod } else { $Vanilla }
    $srcFile = Join-Path $srcDir "$($row.Name).json"
    $dstFile = Join-Path $Dst    "$($row.Name).json"
    Copy-Item $srcFile $dstFile -Force
    Write-Host "  copied [$($row.Origin)] $($row.Name).json  ($($row.Src) bytes)"
}

Write-Host "`n✓ translations/ updated. Rebuild GoMule:" -ForegroundColor Green
Write-Host "    .\gradlew.bat createDistribution copyResourcesToDistribution copyJarToDistribution -x test" -ForegroundColor Cyan
