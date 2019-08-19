package com.android.inputmethod.usecases

import com.google.gson.Gson
import io.reactivex.Single
import no.divvun.dictionary.personal.PersonalDictionaryDatabase
import no.divvun.dictionary.personal.DictionaryWordWithContext
import no.divvun.domain.DictionaryJson
import no.divvun.domain.WordContextJson
import no.divvun.domain.WordJson

class JsonDictionaryUseCase(private val database: PersonalDictionaryDatabase, private val gson: Gson) {
    fun execute(): Single<String> {
        return database.dictionaryDao().dictionaryWithContexts()
                .take(1)
                .singleOrError()
                .map(mapper)
                .map { gson.toJson(it) }
    }
}

val mapper: (List<DictionaryWordWithContext>) -> DictionaryJson = { dictionary ->
    DictionaryJson(
            dictionary.map { wordWithContext ->
                WordJson(wordWithContext.dictionaryWord.word, wordWithContext.dictionaryWord.typeCount,
                        wordWithContext.contexts.map {
                            WordContextJson(it.prevWord, it.nextWord)
                        })
            })

}