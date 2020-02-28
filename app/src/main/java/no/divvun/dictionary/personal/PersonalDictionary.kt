package no.divvun.dictionary.personal

import android.content.Context
import android.util.Log
import com.android.inputmethod.latin.Dictionary
import com.android.inputmethod.latin.NgramContext
import com.android.inputmethod.latin.SuggestedWords.SuggestedWordInfo
import com.android.inputmethod.latin.common.ComposedData
import com.android.inputmethod.latin.settings.SettingsValuesForSuggestion
import com.android.inputmethod.usecases.CreateLanguageUseCase
import no.divvun.createTag
import no.divvun.levenshtein
import java.util.*

class PersonalDictionary(private val context: Context?, locale: Locale) : Dictionary(TYPE_USER, locale) {
    private val TAG = createTag(this)

    private val database: PersonalDictionaryDatabase = PersonalDictionaryDatabase.getInstance(context!!)
    private val languageId by lazy {
        database.dictionaryDao().findLanguage(locale.language, locale.country, locale.variant).first().languageId
    }

    init {
        CreateLanguageUseCase(database).execute(locale.toLanguage())
        Log.d(TAG, "New Personal DictionaryInstance")
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

        Log.d(TAG, "composedData $composedData")

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

    fun learn(word: String) {
        if (isInDictionary(word)) {
            Log.d(TAG, "$word already in personal dictionary")
            val ret = database.dictionaryDao().incWord(languageId, word)
            Log.d(TAG, "Return $ret")
            return
        }

        if (database.candidatesDao().isCandidate(languageId, word) > 0) {
            Log.d(TAG, "$word was candidate, now in personal dictionary")
            // Word is already candidate, second time typed. Time to add to personal dictionary.
            database.candidatesDao().removeCandidate(languageId, word)
            database.dictionaryDao().insertWord(DictionaryWord(word, languageId = languageId)).subscribe()
        } else {
            Log.d(TAG, "$word is new candidate")
            database.candidatesDao().insertCandidate(Candidate(word, languageId = languageId))
        }
    }

    fun unlearn(word: String) {
        if (isInDictionary(word)) {
            Log.d(TAG, "$word already in personal dictionary")
            database.dictionaryDao().decWord(languageId, word)
        } else {
            Log.d(TAG, "$word is no longer candidate")
            database.candidatesDao().removeCandidate(languageId, word)
        }
    }

    private var cachedPrevWord: CachedWordContext? = null

    /* Previously written words, newest word is last */
    fun processContext(prevWords: List<String>, currentWord: String) {
        cachedPrevWord?.let {
            processPreviousWord(it, prevWords, currentWord)
        }

        cachedPrevWord = if (isInDictionary(currentWord)) {
            val currentContextId = database.dictionaryDao().insertContext(languageId, currentWord, prevWords, emptyList())
            CachedWordContext(currentWord, currentContextId)
        } else {
            null
        }
    }

    private fun processPreviousWord(prevContext: CachedWordContext, prevWords: List<String>, currentWord: String) {
        val prevWord = prevWords.lastOrNull() ?: return

        if (prevContext.word == prevWord) {
            val wordBefore = prevWords.dropLast(1)
            database.dictionaryDao().updateContext(prevContext.contextId, wordBefore, listOf(currentWord))
        }
    }

    private data class CachedWordContext(val word: String, val contextId: Long)
}
