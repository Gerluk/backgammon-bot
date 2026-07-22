plugins {
    id("org.openjfx.javafxplugin") version "0.1.0"
}

javafx {
    version = "25"
    modules = listOf("javafx.controls", "javafx.fxml")
}

dependencies {
    implementation(project(":engine"))
    implementation(project(":bots"))
}