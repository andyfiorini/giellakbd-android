package com.android.inputmethod.ui.personaldictionary.dictionary.adapter


sealed class DictionaryWordEvent {
    data class OnSelectWordEvent(val wordId: Long): DictionaryWordEvent()
}