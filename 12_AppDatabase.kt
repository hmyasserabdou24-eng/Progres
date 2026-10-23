package dz.mesrs.webetu.automeals.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import dz.mesrs.webetu.automeals.data.local.dao.BookingAttemptDao
import dz.mesrs.webetu.automeals.data.local.entity.BookingAttemptEntity

@Database(entities = [BookingAttemptEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookingAttemptDao(): BookingAttemptDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val inst = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "webetu_auto_meals.db")
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = inst
                inst
            }
        }
    }
}
