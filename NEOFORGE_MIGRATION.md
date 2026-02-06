# NeoForge 迁移指南 / NeoForge Migration Guide

## 概述 / Overview

本文档记录了将 XPlus-ContingameIME 从 Fabric-only 迁移到支持 NeoForge 的过程。
This document records the process of migrating XPlus-ContingameIME from Fabric-only to supporting NeoForge.

## 当前状态 / Current Status

✅ **已完成 / Completed:**
- NeoForge 模块结构已创建 / NeoForge module structure created
- NeoForge 构建配置已添加 / NeoForge build configuration added
- NeoForge 入口点已实现 / NeoForge entry point implemented
- Mixin 配置已适配 / Mixin configuration adapted
- 依赖配置已更新 / Dependencies configured

⚠️ **已知问题 / Known Issues:**
- Architectury Maven 仓库连接问题 / Architectury Maven repository connection issues
- 需要确认 Architectury API SNAPSHOT 版本的可用性 / Need to verify Architectury API SNAPSHOT version availability

## 迁移详情 / Migration Details

### 1. 项目结构变化 / Project Structure Changes

```
项目根目录 / Project Root
├── common/          # 共享代码模块 / Shared code module
├── fabric/          # Fabric 平台实现 / Fabric platform implementation
└── neoforge/        # NeoForge 平台实现（新增）/ NeoForge platform implementation (NEW)
    ├── build.gradle.kts
    ├── gradle.properties
    └── src/main/
        ├── kotlin/city/windmill/ingameime/neoforge/
        │   └── IngameIMEClientNeoForge.kt
        ├── java/city/windmill/ingameime/neoforge/mixin/
        │   ├── MixinChatScreen.java
        │   ├── MixinConfigPlugin.java
        │   ├── MixinEditBox.java
        │   ├── MixinEditScreen.java
        │   ├── MixinFullScreen.java
        │   ├── MixinMinecraft.java
        │   └── MixinTextFieldWidget.java
        └── resources/
            ├── META-INF/neoforge.mods.toml
            ├── icon.png
            ├── ingameime.accesswidener
            └── ingameime.neoforge.mixins.json
```

### 2. 依赖版本 / Dependency Versions

在 `gradle.properties` 中添加了以下 NeoForge 相关依赖：
The following NeoForge-related dependencies were added to `gradle.properties`:

```properties
# NeoForge
neoforge_version=21.3.26
# Kotlin for NeoForge
kotlinforforge_version=5.7.0
```

### 3. 平台配置 / Platform Configuration

更新 `enabled_platforms` 以包含 neoforge：
Updated `enabled_platforms` to include neoforge:

```properties
enabled_platforms=fabric,neoforge
```

### 4. NeoForge 模块构建配置 / NeoForge Module Build Configuration

`neoforge/build.gradle.kts` 主要配置：
Key configurations in `neoforge/build.gradle.kts`:

```kotlin
architectury {
    platformSetupLoomIde()
    neoForge()
}

dependencies {
    neoForge("net.neoforged:neoforge:${rootProject.property("neoforge_version")}")
    modApi("dev.architectury:architectury-neoforge:${rootProject.property("architectury_version")}")
    implementation("thedarkcolour:kotlinforforge-neoforge:${rootProject.property("kotlinforforge_version")}")
    modImplementation("me.shedaniel.cloth:cloth-config-neoforge:${rootProject.property("cloth_config_version")}")
}
```

### 5. NeoForge 入口点实现 / NeoForge Entry Point Implementation

`IngameIMEClientNeoForge.kt` 实现了 NeoForge 的模组初始化：
`IngameIMEClientNeoForge.kt` implements NeoForge mod initialization:

- 使用 `@Mod` 注解标记主类 / Using `@Mod` annotation for main class
- 使用 Kotlin for NeoForge 的 `MOD_BUS` / Using Kotlin for NeoForge's `MOD_BUS`
- 实现客户端设置和按键注册 / Implementing client setup and key registration
- 与 Fabric 版本功能对等 / Feature parity with Fabric version

### 6. Mixin 适配 / Mixin Adaptation

- 复制了 Fabric 的所有 Mixin 类 / Copied all Mixin classes from Fabric
- 更新包名为 `city.windmill.ingameime.neoforge.mixin` / Updated package name to `city.windmill.ingameime.neoforge.mixin`
- 创建了 NeoForge 专用的 mixin 配置文件 / Created NeoForge-specific mixin configuration file

### 7. 模组元数据 / Mod Metadata

创建了 `neoforge.mods.toml` 包含：
Created `neoforge.mods.toml` containing:

- 模组基本信息 / Basic mod information
- 依赖声明（NeoForge, Minecraft, Architectury API, Cloth Config）/ Dependency declarations
- Mixin 配置引用 / Mixin configuration reference

## 平台差异处理 / Platform-Specific Differences

### Fabric vs NeoForge 对比 / Fabric vs NeoForge Comparison

