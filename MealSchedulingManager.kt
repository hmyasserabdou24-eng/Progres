package dz.mesrs.webetu.automeals.domain.scheduler

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dz.mesrs.webetu.automeals.core.util.TimeUtils
import dz.mesrs.webetu.automeals.data.model.MealType
import dz.mesrs.webetu.automeals.data.model.ScheduleSettings
import dz.mesrs.webetu.automeals.work.MealBookingWorker
import java.time.Duration
import java.util.concurrent.TimeUnit

class MealSchedulingManager(private val context: Context) {
    private val workManager: WorkManager by lazy { WorkManager.getInstance(context) }

    fun syncSchedule(settings: ScheduleSettings) {
        if (!settings.isScheduleActive || !settings.isValid) {
            cancelAllWork()
            return
        }

        val now = TimeUtils.nowInAlgiers()
        val today = now.toLocalDate()
        val activeDays = settings.selectedDays.map { it.dayOfWeek }.toSet()

        for (offset in 0..7) {
            val candidateDate = today.plusDays(offset.toLong())
            if (activeDays.contains(candidateDate.dayOfWeek)) {
                val targetDateStr = TimeUtils.formatIsoDate(candidateDate)
                val attemptDateTime = candidateDate.minusDays(1)
                    .atTime(settings.attemptHour, settings.attemptMinute)
                    .atZone(TimeUtils.ALGIERS_ZONE_ID)

                if (attemptDateTime.isBefore(now)) continue
                val delayMillis = Duration.between(now, attemptDateTime).toMillis()

                for (meal in settings.selectedMeals) {
                    val workName = "meal_${targetDateStr}_${meal.name}_${settings.selectedRestaurantId}"
                    val inputData = Data.Builder()
                        .putString(MealBookingWorker.KEY_STUDENT_ID, settings.studentLocalId)
                        .putString(MealBookingWorker.KEY_TARGET_DATE, targetDateStr)
                        .putString(MealBookingWorker.KEY_MEAL_TYPE, meal.name)
                        .putString(MealBookingWorker.KEY_RESIDENCE_ID, settings.selectedResidenceId)
                        .putString(MealBookingWorker.KEY_RESTAURANT_ID, settings.selectedRestaurantId)
                        .putString(MealBookingWorker.KEY_RESTAURANT_NAME, settings.selectedRestaurantName)
                        .build()

                    val req = OneTimeWorkRequestBuilder<MealBookingWorker>()
                        .setInputData(inputData)
                        .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                        .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                        .addTag("webetu_meal")
                        .build()

                    workManager.enqueueUniqueWork(workName, ExistingWorkPolicy.KEEP, req)
                }
            }
        }
    }

    fun cancelAllWork() {
        workManager.cancelAllWorkByTag("webetu_meal")
    }
}
