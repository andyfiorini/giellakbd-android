package com.android.inputmethod.latin.personaldictionary.word

import com.android.inputmethod.latin.personaldictionary.WordContextUseCase
import io.reactivex.Observable


class WordPresenter(private val useCase: WordContextUseCase, private val wordId: Long) {

    fun start(): Observable<WordViewState> {
        return useCase.execute(wordId)
                .map {
                    WordViewState(it)
                }
    }
}