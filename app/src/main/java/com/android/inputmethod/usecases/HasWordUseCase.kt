package com.android.inputmethod.usecases

import io.reactivex.Observable
import io.reactivex.Single
import no.divvun.dictionary.personal.DictionaryWord
import no.divvun.dictionary.personal.PersonalDictionaryDatabase
import no.divvun.dictionary.personal.WordContext

class HasWordUseCase(private val database: PersonalDictionaryDatabase) {

    fun execute(word: String): Single<Boolean> {
        return database.dictionaryDao().findWordByString(word).map { it.isNotEmpty() }
    }
}