package com.android.inputmethod.usecases

import no.divvun.dictionary.personal.DictionaryWord
import no.divvun.dictionary.personal.PersonalDictionaryDatabase

class AddWordUseCase(val database: PersonalDictionaryDatabase) {
    fun execute(word: String) {
        database.dictionaryDao()
                .insertWord(DictionaryWord(word, 0))
    }
}