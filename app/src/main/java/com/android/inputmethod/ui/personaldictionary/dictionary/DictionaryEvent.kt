package com.android.inputmethod.ui.personaldictionary.dictionary

sealed class DictionaryEvent {
    data class OnWordSelected(val wordId: Long, val word: String) : DictionaryEvent()
    data class OnRemoveEvent(val wordId: Long) : DictionaryEvent()
    sealed class DialogEvent: DictionaryEvent() {
        data class OnDialogInput(val word: String) : DialogEvent()
        object OnDialogInputDismiss : DialogEvent()
        data class OnDialogAddWordEvent(val word: String) : DialogEvent()
    }
}