package no.divvun.dictionary.personal

import androidx.room.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


typealias Dictionary = List<DictionaryWord>

@Entity(tableName = "Language",
        indices = [Index("language", unique = true)])
data class Language(
        val language: String,
        val country: String = "",
        val variant: String = "",
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "language_id")
        val languageId: Long = 0
)

@Entity(tableName = "Candidates",
        foreignKeys = [
            ForeignKey(entity = Language::class,
                    parentColumns = arrayOf("language_id"),
                    childColumns = arrayOf("language_id"),
                    onDelete = ForeignKey.CASCADE)],
        indices = [Index(value = ["word", "language_id"], unique = true), Index("language_id")])
data class Candidate(
        @PrimaryKey
        val word: String = "",
        @ColumnInfo(name = "language_id")
        val languageId: Long
)

@Entity(tableName = "Dictionary",
        foreignKeys = [ForeignKey(entity = Language::class,
                parentColumns = arrayOf("language_id"),
                childColumns = arrayOf("language_id"),
                onDelete = ForeignKey.CASCADE)],
        indices = [Index(value = ["word", "language_id"], unique = true), Index("language_id")])
data class DictionaryWord(
        val word: String = "",
        val typeCount: Long = 2,
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "word_id")
        val wordId: Long = 0,
        @ColumnInfo(name = "language_id")
        val languageId: Long
)

@Entity(tableName = "WordContext",
        foreignKeys = [
            ForeignKey(entity = DictionaryWord::class,
                    parentColumns = arrayOf("word_id"),
                    childColumns = arrayOf("word_id"),
                    onDelete = ForeignKey.CASCADE)],
        indices = [Index("word_id")]
)
@TypeConverters(ListOfStringsTypeConverters::class)
data class WordContext(
        val prevWords: List<String> = emptyList(),
        val nextWords: List<String> = emptyList(),
        @ColumnInfo(name = "word_id")
        val wordId: Long = 0,
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "word_context_id")
        val wordContextId: Long = 0
)


data class WordWithContext(
        @Embedded
        var dictionaryWord: DictionaryWord,

        @Relation(parentColumn = "word_id", entityColumn = "word_id", entity = WordContext::class)
        var contexts: List<WordContext> = emptyList()
)

class ListOfStringsTypeConverters {
    @TypeConverter
    fun stringToListOfString(json: String): List<String> {
        val gson = Gson()
        val type: Type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson<List<String>>(json, type)
    }

    @TypeConverter
    fun listOfStringToString(list: List<String>): String {
        val gson = Gson()
        val type: Type = object : TypeToken<List<String>>() {}.type
        return gson.toJson(list, type)
    }
}
