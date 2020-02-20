package com.android.inputmethod.ui.personaldictionary.dictionary

sealed class DictionaryEvent {
    data class OnWordSelected(val wordId: Long, val word: String) : DictionaryEvent()
    data class OnRemoveEvent(val wordId: Long) : DictionaryEvent()
    object AddWordEvent : DictionaryEvent()
}