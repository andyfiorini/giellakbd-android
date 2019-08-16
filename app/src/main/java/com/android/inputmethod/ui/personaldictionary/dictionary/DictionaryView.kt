package com.android.inputmethod.ui.personaldictionary.dictionary

import io.reactivex.Observable

interface DictionaryView {
    fun render(viewState: DictionaryViewState)
    fun events(): Observable<DictionaryEvent>

    fun navigateToWordFragment(wordId: Long)
    fun navigateToUploadDictionary()
}

