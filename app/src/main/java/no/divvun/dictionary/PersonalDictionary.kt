package no.divvun.dictionary

import android.content.Context
import android.util.Log
import com.android.inputmethod.latin.Dictionary
import com.android.inputmethod.latin.NgramContext
import com.android.inputmethod.latin.SuggestedWords
import com.android.inputmethod.latin.common.ComposedData
import com.android.inputmethod.latin.settings.SettingsValuesForSuggestion
import java.util.*
import com.android.inputmethod.latin.SuggestedWords.SuggestedWordInfo
import no.divvun.DivvunSpell
import no.divvun.DivvunUtils
import no.divvun.createTag
import no.divvun.levenshtein
import kotlin.collections.ArrayList

class PersonalDictionary(private val context: Context?, locale: Locale?) : Dictionary(TYPE_USER, locale) {

    private val words = mutableSetOf<String>()
    private val candidates = mutableSetOf<String>()

    override fun getSuggestions(
            composedData: ComposedData,
            ngramContext: NgramContext,
            proximityInfoHandle: Long,
            settingsValuesForSuggestion: SettingsValuesForSuggestion,
            sessionId: Int,
            weightForLocale: Float,
            inOutWeightOfLangModelVsSpatialModel: FloatArray): ArrayList<SuggestedWordInfo> {


        val scoreMap = words
                .map { it to it.levenshtein(composedData.mTypedWord) }
                .filter { it.second < 4 }
                .sortedBy { it.second }
                .take(5)

        Log.d("PersonalDictionary", scoreMap.toString())

        val results = scoreMap.map { (suggestion, levenshteinScore) ->
            SuggestedWordInfo(suggestion, ngramContext.extractPrevWordsContext(),
                    SuggestedWordInfo.MAX_SCORE - levenshteinScore, SuggestedWordInfo.KIND_COMPLETION, this,
                    SuggestedWordInfo.NOT_AN_INDEX, SuggestedWordInfo.NOT_A_CONFIDENCE)
        }

        return ArrayList(results)
    }

    override fun isInDictionary(word: String): Boolean {
        return words.contains(word)
    }

    fun addWord(word: String) {
        if (isInDictionary(word)) {
            Log.d("PersonalDict", "$word already in personal dictionary")
            return
        }

        if (candidates.contains(word)){
            Log.d("PersonalDict", "$word was candidate, now in personal dictionary")
            // Word is already candidate, second time typed. Time to add to personal dictionary.
            candidates.remove(word)
            words.add(word)
        } else {
            Log.d("PersonalDict", "$word is new candidate")
            candidates.add(word)
        }
    }

    fun undoWord(word: String) {
        Log.d("PersonalDict", "$word is no longer candidate")
        candidates.remove(word)
    }
}
