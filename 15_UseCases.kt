package dz.mesrs.webetu.automeals.domain.usecase

import dz.mesrs.webetu.automeals.core.notifications.MealNotificationManager
import dz.mesrs.webetu.automeals.core.util.AppResult
import dz.mesrs.webetu.automeals.core.util.TimeUtils
import dz.mesrs.webetu.automeals.data.local.entity.BookingAttemptEntity
import dz.mesrs.webetu.automeals.data.model.BookingStatus
import dz.mesrs.webetu.automeals.data.model.MealType
import dz.mesrs.webetu.automeals.data.model.ScheduleSettings
import dz.mesrs.webetu.automeals.domain.provider.BookingRequest
import dz.mesrs.webetu.automeals.domain.provider.MealBookingProvider
import dz.mesrs.webetu.automeals.domain.provider.ProviderResult
import dz.mesrs.webetu.automeals.domain.repository.BookingRepository
import dz.mesrs.webetu.automeals.domain.repository.ScheduleSettingsRepository
import dz.mesrs.webetu.automeals.domain.scheduler.MealSchedulingManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.ZonedDateTime

data class DashboardState(
    val settings: ScheduleSettings = ScheduleSettings(),
    val nextAttemptTime: ZonedDateTime? = null,
    val todayAttempts: List<BookingAttemptEntity> = emptyList(),
    val todayConfirmedCount: Int = 0,
    val latestOperation: BookingAttemptEntity? = null,
    val isLoading: Boolean = false
)

class GetDashboardDataUseCase(
    private val settingsRepository: ScheduleSettingsRepository,
    private val bookingRepository: BookingRepository
) {
    operator fun invoke(): Flow<DashboardState> {
        val todayStr = TimeUtils.formatIsoDate(TimeUtils.todayInAlgiers())
        return combine(
            settingsRepository.settingsFlow,
            bookingRepository.getAttemptsForDate(todayStr),
            bookingRepository.getConfirmedCountForDate(todayStr),
            bookingRepository.getLatestAttempt()
        ) { settings, todayAttempts, confirmedCount, latestOp ->
            val next = if (settings.isScheduleActive) {
                TimeUtils.calculateNextAttempt(
                    settings.attemptHour,
                    settings.attemptMinute,
                    settings.selectedDays.map { it.dayOfWeek }.toSet(),
                    settings.startDateEpochDay,
                    settings.endDateEpochDay
                )
            } else null

            DashboardState(settings, next, todayAttempts, confirmedCount, latestOp, false)
        }
    }
}

class SaveScheduleSettingsUseCase(
    private val settingsRepository: ScheduleSettingsRepository,
    private val schedulingManager: MealSchedulingManager
) {
    suspend operator fun invoke(settings: ScheduleSettings): AppResult<Unit> {
        if (settings.selectedMeals.isEmpty()) return AppResult.Error("يجب اختيار وجبة واحدة على الأقل.")
        if (settings.selectedDays.isEmpty()) return AppResult.Error("يجب اختيار يوم واحد على الأقل.")
        if (settings.selectedResidenceId.isBlank()) return AppResult.Error("يرجى تحديد الإقامة الجامعية.")

        settingsRepository.saveSettings(settings)
        schedulingManager.syncSchedule(settings)
        return AppResult.Success(Unit)
    }
}

class TriggerMealBookingUseCase(
    private val bookingRepository: BookingRepository,
    private val provider: MealBookingProvider,
    private val notificationManager: MealNotificationManager
) {
    suspend operator fun invoke(
        studentId: String,
        targetDate: String,
        mealType: MealType,
        residenceId: String,
        restaurantId: String,
        restaurantName: String
    ): AppResult<BookingStatus> {
        val existing = bookingRepository.findExistingAttempt(studentId, targetDate, mealType, restaurantId)
        if (existing != null && existing.statusEnum == BookingStatus.CONFIRMED) {
            return AppResult.Success(BookingStatus.CONFIRMED)
        }

        bookingRepository.recordAttempt(studentId, targetDate, mealType, restaurantId, restaurantName, BookingStatus.IN_PROGRESS, provider.executionMethod, "جاري الحجز...")

        val req = BookingRequest(studentId, targetDate, mealType, residenceId, restaurantId, restaurantName)
        return when (val res = provider.bookMeal(req)) {
            is ProviderResult.Success -> {
                val conf = res.data
                bookingRepository.recordAttempt(studentId, targetDate, mealType, restaurantId, restaurantName, BookingStatus.CONFIRMED, conf.executionMethod, "تم التأكيد", conf.confirmationReference, conf.verificationProof)
                notificationManager.notifyBookingConfirmed(mealType.displayName, targetDate, restaurantName)
                AppResult.Success(BookingStatus.CONFIRMED)
            }
            is ProviderResult.Failure -> {
                val st = if (res.requiresUserIntervention) BookingStatus.NEEDS_INTERVENTION else BookingStatus.FAILED
                bookingRepository.recordAttempt(studentId, targetDate, mealType, restaurantId, restaurantName, st, provider.executionMethod, res.message)
                AppResult.Error(res.message)
            }
        }
    }
}

class ClearHistoryUseCase(private val repo: BookingRepository) {
    suspend operator fun invoke() = repo.clearAllHistory()
}

class EmergencyStopUseCase(
    private val repo: ScheduleSettingsRepository,
    private val sched: MealSchedulingManager
) {
    suspend operator fun invoke() {
        sched.cancelAllWork()
        repo.setScheduleActive(false)
    }
}
