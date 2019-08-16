package com.android.inputmethod.ui.personaldictionary.dictionary

sealed class DictionaryEvent {
    data class OnWordSelected(val wordId: Long): DictionaryEvent()
    data class OnRemoveEvent(val wordId: Long): DictionaryEvent()

}