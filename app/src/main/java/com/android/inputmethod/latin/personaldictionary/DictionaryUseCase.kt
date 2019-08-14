package com.android.inputmethod.latin.personaldictionary

import io.reactivex.Observable
import no.divvun.dictionary.personal.DictionaryWord
import no.divvun.dictionary.personal.PersonalDictionaryDatabase
import no.divvun.dictionary.personal.WordContext

class DictionaryUseCase(private val database: PersonalDictionaryDatabase) {

    fun execute(): Observable<List<DictionaryWord>> {
        return database.dictionaryDao()
                .dictionaryF()
                .toObservable()
    }
}