package db

import config.HanaConfiguration
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet

object HanaJdbcClient {

    private val hanaConfig: HanaConfiguration = HanaConfiguration.load()

    init {
        Class.forName("com.sap.db.jdbc.Driver")
    }

    fun <T> select(
        sql: String,
        bind: (PreparedStatement.() -> Unit)? = null,
        mapper: (ResultSet) -> T,
    ): List<T> {
        DriverManager.getConnection(
            hanaConfig.url,
            hanaConfig.username,
            hanaConfig.password,
        ).use { connection ->
            connection.prepareStatement(sql).use { statement ->
                bind?.invoke(statement)
                statement.executeQuery().use { rs ->
                    val results = mutableListOf<T>()
                    while (rs.next()) {
                        results += mapper(rs)
                    }
                    return results
                }
            }
        }
    }
}
