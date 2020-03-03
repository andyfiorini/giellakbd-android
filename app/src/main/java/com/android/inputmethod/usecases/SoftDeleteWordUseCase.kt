package com.android.inputmethod.usecases

import arrow.core.Either
import com.lenguyenthanh.rxarrow.z
import io.reactivex.Single
import no.divvun.dictionary.personal.PersonalDictionaryDatabase

class SoftDeleteWordUseCase(val database: PersonalDictionaryDatabase) {
    fun execute(wordId: Long, softDelete: Boolean): Single<Either<RemoveWordException, RemoveWordSuccess>> {
        return database.dictionaryDao()
                .findWordS(wordId)
                .doOnSubscribe { database.beginTransaction() }
                .map { it.first() }
                .flatMap {
                    database.dictionaryDao().updateWordS(it.copy(softDeleted = softDelete))
                }
                .doOnSuccess { database.setTransactionSuccessful() }
                .doFinally { database.endTransaction() }
                .map { RemoveWordSuccess(wordId) }
                .z { RemoveWordException.Unknown(it) }
    }
}

sealed class RemoveWordException {
    data class Unknown(val cause: Throwable) : RemoveWordException()
}

data class RemoveWordSuccess(val wordId: Long)
