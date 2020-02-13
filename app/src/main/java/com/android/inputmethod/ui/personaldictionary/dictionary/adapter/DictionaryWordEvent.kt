package com.android.inputmethod.ui.personaldictionary.dictionary.adapter


sealed class DictionaryWordEvent {
    data class OnClickPressEvent(val wordId: Long, val word: String): DictionaryWordEvent()
    data class OnClickRemoveEvent(val wordId: Long): DictionaryWordEvent()
}