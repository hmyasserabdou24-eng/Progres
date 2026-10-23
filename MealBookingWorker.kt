package dz.mesrs.webetu.automeals.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dz.mesrs.webetu.automeals.WebetuAutoMealsApp
import dz.mesrs.webetu.automeals.core.util.AppResult
import dz.mesrs.webetu.automeals.data.model.MealType

class MealBookingWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    companion object {
        const val KEY_STUDENT_ID = "student_id"
        const val KEY_TARGET_DATE = "target_date"
        const val KEY_MEAL_TYPE = "meal_type"
        const val KEY_RESIDENCE_ID = "residence_id"
        const val KEY_RESTAURANT_ID = "restaurant_id"
        const val KEY_RESTAURANT_NAME = "restaurant_name"
    }

    override suspend fun doWork(): Result {
        val studentId = inputData.getString(KEY_STUDENT_ID) ?: return Result.failure()
        val targetDate = inputData.getString(KEY_TARGET_DATE) ?: return Result.failure()
        val mealTypeName = inputData.getString(KEY_MEAL_TYPE) ?: return Result.failure()
        val resId = inputData.getString(KEY_RESIDENCE_ID) ?: ""
        val restoId = inputData.getString(KEY_RESTAURANT_ID) ?: ""
        val restoName = inputData.getString(KEY_RESTAURANT_NAME) ?: ""

        val mealType = try { MealType.valueOf(mealTypeName) } catch (_: Exception) { return Result.failure() }

        val app = applicationContext as WebetuAutoMealsApp
        val res = app.triggerMealBookingUseCase(studentId, targetDate, mealType, resId, restoId, restoName)

        return when (res) {
            is AppResult.Success -> Result.success()
            is AppResult.Error -> if (runAttemptCount < 3 && res.message.contains("انقطاع")) Result.retry() else Result.failure()
            is AppResult.Loading -> Result.retry()
        }
    }
}
