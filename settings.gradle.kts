pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "backgammon-bot"

include("engine")
include("bots")
include("benchmark")
include("gui")