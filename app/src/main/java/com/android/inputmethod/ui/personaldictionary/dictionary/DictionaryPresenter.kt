package com.android.inputmethod.ui.personaldictionary.dictionary

import com.android.inputmethod.ui.personaldictionary.dictionary.adapter.DictionaryWordViewState
import com.android.inputmethod.usecases.DictionaryUseCase
import com.android.inputmethod.usecases.RemoveWordUseCase
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import no.divvun.dictionary.personal.DictionaryWord

class DictionaryPresenter(
        private val view: DictionaryView,
        private val dictionaryUseCase: DictionaryUseCase,
        private val removeWordUseCase: RemoveWordUseCase
) {
    private val initialViewState: DictionaryViewState = DictionaryViewState()

    val states by lazy { start() }

    fun start(): Observable<DictionaryViewState> {
        return Observable.merge(
                dictionaryUseCase.execute(view.languageId).map { DictionaryUpdate.Dictionary(it) },
                view.events().compose(uiTransformer))
                .scan(initialViewState, { state: DictionaryViewState, event: DictionaryUpdate ->
                    when (event) {
                        is DictionaryUpdate.Dictionary -> {
                            state.copy(dictionary = event.words.map {
                                DictionaryWordViewState(it.wordId, it.typeCount, it.word)
                            })
                        }
                    }
                })
                .replay(1)
                .autoConnect()
    }

    private val uiTransformer = ObservableTransformer<DictionaryEvent, DictionaryUpdate> { it ->
        it.flatMap { dictionaryEvent ->
            when (dictionaryEvent) {
                is DictionaryEvent.OnWordSelected -> {
                    view.navigateToWordFragment(dictionaryEvent.wordId, dictionaryEvent.word)
                    Observable.empty<DictionaryUpdate>()
                }
                is DictionaryEvent.OnRemoveEvent -> {
                    removeWordUseCase.execute(dictionaryEvent.wordId)
                    Observable.empty()
                }
                DictionaryEvent.AddWordEvent -> {
                    view.navigateToUploadDictionary(view.languageId)
                    Observable.empty()
                }
            }
        }
    }

}


sealed class DictionaryUpdate {
    data class Dictionary(val words: List<DictionaryWord>) : DictionaryUpdate()
}
