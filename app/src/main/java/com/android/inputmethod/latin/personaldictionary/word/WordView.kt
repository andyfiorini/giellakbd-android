package com.android.inputmethod.latin.personaldictionary.word

import io.reactivex.Observable

interface WordView {
    fun events(): Observable<WordEvent>
    fun render(viewState: WordViewState)
}