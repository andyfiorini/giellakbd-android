package com.android.inputmethod.ui.personaldictionary.word

import com.android.inputmethod.ui.personaldictionary.word.adapter.WordContextViewState

data class WordViewState(
        val word: String,
        val typeCount: Long,
        val contexts: List<WordContextViewState>
)