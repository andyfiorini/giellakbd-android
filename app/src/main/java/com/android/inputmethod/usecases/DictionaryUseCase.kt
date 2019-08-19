package com.android.inputmethod.usecases

import io.reactivex.Observable
import no.divvun.dictionary.personal.Dictionary
import no.divvun.dictionary.personal.PersonalDictionaryDatabase


class DictionaryUseCase(private val database: PersonalDictionaryDatabase) {

    fun execute(): Observable<Dictionary> {
        return database.dictionaryDao()
                .dictionaryO()
    }
}