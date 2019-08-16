package com.android.inputmethod.ui.personaldictionary.upload

import com.android.inputmethod.ui.personaldictionary.dictionary.adapter.DictionaryWordViewState
import com.android.inputmethod.usecases.DictionaryUseCase
import com.android.inputmethod.usecases.RemoveWordUseCase
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers

class UploadPresenter(private val view: UploadView, private val useCase: DictionaryUseCase) {

    fun start(): Observable<UploadViewState> {
        val disposable = view.events()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    when (it) {
                        is UploadEvent.OnUploadPressed -> {
                        }
                    }
                }

        return useCase.execute()
                .doOnDispose { disposable.dispose() }
                .map {
                    UploadViewState(
                            0
                    )
                }
    }

}

