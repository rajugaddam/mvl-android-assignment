package com.mvl.assignment.domain.usecase

import com.mvl.assignment.domain.NicknamePolicy
import com.mvl.assignment.domain.model.EmptySlotException
import com.mvl.assignment.domain.model.Place
import com.mvl.assignment.domain.model.Slot
import com.mvl.assignment.domain.repository.SelectionRepository
import javax.inject.Inject

/** Sets (or clears, when blank) the nickname of the place in [Slot]. */
class UpdateNicknameUseCase @Inject constructor(
    private val selectionRepository: SelectionRepository,
    private val nicknamePolicy: NicknamePolicy,
) {
    operator fun invoke(slot: Slot, input: String): Result<Place> {
        val nickname = nicknamePolicy.normalize(input).getOrElse { return Result.failure(it) }
        var result: Result<Place>? = null
        // The transform may be retried on concurrent updates, so result is set on every attempt.
        selectionRepository.update { current ->
            val place = current[slot]
            if (place == null) {
                result = Result.failure(EmptySlotException(slot))
                current
            } else {
                val updated = place.copy(nickname = nickname)
                result = Result.success(updated)
                current.with(slot, updated)
            }
        }
        return checkNotNull(result)
    }
}

/** Applies the nickname length limit while the user types (the field never exceeds the maximum). */
class LimitNicknameInputUseCase @Inject constructor(
    private val nicknamePolicy: NicknamePolicy,
) {
    operator fun invoke(input: String): String = nicknamePolicy.limit(input)
}
