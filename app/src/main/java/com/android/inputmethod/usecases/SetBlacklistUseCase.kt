package com.android.inputmethod.usecases

import no.divvun.dictionary.personal.PersonalDictionaryDatabase

class SetBlacklistUseCase(val database: PersonalDictionaryDatabase) {
    fun execute(wordId: Long, blacklist: Boolean) {
        database.dictionaryDao()
                .blacklistWord(wordId, blacklist)
    }
}