package com.android.inputmethod.ui.personaldictionary.word

import android.util.Log
import com.android.inputmethod.ui.personaldictionary.word.adapter.WordContextViewState
import com.android.inputmethod.usecases.WordContextUseCase
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import no.divvun.dictionary.personal.WordWithContext


class WordPresenter(private val wordId: Long, private val wordContextUseCase: WordContextUseCase) {

    private val initialViewState = WordViewState(emptyList())

    fun start(): Observable<WordViewState> {
        return wordContextUseCase.execute(wordId)
                .compose(wordContextTransformer)
                .map { WordViewState(it) }
                .startWith(initialViewState)
                .doOnNext { Log.d("WordPresenter", it.toString()) }
    }

}

val wordContextTransformer: ObservableTransformer<WordWithContext, List<WordContextViewState>> =
        ObservableTransformer { it ->
            it.map { wordWithContext ->
                wordWithContext.contexts.map {
                    WordContextViewState(
                            it.wordContextId,
                            wordWithContext.dictionaryWord.word,
                            it.prevWords,
                            it.nextWords)
                }
            }
        }