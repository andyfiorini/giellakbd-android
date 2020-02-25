package no.divvun.dictionary.personal

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single

@Dao
interface DictionaryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertLanguage(language: Language): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertLanguageC(language: Language): Completable

    @Query("SELECT * FROM languages WHERE language_id = :languageId")
    fun findLanguage(languageId: Long): Array<Language>

    @Query("SELECT * FROM languages WHERE language = :language AND country = :country AND variant = :variant")
    fun findLanguage(language: String, country: String, variant: String): Array<Language>

    @Query("SELECT * FROM languages")
    fun languages(): List<Language>

    @Query("SELECT * FROM languages")
    fun languagesO(): Observable<List<Language>>


    @Query("SELECT * FROM words WHERE language_id=:languageId AND blacklisted=0")
    fun dictionary(languageId: Long): List<DictionaryWord>

    @Query("SELECT * FROM words WHERE language_id=:languageId AND blacklisted=0")
    fun dictionaryO(languageId: Long): Observable<List<DictionaryWord>>

    @Query("SELECT * FROM words WHERE language_id=:languageId AND blacklisted=1")
    fun blacklist(languageId: Long): List<DictionaryWord>

    @Query("SELECT * FROM words WHERE language_id=:languageId AND blacklisted=1")
    fun blacklistO(languageId: Long): Observable<List<DictionaryWord>>


    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertWord(word: DictionaryWord): Completable

    @Query("SELECT * FROM words WHERE word_id = :wordId")
    fun findWord(wordId: Long): List<DictionaryWord>

    @Query("SELECT * FROM words WHERE word_id = :wordId")
    fun findWordO(wordId: Long): Observable<DictionaryWord>

    @Query("SELECT * FROM words WHERE language_id == :languageId AND word = :word")
    fun findWord(languageId: Long, word: String): List<DictionaryWord>

    @Query("SELECT * FROM words WHERE language_id == :languageId AND word = :word")
    fun findWordS(languageId: Long, word: String): Single<List<DictionaryWord>>

    @Query("DELETE FROM words WHERE word_id = :wordId")
    fun removeWord(wordId: Long): Int

    @Update
    fun updateWord(word: DictionaryWord): Int

    @Transaction
    fun blacklistWord(wordId: Long, blacklist: Boolean): Int {
        val dictionaryWord = findWord(wordId).first()
        val updatedWord = dictionaryWord.copy(blacklisted = blacklist)
        return updateWord(updatedWord)
    }

    @Transaction
    fun insertContext(languageId: Long, word: String, wordContext: WordContext): Long {
        val l = findLanguage(languageId).firstOrNull() ?: return 0
        val dictionaryWord = findWord(l.languageId, word).firstOrNull() ?: return 0
        val wC = wordContext.copy(wordId = dictionaryWord.wordId)
        return insertContext(wC)
    }

    @Query("SELECT * FROM word_contexts WHERE word_id = :wordId")
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
    @Query("SELECT * FROM words WHERE language_id = :languageId")
    fun dictionaryWithContexts(languageId: Long): Observable<List<WordWithContext>>

    @Transaction
    @Query("SELECT * FROM words WHERE word_id = :wordId")
    fun wordWithContext(wordId: Long): Observable<WordWithContext>

}
