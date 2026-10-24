package dz.mesrs.webetu.automeals.core.util

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object TimeUtils {
    val ALGIERS_ZONE_ID: ZoneId = ZoneId.of("Africa/Algiers")
    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
    private val DISPLAY_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ENGLISH)

    fun nowInAlgiers(): ZonedDateTime = ZonedDateTime.now(ALGIERS_ZONE_ID)
    fun todayInAlgiers(): LocalDate = nowInAlgiers().toLocalDate()
    fun formatIsoDate(date: LocalDate): String = date.format(DATE_FORMATTER)

    fun formatDateTimeForDisplay(epochMillis: Long): String {
        val zdt = Instant.ofEpochMilli(epochMillis).atZone(ALGIERS_ZONE_ID)
        return zdt.format(DISPLAY_DATETIME_FORMATTER)
    }

    fun calculateNextAttempt(
        attemptHour: Int,
        attemptMinute: Int,
        selectedDaysOfWeek: Set<DayOfWeek>,
        startDateEpochDay: Long,
        endDateEpochDay: Long?
    ): ZonedDateTime? {
        if (selectedDaysOfWeek.isEmpty()) return null
        val now = nowInAlgiers()
        val today = now.toLocalDate()
        val startDate = LocalDate.ofEpochDay(startDateEpochDay)
        val endDate = endDateEpochDay?.let { LocalDate.ofEpochDay(it) }

        val candidateDate = if (today.isBefore(startDate)) startDate else today

        for (offset in 0..14) {
            val checkDate = candidateDate.plusDays(offset.toLong())
            if (endDate != null && checkDate.isAfter(endDate)) return null
            if (selectedDaysOfWeek.contains(checkDate.dayOfWeek)) {
                val candidateDateTime = checkDate.atTime(attemptHour, attemptMinute).atZone(ALGIERS_ZONE_ID)
                if (candidateDateTime.isAfter(now)) return candidateDateTime
            }
        }
        return null
    }
}
