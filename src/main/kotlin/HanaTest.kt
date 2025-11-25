import repositories.DietaRepository

fun main() {
    val dietaId = 1151

//    val itens = DietaRepository.buscarItensPorDieta(dietaId)
    val total = DietaRepository.buscarTotalPorDieta(dietaId)

//    println("Itens da dieta $dietaId:")
//    itens.forEach { println(it) }

    println("Total da dieta $dietaId = $total")
}
