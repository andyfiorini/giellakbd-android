package com.android.inputmethod.ui.personaldictionary.language.adapter


sealed class LanguageWordEvent {
    data class PressEvent(val languageId: Long, val language: String): LanguageWordEvent()
    data class RemoveEvent(val languageId: Long): LanguageWordEvent()
}