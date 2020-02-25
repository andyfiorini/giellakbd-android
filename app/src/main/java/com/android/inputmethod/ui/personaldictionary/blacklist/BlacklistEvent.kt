package com.android.inputmethod.ui.personaldictionary.blacklist

sealed class BlacklistEvent {
    data class OnRemoveEvent(val wordId: Long) : BlacklistEvent()
    data class OnAllowEvent(val wordId: Long) : BlacklistEvent()
}