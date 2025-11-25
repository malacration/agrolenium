import agromobi.AgromobiSession
import agromobi.core.ButtonByTextValue
import com.codeborne.selenide.Condition.exactText
import com.codeborne.selenide.Condition.visible
import com.codeborne.selenide.Configuration
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import com.codeborne.selenide.Selenide.*
import config.DogConfiguration
import org.openqa.selenium.chrome.ChromeOptions
import repositories.DietaRepository
import kotlin.test.AfterTest
import kotlin.test.assertEquals
import com.codeborne.selenide.Selenide.`$$`
import util.waitForResult

class TesteInicial {
    companion object {

        private const val producaoDieta =
            "https://gruporovema.hom.katrid.com.br/web/#/app/?view_type=list&model=confinamento.producao.dieta&menu_id=456&action=447"
        private val dogConfiguration: DogConfiguration = DogConfiguration.load()

        @BeforeAll
        @JvmStatic
        fun setup() {
            Configuration.browser = "chrome"
            Configuration.headless = true
            Configuration.holdBrowserOpen = true
            Configuration.browserSize = "1280x800"
            Configuration.fastSetValue = true

            val profileDir = "/tmp/.selenium/chrome-profile"
            val cacheDir   = "/tmp/.selenium/chrome-cache"

            val options = ChromeOptions().apply {
                addArguments("user-data-dir=$profileDir")
                addArguments("disk-cache-dir=$cacheDir")
                // opcional: não limpar dados ao fechar aba
                // addArguments("profile.exit_type=Normal")
            }
            Configuration.browserCapabilities = options

        }
    }

    @Test
    fun `abre navegador e interage visivelmente`() {

        var dietaIdInt : Int = -1
        var totalUi : Double = -1.0

        AgromobiSession(dogConfiguration.username,dogConfiguration.password)
            .login()
            .producaoDieta()
            .criaDietaValida()
            .refresh()
            .encerrar()
            .refresh().also {
                dietaIdInt = it.getId()
                totalUi = it.getTotal()
            }


        val totalDb = waitForResult {
            DietaRepository.buscarTotalPorDieta(dietaIdInt)
        } ?: error("Nenhum total encontrado no banco para dieta ID=$dietaIdInt")

        println("Total da dieta no banco = $totalDb")

        assertEquals(
            totalDb,
            totalUi,
            0.01,
            "Total da dieta diferente entre UI ($totalUi) e banco ($totalDb) para ID=$dietaIdInt"
        )

        val itensDb = DietaRepository.buscarItensPorDieta(dietaIdInt)

// agrupa por descrição
        val mapaDb: Map<String, Double> = itensDb
            .groupBy { it.description }
            .mapValues { (_, linhas) -> linhas.sumOf { it.quantity } }

        println("== Itens do BANCO (por descrição) ==")
        mapaDb.forEach { (desc, qty) ->
            println("DB -> desc='$desc', qty=$qty")
        }

// ---- UI ----
        val itensUi = lerItensDaTela()

        val mapaUi: Map<String, Double> = itensUi
            .groupBy { it.description }
            .mapValues { (_, linhas) -> linhas.sumOf { it.quantity } }

        println("== Itens da UI (por descrição) ==")
        mapaUi.forEach { (desc, qty) ->
            println("UI -> desc='$desc', qty=$qty")
        }

// ---- COMPARA ITENS ----
        assertEquals(
            mapaDb.keys,
            mapaUi.keys,
            "Descrições diferentes entre UI e banco para dieta ID=$dietaIdInt"
        )

// ---- COMPARA QUANTIDADES ----
        mapaDb.forEach { (desc, qtyDb) ->
            val qtyUi = mapaUi[desc]
                ?: error("Descrição '$desc' existe no DB mas não na UI (dieta ID=$dietaIdInt)")

            assertEquals(
                qtyDb,
                qtyUi,
                0.01,
                "Quantidade divergente para desc='$desc' (UI=$qtyUi, DB=$qtyDb) na dieta ID=$dietaIdInt"
            )
        }
        closeWindow()

        Thread.sleep(50000)
    }

    @AfterTest
    fun affter(){
        closeWindow()
    }
}




data class ItemUi(
    val description: String,
    val quantity: Double,
)

fun lerItensDaTela(): List<ItemUi> {
    // linhas de itens – segundo tbody
    val linhas = `$$`(".table > tbody:nth-child(2) > tr")
    println("Qtd de linhas de itens na UI = ${linhas.size()}")

    return linhas.mapIndexed { index, linha ->
        // AJUSTA o nth-child da descrição se for outra coluna
        val descText = linha.`$`("td:nth-child(2)").text().trim()
        val qtyText  = linha.`$`("td:nth-child(4)").text().trim()

        val qty = qtyText
            .replace(" ", "")
            .replace(".", "")    // milhar
            .replace(",", ".")   // decimal
            .toDouble()

        println("UI item #$index -> desc='$descText', qty=$qty")

        ItemUi(
            description = descText,
            quantity = qty,
        )
    }
}