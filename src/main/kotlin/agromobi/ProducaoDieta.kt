package agromobi

import agromobi.core.ButtonByTextValue
import agromobi.core.SelectAutocomplete
import agromobi.core.Voltar
import com.codeborne.selenide.Condition.visible
import com.codeborne.selenide.Selenide.`$`
import com.codeborne.selenide.Selenide.open
import util.toMonetaryDoubleOrNull
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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

    fun selecionarDataHoje(): ProducaoDieta {
        `$`(".btn-calendar").shouldBe(visible).click()

        val hoje = LocalDate.now().format(DateTimeFormatter.ISO_DATE)

        val seletorData = "div[data-value=\"$hoje\"]"
        `$`(seletorData).shouldBe(visible).click()

        return this
    }

    fun criaDietaValida(semData : Boolean = false, semFilial : Boolean = false): ProducaoDieta {
        `$`(".btn-action-create").shouldBe(visible).click()
        selecionarDataHoje()


////        if(!semData)
////            SelectAutocomplete("#k-input-7","SERRA VERDE")
//

        `$`("#k-input-4").shouldBe(visible).selectOptionContainingText("Manual")


        SelectAutocomplete("#k-input-6","f008","QTH2B58")

        if(!semFilial)
            SelectAutocomplete("#k-input-7","SERRA VERDE")

        SelectAutocomplete("#k-input-8","ITAMAR DOMICIANO")
        SelectAutocomplete("#k-input-9","TERMINAÇÃO 5B")

//
////        var currentId = 21
////        val incremento = 4
////
//
////        adicionarItemDieta(currentId, "MILHO","100","100")
////        currentId += incremento
////
////        adicionarItemDieta(currentId, "SOJA GRAO AVARIADA","100","100")
////        currentId += incremento
////
////        adicionarItemDieta(currentId, "QUIRERA DE MILHO FINA","100","100")
////        currentId += incremento
////
////        adicionarItemDieta(currentId, "FARELO DE SOJA SEMI INTEGRAL","100","100")
////        currentId += incremento
////
////        adicionarItemDieta(currentId, "CASCA DE ALGODAO","100","100")
////        currentId += incremento
////
////        adicionarItemDieta(currentId, "OX NUCLEO BOVINOS VM 200","100","100")
////        currentId += incremento
////
////        adicionarItemDieta(currentId, "UREIA ANIMAL","100","100")
////        currentId += incremento
////
////        adicionarItemDieta(currentId, "AGUA","100","100")
////        SelectAutocomplete("#k-input-21","SOJA GRAO AVARIADA")
////        `$`("#k-input-22").setValue("100")
////        `$`("#k-input-23").setValue("100")
////        `$`("body > div.modal.form-view.editable.show.changing.inserting > div > div > div.modal-footer > button:nth-child(1)").click()
//
//
//
//
        `$`(".btn-action-save").shouldBe(visible).click()
        `$`(".btn-action-edit").shouldBe(visible).click()

        val listaQuantidades = listOf(
            Pair("405", "480"),
        Pair("3672", "3680"),
        Pair("135", "135"),
        Pair("810", "840"),
        Pair("126", "130"),
        Pair("72", "85"),
        Pair("2700", "2700"),
        Pair("1080", "910"),
        )

        listaQuantidades.forEachIndexed { index, (qtd1, qtd2) ->
            val numeroLinha = index + 1
            val baseId = 21 + (index * 4)

            editarItemDieta(baseId, numeroLinha, qtd1, qtd2)
        }
        `$`(".btn-action-save").shouldBe(visible).click()
        Thread.sleep(3000)
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

    fun adicionarItemDieta(baseId: Int, item: String, qtdPlanejada: String, qtdRealizada: String): ProducaoDieta {
        `$`(".btn-action-add").click()

        val itemSelector = "#k-input-$baseId"
        SelectAutocomplete(itemSelector, item)

        val qtd1Selector = "#k-input-${baseId + 1}"
        `$`(qtd1Selector).setValue(qtdPlanejada)

        val qtd2Selector = "#k-input-${baseId + 2}"
        `$`(qtd2Selector).setValue(qtdRealizada)

        // Botão de confirmar/salvar dentro do modal
        `$`("body > div.modal.form-view.editable.show.changing.inserting > div > div > div.modal-footer > button:nth-child(1)").click()
        return this
    }

    fun editarItemDieta(baseId: Int,numeroLinha: Int, quantidade1: String, quantidade2: String): ProducaoDieta {
        val seletorLinha = ".table > tbody:nth-child(2) > tr:nth-child($numeroLinha) > td:nth-child(2) > a:nth-child(1)"

        `$`(seletorLinha).click()

        val qtd1Selector = "#k-input-${baseId + 1}"
        `$`(qtd1Selector).setValue(quantidade1)

        val qtd2Selector = "#k-input-${baseId + 2}"
        `$`(qtd2Selector).setValue(quantidade2)

        `$`(".modal-footer > button:nth-child(1)").click()
        return this
    }

    fun encerrar(): ProducaoDieta {
        ButtonByTextValue("Encerrar").button.click()
        return this
    }
}