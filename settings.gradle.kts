pluginManagement {
    repositories {
        maven {
            name = "Shedaniel Maven"
            setUrl("https://maven.shedaniel.me/")
        }
        mavenCentral()
        maven("https://maven.fabricmc.net")
        maven("https://maven.architectury.dev/")
        maven("https://maven.neoforged.net/releases")
        gradlePluginPortal()
    }
}

include("common")
// include("fabric")  // Fabric 平台已屏蔽
include("neoforge")

rootProject.name = "ContingameIME"
