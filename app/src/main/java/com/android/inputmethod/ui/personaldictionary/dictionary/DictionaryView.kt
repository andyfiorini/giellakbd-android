package com.android.inputmethod.ui.personaldictionary.dictionary

import io.reactivex.Observable

interface DictionaryView {
    val languageId: Long

    fun render(viewState: DictionaryViewState)
    fun events(): Observable<DictionaryEvent>

    fun navigateToWordFragment(wordId: Long, word: String)
    fun navigateToAddWordDialogFragment(languageId: Long)
    fun navigateToUploadDictionary(languageId: Long)
}

