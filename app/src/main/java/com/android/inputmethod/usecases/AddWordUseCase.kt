package com.android.inputmethod.usecases

import android.database.sqlite.SQLiteConstraintException
import arrow.core.Either
import com.lenguyenthanh.rxarrow.z
import io.reactivex.Single
import no.divvun.dictionary.personal.DictionaryWord
import no.divvun.dictionary.personal.PersonalDictionaryDatabase

class AddWordUseCase(val database: PersonalDictionaryDatabase) {
    fun execute(word: String): Single<Either<AddWordException, AddWordSuccess>> {
        return database.dictionaryDao()
                .insertWord(DictionaryWord(word, 0))
                .toSingle { AddWordSuccess }
                .z {
                    if (it is SQLiteConstraintException) {
                        AddWordException.WordAlreadyExists(it)
                    } else {
                        AddWordException.Unknown(it)
                    }
                }
    }
}

sealed class AddWordException(val throwable: Throwable) {
    class WordAlreadyExists(throwable: Throwable) : AddWordException(throwable)
    class Unknown(throwable: Throwable) : AddWordException(throwable)
}

object AddWordSuccess