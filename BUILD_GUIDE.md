# 构建指南

本文适用于 Windows 环境，默认使用 PowerShell 执行命令。包含两种构建方式：全自动构建和手动构建。

[EN-US](BUILD_GUIDE_EN.md)

## 全自动构建

在仓库根目录执行：

```powershell
.\build.ps1 -Task build_all
```

只构建 JNI：

```powershell
.\build.ps1 -Task build_jni
```

常用参数：

```powershell
.\build.ps1 -Task build_jni -Config Release
.\build.ps1 -Task build_jni -SkipX86
.\build.ps1 -Task build_jni -SkipX64
```

## 手动构建

### 1) 初始化子模块

```powershell
git submodule update --init --recursive
```

### 2) 应用子模块补丁

```powershell
.\tools\apply_submodule_patches.ps1
```

### 3) 安装 Git Hooks

```powershell
.\tools\install_hooks.ps1
```

### 4) 切换分支前保存子模块改动

```powershell
git submodule foreach --recursive "git stash"
```

### 5) 构建 JNI (x64 + x86)

```powershell
cmake -S common/src/main/cpp -B build/jni/x64 -A x64
cmake --build build/jni/x64 --config Release --target jni

cmake -S common/src/main/cpp -B build/jni/x86 -A Win32
cmake --build build/jni/x86 --config Release --target jni
```

JNI DLL 会输出到：

```
common/src/main/resources/assets/kitsuneime/natives
```

### 6) 构建模组

```powershell
.\gradlew.bat build
```

## 依赖说明

- CMake 3.18+
- JDK (包含 JNI 头文件)
- Visual Studio Build Tools (用于 MSVC)

如需指定生成器，可在 build.ps1 中通过 `-Generator` 传入，例如：

```powershell
.\build.ps1 -Task build_jni -Generator "Visual Studio 17 2022"
```
