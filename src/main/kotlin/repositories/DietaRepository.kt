package repositories

import db.HanaJdbcClient

data class DietaItem(
    val idProducaoDieta: Int?,
    val itemCode: String,
    val description: String,
    val quantity: Double,
    val price: Double,
    val lineTotal: Double,
    val inmPrice: Double?,
    val ocrCode: String?,
    val ocrCode2: String?,
)

object DietaRepository {

    fun buscarItensPorDieta(dietaId: Int): List<DietaItem> {
        val sql = """
            SELECT 
                COALESCE(
                    CAST(
                        SUBSTRING_REGEXPR(
                            'ID[[:space:]]*([0-9]+)' 
                            IN T0."Comments" 
                            GROUP 1
                        ) AS INTEGER
                    ),
                    CAST(
                        SUBSTRING_REGEXPR(
                            'Produção dieta[[:space:]]*#([0-9]+)' 
                            IN T0."Comments" 
                            GROUP 1
                        ) AS INTEGER
                    )
                ) AS "IdProducaoDieta",
                T1."ItemCode",
                T1."Dscription",
                T1."Quantity",
                T1."Price",
                T1."LineTotal",
                T1."INMPrice",
                T1."OcrCode",
                T1."OcrCode2"
            FROM "OIGE" T0
            INNER JOIN "IGE1" T1 ON T0."DocEntry" = T1."DocEntry" 
            WHERE 
                COALESCE(
                    CAST(
                        SUBSTRING_REGEXPR(
                            'ID[[:space:]]*([0-9]+)' 
                            IN T0."Comments" 
                            GROUP 1
                        ) AS INTEGER
                    ),
                    CAST(
                        SUBSTRING_REGEXPR(
                            'Produção dieta[[:space:]]*#([0-9]+)' 
                            IN T0."Comments" 
                            GROUP 1
                        ) AS INTEGER
                    ) AND T0."DocDate" > '2025-12-01'
                ) = ?
        """.trimIndent()

        return HanaJdbcClient().select(
            sql = sql,
            bind = { setInt(1, dietaId) },
        ) { rs ->
            DietaItem(
                idProducaoDieta = rs.getInt("IdProducaoDieta").let { if (rs.wasNull()) null else it },
                itemCode        = rs.getString("ItemCode"),
                description     = rs.getString("Dscription"),
                quantity        = rs.getDouble("Quantity"),
                price           = rs.getDouble("Price"),
                lineTotal       = rs.getDouble("LineTotal"),
                inmPrice        = rs.getDouble("INMPrice").let { if (rs.wasNull()) null else it },
                ocrCode         = rs.getString("OcrCode"),
                ocrCode2        = rs.getString("OcrCode2"),
            )
        }
    }
    fun buscarTotalPorDieta(dietaId: Int): Double? {
        val sql = """
        SELECT 
            SUM(T1."LineTotal") AS "TotalDieta"
        FROM "OIGE" T0
        INNER JOIN "IGE1" T1 ON T0."DocEntry" = T1."DocEntry" 
        WHERE 
            COALESCE(
                CAST(
                    SUBSTRING_REGEXPR(
                        'ID[[:space:]]*([0-9]+)' 
                        IN T0."Comments" 
                        GROUP 1
                    ) AS INTEGER
                ),
                CAST(
                    SUBSTRING_REGEXPR(
                        'Produção dieta[[:space:]]*#([0-9]+)' 
                        IN T0."Comments" 
                        GROUP 1
                    ) AS INTEGER
                )
            ) = ?
          AND T0."DocDate" > '2025-12-01'
    """.trimIndent()

        val resultados = HanaJdbcClient().select(
            sql = sql,
            bind = { setInt(1, dietaId) },
        ) { rs ->
            rs.getDouble("TotalDieta").let { valor ->
                if (rs.wasNull()) null else valor
            }
        }

        return resultados.firstOrNull()
    }
}
