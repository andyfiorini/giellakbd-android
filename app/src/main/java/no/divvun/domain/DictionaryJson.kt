package no.divvun.domain

data class DictionaryJson(
    val words: List<WordJson>
)

data class WordJson(
        val word: String,
        val typeCount: Long,
        val contexts: List<WordContextJson>
)

data class WordContextJson(
        val prevWord: String?,
        val nextWord: String?
)