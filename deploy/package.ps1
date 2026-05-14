<#
.SYNOPSIS
  Build the Fleet Maintenance System and assemble a deployable
  distribution under dist/.

.DESCRIPTION
  1) Runs `mvn clean package -DskipTests`
  2) Copies the resulting JAR + deploy/config + run scripts into
     dist/fleet-maintenance-<version>/
  3) Zips the assembled folder.

.PARAMETER SkipBuild
  Skip the Maven build step (use the JAR already in target/).

.PARAMETER NoZip
  Do not produce a .zip archive at the end.

.EXAMPLE
  .\deploy\package.ps1
.EXAMPLE
  .\deploy\package.ps1 -SkipBuild
#>
[CmdletBinding()]
param(
    [switch]$SkipBuild,
    [switch]$NoZip
)

$ErrorActionPreference = 'Stop'

$RepoRoot   = (Resolve-Path "$PSScriptRoot\..").Path
$DeployDir  = Join-Path $RepoRoot 'deploy'
$TargetDir  = Join-Path $RepoRoot 'target'
$DistRoot   = Join-Path $RepoRoot 'dist'

Push-Location $RepoRoot
try {
    if (-not $SkipBuild) {
        Write-Host "[1/3] Building JAR (mvn clean package -DskipTests)..." -ForegroundColor Cyan
        mvn -B clean package -DskipTests
        if ($LASTEXITCODE -ne 0) { throw "Maven build failed (exit $LASTEXITCODE)" }
    } else {
        Write-Host "[1/3] Skipping build (-SkipBuild)" -ForegroundColor Yellow
    }

    $jar = Get-ChildItem -Path $TargetDir -Filter 'fleet-maintenance-system-*.jar' `
        | Where-Object { $_.Name -notmatch '\.original$' } `
        | Select-Object -First 1
    if (-not $jar) { throw "Could not find fleet-maintenance-system-*.jar under $TargetDir" }

    $version = ($jar.BaseName -replace '^fleet-maintenance-system-', '')
    $stage   = Join-Path $DistRoot "fleet-maintenance-$version"

    Write-Host "[2/3] Staging $stage" -ForegroundColor Cyan
    if (Test-Path $stage) { Remove-Item $stage -Recurse -Force }
    New-Item -ItemType Directory -Path $stage -Force | Out-Null
    New-Item -ItemType Directory -Path (Join-Path $stage 'config') -Force | Out-Null
    New-Item -ItemType Directory -Path (Join-Path $stage 'logs')   -Force | Out-Null

    New-Item -ItemType Directory -Path (Join-Path $stage 'nginx-sample') -Force | Out-Null

    Copy-Item -Path $jar.FullName                                 -Destination $stage -Force
    Copy-Item -Path (Join-Path $DeployDir 'config\*')             -Destination (Join-Path $stage 'config') -Recurse -Force
    Copy-Item -Path (Join-Path $DeployDir 'nginx\*')              -Destination (Join-Path $stage 'nginx-sample') -Recurse -Force
    Copy-Item -Path (Join-Path $DeployDir 'run.bat')              -Destination $stage -Force
    Copy-Item -Path (Join-Path $DeployDir 'run.sh')               -Destination $stage -Force
    Copy-Item -Path (Join-Path $DeployDir 'ecosystem.config.js')  -Destination $stage -Force
    Copy-Item -Path (Join-Path $DeployDir 'README.md')            -Destination $stage -Force

    if (-not $NoZip) {
        $zip = Join-Path $DistRoot "fleet-maintenance-$version.zip"
        Write-Host "[3/3] Compressing -> $zip" -ForegroundColor Cyan
        if (Test-Path $zip) { Remove-Item $zip -Force }
        Compress-Archive -Path (Join-Path $stage '*') -DestinationPath $zip -Force
        Write-Host ""
        Write-Host ("Done. Folder: {0}" -f $stage) -ForegroundColor Green
        Write-Host ("      Zip:    {0}" -f $zip)   -ForegroundColor Green
    } else {
        Write-Host "[3/3] Skipping zip (-NoZip)" -ForegroundColor Yellow
        Write-Host ""
        Write-Host ("Done. Folder: {0}" -f $stage) -ForegroundColor Green
    }
}
finally {
    Pop-Location
}
