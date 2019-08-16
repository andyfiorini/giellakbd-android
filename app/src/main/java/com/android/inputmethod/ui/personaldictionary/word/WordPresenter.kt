package com.android.inputmethod.ui.personaldictionary.word

import com.android.inputmethod.ui.personaldictionary.WordContextUseCase
import com.android.inputmethod.ui.personaldictionary.word.adapter.WordContextViewState
import io.reactivex.Observable


class WordPresenter(private val useCase: WordContextUseCase, private val wordId: Long) {

    fun start(): Observable<WordViewState> {
        return useCase.execute(wordId)
                .map { wordContexts ->
                    WordViewState(wordContexts.map {
                        WordContextViewState(it.wordContextId, it.prevWord, it.nextWord)
                    })
                }
    }
}