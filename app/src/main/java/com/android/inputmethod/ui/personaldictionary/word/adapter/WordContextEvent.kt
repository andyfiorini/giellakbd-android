package com.android.inputmethod.ui.personaldictionary.word.adapter

sealed class WordContextEvent {
    data class OnSelectWordEvent(val wordId: Long): WordContextEvent()
}