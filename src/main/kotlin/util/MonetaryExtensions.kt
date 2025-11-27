package util

/**
 * Converte strings monetárias como "R$ 1.234,56" para Double ou devolve null se não for possível.
 */
fun String?.toMonetaryDoubleOrNull(): Double? {
    if (this.isNullOrBlank()) return null
    return runCatching {
        this
            .replace("R$", "", ignoreCase = true)
            .replace(" ", "")
            .replace(".", "")
            .replace(",", ".")
            .toDouble()
    }.getOrNull()
}
