<center><div align="center">

<img height="150" width="150" src="icon/400x400.png"/>

# XPlusContingameIME (Unofficial NeoForge Port)

An unofficial NeoForge port of [XPlus-ContingameIME](https://github.com/Wudji/XPlus-ContingameIME). Use input method in full screen Minecraft, with fixes and improvements over the original.

**Note: This version only supports NeoForge. For the original version supporting both Fabric, please visit the [original project](https://github.com/Wudji/XPlus-ContingameIME)**

[ZH-CN](README.md) / EN-US

</div></center>

# Highlights

- Now works on the NeoForge loader (Minecraft 1.21-1.21.3).
- Detects the active IME instead of brute-checking the client locale.
- Candidate list uses a clear selection highlight.
- Fixes caret tracking when Caxton or Modern UI is installed.
- JNI DLL source and build parameters are included; project layout is refactored and managed via git submodules for better maintainability.

# Preview

### Window Mode

<div align="center">
<img height="250" width="450" src="old/docs/WindowInput.gif"/>
</div>

### Full screen Mode

<div align="center">
<img height="250" width="450" src="old/docs/FullScreenInput.gif"/>
</div>

# Toggle Key

- Click the hotkey to switch between **temporary input state** and **closed state**
- Double-click the hot key to switch to **open mode**
- **temporary input state** switch to **closed State** when mouse move and something was committed

# Build Guide

- See [BUILD_GUIDE_EN.md](BUILD_GUIDE_EN.md)

## Dependencies

- NeoForge
    - [Kotlin for NeoForge](https://www.curseforge.com/minecraft/mc-mods/kotlin-for-forge)
    - [Cloth Config API (NeoForge)](https://www.curseforge.com/minecraft/mc-mods/cloth-config)
    - [Architectury API (NeoForge)](https://www.curseforge.com/minecraft/mc-mods/architectury-api)


### This Mod requires VCRuntime141 to run, download it on the official website of Microsoft [Download Link](https://learn.microsoft.com/en-US/cpp/windows/latest-supported-vc-redist)

# This mod code reference/use

- [Windmill-City/IngameIME-Minecraft](https://github.com/Windmill-City/IngameIME-Minecraft) (LGPL-2.1)
- [Windmill-City/IngameIME](https://github.com/Windmill-City/IngameIME) (LGPL-2.1)
- [Wybxc/IngameIME-Minecraft](https://github.com/Wybxc/IngameIME-Minecraft) (LGPL-2.1)
