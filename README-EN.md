<center><div align="center">

<img height="150" width="150" src="icon/400x400.png"/>

# ContingameIME-Neo (Maintenance Fork)

This mod is a **subsequent maintenance version (unofficial NeoForge port)** of [XPlus-ContingameIME](https://github.com/Wudji/XPlus-ContingameIME), further developed with reference to [IngameIME](https://github.com/Windmill-City/IngameIME-Minecraft).

It incorporates the JNI library source code written and used by the Windmill-City author, managed via git submodules, restoring JNI DLL source code and build parameters, and restructuring the project directory to enhance maintainability. It enables Input Method usage in full-screen Minecraft, with various fixes and improvements based on the original version.

**Note: This version only supports the NeoForge platform. For the version supporting Fabric, please visit the [original project](https://github.com/Wudji/XPlus-ContingameIME).**

[ZH-CN](README.md) / EN-US

</div></center>

# Highlights

- Now works on the NeoForge loader (Minecraft 1.21-1.21.3).
- Detects the active IME instead of brute-checking the client locale.
- Candidate list uses a clear selection highlight with customizable highlight color in config.
- Fixed bug where IME candidate window would not display after toggling onscreen and restarting.
- Fixes caret tracking when Caxton or Modern UI is installed.

# Preview

### Window Mode Demo

<div align="center">
<img height="360" src="pictures/McModIme-window-ingameime-intg.png" alt="Window Mode Demo Integrated"/>
</div>

<div align="center">
<img height="350" src="pictures/gif/00Windowed_Text_Box_Input.gif" alt="Window Mode Demo"/>
</div>

### Fullscreen Mode Demo

<div align="center">
<img height="330" src="pictures/gif/01Fullscreen_Text_Box_Input_All.gif" alt="Fullscreen Mode Demo"/>
</div>

### Input Styles Adaptation

The IME integrates perfectly even in special interfaces.

> 🎥 **View Animated GIFs:** [GALLERY.md](GALLERY.md)

|  | Fullscreen (Fullscreen) | Windowed (Windowed) |
| :---: | :---: | :---: |
| **Sign** | ![Sign-Fullscreen](pictures/Fullscreen_Sign_edit.png) | ![Sign-Windowed](pictures/Windowed_Sign_Input.png) |
| **Book** | ![Book-Fullscreen](pictures/Fullscreen_Book_Edit.png) | ![Book-Windowed](pictures/Windowed_Book_Edit.png) |

# Toggle Key

- Single press the hotkey to switch between **Temporary Input State** and **Closed State**.
- Double press the hotkey to switch to **Open Mode**.
- When the mouse moves and an event occurs, **Temporary Input State** switches to **Closed State**.

# Configuration Preview

The mod provides rich customization options, supporting custom candidate box background colors, highlight colors, border styles, etc.

<div align="center">
    <img src="pictures/Setting1.png" height="200" alt="General Config"/>
    <img src="pictures/Setting2.png" height="200" alt="Color Customization"/>
    <img src="pictures/Setting3.png" height="200" alt="Style Adjustment"/>
</div>

# Build Guide

- See [BUILD_GUIDE_EN.md](BUILD_GUIDE_EN.md)

## Dependencies

- NeoForge
    - [Kotlin for NeoForge](https://www.curseforge.com/minecraft/mc-mods/kotlin-for-forge)
    - [Cloth Config API (NeoForge)](https://www.curseforge.com/minecraft/mc-mods/cloth-config)
    - [Architectury API (NeoForge)](https://www.curseforge.com/minecraft/mc-mods/architectury-api)


- System Runtime
  - [Microsoft Visual C++ Redistributable (VCRuntime141)](https://learn.microsoft.com/en-us/cpp/windows/latest-supported-vc-redist)

# Tested Environment

This mod has been tested with the following IME:
- Microsoft Pinyin IME
- Japanese IME

Other IME implementations may work but have not been fully tested.

# Code Reference/Usage

- [Windmill-City/IngameIME-Minecraft](https://github.com/Windmill-City/IngameIME-Minecraft) (LGPL-2.1)
- [Windmill-City/IngameIME](https://github.com/Windmill-City/IngameIME) (LGPL-2.1)
- [Wybxc/IngameIME-Minecraft](https://github.com/Wybxc/IngameIME-Minecraft) (LGPL-2.1)
