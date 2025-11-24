import com.codeborne.selenide.Condition.exactText
import com.codeborne.selenide.Condition.visible
import com.codeborne.selenide.Configuration
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import com.codeborne.selenide.Selenide.*
import com.codeborne.selenide.Selectors.byPartialLinkText
import org.openqa.selenium.chrome.ChromeOptions
import kotlin.test.AfterTest


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
