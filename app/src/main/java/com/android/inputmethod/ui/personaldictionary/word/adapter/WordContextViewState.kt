package com.android.inputmethod.ui.personaldictionary.word.adapter

import com.android.inputmethod.ui.components.recycleradapter.Diffable

data class WordContextViewState(
        val wordContextId: Long,
        val word: String,
        val prevWords: String?,
        val nextWords: String?
) : Diffable {
    override fun isSameAs(other: Diffable): Boolean {
        if(other is WordContextViewState){
            return other.wordContextId== wordContextId
        }
        return false
    }
}