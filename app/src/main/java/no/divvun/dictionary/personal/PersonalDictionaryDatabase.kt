package no.divvun.dictionary.personal

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Candidate::class, DictionaryWord::class, WordContext::class], version = 2, exportSchema = false)
abstract class PersonalDictionaryDatabase : RoomDatabase() {
    companion object {

        @Volatile
        private var INSTANCE: PersonalDictionaryDatabase? = null

        fun getInstance(context: Context): PersonalDictionaryDatabase =
                INSTANCE ?: synchronized(this) {
                    INSTANCE
                            ?: buildDatabase(context).also { INSTANCE = it }
                }

        private fun buildDatabase(context: Context) =
                Room.databaseBuilder(context.applicationContext,
                        PersonalDictionaryDatabase::class.java, "Sample.db")
                        // TODO, remove destructive migrations
                        .fallbackToDestructiveMigration()
                        // TODO, Do not work on main thread unless needed.
                        .allowMainThreadQueries()
                        .build()
    }

    abstract fun candidatesDao(): CandidatesDao
    abstract fun dictionaryDao(): DictionaryDao
}

