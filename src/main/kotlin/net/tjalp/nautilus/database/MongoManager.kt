package net.tjalp.nautilus.database

import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoDatabase
import net.tjalp.nautilus.config.details.MongoDetails
import org.bson.UuidRepresentation
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.util.KMongoJacksonFeature
import org.litote.kmongo.util.KotlinModuleConfiguration
import java.util.logging.Level
import java.util.logging.Logger

/**
 * The Mongo manager manages everything that
 * has to do with database stuff, such as
 * writing, reading etc.
 */
class MongoManager(private val logger: Logger, private val details: MongoDetails) {

    /** The MongoDB database client */
    val mongoClient: MongoClient

    /** The MongoDB database client using coroutines */
    val mongoCoroutine: CoroutineClient

    /** The database that Nautilus should use */
    val mongoDatabase: MongoDatabase

    init {
        // Disable Mongo logging
        val mongoLogger = Logger.getLogger("org.mongodb.driver")
        mongoLogger.level = Level.WARNING

        System.setProperty("org.litote.mongo.mapping.service", "org.litote.kmongo.jackson.JacksonClassMappingTypeService")
        KotlinModuleConfiguration.kotlinModuleInitializer = {
            enable(KotlinFeature.NullIsSameAsDefault)
            enable(KotlinFeature.SingletonSupport)
        }
        KMongoJacksonFeature.setUUIDRepresentation(UuidRepresentation.STANDARD)

        val connectionString = this.details.connectionString.ifEmpty {
            "mongodb://${this.details.server}:${this.details.port}/"
        }
        val database = this.details.database

        this.logger.info("MongoDB -> Using connection '$connectionString'")
        this.logger.info("MongoDB -> Using database '$database'")

        this.mongoClient = KMongo.createClient(
            MongoClientSettings
                .builder()
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .applyConnectionString(ConnectionString(connectionString))
                .build()
        )
        this.mongoCoroutine = this.mongoClient.coroutine
        this.mongoDatabase = this.mongoClient.getDatabase(database)
    }

    /**
     * Dispose the mongo client
     */
    fun dispose() {
        this.mongoClient.close()
    }
}