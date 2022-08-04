pluginManagement {

    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }

    plugins {
        val kotlinVersion: String by System.getProperties()
        kotlin("jvm").version(kotlinVersion)
    }
}