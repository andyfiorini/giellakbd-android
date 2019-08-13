package no.divvun.dictionary.personal

import android.content.Context
import android.util.Log
import com.android.inputmethod.latin.Dictionary
import com.android.inputmethod.latin.NgramContext
import com.android.inputmethod.latin.SuggestedWords.SuggestedWordInfo
import com.android.inputmethod.latin.common.ComposedData
import com.android.inputmethod.latin.settings.SettingsValuesForSuggestion
import no.divvun.levenshtein
import java.util.*

class PersonalDictionary(private val context: Context?, locale: Locale?) : Dictionary(TYPE_USER, locale) {

    private val database: PersonalDictionaryDatabase = PersonalDictionaryDatabase.getInstance(context!!)

    override fun getSuggestions(
            composedData: ComposedData,
            ngramContext: NgramContext,
            proximityInfoHandle: Long,
            settingsValuesForSuggestion: SettingsValuesForSuggestion,
            sessionId: Int,
            weightForLocale: Float,
            inOutWeightOfLangModelVsSpatialModel: FloatArray): ArrayList<SuggestedWordInfo> {


        val scoreMap = database.dictionaryDao().dictionary()
                .asSequence()
                .map { it.word }
                .map { it to it.levenshtein(composedData.mTypedWord) }
                .filter { it.second < 4 }
                .sortedBy { it.second }
                .take(5).toList()

        Log.d("PersonalDictionary", scoreMap.toString())

        val results = scoreMap.map { (suggestion, levenshteinScore) ->
            SuggestedWordInfo(suggestion, ngramContext.extractPrevWordsContext(),
                    SuggestedWordInfo.MAX_SCORE - levenshteinScore, SuggestedWordInfo.KIND_COMPLETION, this,
                    SuggestedWordInfo.NOT_AN_INDEX, SuggestedWordInfo.NOT_A_CONFIDENCE)
        }

        return ArrayList(results)
    }

    override fun isInDictionary(word: String): Boolean {
        return database.dictionaryDao().isInDictionary(word)
    }

    fun addWord(word: String) {
        if (isInDictionary(word)) {
            Log.d("PersonalDict", "$word already in personal dictionary")
            return
        }

        if (database.candidatesDao().isCandidate(word) > 0) {
            Log.d("PersonalDict", "$word was candidate, now in personal dictionary")
            // Word is already candidate, second time typed. Time to add to personal dictionary.
            database.candidatesDao().removeCandidate(word)
            database.dictionaryDao().insertWord(DictionaryWord(word))
        } else {
            Log.d("PersonalDict", "$word is new candidate")
            database.candidatesDao().insertCandidate(Candidate(word))
        }
    }

    fun undoWord(word: String) {
        Log.d("PersonalDict", "$word is no longer candidate")
        database.candidatesDao().removeCandidate(word)
    }

    fun updateContext(ngramContext: NgramContext, nextWord: String?) {
        val word = ngramContext.getNthPrevWord(1)
        if (word != null && isInDictionary(word.toString())) {
            val prevWord = ngramContext.getNthPrevWord(2)?.toString()
            database.dictionaryDao().insertContext(word.toString(), WordContext(prevWord, nextWord))
        }
    }
}
