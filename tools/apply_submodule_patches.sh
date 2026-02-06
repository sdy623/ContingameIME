#!/usr/bin/env bash
set -euo pipefail

check_only=false
if [[ "${1-}" == "--check" ]]; then
  check_only=true
fi

root_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
patch_dir="$root_dir/patches/submodules"

targets=(
  "IngameIME|$root_dir/3rd/IngameIME|$patch_dir/IngameIME.patch"
  "libtf|$root_dir/3rd/IngameIME/libtf|$patch_dir/libtf.patch"
)

for t in "${targets[@]}"; do
  IFS='|' read -r name path patch <<< "$t"

  if [[ ! -f "$patch" ]]; then
    echo "Warning: patch not found: $patch" >&2
    continue
  fi
  if [[ ! -d "$path" ]]; then
    echo "Error: submodule path not found: $path" >&2
    exit 1
  fi

  echo "Applying $name patch..."
  if $check_only; then
    if git -C "$path" apply --check --recount "$patch"; then
      echo "Check passed: $name"
      continue
    fi

    if git -C "$path" apply --reverse --check --recount "$patch"; then
      echo "Already applied: $name"
      continue
    fi

    echo "Patch check failed: $name" >&2
    exit 1
  else
    if git -C "$path" apply --recount "$patch"; then
      git -C "$path" add -A
      echo "Applied and staged: $name"
      continue
    fi

    if git -C "$path" apply --reverse --check --recount "$patch"; then
      echo "Already applied: $name"
      continue
    fi

    echo "Patch apply failed: $name" >&2
    exit 1
  fi

done
