<#
.SYNOPSIS
    Unified GoMule prelaunch sync using TylerPack first and orig fallback.

.DESCRIPTION
    Syncs both tracked translations under src/main/resources/d2Files/D2R_1.0
    and the d2111 tables required by GoMule before launch.

    Source priority is:
      1. TylerPack deployed data
      2. orig vanilla dump

    Files with identical hashes are skipped. If TylerPack is missing a required
    file but orig has it, the orig file is used and logged as a fallback.
    If neither TylerPack nor orig provides a required file, the script fails.

    By default the script reports only. Use -Apply to sync changed files.

.PARAMETER Apply
    Copy changed files into GoMule tracked resources and d2111.

.PARAMETER RefreshDistribution
    Refresh the GoMule distribution after sync completes.

.EXAMPLE
    pwsh ./tools/prelaunch-sync-tylerpack.ps1
    pwsh ./tools/prelaunch-sync-tylerpack.ps1 -Apply
    pwsh ./tools/prelaunch-sync-tylerpack.ps1 -Apply -RefreshDistribution
#>
[CmdletBinding()]
param(
    [switch]$Apply,
    [switch]$RefreshDistribution
)

$ErrorActionPreference = 'Stop'

function Get-TrackedTranslationNames {
    param([string]$LoaderPath)

    Select-String -Path $LoaderPath -Pattern 'translations/([a-zA-Z0-9_-]+)\.json' -AllMatches |
        ForEach-Object { $_.Matches } |
        ForEach-Object { $_.Groups[1].Value } |
        Sort-Object -Unique
}

function Get-TrackedD2111Names {
    param([string]$JavaPath)

    Select-String -Path $JavaPath -Pattern 'new D2TxtFile\("([^"]+)"\)' -AllMatches |
        ForEach-Object { $_.Matches } |
        ForEach-Object { $_.Groups[1].Value } |
        Sort-Object -Unique
}

function New-SyncResult {
    param(
        [string]$Group,
        [string]$Relative,
        [string]$Status,
        [string]$Origin,
        [long]$Src,
        [long]$Dst
    )

    [pscustomobject]@{
        Group    = $Group
        Relative = $Relative
        Status   = $Status
        Origin   = $Origin
        Src      = $Src
        Dst      = $Dst
    }
}

function Compare-SyncTarget {
    param(
        [string]$Group,
        [string]$Relative,
        [string]$DestinationPath,
        [string]$TylerPackPath,
        [string]$OrigPath,
        [switch]$Protected
    )

    $dstExists = Test-Path $DestinationPath
    $dstLength = if ($dstExists) { (Get-Item $DestinationPath).Length } else { 0 }

    if ($Protected) {
        return New-SyncResult -Group $Group -Relative $Relative -Status 'PROTECTED' -Origin 'gomule' -Src 0 -Dst $dstLength
    }

    $srcPath = $null
    $origin = 'NONE'
    if (Test-Path $TylerPackPath) {
        $srcPath = $TylerPackPath
        $origin = 'tylerpack'
    } elseif (Test-Path $OrigPath) {
        $srcPath = $OrigPath
        $origin = 'orig'
    }

    if (-not $srcPath) {
        return New-SyncResult -Group $Group -Relative $Relative -Status 'NO-SOURCE' -Origin $origin -Src 0 -Dst $dstLength
    }

    $srcLength = (Get-Item $srcPath).Length
    if (-not $dstExists) {
        return New-SyncResult -Group $Group -Relative $Relative -Status 'ADD' -Origin $origin -Src $srcLength -Dst 0
    }

    $srcHash = (Get-FileHash $srcPath -Algorithm SHA1).Hash
    $dstHash = (Get-FileHash $DestinationPath -Algorithm SHA1).Hash
    $status = if ($srcHash -eq $dstHash) { 'OK' } else { 'DIFF' }

    return New-SyncResult -Group $Group -Relative $Relative -Status $status -Origin $origin -Src $srcLength -Dst $dstLength
}

function Write-SyncSummary {
    param(
        [string]$Title,
        [object[]]$Rows
    )

    Write-Host ""
    Write-Host $Title -ForegroundColor Cyan
    $Rows | Format-Table -AutoSize
}

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectRoot = (Resolve-Path (Join-Path $ScriptDir '..')).Path
$TylerPackData = (Resolve-Path 'D:\BlizGames\Diablo II Resurrected\mods\TylerPack\TylerPack.mpq\data').Path
$OrigData = (Resolve-Path 'D:\260305\D2RM_Ladiks Casc Viewer\Work\data\data').Path
$TrackedTranslationsRoot = (Resolve-Path (Join-Path $ProjectRoot 'src\main\resources\d2Files\D2R_1.0\translations')).Path
$TrackedD2111Root = (Resolve-Path (Join-Path $ProjectRoot 'd2111')).Path
$TranslationsLoader = Join-Path $ProjectRoot 'src\main\java\gomule\translations\TranslationsLoader.java'
$D2TxtFileJava = Join-Path $ProjectRoot 'src\main\java\randall\d2files\D2TxtFile.java'

