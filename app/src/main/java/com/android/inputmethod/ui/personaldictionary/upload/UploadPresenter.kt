package com.android.inputmethod.ui.personaldictionary.upload

import android.util.Log
import com.android.inputmethod.usecases.UploadUseCase
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

class UploadPresenter(private val view: UploadView, private val useCase: UploadUseCase) {

    private val initialViewState = UploadViewState(
            uploadEnabled = true,
            loading = false,
            errorMessage = null
    )

    fun start(): Observable<UploadViewState> {
        return view.events().flatMap { event ->
            when (event) {
                is UploadEvent.OnUploadPressed -> {
                    Log.d("UploadPresenter", "Upload was pressed!")
                    Observable.concat<UploadUpdate>(
                            Observable.just(UploadUpdate.UploadStarted),
                            Observable.just("").delay(3, TimeUnit.SECONDS).flatMapSingle { useCase.execute() }.map {
                                UploadUpdate.UploadComplete
                            }).onErrorReturn {
                        UploadUpdate.UploadFailed(it.message ?: "")
                    }

                }
            }
        }.scan(initialViewState, { viewState: UploadViewState, update: UploadUpdate ->
            when (update) {
                is UploadUpdate.UploadStarted -> {
                    viewState.copy(uploadEnabled = false, loading = true)
                }
                UploadUpdate.UploadComplete -> {
                    view.navigateToSuccess()
                    viewState
                }
                is UploadUpdate.UploadFailed -> {
                    viewState.copy(uploadEnabled = true, errorMessage = update.errorMessage, loading = false)
                }
            }
        })
    }

}

sealed class UploadUpdate {
    object UploadStarted : UploadUpdate()
    object UploadComplete : UploadUpdate()
    data class UploadFailed(val errorMessage: String) : UploadUpdate()
}