| 特性 / Feature | Fabric | NeoForge |
|---|---|---|
| 模组加载器 / Mod Loader | `ClientModInitializer` | `@Mod` 注解 / annotation |
| 生命周期事件 / Lifecycle Events | `ClientLifecycleEvents.CLIENT_STARTED` | `FMLClientSetupEvent` |
| 按键绑定注册 / Key Binding Registration | `KeyBindingHelper.registerKeyBinding()` | `RegisterKeyMappingsEvent` |
| 环境注解 / Environment Annotation | `@Environment(EnvType.CLIENT)` | `@EventBusSubscriber(value = [Dist.CLIENT])` |
| Kotlin 支持 / Kotlin Support | Fabric Language Kotlin | Kotlin for NeoForge |

## 构建和测试 / Building and Testing

### 构建命令 / Build Commands

```bash
# 构建所有平台 / Build all platforms
./gradlew build

# 仅构建 Fabric / Build Fabric only
./gradlew :fabric:build

# 仅构建 NeoForge / Build NeoForge only
./gradlew :neoforge:build
```

### 输出位置 / Output Locations

- Fabric: `fabric/build/libs/*-fabric.jar`
- NeoForge: `neoforge/build/libs/*-neoforge.jar`

## 待办事项 / TODOs

- [ ] 解决 Architectury Maven 仓库连接问题 / Resolve Architectury Maven repository connection issues
- [ ] 验证构建配置 / Verify build configuration
- [ ] 在 NeoForge 环境中测试模组 / Test mod in NeoForge environment
- [ ] 更新 README 文档添加 NeoForge 支持说明 / Update README with NeoForge support information
- [ ] 更新 CI/CD 工作流支持 NeoForge 构建 / Update CI/CD workflow to support NeoForge builds

## 依赖关系 / Dependencies

### Common 模块 / Common Module
- Architectury API 14.0.4
- Fabric Loader (仅用于注解 / for annotations only)
- Cloth Config

### Fabric 模块 / Fabric Module
- Fabric Loader 0.16.9
- Fabric API 0.108.0+1.21.3
- Fabric Language Kotlin 1.12.3+kotlin.2.0.21
- Architectury Fabric 14.0.4
- Satin API 2.0.0 (可选 / optional)

### NeoForge 模块 / NeoForge Module
- NeoForge 21.3.26
- Kotlin for NeoForge 5.7.0
- Architectury NeoForge 14.0.4
- Cloth Config NeoForge

## 技术说明 / Technical Notes

### Architectury API

Architectury API 是一个跨模组加载器的抽象层，允许在 common 模块编写一次代码，然后在不同平台（Fabric/NeoForge）上运行。
Architectury API is a cross-mod-loader abstraction layer that allows writing code once in the common module and running it on different platforms (Fabric/NeoForge).

版本 14.0.4 支持：
Version 14.0.4 supports:
- Minecraft 1.21.x
- Fabric
- NeoForge

### 访问宽化器 / Access Widener

两个平台共享同一个访问宽化器文件 `ingameime.accesswidener`，该文件位于 common 模块。
Both platforms share the same access widener file `ingameime.accesswidener` located in the common module.

### Mixin 兼容性 / Mixin Compatibility

Mixin 在 Fabric 和 NeoForge 之间是兼容的，因此大部分 mixin 代码可以直接复用。只需要：
Mixins are compatible between Fabric and NeoForge, so most mixin code can be directly reused. Only need to:
1. 更新包名 / Update package names
2. 更新 mixin 配置文件中的包路径 / Update package path in mixin configuration file

## 问题排查 / Troubleshooting

### 构建失败 / Build Failures

如果遇到依赖解析问题：
If encountering dependency resolution issues:

1. 检查 maven 仓库连接 / Check maven repository connectivity
2. 清理 Gradle 缓存：`./gradlew clean --refresh-dependencies`
3. 验证 Architectury API 版本兼容性 / Verify Architectury API version compatibility

### Architectury Maven 仓库问题 / Architectury Maven Repository Issues

当前项目使用 SNAPSHOT 版本，可能需要：
The project currently uses SNAPSHOT versions, may need to:

1. 等待 maven.architectury.dev 恢复服务 / Wait for maven.architectury.dev to restore service
2. 或切换到稳定的发布版本 / Or switch to stable release versions
3. 或使用镜像仓库 / Or use mirror repositories

## 参考资料 / References

- [Architectury API](https://github.com/architectury/architectury-api)
- [Architectury Plugin](https://github.com/architectury/architectury-plugin)
- [NeoForge Documentation](https://docs.neoforged.net/)
- [Kotlin for NeoForge](https://github.com/thedarkcolour/KotlinForForge)
- [Cloth Config](https://github.com/shedaniel/ClothConfig)

## 联系方式 / Contact

如有问题，请在 GitHub Issues 中提出。
For questions, please open an issue on GitHub.

---

**更新日期 / Last Updated:** 2026-02-06
**贡献者 / Contributors:** GitHub Copilot Coding Agent
