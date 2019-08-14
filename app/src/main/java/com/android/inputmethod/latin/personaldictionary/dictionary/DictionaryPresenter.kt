package com.android.inputmethod.latin.personaldictionary.dictionary

import com.android.inputmethod.latin.personaldictionary.DictionaryUseCase
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers

class DictionaryPresenter(private val view: DictionaryView, private val useCase: DictionaryUseCase) {

    fun start(): Observable<DictionaryViewState> {
        val disposable = view.events()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    when(it){
                        is DictionaryEvent.OnWordSelected -> {
                            view.navigateToWordFragment(it.wordId)
                        }
                    }
        }


        return useCase.execute()
                .doOnDispose { disposable.dispose() }
                .map { dictionary -> DictionaryViewState(dictionary) }
    }

}

