package dz.mesrs.webetu.automeals.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import dz.mesrs.webetu.automeals.data.model.BookingStatus
import dz.mesrs.webetu.automeals.data.model.ExecutionMethod
import dz.mesrs.webetu.automeals.data.model.MealType

@Entity(
    tableName = "booking_attempts",
    indices = [
        Index(value = ["studentLocalId", "mealDate", "mealType", "restaurantId"], unique = true),
        Index(value = ["status"]),
        Index(value = ["attemptTimestamp"])
    ]
)
data class BookingAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentLocalId: String,
    val mealDate: String,
    val mealType: String,
    val restaurantId: String,
    val restaurantName: String,
    val attemptTimestamp: Long,
    val status: String,
    val executionMethod: String,
    val message: String,
    val confirmationReference: String? = null,
    val rawVerificationProof: String? = null
) {
    val mealTypeEnum: MealType get() = try { MealType.valueOf(mealType) } catch (_: Exception) { MealType.LUNCH }
    val statusEnum: BookingStatus get() = try { BookingStatus.valueOf(status) } catch (_: Exception) { BookingStatus.UNCONFIRMED }
    val executionMethodEnum: ExecutionMethod get() = try { ExecutionMethod.valueOf(executionMethod) } catch (_: Exception) { ExecutionMethod.ASSISTED_MANUAL }
}
