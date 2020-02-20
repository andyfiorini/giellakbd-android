package no.divvun.dictionary.personal

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single

@Dao
interface DictionaryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLanguage(language: Language): Completable

    @Query("SELECT * FROM Language WHERE language = :language")
    fun findLanguage(language: String): Array<Language>

    @Query("SELECT * FROM Language")
    fun languages(): List<Language>

    @Query("SELECT * FROM Language")
    fun languagesO(): Observable<List<Language>>


    @Query("SELECT * FROM Dictionary WHERE language_id=:languageId")
    fun dictionary(languageId: Long): List<DictionaryWord>

    @Query("SELECT * FROM Dictionary WHERE language_id=:languageId")
    fun dictionaryO(languageId: Long): Observable<List<DictionaryWord>>


    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertWord(word: DictionaryWord): Completable

    @Query("SELECT * FROM Dictionary WHERE word_id = :wordId")
    fun findWord(wordId: Long): List<DictionaryWord>

    @Query("SELECT * FROM Dictionary WHERE word_id = :wordId")
    fun findWordO(wordId: Long): Observable<DictionaryWord>

    @Query("SELECT * FROM Dictionary WHERE language_id == :languageId AND word = :word")
    fun findWord(languageId: Long, word: String): List<DictionaryWord>

    @Query("SELECT * FROM Dictionary WHERE language_id == :languageId AND word = :word")
    fun findWordS(languageId: Long, word: String): Single<List<DictionaryWord>>

    @Query("DELETE FROM Dictionary WHERE word_id = :wordId")
    fun removeWord(wordId: Long): Int

    @Update
    fun updateWord(word: DictionaryWord): Int

    @Transaction
    fun insertContext(language: String, word: String, wordContext: WordContext): Long {
        val l = findLanguage(language).firstOrNull() ?: return 0
        val dictionaryWord = findWord(l.languageId, word).firstOrNull() ?: return 0
        val wC = wordContext.copy(wordId = dictionaryWord.wordId)
        return insertContext(wC)
    }

    @Query("SELECT * FROM WordContext WHERE word_id = :wordId")
    fun wordContexts(wordId: Long): Flowable<List<WordContext>>

    @Insert
    fun insertContext(wordContext: WordContext): Long

    @Delete
    fun removeContext(wordContext: WordContext): Int

    @Transaction
    fun incWord(languageId: Long, word: String): Int {
        val dictionaryWord = findWord(languageId, word).firstOrNull() ?: return 0
        return updateWord(dictionaryWord.copy(typeCount = dictionaryWord.typeCount.inc()))
    }

    @Transaction
    fun incWord(wordId: Long): Int {
        val dictionaryWord = findWord(wordId).firstOrNull() ?: return 0
        return updateWord(dictionaryWord.copy(typeCount = dictionaryWord.typeCount.inc()))
    }

    @Transaction
    fun decWord(languageId: Long, word: String): Int {
        val dictionaryWord = findWord(languageId, word).firstOrNull() ?: return 0
        return updateWord(dictionaryWord.copy(typeCount = dictionaryWord.typeCount.dec()))
    }

    @Transaction
    fun decWord(wordId: Long): Int {
        val dictionaryWord = findWord(wordId).firstOrNull() ?: return 0
        return updateWord(dictionaryWord.copy(typeCount = dictionaryWord.typeCount.dec()))
    }

    @Transaction
    @Query("SELECT * FROM Dictionary WHERE language_id = :languageId")
    fun dictionaryWithContexts(languageId: Long): Observable<List<WordWithContext>>

    @Transaction
    @Query("SELECT * FROM Dictionary WHERE word_id = :wordId")
    fun wordWithContext(wordId: Long): Observable<WordWithContext>

}
