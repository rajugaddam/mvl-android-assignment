package com.mvl.assignment.domain

import com.mvl.assignment.domain.model.NicknameTooLongException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NicknamePolicyTest {

    private val policy = NicknamePolicy()

    @Test
    fun `blank input clears the nickname`() {
        assertNull(policy.normalize("   ").getOrThrow())
        assertNull(policy.normalize("").getOrThrow())
    }

    @Test
    fun `input is trimmed`() {
        assertEquals("Home", policy.normalize("  Home ").getOrThrow())
    }

    @Test
    fun `exactly 20 characters is accepted`() {
        val nickname = "a".repeat(20)
        assertEquals(nickname, policy.normalize(nickname).getOrThrow())
    }

    @Test
    fun `21 characters is rejected`() {
        val error = policy.normalize("a".repeat(21)).exceptionOrNull()
        assertTrue(error is NicknameTooLongException)
    }

    @Test
    fun `emoji count as one character`() {
        val nickname = "😀".repeat(20) // 20 emoji, 40 UTF-16 chars
        assertEquals(20, policy.length(nickname))
        assertEquals(nickname, policy.normalize(nickname).getOrThrow())
    }

    @Test
    fun `limit truncates without splitting surrogate pairs`() {
        val input = "😀".repeat(25)
        val limited = policy.limit(input)
        assertEquals(20, policy.length(limited))
        assertEquals("😀".repeat(20), limited)
    }

    @Test
    fun `limit keeps short input unchanged`() {
        assertEquals("Office", policy.limit("Office"))
    }
}
