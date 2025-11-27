package agromobi.core

import com.codeborne.selenide.Condition.exactText
import com.codeborne.selenide.Condition.visible
import com.codeborne.selenide.Selenide.*
import com.codeborne.selenide.Selectors.byPartialLinkText

class ButtonByTextValue (val textvalue : String){

    val button = `$$`("button").findBy(exactText(textvalue)).shouldBe(visible)
}