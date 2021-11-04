package com.android.gradle.replicator.generator.util


/**
 * This class wraps around regex and allows you to match strings with wildcards as you would regex
 * Ex: WildcardString("ab*d").match(...) would match "abcd", "abd", "abced", etc.
 * It also overrides match and hashcode so you can compare it: WildcardString("abc") == WildcardString("abc")
 * and use it as a map key: wildcardMap = mapOf(WildcardString("abc") to 1), wildcardMap[WildcardString("abc")]
 */
class WildcardString(private val baseString: String) {
    private val regex: Regex by lazy { convertToRegex(baseString) }

    /**
     * Tells you whether the base string is a wildcard pattern
     * @return true if wildcard pattern, false if plain string
     */
    val isWildcard: Boolean by lazy { isWildcardString(baseString) }

    private fun isWildcardString(str: String): Boolean {
        val wildcardCharacters = listOf('*', '?', '#')
        for (i in wildcardCharacters) {
            if (str.contains(i)) {
                return true
            }
        }
        return false
    }

    // Separate wildcard tokens and create a regex string. Does not support !, ^ and []
    private fun convertToRegex(wildcardString: String): Regex {
        var ret = "\\Q"
        for (c in wildcardString) {
            when(c) {
                '*' -> {
                    ret += "\\E.*\\Q"
                }
                '?' -> {
                    ret += "\\E.\\Q"
                }
                '#' -> {
                    ret += "\\E[0-9]\\Q"
                }
                else -> {
                    ret += c
                }
            }
        }
        ret += "\\E"
        // Remove empty regex blocks which are bad Ex: "[0-9]\Q\E[0-9]"
        return ret.replace("\\Q\\E", "").toRegex()
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || this::class.java != other::class.java) {
            return false;
        }
        return this.baseString == (other as WildcardString).baseString
    }

    override fun hashCode(): Int {
        return this.baseString.hashCode()
    }

    /**
     * Indicates whether the wildcard pattern matches the entire input
     * @param input the string to match the pattern against
     * @return true if pattern matches input, false otherwise
     */
    fun matches(input: String): Boolean {
        // Small optimization
        return if (this.isWildcard) {
            regex.matches(input)
        } else {
            this.baseString == input
        }
    }
}
