package com.android.inputmethod.ui.personaldictionary.dictionary

import no.divvun.dictionary.personal.PersonalDictionaryDatabase

class RemoveWordUseCase(val database: PersonalDictionaryDatabase) {
    fun execute(wordId: Long) {
        database.dictionaryDao()
                .removeWord(wordId)
    }
}