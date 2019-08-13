package no.divvun.dictionary.personal

import androidx.room.*
import io.reactivex.Flowable

@Dao
interface DictionaryDao {
    @Query("SELECT * FROM Dictionary")
    fun dictionary(): List<DictionaryWord>

    @Query("SELECT * FROM Dictionary")
    fun dictionaryF(): Flowable<List<DictionaryWord>>

    @Query("SELECT * FROM Dictionary WHERE word=:word")
    fun isInDictionary(word: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertWord(word: DictionaryWord): Long

    @Query("SELECT * FROM Dictionary WHERE word = :word")
    fun findWord(word: String): Array<DictionaryWord>

    @Query("DELETE FROM Dictionary WHERE word = :word")
    fun removeWord(word: String): Int

    @Transaction
    fun insertContext(word: String, wordContext: WordContext): Long {
        val dictionaryWord = findWord(word).firstOrNull() ?: return 0
        wordContext.wordId = dictionaryWord.wordId
        return insertContext(wordContext)
    }

    @Insert
    fun insertContext(wordContext: WordContext): Long

    @Delete
    fun removeContext(wordContext: WordContext): Int

    /**
    @@Transaction
    fun findContext(wordContext: WordContext): Flowable<List<WordContext>>
     */
}