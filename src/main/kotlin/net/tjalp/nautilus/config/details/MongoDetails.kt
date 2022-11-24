package net.tjalp.nautilus.config.details

/**
 * All MongoDB details that are used to set up
 * a connection to the database.
 */
class MongoDetails {

    /**
     * The connection string to use instead of the constructed one.
     *
     * If blank, the constructed string will be used based on the
     * other details.
     */
    val connectionString = ""

    /**
     * The server address to use
     */
    val server = "localhost"

    /**
     * The port to use
     */
    val port = 27017

    /**
     * The database to use
     */
    val database = "nautilus"
}