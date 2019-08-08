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

class PersonalDictionary(private val context: Context?, locale: Locale?): Dictionary(TYPE_USER, locale) {

    val words = mutableSetOf("banana", "hokuspokus", "bananab")

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

    companion object {
        const val N_BEST_SUGGESTION_SIZE = 3L
    }
}