package dz.mesrs.webetu.automeals.domain.repository

import dz.mesrs.webetu.automeals.data.local.dao.BookingAttemptDao
import dz.mesrs.webetu.automeals.data.local.entity.BookingAttemptEntity
import dz.mesrs.webetu.automeals.data.model.BookingStatus
import dz.mesrs.webetu.automeals.data.model.ExecutionMethod
import dz.mesrs.webetu.automeals.data.model.MealType
import kotlinx.coroutines.flow.Flow

interface BookingRepository {
    fun getAllAttempts(): Flow<List<BookingAttemptEntity>>
    fun getAttemptsForDate(date: String): Flow<List<BookingAttemptEntity>>
    fun getLatestAttempt(): Flow<BookingAttemptEntity?>
    fun getConfirmedCountForDate(date: String): Flow<Int>
    suspend fun findExistingAttempt(studentLocalId: String, mealDate: String, mealType: MealType, restaurantId: String): BookingAttemptEntity?
    suspend fun recordAttempt(
        studentLocalId: String,
        mealDate: String,
        mealType: MealType,
        restaurantId: String,
        restaurantName: String,
        status: BookingStatus,
        executionMethod: ExecutionMethod,
        message: String,
        confirmationReference: String? = null,
        verificationProof: String? = null
    ): Long
    suspend fun clearAllHistory()
}

class BookingRepositoryImpl(private val dao: BookingAttemptDao) : BookingRepository {
    override fun getAllAttempts(): Flow<List<BookingAttemptEntity>> = dao.getAllAttempts()
    override fun getAttemptsForDate(date: String): Flow<List<BookingAttemptEntity>> = dao.getAttemptsForDate(date)
    override fun getLatestAttempt(): Flow<BookingAttemptEntity?> = dao.getLatestAttempt()
    override fun getConfirmedCountForDate(date: String): Flow<Int> = dao.getConfirmedCountForDate(date)

    override suspend fun findExistingAttempt(studentLocalId: String, mealDate: String, mealType: MealType, restaurantId: String): BookingAttemptEntity? {
        return dao.findExistingAttempt(studentLocalId, mealDate, mealType.name, restaurantId)
    }

    override suspend fun recordAttempt(
        studentLocalId: String,
        mealDate: String,
        mealType: MealType,
        restaurantId: String,
        restaurantName: String,
        status: BookingStatus,
        executionMethod: ExecutionMethod,
        message: String,
        confirmationReference: String?,
        verificationProof: String?
    ): Long {
        val existing = dao.findExistingAttempt(studentLocalId, mealDate, mealType.name, restaurantId)
        val entity = BookingAttemptEntity(
            id = existing?.id ?: 0,
            studentLocalId = studentLocalId,
            mealDate = mealDate,
            mealType = mealType.name,
            restaurantId = restaurantId,
            restaurantName = restaurantName,
            attemptTimestamp = System.currentTimeMillis(),
            status = status.name,
            executionMethod = executionMethod.name,
            message = message,
            confirmationReference = confirmationReference,
            rawVerificationProof = verificationProof
        )
        return dao.insertOrUpdate(entity)
    }

    override suspend fun clearAllHistory() { dao.clearAll() }
}
