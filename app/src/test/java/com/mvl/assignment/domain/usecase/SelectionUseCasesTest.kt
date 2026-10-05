package com.mvl.assignment.domain.usecase

import com.mvl.assignment.domain.FakeSelectionRepository
import com.mvl.assignment.domain.NicknamePolicy
import com.mvl.assignment.domain.model.EmptySlotException
import com.mvl.assignment.domain.model.NicknameTooLongException
import com.mvl.assignment.domain.model.Selection
import com.mvl.assignment.domain.model.SelectionCompleteException
import com.mvl.assignment.domain.model.SelectionStep
import com.mvl.assignment.domain.model.Slot
import com.mvl.assignment.domain.place
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SelectionUseCasesTest {

    private val repository = FakeSelectionRepository()
    private val selectPlace = SelectPlaceUseCase(repository)
    private val updateNickname = UpdateNicknameUseCase(repository, NicknamePolicy())
    private val resetSelection = ResetSelectionUseCase(repository)

    @Test
    fun `select fills A then B then refuses`() {
        val a = place("A")
        val b = place("B")

        assertEquals(SelectionStep.SET_B, selectPlace(a).getOrThrow().step)
        assertEquals(SelectionStep.BOOK, selectPlace(b).getOrThrow().step)
        assertTrue(selectPlace(place("C")).exceptionOrNull() is SelectionCompleteException)

        assertEquals(Selection(a = a, b = b), repository.selection.value)
    }

    @Test
    fun `nickname is set and cleared`() {
        selectPlace(place("Seocho District"))

        assertEquals("Home", updateNickname(Slot.A, "Home").getOrThrow().nickname)
        assertEquals("Home", repository.selection.value.a?.displayName)

        assertNull(updateNickname(Slot.A, "  ").getOrThrow().nickname)
        assertEquals("Seocho District", repository.selection.value.a?.displayName)
    }

    @Test
    fun `nickname on empty slot fails`() {
        assertTrue(updateNickname(Slot.B, "Work").exceptionOrNull() is EmptySlotException)
    }

    @Test
    fun `too long nickname fails and keeps previous value`() {
        selectPlace(place("A", nickname = null))
        updateNickname(Slot.A, "Home")

        assertTrue(updateNickname(Slot.A, "x".repeat(21)).exceptionOrNull() is NicknameTooLongException)
        assertEquals("Home", repository.selection.value.a?.nickname)
    }

    @Test
    fun `reset returns to Set A`() {
        selectPlace(place("A"))
        selectPlace(place("B"))

        resetSelection()

        assertEquals(Selection(), repository.selection.value)
        assertEquals(SelectionStep.SET_A, repository.selection.value.step)
    }
}
