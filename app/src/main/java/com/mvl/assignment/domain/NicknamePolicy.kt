package com.mvl.assignment.domain

import com.mvl.assignment.domain.model.NicknameTooLongException
import javax.inject.Inject

/** Nickname rules: at most [MAX_LENGTH] characters (code points, so emoji count once); blank clears it. */
class NicknamePolicy @Inject constructor() {

    fun length(input: String): Int = input.codePointCount(0, input.length)

    /** Hard limit for the text field: truncates without splitting surrogate pairs. */
    fun limit(input: String): String =
        if (length(input) <= MAX_LENGTH) input
        else input.substring(0, input.offsetByCodePoints(0, MAX_LENGTH))

    /** @return the nickname to store, `null` to clear it, or a failure when too long. */
    fun normalize(input: String): Result<String?> {
        val trimmed = input.trim()
        return when {
            trimmed.isEmpty() -> Result.success(null)
            length(trimmed) > MAX_LENGTH -> Result.failure(NicknameTooLongException(MAX_LENGTH))
            else -> Result.success(trimmed)
        }
    }

    companion object {
        const val MAX_LENGTH = 20
    }
}
