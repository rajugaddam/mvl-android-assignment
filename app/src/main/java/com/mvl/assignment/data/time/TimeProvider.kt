package com.mvl.assignment.data.time

import com.mvl.assignment.domain.model.BookingMonth
import com.mvl.assignment.domain.repository.CurrentMonthProvider
import java.util.Calendar
import javax.inject.Inject

/** Wall-clock source, injected so time-dependent code is testable. */
interface TimeProvider {
    fun nowMillis(): Long
}

class SystemTimeProvider @Inject constructor() : TimeProvider {
    override fun nowMillis(): Long = System.currentTimeMillis()
}

class CurrentMonthProviderImpl @Inject constructor(
    private val timeProvider: TimeProvider,
) : CurrentMonthProvider {
    override fun currentMonth(): BookingMonth = bookingMonthOf(timeProvider.nowMillis())
}

/** Calendar month (device time zone) that [epochMillis] falls in. */
fun bookingMonthOf(epochMillis: Long): BookingMonth {
    val calendar = Calendar.getInstance().apply { timeInMillis = epochMillis }
    return BookingMonth(
        year = calendar.get(Calendar.YEAR),
        month = calendar.get(Calendar.MONTH) + 1,
    )
}
