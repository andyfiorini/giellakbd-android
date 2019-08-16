package com.android.inputmethod.usecases

import io.reactivex.Observable
import no.divvun.dictionary.personal.PersonalDictionaryDatabase
import no.divvun.dictionary.personal.WordContext

class WordContextUseCase(private val database: PersonalDictionaryDatabase) {

    fun execute(wordId: Long): Observable<List<WordContext>> {
        return database.dictionaryDao()
                .wordContexts(wordId)
                .toObservable()
    }
}