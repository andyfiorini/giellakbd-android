package com.android.inputmethod.ui.personaldictionary.blacklist

import com.android.inputmethod.ui.personaldictionary.blacklist.adapter.BlacklistWordViewState
import com.android.inputmethod.usecases.BlacklistUseCase
import com.android.inputmethod.usecases.SetBlacklistUseCase
import com.android.inputmethod.usecases.SoftDeleteWordUseCase
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import no.divvun.dictionary.personal.DictionaryWord
import java.util.*

class BlacklistPresenter(
        private val view: BlacklistView,
        private val blacklistUseCase: BlacklistUseCase,
        private val softDeleteWordUseCase: SoftDeleteWordUseCase,
        private val blacklistWordUseCase: SetBlacklistUseCase
) {
    private val initialViewState: BlacklistViewState = BlacklistViewState()

    val states by lazy { start() }

    fun start(): Observable<BlacklistViewState> {
        return Observable.merge(
                blacklistUseCase.execute(view.languageId).map { BlacklistUpdate.Blacklist(it) },
                view.events().compose(uiTransformer))
                .scan(initialViewState, { state: BlacklistViewState, event: BlacklistUpdate ->
                    when (event) {
                        is BlacklistUpdate.Blacklist -> {
                            state.copy(blacklist = event.words.map {
                                BlacklistWordViewState(it.wordId, it.word)
                            }.sortedBy { it.word.toLowerCase(Locale.getDefault()) })
                        }
                    }
                })
                .replay(1)
                .autoConnect()
    }

    private val uiTransformer = ObservableTransformer<BlacklistEvent, BlacklistUpdate> { it ->
        it.flatMap { blacklistEvent ->
            when (blacklistEvent) {
                is BlacklistEvent.OnRemoveEvent -> {
                    softDeleteWordUseCase.execute(blacklistEvent.wordId, true)
                    Observable.empty<BlacklistUpdate>()
                }
                is BlacklistEvent.OnAllowEvent -> {
                    blacklistWordUseCase.execute(blacklistEvent.wordId, false)
                    Observable.empty()
                }
            }
        }
    }

}


sealed class BlacklistUpdate {
    data class Blacklist(val words: List<DictionaryWord>) : BlacklistUpdate()
}
