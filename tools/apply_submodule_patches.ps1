param(
    [switch]$CheckOnly
)

$ErrorActionPreference = "Stop"

$root = Resolve-Path "$PSScriptRoot/.."
$patchDir = Join-Path $root "patches/submodules"

$targets = @(
    @{ Name = "IngameIME"; Path = Join-Path $root "3rd/IngameIME"; Patch = Join-Path $patchDir "IngameIME.patch" },
    @{ Name = "libtf"; Path = Join-Path $root "3rd/IngameIME/libtf"; Patch = Join-Path $patchDir "libtf.patch" }
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
