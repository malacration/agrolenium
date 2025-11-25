import com.codeborne.selenide.Condition.exactText
import com.codeborne.selenide.Condition.visible
import com.codeborne.selenide.Configuration
import com.codeborne.selenide.ElementsCollection
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import com.codeborne.selenide.Selenide.*
import com.codeborne.selenide.Selectors.byPartialLinkText
import config.DogConfiguration
import org.openqa.selenium.chrome.ChromeOptions
import repositories.DietaRepository
import kotlin.test.AfterTest
import kotlin.test.assertEquals
import com.codeborne.selenide.Selenide.`$$`
import com.codeborne.selenide.SelenideElement

class TesteInicial {
    companion object {

        private const val producaoDieta =
            "https://gruporovema.hom.katrid.com.br/web/#/app/?view_type=list&model=confinamento.producao.dieta&menu_id=456&action=447"
        private val dogConfiguration: DogConfiguration = DogConfiguration.load()

        @BeforeAll
        @JvmStatic
        fun setup() {
            Configuration.browser = "chrome"
            Configuration.headless = false
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
        // Abre a página
        open("https://gruporovema.hom.katrid.com.br/web/login/?next=/web/")

        `$`("#id-username").shouldBe(visible).setValue(dogConfiguration.username)
        `$`("#id-password").shouldBe(visible).setValue(dogConfiguration.password)
        `$`(".btn").click()
        `$`(".fa-duotone").shouldBe(visible)


        // redireciona para o menu desejado após login bem-sucedido
        open(producaoDieta)
        `$`(".btn-action-create").shouldBe(visible).click()
        //TODO data nao foi selecionada
        `$`("#k-input-4").shouldBe(visible).selectOptionContainingText("Manual")


        TesteEmcapsula("#k-input-6","f008","QTH2B58")

        TesteEmcapsula("#k-input-7","SERRA VERDE")
        TesteEmcapsula("#k-input-8","ITAMAR DOMICIANO")
        TesteEmcapsula("#k-input-9","TERMINAÇÃO 3 - B")

        `$`(".btn-action-add").click()
        TesteEmcapsula("#k-input-21","MILHO")

        `$`("#k-input-22").setValue("100")
        `$`("#k-input-23").setValue("100")

        `$`("body > div.modal.form-view.editable.show.changing.inserting > div > div > div.modal-footer > button:nth-child(1)").click()

        `$`(".btn-action-save").shouldBe(visible).click()

        `$`(".btn-action-refresh").shouldBe(visible).click()

        //Fazer um wrapper para esse cara, click by text value!
        `$$`("button").findBy(exactText("Encerrar")).shouldBe(visible).click()

        Thread.sleep(1000) // 1s pause after click

        `$`(".btn-action-refresh").shouldBe(visible).click()

        val dietaID = `$`("section.form-field-section:nth-child(2) > div:nth-child(3) > span:nth-child(1)").shouldBe(visible).text()
        println("ID da dieta na tela = $dietaID")

        val TotalDieta = `$`("section.form-field-section:nth-child(15) > div:nth-child(3)").shouldBe(visible).text()
        println("Total da dieta na tela = $TotalDieta")
        val totalSegundos = 2 * 60
        for (restante in totalSegundos downTo 1) {
            println("Aguardando verificação no banco... faltam ${restante}s")
            Thread.sleep(1000)
        }

        val dietaIdInt = dietaID.trim().toInt()

// total da tela -> normalizar para Double
        val totalUi = TotalDieta
            .replace("R$", "")  // se tiver símbolo de moeda
            .replace(" ", "")
            .replace(".", "")   // tira separador de milhar
            .replace(",", ".")  // vírgula decimal -> ponto
            .toDouble()

        println("Total da dieta na tela (double) = $totalUi")

// total no banco
        val totalDb = DietaRepository.buscarTotalPorDieta(dietaIdInt)
            ?: error("Nenhum total encontrado no banco para dieta ID=$dietaIdInt")

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


//        #action-manager > action-view > div > div.content > header > button:nth-child(1)

//        #action-manager > action-view > div > div.data-heading.panel.panel-default > div.panel-body > div > div:nth-child(1) > div > div > button.btn.btn-primary
//        `$`(".fa-duotone").click()
//        `$`("#ui-menu-456 > img").click()
//        `$`("#ui-menu-529").click()
//        `$`(".toolbar").click()
//
//        `$`("#ui-menu-529").click()

//        id=ui-menu-529


        // dá um tempinho pra você ver o que aconteceu
        Thread.sleep(50000)
    }

    @AfterTest
    fun affter(){
        closeWindow()
    }
}


class TesteEmcapsula(val inputSelector : String, val valueType : String, val textLinkClick : String? = null){

    init{
        val field = `$`(inputSelector).shouldBe(visible)
        field.click()
        field.type(valueType)
        `$`(byPartialLinkText(textLinkClick ?: valueType))
            .shouldBe(visible)
            .click()
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