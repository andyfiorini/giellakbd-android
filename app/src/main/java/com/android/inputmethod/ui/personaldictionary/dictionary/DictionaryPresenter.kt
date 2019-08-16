package com.android.inputmethod.ui.personaldictionary.dictionary

import com.android.inputmethod.ui.personaldictionary.DictionaryUseCase
import com.android.inputmethod.ui.personaldictionary.dictionary.adapter.DictionaryWordViewState
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers

class DictionaryPresenter(private val view: DictionaryView, private val useCase: DictionaryUseCase, private val removeWordUseCase: RemoveWordUseCase) {

    fun start(): Observable<DictionaryViewState> {
        val disposable = view.events()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    when (it) {
                        is DictionaryEvent.OnWordSelected -> {
                            view.navigateToWordFragment(it.wordId)
                        }
                        is DictionaryEvent.OnRemoveEvent -> {
                            removeWordUseCase.execute(it.wordId)
                        }
                    }
                }


        return useCase.execute()
                .doOnDispose { disposable.dispose() }
                .map { dictionary ->
                    DictionaryViewState(
                            dictionary.map {
                                DictionaryWordViewState(it.wordId, it.typeCount, it.word)
                            }
                    )
                }
    }

}

