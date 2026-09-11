plugins {
    application
}

dependencies {
    implementation(project(":engine"))
    implementation(project(":bots"))
}

application {
    mainClass.set("org.example.benchmark.BenchmarkMain")
}