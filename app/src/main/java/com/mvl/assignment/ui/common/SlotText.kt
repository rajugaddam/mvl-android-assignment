package com.mvl.assignment.ui.common

import androidx.annotation.StringRes
import com.mvl.assignment.R
import com.mvl.assignment.domain.model.SelectionStep
import com.mvl.assignment.domain.model.Slot

@StringRes
fun Slot.labelRes(): Int = when (this) {
    Slot.A -> R.string.slot_a
    Slot.B -> R.string.slot_b
}

/** Text of the map screen's main button, derived from the selection step. */
@StringRes
fun SelectionStep.actionRes(): Int = when (this) {
    SelectionStep.SET_A -> R.string.action_set_a
    SelectionStep.SET_B -> R.string.action_set_b
    SelectionStep.BOOK -> R.string.action_book
}
