package com.mvl.assignment.domain

import com.mvl.assignment.domain.model.Selection
import com.mvl.assignment.domain.model.SelectionStep
import com.mvl.assignment.domain.model.Slot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SelectionTest {

    @Test
    fun `empty selection starts at Set A`() {
        val selection = Selection()
        assertEquals(SelectionStep.SET_A, selection.step)
        assertEquals(Slot.A, selection.nextSlot)
    }

    @Test
    fun `A set moves to Set B`() {
        val selection = Selection(a = place("A"))
        assertEquals(SelectionStep.SET_B, selection.step)
        assertEquals(Slot.B, selection.nextSlot)
    }

    @Test
    fun `A and B set moves to Book`() {
        val selection = Selection(a = place("A"), b = place("B"))
        assertEquals(SelectionStep.BOOK, selection.step)
        assertNull(selection.nextSlot)
    }

    @Test
    fun `display name prefers nickname`() {
        assertEquals("Home", place("Seocho District, Yangjae 2(i)-dong", nickname = "Home").displayName)
        assertEquals("Seocho District", place("Seocho District").displayName)
    }
}
