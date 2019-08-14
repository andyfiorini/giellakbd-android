package no.divvun.dictionary.personal

import androidx.room.*

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
        val typeCount: Long = 0
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var wordId: Long = 0
}

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
        val nextWord: String? = null
) {

    @ColumnInfo(name = "word_id")
    var wordId: Long = 0

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "word_context_id")
    var wordContextId: Long = 0
}
