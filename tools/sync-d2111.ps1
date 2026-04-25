<#
.SYNOPSIS
    Verify and sync GoMule's d2111/ data dir against the active D2R mod (MDK) and vanilla D2R.

.DESCRIPTION
    GoMule loads a fixed set of *.txt tables on startup (see
    src/main/java/randall/d2files/D2TxtFile.java). For correct .d2s/.d2i parsing
    of saves produced by a modded D2R install, those .txt files must match the
    *exact* tables the game used.

    Resolution priority (mirrors D2R MPQ load order):
      1. Mod source        (MDK V3 MPQ)
      2. Vanilla D2R dump  (Casc Viewer extract)

    By default the script REPORTS only. Use -Apply to overwrite d2111/.

.PARAMETER Apply
    Actually copy mismatched files into d2111/. Without -Apply the script only
    prints a diff report.

.PARAMETER Mod
    Mod excel dir. Default: MDK V3.

.PARAMETER Vanilla
    Vanilla D2R excel dump. Default: Casc Viewer extract.

.EXAMPLE
    pwsh ./tools/sync-d2111.ps1
    pwsh ./tools/sync-d2111.ps1 -Apply
#>
[CmdletBinding()]
param(
    [switch]$Apply,
    [string]$Mod     = 'D:\BlizGames\Diablo II Resurrected\mods\D2RMMMDKV3\D2RMMMDKV3.mpq\data\global\excel',
    [string]$Vanilla = 'D:\260305\D2RM_Ladiks Casc Viewer\Work\data\data\global\excel',
    [string]$Dst     = ''
)

$ErrorActionPreference = 'Stop'

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
if (-not $Dst) { $Dst = Join-Path $ScriptDir '..\d2111' }

# Auto-discovered list of tables GoMule actually loads.
$java = Join-Path $ScriptDir '..\src\main\java\randall\d2files\D2TxtFile.java'
$needed = Select-String -Path $java -Pattern 'new D2TxtFile\("([^"]+)"\)' -AllMatches |
    ForEach-Object { $_.Matches } | ForEach-Object { $_.Groups[1].Value } | Sort-Object -Unique

if (-not (Test-Path $Mod))     { throw "Mod excel dir not found: $Mod" }
if (-not (Test-Path $Vanilla)) { throw "Vanilla excel dir not found: $Vanilla" }
if (-not (Test-Path $Dst))     { throw "GoMule d2111 dir not found: $Dst" }

$results = foreach ($name in $needed) {
    $modFile = Get-ChildItem $Mod     -Filter "$name.txt" -EA SilentlyContinue | Select-Object -First 1
    $vanFile = Get-ChildItem $Vanilla -Filter "$name.txt" -EA SilentlyContinue | Select-Object -First 1
    $dstFile = Get-ChildItem $Dst     -Filter "$name.txt" -EA SilentlyContinue | Select-Object -First 1

    $src = if ($modFile) { $modFile } elseif ($vanFile) { $vanFile } else { $null }
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
    Write-Host "`n✓ d2111/ is in sync with mod+vanilla sources." -ForegroundColor Green
    exit 0
}

if (-not $Apply) {
    Write-Host "`n$(($todo).Count) file(s) out of sync. Re-run with -Apply to fix." -ForegroundColor Yellow
    exit 1
}

foreach ($row in $todo) {
    $srcDir = if ($row.Origin -eq 'mod') { $Mod } else { $Vanilla }
    $srcFile = Join-Path $srcDir "$($row.Name).txt"
    $dstFile = Join-Path $Dst    "$($row.Name).txt"
    Copy-Item $srcFile $dstFile -Force
    Write-Host "  copied [$($row.Origin)] $($row.Name).txt  ($($row.Src) bytes)"
}
Write-Host "`n✓ Synced $($todo.Count) file(s) into $Dst" -ForegroundColor Green
