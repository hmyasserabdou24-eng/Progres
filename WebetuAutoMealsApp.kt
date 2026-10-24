package dz.mesrs.webetu.automeals

import android.app.Application
import dz.mesrs.webetu.automeals.core.notifications.MealNotificationManager
import dz.mesrs.webetu.automeals.data.local.database.AppDatabase
import dz.mesrs.webetu.automeals.data.local.datastore.SchedulePreferencesDataStore
import dz.mesrs.webetu.automeals.domain.provider.MealBookingProvider
import dz.mesrs.webetu.automeals.domain.provider.assisted.AssistedBookingProvider
import dz.mesrs.webetu.automeals.domain.repository.BookingRepository
import dz.mesrs.webetu.automeals.domain.repository.BookingRepositoryImpl
import dz.mesrs.webetu.automeals.domain.repository.ScheduleSettingsRepository
import dz.mesrs.webetu.automeals.domain.repository.ScheduleSettingsRepositoryImpl
import dz.mesrs.webetu.automeals.domain.scheduler.MealSchedulingManager
import dz.mesrs.webetu.automeals.domain.usecase.ClearHistoryUseCase
import dz.mesrs.webetu.automeals.domain.usecase.EmergencyStopUseCase
import dz.mesrs.webetu.automeals.domain.usecase.GetDashboardDataUseCase
import dz.mesrs.webetu.automeals.domain.usecase.SaveScheduleSettingsUseCase
import dz.mesrs.webetu.automeals.domain.usecase.TriggerMealBookingUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WebetuAutoMealsApp : Application() {
    lateinit var notificationManager: MealNotificationManager; private set
    lateinit var database: AppDatabase; private set
    lateinit var dataStore: SchedulePreferencesDataStore; private set
    lateinit var bookingRepository: BookingRepository; private set
    lateinit var settingsRepository: ScheduleSettingsRepository; private set
    lateinit var schedulingManager: MealSchedulingManager; private set
    lateinit var bookingProvider: MealBookingProvider; private set

    lateinit var getDashboardDataUseCase: GetDashboardDataUseCase; private set
    lateinit var saveScheduleSettingsUseCase: SaveScheduleSettingsUseCase; private set
    lateinit var triggerMealBookingUseCase: TriggerMealBookingUseCase; private set
    lateinit var clearHistoryUseCase: ClearHistoryUseCase; private set
    lateinit var emergencyStopUseCase: EmergencyStopUseCase; private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        notificationManager = MealNotificationManager(this)
        database = AppDatabase.getInstance(this)
        dataStore = SchedulePreferencesDataStore(this)
        bookingRepository = BookingRepositoryImpl(database.bookingAttemptDao())
        settingsRepository = ScheduleSettingsRepositoryImpl(dataStore)
        schedulingManager = MealSchedulingManager(this)
        bookingProvider = AssistedBookingProvider(this, notificationManager)

        getDashboardDataUseCase = GetDashboardDataUseCase(settingsRepository, bookingRepository)
        saveScheduleSettingsUseCase = SaveScheduleSettingsUseCase(settingsRepository, schedulingManager)
        triggerMealBookingUseCase = TriggerMealBookingUseCase(bookingRepository, bookingProvider, notificationManager)
        clearHistoryUseCase = ClearHistoryUseCase(bookingRepository)
        emergencyStopUseCase = EmergencyStopUseCase(settingsRepository, schedulingManager)

        // WorkManager jobs only cover the next few days, so re-arm them whenever the app starts.
        appScope.launch {
            val settings = settingsRepository.settingsFlow.first()
            if (settings.isScheduleActive && settings.isValid) {
                schedulingManager.syncSchedule(settings)
            }
        }
    }
}
