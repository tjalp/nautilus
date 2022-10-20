package net.tjalp.nautilus.database

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import com.mongodb.reactivestreams.client.MongoDatabase
import org.bson.UuidRepresentation
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.util.KMongoJacksonFeature
import java.util.logging.Level
import java.util.logging.Logger

/**
 * The Mongo manager manages everything that
 * has to do with database stuff, such as
 * writing, reading etc.
 */
class MongoManager {

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
        KMongoJacksonFeature.setUUIDRepresentation(UuidRepresentation.STANDARD)

        this.mongoClient = KMongo.createClient(
            MongoClientSettings
                .builder()
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .applyConnectionString(ConnectionString(CONNECTION_STRING))
                .build()
        )
        this.mongoCoroutine = this.mongoClient.coroutine
        this.mongoDatabase = this.mongoClient.getDatabase("nautilus") // todo dont hardcode this
    }

    /**
     * Dispose the mongo client
     */
    fun dispose() {
        this.mongoClient.close()
    }

    companion object {

        // todo dont hardcore this
        private const val CONNECTION_STRING = "mongodb+srv://tjalp:mongopw@tjalp-cluster.dtmviyw.mongodb.net/?retryWrites=true&w=majority"
    }
}