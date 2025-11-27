package agromobi

import agromobi.core.ButtonByTextValue
import agromobi.core.SelectAutocomplete
import agromobi.core.Voltar
import com.codeborne.selenide.Condition.visible
import com.codeborne.selenide.Selenide.`$`
import com.codeborne.selenide.Selenide.open
import util.toMonetaryDoubleOrNull

class ProducaoDieta : Voltar() {

    init {
        open("https://gruporovema.hom.katrid.com.br/web/#/app/?view_type=list&model=confinamento.producao.dieta&menu_id=456&action=447")
    }

    fun getId(): Int {
        return `$`("section.form-field-section:nth-child(2) > div:nth-child(3) > span:nth-child(1)").shouldBe(visible).text().toInt()
    }

    fun getTotal(): Double {
        return `$`("section.form-field-section:nth-child(15) > div:nth-child(3)").shouldBe(visible)
            .text().toMonetaryDoubleOrNull()
            ?: throw Exception("Nao foi possivel extrair o valor de monetario")
    }

    fun criaDietaValida(semData : Boolean = false, semFilial : Boolean = false): ProducaoDieta {
        `$`(".btn-action-create").shouldBe(visible).click()

//        if(!semData)
//            SelectAutocomplete("#k-input-7","SERRA VERDE")


        `$`("#k-input-4").shouldBe(visible).selectOptionContainingText("Manual")


        SelectAutocomplete("#k-input-6","f008","QTH2B58")

        if(!semFilial)
            SelectAutocomplete("#k-input-7","SERRA VERDE")

        SelectAutocomplete("#k-input-8","ITAMAR DOMICIANO")
        SelectAutocomplete("#k-input-9","TERMINAÇÃO 3 - B")

        `$`(".btn-action-add").click()
        SelectAutocomplete("#k-input-21","MILHO")

        `$`("#k-input-22").setValue("100")
        `$`("#k-input-23").setValue("100")

        `$`("body > div.modal.form-view.editable.show.changing.inserting > div > div > div.modal-footer > button:nth-child(1)").click()

        `$`(".btn-action-save").shouldBe(visible).click()
        return this
    }

    fun criaDietaInvalidaData(){
        this.criaDietaValida(true,false)
    }

    fun criaDietaInvalidaSemFilial(){
        this.criaDietaValida(false,true)
    }

    fun refresh(): ProducaoDieta {
        Thread.sleep(1000)
        `$`(".btn-action-refresh").shouldBe(visible).click()
        return this
    }

    fun encerrar(): ProducaoDieta {
        ButtonByTextValue("Encerrar").button.click()
        return this
    }
}