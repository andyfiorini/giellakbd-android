package com.android.inputmethod.ui.personaldictionary.blacklist

import com.android.inputmethod.ui.personaldictionary.blacklist.adapter.BlacklistWordViewState

data class BlacklistViewState(
        val blacklist: List<BlacklistWordViewState> = emptyList()
)
