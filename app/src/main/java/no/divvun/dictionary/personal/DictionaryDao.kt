package no.divvun.dictionary.personal

import android.util.Log
import androidx.room.*
import io.reactivex.Flowable
import io.reactivex.Observable

@Dao
interface DictionaryDao {
    @Query("SELECT * FROM Dictionary")
    fun dictionary(): List<DictionaryWord>

    @Query("SELECT * FROM Dictionary")
    fun dictionaryO(): Observable<List<DictionaryWord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWord(word: DictionaryWord): Long

    @Query("SELECT * FROM Dictionary WHERE word = :word")
    fun findWord(word: String): Array<DictionaryWord>

    @Query("SELECT * FROM Dictionary WHERE id = :wordId")
    fun findWord(wordId: Long): Observable<DictionaryWord>

    @Query("DELETE FROM Dictionary WHERE id = :wordId")
    fun removeWord(wordId: Long): Int

    @Update
    fun updateWord(word: DictionaryWord): Int

    @Transaction
    fun insertContext(word: String, wordContext: WordContext): Long {
        val dictionaryWord = findWord(word).firstOrNull() ?: return 0
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
    fun incWord(word: String): Int {
        val dictionaryWord = findWord(word).firstOrNull() ?: return 0
        return updateWord(dictionaryWord.copy(typeCount = dictionaryWord.typeCount.inc()))
    }

    @Transaction
    fun decWord(word: String): Int {
        val dictionaryWord = findWord(word).firstOrNull() ?: return 0
        return updateWord(dictionaryWord.copy(typeCount = dictionaryWord.typeCount.dec()))
    }

    @Transaction @Query("SELECT * FROM Dictionary")
    fun dictionaryWithContexts(): Observable<List<DictionaryWordWithContext>>

}
