package com.android.inputmethod.ui.personaldictionary.dictionary

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.android.inputmethod.ui.personaldictionary.dictionary.adapter.DictionaryWordViewState
import com.android.inputmethod.usecases.AddWordUseCase
import com.android.inputmethod.usecases.DictionaryUseCase
import com.android.inputmethod.usecases.HasWordUseCase
import com.android.inputmethod.usecases.RemoveWordUseCase
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import no.divvun.dictionary.personal.DictionaryWord

class DictionaryPresenter(
        private val view: DictionaryView,
        private val dictionaryUseCase: DictionaryUseCase,
        private val removeWordUseCase: RemoveWordUseCase,
        private val addWordUseCase: AddWordUseCase,
        private val hasWordUseCase: HasWordUseCase
) {
    private val initialViewState: DictionaryViewState = DictionaryViewState()

    val states by lazy { start() }

    fun start(): Observable<DictionaryViewState> {
        return Observable.merge(
                dictionaryUseCase.execute().map { DictionaryUpdate.Dictionary(it) },
                view.events().compose(uiTransformer))
                .scan(initialViewState, { state: DictionaryViewState, event: DictionaryUpdate ->
                    when (event) {
                        is DictionaryUpdate.Dictionary -> {
                            state.copy(dictionary = event.words.map {
                                DictionaryWordViewState(it.wordId, it.typeCount, it.word)
                            })
                        }
                        is DictionaryUpdate.DialogError -> {
                            state.copy(alertError = event.error.toString())
                        }
                        is DictionaryUpdate.DialogClearError -> {
                            state.copy(alertError = null)
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
                is DictionaryEvent.DialogEvent.OnDialogAddWordEvent -> {
                    addWordUseCase.execute(dictionaryEvent.word.trim())
                    Observable.empty()
                }
                is DictionaryEvent.DialogEvent.OnDialogInput -> {
                    validateInput(dictionaryEvent.word.trim()).toObservable().map { it ->
                        it.fold({
                            DictionaryUpdate.DialogError(it)
                        }, {
                            DictionaryUpdate.DialogClearError
                        })
                    }
                }
                DictionaryEvent.DialogEvent.OnDialogInputDismiss -> {
                    Observable.just(DictionaryUpdate.DialogClearError)
                }
            }
        }
    }

    private fun validateInput(word: String): Single<Either<AlertDialogException, ValidationSuccess>> {
        return when {
            word.isEmpty() -> {
                Single.just(AlertDialogException.EmptyWord.left())
            }
            word.contains(' ') -> {
                Single.just(AlertDialogException.WordContainsSpace.left() as Either<AlertDialogException, ValidationSuccess>)
            }
            else -> {
                hasWordUseCase.execute(word).map {
                    val result: Either<AlertDialogException, ValidationSuccess> = if (it) {
                        AlertDialogException.WordAlreadyExists.left()
                    } else {
                        ValidationSuccess.right()
                    }
                    result
                }

            }
        }

    }

}


sealed class DictionaryUpdate {
    data class Dictionary(val words: List<DictionaryWord>) : DictionaryUpdate()
    data class DialogError(val error: AlertDialogException) : DictionaryUpdate()
    object DialogClearError : DictionaryUpdate()
}

sealed class AlertDialogException {
    object WordAlreadyExists : AlertDialogException()
    object WordContainsSpace : AlertDialogException()
    object EmptyWord : AlertDialogException()
}

object ValidationSuccess
