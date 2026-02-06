# Build Guide

This guide targets Windows and uses PowerShell to run the commands below. It provides two paths: fully automatic build and manual build.

[ZH-CN](BUILD_GUIDE.md)

## Automatic Build

Run in the repo root:

```powershell
.\build.ps1 -Task build_all
```

Build JNI only:

```powershell
.\build.ps1 -Task build_jni
```

Common options:

```powershell
.\build.ps1 -Task build_jni -Config Release
.\build.ps1 -Task build_jni -SkipX86
.\build.ps1 -Task build_jni -SkipX64
```

## Manual Build

### 1) Initialize submodules

```powershell
git submodule update --init --recursive
```

### 2) Build JNI (x64 + x86)

```powershell
cmake -S common/src/main/cpp -B build/jni/x64 -A x64
cmake --build build/jni/x64 --config Release --target jni

cmake -S common/src/main/cpp -B build/jni/x86 -A Win32
cmake --build build/jni/x86 --config Release --target jni
```

JNI DLL output:

```
common/src/main/resources/assets/ingameime/natives
```

### 3) Build the mod

```powershell
.\gradlew.bat build
```

## Requirements

- CMake 3.18+
- JDK (with JNI headers)
- Visual Studio Build Tools (MSVC)

To specify a generator, pass `-Generator` to build.ps1, for example:

```powershell
.\build.ps1 -Task build_jni -Generator "Visual Studio 17 2022"
```
