package com.android.inputmethod.latin.personaldictionary.dictionary

sealed class DictionaryEvent {
    data class OnWordSelected(val wordId: Long): DictionaryEvent()

}