
package com.example.data

import java.text.Normalizer
import java.util.Locale

/**
 * Normalizes Arabic and Latin text for reliable search.
 * Handles diacritics, tatweel, common Arabic letter variants,
 * Arabic-Indic digits, and Persian digits.
 */
object SearchTextNormalizer {

    fun normalize(value: String): String {
        val decomposed = Normalizer.normalize(
            value.lowercase(Locale.ROOT),
            Normalizer.Form.NFD
        )

        return buildString(decomposed.length) {
            decomposed.forEach { character ->
                val type = Character.getType(character)

                if (
                    type == Character.NON_SPACING_MARK.toInt() ||
                    type == Character.COMBINING_SPACING_MARK.toInt() ||
                    type == Character.ENCLOSING_MARK.toInt() ||
                    character == '\u0640'
                ) {
                    return@forEach
                }

                append(
                    when (character) {
                        in '\u0660'..'\u0669' ->
                            ('0'.code + character.code - '\u0660'.code).toChar()

                        in '\u06F0'..'\u06F9' ->
                            ('0'.code + character.code - '\u06F0'.code).toChar()

                        '\u0622', '\u0623', '\u0625', '\u0671' -> '\u0627'
                        '\u0649', '\u0626' -> '\u064a'
                        '\u0624' -> '\u0648'
                        '\u0629' -> '\u0647'
                        else -> character
                    }
                )
            }
        }.replace(Regex("\\s+"), " ").trim()
    }
}
