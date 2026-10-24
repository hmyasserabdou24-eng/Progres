package dz.mesrs.webetu.automeals.domain.provider.mock

import dz.mesrs.webetu.automeals.data.model.BookingStatus
import dz.mesrs.webetu.automeals.data.model.ExecutionMethod
import dz.mesrs.webetu.automeals.data.model.MealType
import dz.mesrs.webetu.automeals.domain.provider.BookingConfirmation
import dz.mesrs.webetu.automeals.domain.provider.BookingRequest
import dz.mesrs.webetu.automeals.domain.provider.MealBookingProvider
import dz.mesrs.webetu.automeals.domain.provider.ProviderResult

// Test-only provider. It is never wired into the app (see WebetuAutoMealsApp).
class MockMealBookingProvider(
    var simulatedConnectionAvailable: Boolean = true,
    var simulatedSessionValid: Boolean = true,
    var shouldSimulateTimeout: Boolean = false,
    var shouldReturnConfirmedProof: Boolean = true
) : MealBookingProvider {
    override val executionMethod: ExecutionMethod = ExecutionMethod.MOCK_SIMULATION
    override suspend fun checkConnection(): Boolean = simulatedConnectionAvailable

    override suspend fun checkSessionValidity(): ProviderResult<Boolean> {
        return if (simulatedSessionValid) ProviderResult.Success(true)
        else ProviderResult.Failure("انتهت الجلسة", isSessionExpired = true)
    }

    override suspend fun bookMeal(request: BookingRequest): ProviderResult<BookingConfirmation> {
        if (!simulatedConnectionAvailable) return ProviderResult.Failure("انقطاع الاتصال بالإنترنت", isRetryable = true)
        if (shouldSimulateTimeout) return ProviderResult.Failure("انتهاء مهلة الاتصال", isRetryable = true)
        if (!shouldReturnConfirmedProof) return ProviderResult.Failure("لم يصدر إشعار تأكيد رسمي", requiresUserIntervention = true)

        return ProviderResult.Success(
            BookingConfirmation(
                confirmationReference = "MOCK-REF-2026",
                verificationProof = "VALID-HASH",
                timestamp = System.currentTimeMillis(),
                status = BookingStatus.CONFIRMED,
                executionMethod = executionMethod
            )
        )
    }

    override suspend fun checkBookingStatus(studentLocalId: String, mealDate: String, mealType: MealType, restaurantId: String): ProviderResult<BookingStatus> {
        return if (shouldReturnConfirmedProof) ProviderResult.Success(BookingStatus.CONFIRMED)
        else ProviderResult.Success(BookingStatus.UNCONFIRMED)
    }
}
