$ErrorActionPreference = "Stop"

$repoRoot = Resolve-Path "$PSScriptRoot/.."
Set-Location $repoRoot

git config core.hooksPath .githooks

Write-Host "Configured core.hooksPath to .githooks" -ForegroundColor Green
