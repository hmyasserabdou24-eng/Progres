package dz.mesrs.webetu.automeals.domain.repository

import dz.mesrs.webetu.automeals.data.local.datastore.SchedulePreferencesDataStore
import dz.mesrs.webetu.automeals.data.model.ScheduleSettings
import kotlinx.coroutines.flow.Flow

interface ScheduleSettingsRepository {
    val settingsFlow: Flow<ScheduleSettings>
    suspend fun saveSettings(settings: ScheduleSettings)
    suspend fun setScheduleActive(active: Boolean)
    suspend fun clearSettings()
}

class ScheduleSettingsRepositoryImpl(private val dataStore: SchedulePreferencesDataStore) : ScheduleSettingsRepository {
    override val settingsFlow: Flow<ScheduleSettings> = dataStore.scheduleSettingsFlow
    override suspend fun saveSettings(settings: ScheduleSettings) { dataStore.saveScheduleSettings(settings) }
    override suspend fun setScheduleActive(active: Boolean) { dataStore.setScheduleActive(active) }
    override suspend fun clearSettings() { dataStore.clearSettings() }
}
