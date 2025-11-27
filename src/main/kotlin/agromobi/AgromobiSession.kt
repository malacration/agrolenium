package agromobi

import com.codeborne.selenide.Condition.visible
import com.codeborne.selenide.Selenide.*



class AgromobiSession(private val username : String, private val password : String) : AutoCloseable {

    fun login(): MenuPage{

        open("https://gruporovema.hom.katrid.com.br/web/login/?next=/web/")

        `$`("#id-username").shouldBe(visible).setValue(username)
        `$`("#id-password").shouldBe(visible).setValue(password)
        `$`(".btn").click()
        `$`(".fa-duotone").shouldBe(visible)
        return MenuPage()
    }

    override fun close() {
        closeWebDriver()
    }
}