package com.android.inputmethod.ui.personaldictionary.upload

import android.util.Log
import com.android.inputmethod.usecases.JsonDictionaryUseCase
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber

class UploadPresenter(private val view: UploadView, private val useCase: JsonDictionaryUseCase) {

    private val initialViewState = UploadViewState(
            0
    )

    fun start(): Observable<UploadViewState> {
        val disposable = view.events()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    when (it) {
                        is UploadEvent.OnUploadPressed -> {
                            useCase.execute().subscribe { dictionary ->
                                Log.d("UploadPresenter", "DictionaryJson: $dictionary")
                                Timber.d("DictionaryJson: $dictionary")
                            }
                        }
                    }
                }

        return Observable.just(initialViewState)
                .doOnDispose { disposable.dispose() }
    }

}

