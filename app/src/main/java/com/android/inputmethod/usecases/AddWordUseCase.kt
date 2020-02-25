package com.android.inputmethod.usecases

import android.database.sqlite.SQLiteConstraintException
import arrow.core.Either
import arrow.core.left
import com.lenguyenthanh.rxarrow.z
import io.reactivex.Single
import no.divvun.dictionary.personal.DictionaryWord
import no.divvun.dictionary.personal.PersonalDictionaryDatabase

class AddWordUseCase(
        private val database: PersonalDictionaryDatabase,
        private val validateWordUseCase: ValidateWordUseCase) {

    fun execute(languageId: Long, word: String): Single<Either<AddWordException, AddWordSuccess>> {
        return validateWordUseCase.execute(word)
                .flatMap { validationE ->
                    validationE.fold({
                        Single.just(AddWordException.Validation(it).left())
                    }, {
                        database.dictionaryDao()
                                .insertWord(DictionaryWord(word, 0, true, languageId = languageId))
                                .toSingle { AddWordSuccess }
                                .z {
                                    when (it) {
                                        is SQLiteConstraintException -> {
                                            AddWordException.WordAlreadyExists(it)
                                        }
                                        else -> {
                                            AddWordException.Unknown(it)
                                        }
                                    }
                                }
                    })
                }
    }
}


sealed class AddWordException {
    class Validation(val validationException: ValidationException) : AddWordException()
    data class WordAlreadyExists(val cause: Throwable) : AddWordException()
    data class Unknown(val cause: Throwable) : AddWordException()
}

object AddWordSuccess