package com.android.inputmethod.ui.personaldictionary.blacklist.adapter


sealed class BlacklistWordEvent {
    data class RemoveEvent(val wordId: Long) : BlacklistWordEvent()
    data class AllowEvent(val wordId: Long) : BlacklistWordEvent()
}