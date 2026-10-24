package dz.mesrs.webetu.automeals

import dz.mesrs.webetu.automeals.core.security.SecurityAudit
import dz.mesrs.webetu.automeals.data.model.*
import dz.mesrs.webetu.automeals.domain.provider.*
import dz.mesrs.webetu.automeals.domain.provider.mock.MockMealBookingProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class AllTests {
    @Test
    fun test01_singleMeal() {
        val s = ScheduleSettings(isScheduleActive = true, selectedResidenceId = "res1", selectedMeals = setOf(MealType.LUNCH), selectedDays = setOf(DayOfWeekOption.SUNDAY))
        assertTrue(s.isValid)
    }

    @Test
    fun test05_networkLoss() = runBlocking {
        val mock = MockMealBookingProvider(simulatedConnectionAvailable = false)
        val res = mock.bookMeal(BookingRequest("1", "2026-10-24", MealType.LUNCH, "res1", "r1", "central"))
        assertTrue(res is ProviderResult.Failure)
    }

    @Test
    fun test14_logSanitization() {
        val raw = "User login password=MySecretPass123!"
        val clean = SecurityAudit.sanitize(raw)
        assertFalse(clean.contains("MySecretPass123!"))
    }
}
