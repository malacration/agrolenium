package config

import java.util.Properties

/**
 * Loads dog.* entries from application.properties on the classpath.
 */
class DogConfiguration private constructor(
    val username: String,
    val password: String,
    private val dogProperties: Properties,
) {
    companion object {
        private const val PREFIX = "dog."
        private const val DEFAULT_RESOURCE = "/application.properties"

        fun load(resourcePath: String = DEFAULT_RESOURCE): DogConfiguration {
            val allProperties = Properties()
            val stream = DogConfiguration::class.java.getResourceAsStream(resourcePath)
                ?: throw IllegalStateException("Resource $resourcePath not found on classpath")
            stream.use { allProperties.load(it) }

            val dogProperties = Properties()
            allProperties.forEach { (k, v) ->
                val key = k.toString()
                if (key.startsWith(PREFIX)) {
                    dogProperties[key.removePrefix(PREFIX)] = v
                }
            }

            val username = dogProperties.getProperty("username")
                ?: throw IllegalStateException("Missing property: ${PREFIX}username")
            val password = dogProperties.getProperty("password")
                ?: throw IllegalStateException("Missing property: ${PREFIX}password")

            return DogConfiguration(username, password, dogProperties)
        }
    }

    fun get(key: String): String? = dogProperties.getProperty(key)
}
