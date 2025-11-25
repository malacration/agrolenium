package config

import java.util.Properties

class HanaConfiguration private constructor(
    val url: String,
    val username: String,
    val password: String,
    private val hanaProperties: Properties,
) {
    companion object {
        private const val PREFIX = "hana."
        private const val DEFAULT_RESOURCE = "/application.properties"

        fun load(resourcePath: String = DEFAULT_RESOURCE): HanaConfiguration {
            val allProperties = Properties()
            val stream = HanaConfiguration::class.java.getResourceAsStream(resourcePath)
                ?: throw IllegalStateException("Resource $resourcePath not found on classpath")
            stream.use { allProperties.load(it) }

            val hanaProperties = Properties()
            allProperties.forEach { (k, v) ->
                val key = k.toString()
                if (key.startsWith(PREFIX)) {
                    hanaProperties[key.removePrefix(PREFIX)] = v
                }
            }

            val url = hanaProperties.getProperty("url")
                ?: throw IllegalStateException("Missing property: ${PREFIX}url")
            val username = hanaProperties.getProperty("username")
                ?: throw IllegalStateException("Missing property: ${PREFIX}username")
            val password = hanaProperties.getProperty("password")
                ?: throw IllegalStateException("Missing property: ${PREFIX}password")

            return HanaConfiguration(url, username, password, hanaProperties)
        }
    }

    fun get(key: String): String? = hanaProperties.getProperty(key)
}
