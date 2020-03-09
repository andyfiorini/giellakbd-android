package no.divvun.dictionary

import android.content.Context
import android.util.Log
import android.util.LruCache
import com.android.inputmethod.keyboard.Keyboard
import com.android.inputmethod.latin.DictionaryFacilitator
import com.android.inputmethod.latin.DictionaryStats
import com.android.inputmethod.latin.NgramContext
import com.android.inputmethod.latin.common.ComposedData
import com.android.inputmethod.latin.settings.SettingsValuesForSuggestion
import com.android.inputmethod.latin.utils.SuggestionResults
import no.divvun.createTag
import no.divvun.dictionary.personal.PersonalDictionary
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

class DivvunDictionaryFacilitator : DictionaryFacilitator {
    private val TAG = createTag(this)
    private var isActive = false

    private var dictionary = DivvunDictionary(null, null)
    private lateinit var personalDictionary: PersonalDictionary

    // STUB
    override fun setValidSpellingWordReadCache(cache: LruCache<String, Boolean>) {
        Log.d(TAG, "setValidSpellingWordReadCache")
    }

    // STUB
    override fun setValidSpellingWordWriteCache(cache: LruCache<String, Boolean>?) {
        Log.d(TAG, "setValidSpellingWordWriteCache")
    }

    override fun isForLocale(locale: Locale?): Boolean {
        Log.d(TAG, "isForLocale $locale")
        return dictionary.mLocale == locale
    }

    // STUB
    override fun isForAccount(account: String?): Boolean {
        Log.d(TAG, "isForAccount")
        return false
    }

    // STUB
    override fun onStartInput() {
        Log.d(TAG, "onStartInput")
        isActive = true
    }

    // STUB
    override fun onFinishInput(context: Context?) {
        Log.d(TAG, "onFinishInput")
        isActive = false
    }

    // STUB
    override fun isActive(): Boolean {
        Log.d(TAG, "isActive")
        return isActive
    }

    override fun getLocale(): Locale {
        Log.d(TAG, "getLocale")
        return dictionary.mLocale!!
    }

    // STUB
    override fun usesContacts(): Boolean {
        Log.d(TAG, "usesContacts")
        return false
    }

    // STUB
    override fun getAccount(): String {
        Log.d(TAG, "getAccount")
        return ""
    }

    // STUB
    override fun resetDictionaries(context: Context?, newLocale: Locale?, useContactsDict: Boolean, usePersonalizedDicts: Boolean, forceReloadMainDictionary: Boolean, account: String?, dictNamePrefix: String?, listener: DictionaryFacilitator.DictionaryInitializationListener?) {
        context!!.let {
            dictionary = DivvunDictionary(it, newLocale)
            personalDictionary = PersonalDictionary(it, newLocale!!)
        }
    }

    // STUB
    override fun resetDictionariesForTesting(context: Context?, locale: Locale?, dictionaryTypes: ArrayList<String>?, dictionaryFiles: HashMap<String, File>?, additionalDictAttributes: MutableMap<String, MutableMap<String, String>>?, account: String?) {
        Log.d(TAG, "resetDictionariesForTesting")
    }

    // STUB
    override fun closeDictionaries() {
        Log.d(TAG, "closeDictionaries")
    }

    // STUB
    override fun hasAtLeastOneInitializedMainDictionary(): Boolean {
        Log.d(TAG, "hasAtLeastOneInitializedMainDictionary")
        return dictionary.isInitialized
    }

    // STUB
    override fun hasAtLeastOneUninitializedMainDictionary(): Boolean {
        Log.d(TAG, "hasAtLeastOneUninitializedMainDictionary")
        return !dictionary.isInitialized
    }

    // STUB
    override fun waitForLoadingMainDictionaries(timeout: Long, unit: TimeUnit?) {
        Log.d(TAG, "waitForLoadingMainDictionaries")
    }

    // STUB
    override fun waitForLoadingDictionariesForTesting(timeout: Long, unit: TimeUnit?) {
        Log.d(TAG, "waitForLoadingDictionariesForTesting")
    }

    // STUB
    override fun addToUserHistory(word: String, wasAutoCapitalized: Boolean, ngramContext: NgramContext, timeStampInSeconds: Long, blockPotentiallyOffensive: Boolean) {
        if (!dictionary.isInDictionary(word)) {
            Log.d(TAG, "Adding non known word: $word")
            personalDictionary.learn(word)
        }

        val previousWords = ngramContext.extractPrevWordsContextArray().toList().filter { it != NgramContext.BEGINNING_OF_SENTENCE_TAG }.takeLast(2)
        personalDictionary.processContext(previousWords, word)
    }

    // STUB
    override fun unlearnFromUserHistory(word: String?, ngramContext: NgramContext, timeStampInSeconds: Long, eventType: Int) {
        Log.d(TAG, "unlearnFromUserHistory")
        word?.let {
            personalDictionary.unlearn(word)
        }
    }

    override fun getSuggestionResults(composedData: ComposedData, ngramContext: NgramContext, keyboard: Keyboard, settingsValuesForSuggestion: SettingsValuesForSuggestion, sessionId: Int, inputStyle: Int): SuggestionResults {
        val divvunSuggestions = dictionary.getSuggestions(composedData, ngramContext, 0, settingsValuesForSuggestion, sessionId, 0f, FloatArray(0))
        val personalSuggestions = personalDictionary.getSuggestions(composedData, ngramContext, 0, settingsValuesForSuggestion, sessionId, 0f, FloatArray(0))

        val suggestionResults = SuggestionResults(divvunSuggestions.size + personalSuggestions.size, ngramContext.isBeginningOfSentenceContext, true)

        // Add all our suggestions
        suggestionResults.addAll(divvunSuggestions)
        suggestionResults.addAll(personalSuggestions)

        Log.d(TAG, "Personal suggestions: $personalSuggestions")
        Log.d(TAG, "All suggestions: $suggestionResults")

        return suggestionResults
    }

    override fun isValidSpellingWord(word: String): Boolean = dictionary.isValidWord(word)
    override fun isValidSuggestionWord(word: String): Boolean = dictionary.isValidWord(word)

    // STUB
    override fun clearUserHistoryDictionary(context: Context?): Boolean {
        Log.d(TAG, "clearUserHistoryDictionary")
        return true
    }

    // STUB
    override fun dump(context: Context?): String {
        Log.d(TAG, "dump")
        return ""
    }

    // STUB
    override fun dumpDictionaryForDebug(dictName: String?) {
        Log.d(TAG, "dumpDictionaryForDebug")
    }

    // STUB
    override fun getDictionaryStats(context: Context?): MutableList<DictionaryStats> {
        Log.d(TAG, "getDictionaryStats")
        return mutableListOf()
    }

}
