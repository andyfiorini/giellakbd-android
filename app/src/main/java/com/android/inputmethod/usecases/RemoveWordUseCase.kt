package com.android.inputmethod.usecases

import no.divvun.dictionary.personal.PersonalDictionaryDatabase

class RemoveWordUseCase(val database: PersonalDictionaryDatabase) {
    fun execute(wordId: Long) {
        database.dictionaryDao()
                .removeWord(wordId)
    }
}