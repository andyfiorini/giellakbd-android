package no.divvun.dictionary.personal

import androidx.room.*

typealias Dictionary = List<DictionaryWord>

@Entity(tableName = "Candidates",
        primaryKeys = ["word"],
        indices = [Index("word", unique = true)])
data class Candidate(
        val word: String = ""
)

@Entity(tableName = "Dictionary",
        indices = [Index("word", unique = true)])
data class DictionaryWord(
        val word: String = "",
        val typeCount: Long = 2,
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "id")
        val wordId: Long = 0
)

@Entity(tableName = "WordContext",
        foreignKeys = [
            ForeignKey(entity = DictionaryWord::class,
                    parentColumns = arrayOf("id"),
                    childColumns = arrayOf("word_id"),
                    onDelete = ForeignKey.CASCADE)],
        indices = [Index("word_id")]
)

data class WordContext(
        val prevWord: String? = null,
        val nextWord: String? = null,
        @ColumnInfo(name = "word_id")
        val wordId: Long = 0,
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "word_context_id")
        val wordContextId: Long = 0
)

data class DictionaryWordWithContext(
        @Embedded
        var dictionaryWord: DictionaryWord = DictionaryWord(),

        @Relation(parentColumn = "id", entityColumn = "word_id", entity = WordContext::class)
        var contexts: List<WordContext> = emptyList()
)
