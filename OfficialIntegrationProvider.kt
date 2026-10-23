package dz.mesrs.webetu.automeals.domain.provider.official

import dz.mesrs.webetu.automeals.data.model.BookingStatus
import dz.mesrs.webetu.automeals.data.model.ExecutionMethod
import dz.mesrs.webetu.automeals.data.model.MealType
import dz.mesrs.webetu.automeals.domain.provider.BookingConfirmation
import dz.mesrs.webetu.automeals.domain.provider.BookingRequest
import dz.mesrs.webetu.automeals.domain.provider.MealBookingProvider
import dz.mesrs.webetu.automeals.domain.provider.ProviderResult

class OfficialIntegrationProvider(
    private val isOfficialApiAuthorized: Boolean = false
) : MealBookingProvider {
    override val executionMethod: ExecutionMethod = ExecutionMethod.OFFICIAL_API
    override suspend fun checkConnection(): Boolean = isOfficialApiAuthorized
    override suspend fun checkSessionValidity(): ProviderResult<Boolean> =
        ProviderResult.Failure("لا تتوفر واجهة API رسمية مصرح بها حالياً.", requiresUserIntervention = true)

    override suspend fun bookMeal(request: BookingRequest): ProviderResult<BookingConfirmation> {
        return ProviderResult.Failure("لا تتوفر واجهة API موثقة للأتمتة الخارجية. تم التحويل إلى وضع المساعدة اليدوية.", requiresUserIntervention = true)
    }

    override suspend fun checkBookingStatus(studentLocalId: String, mealDate: String, mealType: MealType, restaurantId: String): ProviderResult<BookingStatus> {
        return ProviderResult.Success(BookingStatus.UNCONFIRMED)
    }
}
