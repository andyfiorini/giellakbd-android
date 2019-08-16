package com.android.inputmethod.ui.personaldictionary.word

import android.annotation.SuppressLint
import com.android.inputmethod.ui.personaldictionary.word.adapter.WordContextViewState
import com.android.inputmethod.usecases.WordContextUseCase
import com.android.inputmethod.usecases.WordUseCase
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import no.divvun.dictionary.personal.WordContext


class WordPresenter(private val wordId: Long, private val wordUseCase: WordUseCase, private val wordContextUseCase: WordContextUseCase) {

    private val initalViewState = WordViewState("", 0L, emptyList())

    @SuppressLint("CheckResult")
    fun start(): Observable<WordViewState> {
        return Observable.merge<WordViewStateUpdate>(
                wordContextUseCase.execute(wordId).compose(wordContextTransformer).map(WordViewStateUpdate::ContextUpdate),
                wordUseCase.execute(wordId).map { word -> WordViewStateUpdate.WordUpdate(word.word, word.typeCount) }
        ).scan(
                initalViewState, { state, update ->
            when (update) {
                is WordViewStateUpdate.WordUpdate -> {
                    state.copy(word = update.word, typeCount = update.typeCount)
                }
                is WordViewStateUpdate.ContextUpdate -> {
                    state.copy(contexts = update.contexts)
                }
            }
        })
    }

}

sealed class WordViewStateUpdate {
    data class WordUpdate(val word: String, val typeCount: Long) : WordViewStateUpdate()
    data class ContextUpdate(val contexts: List<WordContextViewState>) : WordViewStateUpdate()
}

val wordContextTransformer: ObservableTransformer<List<WordContext>, List<WordContextViewState>> =
        ObservableTransformer { it ->
            it.map { contexts ->
                contexts.map { WordContextViewState(it.wordContextId, it.prevWord, it.nextWord) }
            }
        }