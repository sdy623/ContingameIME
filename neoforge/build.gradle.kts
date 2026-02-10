plugins {
    id("com.github.johnrengelman.shadow") version "8.+"
}

architectury {
    platformSetupLoomIde()
    neoForge()
}

loom {
    accessWidenerPath.set(project(":common").loom.accessWidenerPath)
}

val common: Configuration by configurations.creating
val shadowCommon: Configuration by configurations.creating
val developmentNeoForge: Configuration = configurations.getByName("developmentNeoForge")
configurations {
    compileClasspath.get().extendsFrom(configurations["common"])
    runtimeClasspath.get().extendsFrom(configurations["common"])
    developmentNeoForge.extendsFrom(configurations["common"])
    // Avoid duplicate fml_loader modules from raw vs transformed loader jars.
    runtimeClasspath.get().exclude(group = "net.neoforged.fancymodloader", module = "loader")
    developmentNeoForge.exclude(group = "net.neoforged.fancymodloader", module = "loader")
}

repositories {
    maven("https://maven.neoforged.net/releases")
    maven("https://maven.shedaniel.me/")
    maven("https://maven.terraformersmc.com/releases/")
    maven("https://thedarkcolour.github.io/KotlinForForge/")
}

dependencies {
    //NeoForge
    neoForge("net.neoforged:neoforge:${rootProject.property("neoforge_version")}")
    //Architectury API
    modApi("dev.architectury:architectury-neoforge:${rootProject.property("architectury_version")}")
    //Kotlin for NeoForge
    implementation("thedarkcolour:kotlinforforge-neoforge:${rootProject.property("kotlinforforge_version")}")
    //Cloth Config
    modImplementation("me.shedaniel.cloth:cloth-config-neoforge:${rootProject.property("cloth_config_version")}")
    //REI (compile-only for conditional mixin support)
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-neoforge:${rootProject.property("rei_version")}")

    common(project(":common", configuration = "namedElements")) { isTransitive = false }
    shadowCommon(project(":common", configuration = "transformProductionNeoForge")) { isTransitive = false }
}

tasks {
    processResources {
        inputs.property("version", project.version)

        filesMatching("META-INF/neoforge.mods.toml") {
            expand("version" to project.version)
        }
    }

    shadowJar {
        exclude("architectury.common.json")
        exclude("fabric.mod.json")
        configurations = listOf(project.configurations["shadowCommon"])
        archiveClassifier.set("dev-shadow")
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }

    remapJar {
        injectAccessWidener.set(true)
        inputFile.set(shadowJar.flatMap { it.archiveFile })
        dependsOn(shadowJar)
        archiveClassifier.set("neoforge")
    }

    jar {
        archiveClassifier.set("dev")
    }

    sourcesJar {
        val commonSources = project(":common").tasks.getByName<Jar>("sourcesJar")
        dependsOn(commonSources)
        from(commonSources.archiveFile.map { zipTree(it) })
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
}
