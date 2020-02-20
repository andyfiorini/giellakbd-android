package com.android.inputmethod.ui.personaldictionary.language

sealed class LanguageEvent {
    data class OnLanguageSelected(val languageId: Long) : LanguageEvent()
    data class OnRemoveEvent(val languageId: Long) : LanguageEvent()
}