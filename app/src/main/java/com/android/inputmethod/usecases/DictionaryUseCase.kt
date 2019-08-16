package com.android.inputmethod.usecases

import io.reactivex.Observable
import no.divvun.dictionary.personal.DictionaryWord
import no.divvun.dictionary.personal.PersonalDictionaryDatabase

class DictionaryUseCase(private val database: PersonalDictionaryDatabase) {

    fun execute(): Observable<List<DictionaryWord>> {
        return database.dictionaryDao()
                .dictionaryF()
                .toObservable()
    }
}