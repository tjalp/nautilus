plugins {
    id("io.papermc.paperweight.userdev")
}

group = "net.tjalp.nautilus.mod"
version = "0.1.0"

dependencies {
    paperweight.paperDevBundle("1.19.4-R0.1-SNAPSHOT")
    implementation("space.vectrix.ignite:ignite-api:0.8.1")
    implementation("org.spongepowered:mixin:0.8.5")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.10")

//    compileOnly(":plugin")
}

tasks {
    reobfJar {
        remapperArgs.add("--mixin")
    }

    assemble {
        dependsOn("reobfJar")
    }

    build {
        doLast {
            val nautilusModOut: String by project

            if (project.hasProperty("nautilusModOut")) {
                copy {
                    from(buildDir.resolve("libs").resolve("${project.name}-$version.jar"))
                    into(nautilusModOut)

                    rename("${project.name}-$version.jar", "${project.name}.jar")
                }
            }
        }
    }
}