param(
    [switch]$CheckOnly
)

$ErrorActionPreference = "Stop"

$root = Resolve-Path "$PSScriptRoot/.."
$patchDir = Join-Path $root "patches/submodules"

$targets = @(
    @{ Name = "IngameIME"; Path = Join-Path $root "3rd/IngameIME"; Patch = Join-Path $patchDir "IngameIME.patch" },
    @{ Name = "libtf"; Path = Join-Path $root "3rd/IngameIME/libtf"; Patch = Join-Path $patchDir "libtf.patch" },
    @{ Name = "IngameIME_Win32"; Path = Join-Path $root "3rd/IngameIME_Win32"; Patch = Join-Path $patchDir "IngameIME_Win32.patch" },
    @{ Name = "IngameIME-Common"; Path = Join-Path $root "3rd/IngameIME-Common"; Patch = Join-Path $patchDir "IngameIME-Common.patch" },
    @{ Name = "IngameIME-Common (Win32)"; Path = Join-Path $root "3rd/IngameIME_Win32/IngameIME-Common"; Patch = Join-Path $patchDir "IngameIME-Common.patch" }
)

foreach ($t in $targets) {
    if (-not (Test-Path $t.Patch)) {
        Write-Warning "Patch not found: $($t.Patch)"
        continue
    }
    if (-not (Test-Path $t.Path)) {
        throw "Submodule path not found: $($t.Path)"
    }

    Write-Host "Applying $($t.Name) patch..." -ForegroundColor Cyan

    if ($CheckOnly) {
        git -C $t.Path apply --check --recount $t.Patch
        if ($LASTEXITCODE -eq 0) {
            Write-Host "Check passed: $($t.Name)" -ForegroundColor Green
            continue
        }

        git -C $t.Path apply --reverse --check --recount $t.Patch
        if ($LASTEXITCODE -eq 0) {
            Write-Host "Already applied: $($t.Name)" -ForegroundColor Yellow
            continue
        }

        throw "Patch check failed: $($t.Name)"
    } else {
        git -C $t.Path apply --recount $t.Patch
        if ($LASTEXITCODE -eq 0) {
            git -C $t.Path add -A
            Write-Host "Applied and staged: $($t.Name)" -ForegroundColor Green
            continue
        }

        git -C $t.Path apply --reverse --check --recount $t.Patch
        if ($LASTEXITCODE -eq 0) {
            Write-Host "Already applied: $($t.Name)" -ForegroundColor Yellow
            continue
        }

        throw "Patch apply failed: $($t.Name)"
    }
}
