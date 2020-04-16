package no.divvun

import android.content.Context
import com.android.inputmethod.latin.BuildConfig
import io.sentry.Sentry
import no.divvun.divvunspell.ThfstChunkedBoxSpellerArchive
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.util.*

//@SuppressLint("StaticFieldLeak")
object DivvunUtils {
    private val TAG = DivvunUtils::class.java.simpleName

    private fun dictFileName(locale: Locale) = "${locale.language}.bhfst"
    private fun cachedDictFileName(locale: Locale) = "${locale.language}_v${BuildConfig.VERSION_NAME}.bhfst"

    private fun clearOldDicts(context: Context, locale: Locale) {
        val filesDir = File(context.filesDir.absolutePath)

        val oldDicts = filesDir.listFiles { path ->
            path.startsWith("${locale.language}_v") &&
                    !path.endsWith("_v${BuildConfig.VERSION_NAME}.bhfst")
        }
        if (oldDicts.isNotEmpty()) {
            oldDicts.forEach {
                try {
                    it.delete()
                } catch (ex: Exception) {
                    // Do nothing.
                }
            }
        }

        val derp = File("${context.filesDir.absolutePath}/${cachedDictFileName(locale)}")

        if (!derp.exists()) {
            writeDict(context, locale)
        }
    }

    private fun hasDictInAssets(context: Context, locale: Locale): Boolean {
        return try {
            context.resources.assets.open("dicts/${dictFileName(locale)}")
            true
        } catch (e: Exception) {
            Timber.e("hasDictInAssets $e")
            false
        }
    }

    private fun writeDict(context: Context, locale: Locale) {
        val inputStream = context.resources.assets.open("dicts/${dictFileName(locale)}")
        Timber.d("Outputting file to ${context.filesDir.absolutePath}/${dictFileName(locale)}")
        val outputStream = FileOutputStream(File(context.filesDir, cachedDictFileName(locale)))
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.flush()
        outputStream.close()
    }

    private fun ensureCached(context: Context, locale: Locale) {
        // Check for anything prefixed with this language and delete it if it's not the correct version

        clearOldDicts(context, locale)
    }

    private val lock: Any = object {}
    private val loadedArchives = mutableMapOf<Locale, ThfstChunkedBoxSpellerArchive>()

    fun getSpeller(context: Context, locale: Locale?): ThfstChunkedBoxSpellerArchive? {
        Timber.d("getSpeller() for $locale")

        // We do not trust Java to provide us this non-null.
        if (locale == null || !hasDictInAssets(context, locale)) {
            return null
        }

        synchronized(lock) {
            try {
                ensureCached(context, locale)
            } catch (ex: Exception) {
                Sentry.capture(ex)
                return null
            }
        }

        return try {
            if (!loadedArchives.containsKey(locale)) {
                loadedArchives[locale] = ThfstChunkedBoxSpellerArchive.open("${context.filesDir.absolutePath}/${cachedDictFileName(locale)}")
            }

            return loadedArchives[locale]
        } catch (ex: Exception) {
            Sentry.capture(ex)
            null
        }
    }

    fun dumpMemoryMaps() {
        File("/proc/self/maps").forEachLine { Timber.v("/proc/self/maps, $it") }
    }
}