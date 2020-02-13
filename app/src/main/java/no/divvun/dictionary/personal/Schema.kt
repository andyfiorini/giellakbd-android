package no.divvun.dictionary.personal

import androidx.room.*
import com.google.gson.reflect.TypeToken

import com.google.gson.Gson
import java.lang.reflect.Type


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
        var dictionaryWord: DictionaryWord = DictionaryWord(),

        @Relation(parentColumn = "id", entityColumn = "word_id", entity = WordContext::class)
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
