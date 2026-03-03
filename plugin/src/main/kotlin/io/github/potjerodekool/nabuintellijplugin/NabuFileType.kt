package io.github.potjerodekool.nabuintellijplugin

import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon


class NabuFileType protected constructor() : LanguageFileType(NabuLanguage) {
    override fun getName(): String {
        return "Nabu File"
    }

    override fun getDescription(): String {
        return "Nabu language file"
    }

    override fun getDefaultExtension(): String {
        return "nabu"
    }

    override fun getIcon(): Icon? {
        return null
    }

    companion object {
        val INSTANCE: NabuFileType = NabuFileType()
    }
}