package com.android.inputmethod.ui.personaldictionary.word.adapter

sealed class WordContextEvent {
    data class Delete(val wordContextId: Long): WordContextEvent()
}