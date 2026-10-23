package dz.mesrs.webetu.automeals.domain.provider

import dz.mesrs.webetu.automeals.data.model.BookingStatus
import dz.mesrs.webetu.automeals.data.model.ExecutionMethod
import dz.mesrs.webetu.automeals.data.model.MealType

sealed class ProviderResult<out T> {
    data class Success<out T>(val data: T) : ProviderResult<T>()
    data class Failure(
        val message: String,
        val isRetryable: Boolean = false,
        val requiresUserIntervention: Boolean = false,
        val isSessionExpired: Boolean = false
    ) : ProviderResult<Nothing>()
}

data class BookingRequest(
    val studentLocalId: String,
    val targetMealDate: String,
    val mealType: MealType,
    val residenceId: String,
    val restaurantId: String,
    val restaurantName: String
)

data class BookingConfirmation(
    val confirmationReference: String,
    val verificationProof: String,
    val timestamp: Long,
    val status: BookingStatus,
    val executionMethod: ExecutionMethod
)

interface MealBookingProvider {
    val executionMethod: ExecutionMethod
    suspend fun checkConnection(): Boolean
    suspend fun checkSessionValidity(): ProviderResult<Boolean>
    suspend fun bookMeal(request: BookingRequest): ProviderResult<BookingConfirmation>
    suspend fun checkBookingStatus(studentLocalId: String, mealDate: String, mealType: MealType, restaurantId: String): ProviderResult<BookingStatus>
}
