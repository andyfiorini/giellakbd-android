package no.divvun.dictionary.personal

import android.content.Context
import android.util.Log
import com.android.inputmethod.latin.Dictionary
import com.android.inputmethod.latin.NgramContext
import com.android.inputmethod.latin.SuggestedWords.SuggestedWordInfo
import com.android.inputmethod.latin.common.ComposedData
import com.android.inputmethod.latin.settings.SettingsValuesForSuggestion
import com.android.inputmethod.usecases.CreateLanguageUseCase
import no.divvun.levenshtein
import java.util.*

class PersonalDictionary(private val context: Context?, locale: Locale) : Dictionary(TYPE_USER, locale) {

    private val database: PersonalDictionaryDatabase = PersonalDictionaryDatabase.getInstance(context!!)
    private val languageId by lazy {
        database.dictionaryDao().findLanguage(locale.language, locale.country, locale.variant).first().languageId
    }

    init {
        CreateLanguageUseCase(database).execute(locale.toLanguage())
    }

    private fun Locale.toLanguage(): Language {
        return Language(language, country, variant)
    }

    override fun getSuggestions(
            composedData: ComposedData,
            ngramContext: NgramContext,
            proximityInfoHandle: Long,
            settingsValuesForSuggestion: SettingsValuesForSuggestion,
            sessionId: Int,
            weightForLocale: Float,
            inOutWeightOfLangModelVsSpatialModel: FloatArray): ArrayList<SuggestedWordInfo> {


        val scoreMap = database.dictionaryDao().dictionary(languageId)
                .asSequence()
                .map { it.word }
                .map { it to it.levenshtein(composedData.mTypedWord) }
                .filter { it.second < 4 }
                .sortedBy { it.second }
                .take(5).toList()

        Log.d("PersonalDictionary", "composedData $composedData")

        val results = scoreMap.map { (suggestion, levenshteinScore) ->
            SuggestedWordInfo(suggestion, ngramContext.extractPrevWordsContext(),
                    SuggestedWordInfo.MAX_SCORE - levenshteinScore, SuggestedWordInfo.KIND_COMPLETION, this,
                    SuggestedWordInfo.NOT_AN_INDEX, SuggestedWordInfo.NOT_A_CONFIDENCE)
        }

        return ArrayList(results)
    }

    override fun isInDictionary(word: String): Boolean {
        return database.dictionaryDao().findWord(languageId, word).isNotEmpty()
    }

    fun addWord(word: String) {
        if (isInDictionary(word)) {
            Log.d("PersonalDict", "$word already in personal dictionary")
            val ret = database.dictionaryDao().incWord(languageId, word)
            Log.d("PersonalDict", "Return $ret")
            return
        }

        if (database.candidatesDao().isCandidate(languageId, word) > 0) {
            Log.d("PersonalDict", "$word was candidate, now in personal dictionary")
            // Word is already candidate, second time typed. Time to add to personal dictionary.
            database.candidatesDao().removeCandidate(languageId, word)
            database.dictionaryDao().insertWord(DictionaryWord(word, languageId = languageId)).subscribe()
        } else {
            Log.d("PersonalDict", "$word is new candidate")
            database.candidatesDao().insertCandidate(Candidate(word, languageId = languageId))
        }
    }

    fun undoWord(word: String) {
        if (isInDictionary(word)) {
            Log.d("PersonalDict", "$word already in personal dictionary")
            database.dictionaryDao().decWord(languageId, word)
        } else {
            Log.d("PersonalDict", "$word is no longer candidate")
            database.candidatesDao().removeCandidate(languageId, word)
        }

    }

    fun updateContext(ngramContext: NgramContext, nextWords: List<String>) {
        val word = ngramContext.getNthPrevWord(1)
        if (word != null && isInDictionary(word.toString())) {
            val prevWord2 = ngramContext.getNthPrevWord(2)?.toString()
            val prevWord3 = ngramContext.getNthPrevWord(3)?.toString()
            val prevWords = listOfNotNull(prevWord2, prevWord3)
            database.dictionaryDao().insertContext(languageId, word.toString(), WordContext(prevWords, nextWords))
        }
    }
}