if (-not (Test-Path $TylerPackData)) { throw "TylerPack data dir not found: $TylerPackData" }
if (-not (Test-Path $OrigData)) { throw "orig data dir not found: $OrigData" }
if (-not (Test-Path $TrackedTranslationsRoot)) { throw "Tracked translations dir not found: $TrackedTranslationsRoot" }
if (-not (Test-Path $TrackedD2111Root)) { throw "Tracked d2111 dir not found: $TrackedD2111Root" }
if (-not (Test-Path $TranslationsLoader)) { throw "TranslationsLoader.java not found: $TranslationsLoader" }
if (-not (Test-Path $D2TxtFileJava)) { throw "D2TxtFile.java not found: $D2TxtFileJava" }

$translationResults = foreach ($name in Get-TrackedTranslationNames -LoaderPath $TranslationsLoader) {
    $relative = "translations\$name.json"
    $dstPath = Join-Path $TrackedTranslationsRoot "$name.json"
    $tylerPackPath = Join-Path $TylerPackData "local\lng\strings\$name.json"
    $origPath = Join-Path $OrigData "local\lng\strings\$name.json"
    Compare-SyncTarget -Group 'translations' -Relative $relative -DestinationPath $dstPath -TylerPackPath $tylerPackPath -OrigPath $origPath -Protected:($name -eq 'custom-gomule')
}

$d2111Results = foreach ($name in Get-TrackedD2111Names -JavaPath $D2TxtFileJava) {
    $relative = "d2111\$name.txt"
    $dstPath = Join-Path $TrackedD2111Root "$name.txt"
    $tylerPackPath = Join-Path $TylerPackData "global\excel\$name.txt"
    $origPath = Join-Path $OrigData "global\excel\$name.txt"
    Compare-SyncTarget -Group 'd2111' -Relative $relative -DestinationPath $dstPath -TylerPackPath $tylerPackPath -OrigPath $origPath
}

Write-SyncSummary -Title 'Translations' -Rows $translationResults
Write-SyncSummary -Title 'd2111' -Rows $d2111Results

$allResults = @($translationResults) + @($d2111Results)
$fallbackRows = $allResults | Where-Object Origin -eq 'orig'
$missingRows = $allResults | Where-Object Status -eq 'NO-SOURCE'
$todoRows = $allResults | Where-Object { $_.Status -in @('ADD', 'DIFF') }

if ($fallbackRows) {
    Write-Warning "Using orig fallback for ($($fallbackRows.Count)): $(($fallbackRows.Relative) -join ', ')"
}

if ($missingRows) {
    Write-Error "Missing from both TylerPack and orig ($($missingRows.Count)): $(($missingRows.Relative) -join ', ')"
    exit 2
}

if (-not $todoRows) {
    Write-Host "`nOK: TylerPack prelaunch sync is already up to date." -ForegroundColor Green
    if (-not $RefreshDistribution) {
        exit 0
    }
}

if (-not $Apply) {
    Write-Host "`n$($todoRows.Count) file(s) differ. Re-run with -Apply to sync." -ForegroundColor Yellow
    exit 1
}

$copied = @()
foreach ($row in $todoRows) {
    $fileName = [System.IO.Path]::GetFileName($row.Relative)
    $targetPath = if ($row.Group -eq 'translations') {
        Join-Path $TrackedTranslationsRoot $fileName
    } else {
        Join-Path $TrackedD2111Root $fileName
    }

    $sourcePath = if ($row.Group -eq 'translations') {
        if ($row.Origin -eq 'tylerpack') {
            Join-Path $TylerPackData ('local\lng\strings\' + $fileName)
        } else {
            Join-Path $OrigData ('local\lng\strings\' + $fileName)
        }
    } else {
        if ($row.Origin -eq 'tylerpack') {
            Join-Path $TylerPackData ('global\excel\' + $fileName)
        } else {
            Join-Path $OrigData ('global\excel\' + $fileName)
        }
    }

    Copy-Item $sourcePath $targetPath -Force
    $copied += $row.Relative
    Write-Host "  copied [$($row.Origin)] $($row.Relative)"
}

Write-Host "`nOK: synced $($copied.Count) file(s) from TylerPack/orig sources." -ForegroundColor Green

if (-not $RefreshDistribution) {
    exit 0
}

Push-Location $ProjectRoot
try {
    & .\gradlew.bat createDistribution copyResourcesToDistribution copyJarToDistribution -x test
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle refresh failed with exit code $LASTEXITCODE"
    }
} finally {
    Pop-Location
}

Write-Host "OK: TylerPack prelaunch sync and distribution refresh completed." -ForegroundColor Green
