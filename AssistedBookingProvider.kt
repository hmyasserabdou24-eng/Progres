package dz.mesrs.webetu.automeals.domain.provider.assisted

import android.content.Context
import dz.mesrs.webetu.automeals.core.notifications.MealNotificationManager
import dz.mesrs.webetu.automeals.data.model.BookingStatus
import dz.mesrs.webetu.automeals.data.model.ExecutionMethod
import dz.mesrs.webetu.automeals.data.model.MealType
import dz.mesrs.webetu.automeals.domain.provider.BookingConfirmation
import dz.mesrs.webetu.automeals.domain.provider.BookingRequest
import dz.mesrs.webetu.automeals.domain.provider.MealBookingProvider
import dz.mesrs.webetu.automeals.domain.provider.ProviderResult

class AssistedBookingProvider(
    private val context: Context,
    private val notificationManager: MealNotificationManager
) : MealBookingProvider {
    override val executionMethod: ExecutionMethod = ExecutionMethod.ASSISTED_MANUAL

    override suspend fun checkConnection(): Boolean = true
    override suspend fun checkSessionValidity(): ProviderResult<Boolean> = ProviderResult.Success(true)

    override suspend fun bookMeal(request: BookingRequest): ProviderResult<BookingConfirmation> {
        notificationManager.notifyUserInterventionRequired(request.mealType.displayName, request.targetMealDate)
        return ProviderResult.Failure(
            message = "تم توجيهك إلى منصة Webetu لإتمام الحجز رسميًا.",
            requiresUserIntervention = true
        )
    }

    override suspend fun checkBookingStatus(studentLocalId: String, mealDate: String, mealType: MealType, restaurantId: String): ProviderResult<BookingStatus> {
        return ProviderResult.Success(BookingStatus.NEEDS_INTERVENTION)
    }
}
