import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    `java-library`
    val kotlinVersion: String by System.getProperties()
    kotlin("jvm") version kotlinVersion
    id("io.papermc.paperweight.userdev") version "1.3.7"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.2"
    id("xyz.jpenilla.run-paper") version "1.0.6"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

base {
    val archivesBaseName: String by project
    archivesName.set(archivesBaseName)
    project.version = "1.0.0"
}

val mavenGroup: String by project
group = mavenGroup

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.kryptonmc.org/releases")
}

dependencies {
    implementation(kotlin("stdlib"))
    paperDevBundle("1.19.1-R0.1-SNAPSHOT")
    implementation("cloud.commandframework:cloud-paper:1.7.0")
    compileOnly("net.luckperms:api:5.4")
    compileOnly("me.neznamy:tab-api:3.1.2")
}

tasks {
    val javaVersion = JavaVersion.VERSION_17

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        sourceCompatibility = javaVersion.toString()
        targetCompatibility = javaVersion.toString()
        options.release.set(javaVersion.toString().toInt())
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions { jvmTarget = javaVersion.toString() }
    }

    jar { from("LICENSE") { rename { "${it}_${base.archivesName}" } } }

    processResources {
        inputs.property("version", project.version)
    }

    java {
        toolchain { languageVersion.set(JavaLanguageVersion.of(javaVersion.toString())) }
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        withSourcesJar()
    }

    assemble {
        dependsOn(reobfJar)
    }

    runServer {
        minecraftVersion("1.19.1")
    }

    shadowJar {
        relocate("cloud.commandframework", "net.tjalp.aquarium.lib.cloud")
        relocate("kotlin", "net.tjalp.aquarium.lib.kotlin")
    }
}

bukkit {
    load = BukkitPluginDescription.PluginLoadOrder.POSTWORLD
    authors = listOf("tjalp")
    apiVersion = "1.19"
    main = "net.tjalp.aquarium.AquariumLoader"
    version = project.version.toString()
    name = "Aquarium"
    depend = listOf("LuckPerms", "TAB")
}