package com.android.inputmethod.usecases

import no.divvun.service.DivvunDictionaryUploadService
import io.reactivex.Single
import no.divvun.dictionary.personal.WordWithContext
import no.divvun.dictionary.personal.PersonalDictionaryDatabase
import no.divvun.domain.DictionaryJson
import no.divvun.domain.WordContextJson
import no.divvun.domain.WordJson

class UploadUseCase(private val database: PersonalDictionaryDatabase, private val divvunDictionaryUploadService: DivvunDictionaryUploadService) {
    fun execute(): Single<DictionaryJson> {
        return database.dictionaryDao().dictionaryWithContexts()
                .take(1)
                .singleOrError()
                .map(mapper)
                .flatMap { divvunDictionaryUploadService.uploadDictionary(it) }

    }
}

val mapper: (List<WordWithContext>) -> DictionaryJson = { dictionary ->
    DictionaryJson(
            dictionary.map { wordWithContext ->
                WordJson(wordWithContext.dictionaryWord.word, wordWithContext.dictionaryWord.typeCount,
                        wordWithContext.contexts.map {
                            WordContextJson(it.prevWord, it.nextWord)
                        })
            })
}