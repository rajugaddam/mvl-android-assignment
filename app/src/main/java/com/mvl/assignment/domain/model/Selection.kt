package com.mvl.assignment.domain.model

enum class Slot { A, B }

/** What the main (V) button on the map screen does next. */
enum class SelectionStep { SET_A, SET_B, BOOK }

/** The trip being assembled on the map screen. */
data class Selection(
    val a: Place? = null,
    val b: Place? = null,
) {
    val step: SelectionStep
        get() = when {
            a == null -> SelectionStep.SET_A
            b == null -> SelectionStep.SET_B
            else -> SelectionStep.BOOK
        }

    /** Slot that the next "Set" action fills, or `null` once both are set. */
    val nextSlot: Slot?
        get() = when (step) {
            SelectionStep.SET_A -> Slot.A
            SelectionStep.SET_B -> Slot.B
            SelectionStep.BOOK -> null
        }

    operator fun get(slot: Slot): Place? = when (slot) {
        Slot.A -> a
        Slot.B -> b
    }

    fun with(slot: Slot, place: Place?): Selection = when (slot) {
        Slot.A -> copy(a = place)
        Slot.B -> copy(b = place)
    }
}
