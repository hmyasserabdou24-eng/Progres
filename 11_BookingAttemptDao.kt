package dz.mesrs.webetu.automeals.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import dz.mesrs.webetu.automeals.data.local.entity.BookingAttemptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookingAttemptDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(attempt: BookingAttemptEntity): Long

    @Update
    suspend fun update(attempt: BookingAttemptEntity)

    @Query("SELECT * FROM booking_attempts WHERE studentLocalId = :studentLocalId AND mealDate = :mealDate AND mealType = :mealType AND restaurantId = :restaurantId LIMIT 1")
    suspend fun findExistingAttempt(studentLocalId: String, mealDate: String, mealType: String, restaurantId: String): BookingAttemptEntity?

    @Query("SELECT * FROM booking_attempts ORDER BY attemptTimestamp DESC")
    fun getAllAttempts(): Flow<List<BookingAttemptEntity>>

    @Query("SELECT * FROM booking_attempts WHERE mealDate = :date ORDER BY attemptTimestamp DESC")
    fun getAttemptsForDate(date: String): Flow<List<BookingAttemptEntity>>

    @Query("SELECT * FROM booking_attempts ORDER BY attemptTimestamp DESC LIMIT 1")
    fun getLatestAttempt(): Flow<BookingAttemptEntity?>

    @Query("SELECT COUNT(*) FROM booking_attempts WHERE mealDate = :date AND status = 'CONFIRMED'")
    fun getConfirmedCountForDate(date: String): Flow<Int>

    @Query("DELETE FROM booking_attempts")
    suspend fun clearAll()
}
