pluginManagement {
    repositories {
        maven {
            name = "Aliyun Mirror"
            setUrl("https://maven.aliyun.com/repository/public")
        }
        maven {
            name = "Aliyun Gradle Plugins"
            setUrl("https://maven.aliyun.com/repository/gradle-plugin")
        }
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
include("fabric")
//include("neoforge")

rootProject.name = "ContingameIME"
