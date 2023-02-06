import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    `java-library`
    val kotlinVersion: String by System.getProperties()
    kotlin("jvm") version kotlinVersion
    kotlin("kapt") version kotlinVersion
    id("io.papermc.paperweight.userdev") version "1.4.0"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.2"
    id("xyz.jpenilla.run-paper") version "2.0.0"
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
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.kryptonmc.org/releases/")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://repo.md-5.net/content/groups/public/")
    maven("https://repo.opencollab.dev/maven-snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://hub.jeff-media.com/nexus/repository/jeff-media-public/")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("io.ktor:ktor-client-okhttp-jvm:2.2.1")
    implementation("io.ktor:ktor-server-core-jvm:2.2.3")
    implementation("io.ktor:ktor-server-netty-jvm:2.2.1")
    implementation("io.ktor:ktor-client-core-jvm:2.2.1")
    paperDevBundle("1.19.3-R0.1-SNAPSHOT")
    implementation("cloud.commandframework:cloud-annotations:1.8.0")
    implementation("cloud.commandframework:cloud-paper:1.8.0")
//    implementation("org.litote.kmongo:kmongo-async:4.8.0")
    implementation("org.litote.kmongo:kmongo-coroutine:4.8.0")
    implementation("org.ocpsoft.prettytime:prettytime:5.0.6.Final")
    implementation("org.incendo.interfaces:interfaces-paper:1.0.0-SNAPSHOT")
    implementation("org.incendo.interfaces:interfaces-kotlin:1.0.0-SNAPSHOT")
    implementation("com.jeff_media:MorePersistentDataTypes:2.3.1")
    implementation("com.jeff_media:CustomBlockData:2.1.0")
    //implementation("com.github.twitch4j:twitch4j:1.11.0")
    compileOnly("net.luckperms:api:5.4")
    compileOnly("me.neznamy:tab-api:3.2.1")
    compileOnly("com.comphenix.protocol:ProtocolLib:5.0.0-SNAPSHOT")
    compileOnly("org.geysermc.floodgate:api:2.2.0-SNAPSHOT")
    compileOnly("LibsDisguises:LibsDisguises:10.0.31") {
        exclude("org.spigotmc")
    }

    kapt("cloud.commandframework:cloud-annotations:1.7.1")
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
        minecraftVersion("1.19.3")
    }

    shadowJar {
        relocate("cloud.commandframework", "net.tjalp.nautilus.lib.cloud")
        relocate("com.github.twitch4j", "net.tjalp.nautilus.lib.twitch4j")
        relocate("com.jeff_media.morepersistentdatatypes", "net.tjalp.nautilus.lib.persistentdatatypes")
        relocate("com.jeff_media.customblockdata", "net.tjalp.nautilus.lib.customblockdata")
        relocate("org.incendo.interfaces", "net.tjalp.nautilus.lib.interfaces")
        //relocate("kotlin", "net.tjalp.aquarium.lib.kotlin")
    }

    build {
        doLast {
            val nautilusOut: String by project

            if (project.hasProperty("nautilusOut")) {
                copy {
                    from("./build/libs/nautilus-$version.jar")
                    into(nautilusOut)

                    rename("nautilus-$version.jar", "nautilus.jar")
                }
            }
        }
    }
}

bukkit {
    load = BukkitPluginDescription.PluginLoadOrder.POSTWORLD
    authors = listOf("tjalp")
    apiVersion = "1.19"
    main = "net.tjalp.nautilus.Nautilus"
    version = project.version.toString()
    name = "Nautilus"
    softDepend = listOf("LibsDisguises", "TAB", "ProtocolLib", "floodgate")
}