package com.android.gradle.replicator.generator.util

class WildcardString(private val baseString: String) {
    private val regex: Regex by lazy { convertToRegex(baseString) }

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

    fun matches(str: String): Boolean {
        // Small optimization
        return if (this.isWildcard) {
            regex.matches(str)
        } else {
            this.baseString == str
        }
    }
}
