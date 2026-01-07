plugins {
    `java-library`
    // Shade plugin
    id("com.gradleup.shadow") version "9.3.0"
}

repositories {
    mavenCentral()
    // Spigot
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    // Jitpack
    maven("https://jitpack.io")
    // Floodgate / Geyser
    maven("https://repo.opencollab.dev/main/")
}

dependencies {
    implementation(libs.dev.dejvokep.boosted.yaml)
    implementation(libs.com.github.earthcow.javadiscordwebhook)
    implementation(libs.org.bstats.bstats.api)

    compileOnly(files("libs/ThemisAPI.jar"))
    compileOnly(libs.org.spigotmc.spigot.api)
    compileOnly(libs.org.jetbrains.annotations)
    compileOnly(libs.org.geysermc.floodgate.api)
}

group = "xyz.earthcow"
version = "0.3.0"

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc>() {
    options.encoding = "UTF-8"
}

tasks.processResources {
    val props = mapOf(
        "name" to project.name,
        "version" to project.version,
    )

    inputs.properties(props)

    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.shadowJar {
    // Overwrite default jar
    archiveClassifier.set("")
    // Relocate shaded dependencies to internal libs directory
    relocate("dev.dejvokep.boostedyaml", "xyz.earthcow.themistodiscord.libs.boostedyaml")
    relocate("org.bstats", "xyz.earthcow.themistodiscord.libs.bstats")
    // Exclude annotation packages from the uber jar file
    exclude("org/intellij/**", "org/jetbrains/**")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
