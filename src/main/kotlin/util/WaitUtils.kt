package util

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

/**
 * Faz polling de uma ação até obter resultado não nulo ou estourar o timeout.
 * Útil para aguardar a UI refletir dados do banco, consultando a cada [interval].
 *
 * @param timeout tempo máximo de espera (default: 3 minutos)
 * @param interval intervalo entre tentativas (default: 1 segundo)
 * @param block ação que consulta a condição; retorne um valor não nulo quando estiver pronto
 * @return o valor retornado por [block] ou null se estourar o timeout
 */
fun <T> waitForResult(
    timeout: Duration = 3.minutes,
    interval: Duration = 1.seconds,
    block: () -> T,
): T {
    val start = TimeSource.Monotonic.markNow()
    while (true) {
        block()?.let { return it }
        if (start.elapsedNow() >= timeout) return throw RuntimeException("Tempo limite atingido para executar a função")
        Thread.sleep(interval.inWholeMilliseconds)
    }
}

/**
 * Variante booleana: aguarda até [condition] ser true ou até o timeout. Retorna true se concluiu.
 */
fun waitUntil(
    timeout: Duration = 3.minutes,
    interval: Duration = 1.seconds,
    condition: () -> Boolean,
): Boolean = waitForResult(timeout, interval) { if (condition()) true else null } ?: false
