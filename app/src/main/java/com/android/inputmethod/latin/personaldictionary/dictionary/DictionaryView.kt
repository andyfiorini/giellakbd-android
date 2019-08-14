package com.android.inputmethod.latin.personaldictionary.dictionary

import io.reactivex.Observable

interface DictionaryView {
    fun render(viewState: DictionaryViewState)
    fun events(): Observable<DictionaryEvent>

    fun navigateToWordFragment(wordId: Long)
}

