package dz.mesrs.webetu.automeals

import dz.mesrs.webetu.automeals.core.security.SecurityAudit
import dz.mesrs.webetu.automeals.core.util.TimeUtils
import dz.mesrs.webetu.automeals.data.model.BookingStatus
import dz.mesrs.webetu.automeals.data.model.DayOfWeekOption
import dz.mesrs.webetu.automeals.data.model.MealType
import dz.mesrs.webetu.automeals.data.model.ScheduleSettings
import dz.mesrs.webetu.automeals.domain.provider.BookingRequest
import dz.mesrs.webetu.automeals.domain.provider.ProviderResult
import dz.mesrs.webetu.automeals.domain.provider.mock.MockMealBookingProvider
import dz.mesrs.webetu.automeals.domain.provider.official.OfficialIntegrationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AllTests {
    @Test
    fun test01_singleMeal() {
        val s = ScheduleSettings(isScheduleActive = true, selectedResidenceId = "res1", selectedMeals = setOf(MealType.LUNCH), selectedDays = setOf(DayOfWeekOption.SUNDAY))
        assertTrue(s.isValid)
        assertEquals(1, s.selectedMeals.size)
    }

    @Test
    fun test02_threeMeals() {
        val s = ScheduleSettings(isScheduleActive = true, selectedResidenceId = "res1", selectedMeals = setOf(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER), selectedDays = setOf(DayOfWeekOption.SUNDAY))
        assertTrue(s.isValid)
        assertEquals(3, s.selectedMeals.size)
    }

    @Test
    fun test03_allDays() {
        val s = ScheduleSettings(isScheduleActive = true, selectedResidenceId = "res1", selectedMeals = setOf(MealType.LUNCH), selectedDays = DayOfWeekOption.entries.toSet())
        assertTrue(s.isValid)
        assertEquals(7, s.selectedDays.size)
    }

    @Test
    fun test05_networkLoss() = runBlocking {
        val mock = MockMealBookingProvider(simulatedConnectionAvailable = false)
        val res = mock.bookMeal(BookingRequest("1", "2026-10-24", MealType.LUNCH, "res1", "r1", "central"))
        assertTrue(res is ProviderResult.Failure)
        assertTrue((res as ProviderResult.Failure).isRetryable)
    }

    @Test
    fun test06_sessionExpiry() = runBlocking {
        val mock = MockMealBookingProvider(simulatedSessionValid = false)
        val res = mock.checkSessionValidity()
        assertTrue(res is ProviderResult.Failure)
        assertTrue((res as ProviderResult.Failure).isSessionExpired)
    }

    @Test
    fun test14_logSanitization() {
        val raw = "User login with password=MySecretPass123! and token=abcde"
        val clean = SecurityAudit.sanitize(raw)
        assertFalse(clean.contains("MySecretPass123!"))
        assertTrue(clean.contains("[REDACTED]"))
    }
}
