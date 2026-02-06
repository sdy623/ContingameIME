plugins {
    java
    id("architectury-plugin") version "3.4.162"
    id("dev.architectury.loom") version "1.7-SNAPSHOT" apply false
    id("me.shedaniel.unified-publishing") version "0.1.+" apply false
    kotlin("jvm") version "1.9.24" apply false
}

architectury {
    minecraft = rootProject.property("minecraft_version").toString()
}

subprojects {
    apply(plugin = "dev.architectury.loom")
    apply(plugin = "me.shedaniel.unified-publishing")

    val loom = project.extensions.getByName<net.fabricmc.loom.api.LoomGradleExtensionAPI>("loom")

    loom.silentMojangMappingsLicense()

    dependencies {
        "minecraft"("com.mojang:minecraft:${project.property("minecraft_version")}")
        "mappings"(loom.officialMojangMappings())
    }
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "architectury-plugin")
    apply(plugin = "maven-publish")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    base.archivesName.set(rootProject.property("archives_base_name").toString())
    version = "${rootProject.property("mod_version")}-${project.property("minecraft_version")}"
    group = rootProject.property("maven_group").toString()

    repositories {
        mavenCentral()
        maven("https://maven.fabricmc.net")
        maven("https://maven.architectury.dev/")
        maven("https://maven.neoforged.net/releases")
        maven("https://maven.shedaniel.me/")
        maven("https://maven.terraformersmc.com/releases/")
        maven("https://thedarkcolour.github.io/KotlinForForge/")
    }

    dependencies {
        compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:${property("kotlin_version")}")
    }

    // Configure Java toolchain to use Java 21
    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
        withSourcesJar()
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(21)
    }


    val compileKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks
    compileKotlin.kotlinOptions {
        jvmTarget = "21"
    }
    val compileTestKotlin: org.jetbrains.kotlin.gradle.tasks.KotlinCompile by tasks
    compileTestKotlin.kotlinOptions {
        jvmTarget = "21"
    }
}
