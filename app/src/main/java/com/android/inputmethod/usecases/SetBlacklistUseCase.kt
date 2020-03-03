package com.android.inputmethod.usecases

import arrow.core.Either
import com.lenguyenthanh.rxarrow.z
import io.reactivex.Single
import no.divvun.dictionary.personal.PersonalDictionaryDatabase

class SetBlacklistUseCase(val database: PersonalDictionaryDatabase) {
    fun execute(wordId: Long, blacklist: Boolean): Single<Either<BlacklistWordException, BlacklistWordSuccess>> {
        return database.dictionaryDao()
                .findWordS(wordId)
                .doOnSubscribe { database.beginTransaction() }
                .map {
                    it.first().copy(blacklisted = blacklist)
                }.flatMap {
                    database.dictionaryDao()
                            .upsertWord(it)
                            .toSingle { BlacklistWordSuccess }
                }
                .doOnSuccess { database.setTransactionSuccessful() }
                .doFinally { database.endTransaction() }
                .z {
                    BlacklistWordException.Unknown(it)
                }
    }
}

sealed class BlacklistWordException {
    data class Unknown(val cause: Throwable) : BlacklistWordException()
}

object BlacklistWordSuccess
