package net.tjalp.nautilus.config

import net.tjalp.nautilus.Nautilus
import net.tjalp.nautilus.config.details.MongoDetails
import net.tjalp.nautilus.config.details.ResourcePackDetails
import net.tjalp.nautilus.util.GsonHelper
import kotlin.io.path.*

class NautilusConfig {

    val mongo: MongoDetails = MongoDetails()
    val resourcepack: ResourcePackDetails = ResourcePackDetails()

    companion object {

        /**
         * Load a new config instance from the config file.
         *
         * If the file does not exist, a new one will be created.
         *
         * @param nautilus The [Nautilus] instance to use
         * @return The [NautilusConfig] instance
         */
        fun load(nautilus: Nautilus): NautilusConfig {
            val directory = nautilus.dataFolder.toPath()
            val file = directory.resolve("config.json")

            if (file.notExists()) {
                val config = NautilusConfig()
                val json = GsonHelper.pretty().toJson(config)
                directory.createDirectories()
                file.createFile().writeText(json)
                return config
            }

            val text = file.readText()

            return GsonHelper.global().fromJson(text, NautilusConfig::class.java)
        }
    }
}