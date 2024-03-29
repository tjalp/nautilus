package net.tjalp.nautilus.config.details

/**
 * The resource pack server details
 */
class ResourcePackDetails {

    /**
     * Whether to enable resource pack serving
     */
    val enabled = false

    /**
     * The URL to use instead of the automatically constructed one.
     * This allows you to use HTTPS.
     *
     * By default, this is blank so the constructed URL will be used.
     */
    val overrideUrl = ""

    /**
     * The port to host the server on.
     */
    val hostPort = 8080

    /**
     * The host address
     */
    val host = "0.0.0.0"

    /**
     * The file name of the file that is uploaded and to get
     * the hash of.
     */
    val fileName = "pack.zip"

    /**
     * The hash of the file to upload to the player.
     */
    val hash = ""

    /**
     * The path that the resource pack should be uploaded
     * from.
     */
    val hostPath = "/resource-pack"
}