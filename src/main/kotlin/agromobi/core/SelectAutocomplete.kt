package agromobi.core

import com.codeborne.selenide.Condition.visible
import com.codeborne.selenide.Selenide.*
import com.codeborne.selenide.Selectors.byPartialLinkText

class SelectAutocomplete (val inputSelector : String, val valueType : String, val textLinkClick : String? = null){

    init{
        val field = `$`(inputSelector).shouldBe(visible)
        field.click()
        field.type(valueType)
        `$`(byPartialLinkText(textLinkClick ?: valueType))
            .shouldBe(visible)
            .click()
    }
}