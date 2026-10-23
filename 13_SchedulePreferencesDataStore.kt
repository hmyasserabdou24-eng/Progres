package dz.mesrs.webetu.automeals.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dz.mesrs.webetu.automeals.data.model.DayOfWeekOption
import dz.mesrs.webetu.automeals.data.model.MealType
import dz.mesrs.webetu.automeals.data.model.ScheduleSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.scheduleDataStore by preferencesDataStore(name = "schedule_preferences")

class SchedulePreferencesDataStore(private val context: Context) {
    private object Keys {
        val ACTIVE = booleanPreferencesKey("is_schedule_active")
        val RES_ID = stringPreferencesKey("selected_residence_id")
        val RES_NAME = stringPreferencesKey("selected_residence_name")
        val RESTO_ID = stringPreferencesKey("selected_restaurant_id")
        val RESTO_NAME = stringPreferencesKey("selected_restaurant_name")
        val MEALS = stringSetPreferencesKey("selected_meals")
        val DAYS = stringSetPreferencesKey("selected_days")
        val HOUR = intPreferencesKey("attempt_hour")
        val MINUTE = intPreferencesKey("attempt_minute")
    }

    val scheduleSettingsFlow: Flow<ScheduleSettings> = context.scheduleDataStore.data.map { pref ->
        ScheduleSettings(
            isScheduleActive = pref[Keys.ACTIVE] ?: false,
            selectedResidenceId = pref[Keys.RES_ID] ?: "",
            selectedResidenceName = pref[Keys.RES_NAME] ?: "",
            selectedRestaurantId = pref[Keys.RESTO_ID] ?: "",
            selectedRestaurantName = pref[Keys.RESTO_NAME] ?: "",
            selectedMeals = pref[Keys.MEALS]?.mapNotNull { try { MealType.valueOf(it) } catch (_: Exception) { null } }?.toSet() ?: emptySet(),
            selectedDays = pref[Keys.DAYS]?.mapNotNull { try { DayOfWeekOption.valueOf(it) } catch (_: Exception) { null } }?.toSet() ?: emptySet(),
            attemptHour = pref[Keys.HOUR] ?: 19,
            attemptMinute = pref[Keys.MINUTE] ?: 0
        )
    }

    suspend fun saveScheduleSettings(settings: ScheduleSettings) {
        context.scheduleDataStore.edit { pref ->
            pref[Keys.ACTIVE] = settings.isScheduleActive
            pref[Keys.RES_ID] = settings.selectedResidenceId
            pref[Keys.RES_NAME] = settings.selectedResidenceName
            pref[Keys.RESTO_ID] = settings.selectedRestaurantId
            pref[Keys.RESTO_NAME] = settings.selectedRestaurantName
            pref[Keys.MEALS] = settings.selectedMeals.map { it.name }.toSet()
            pref[Keys.DAYS] = settings.selectedDays.map { it.name }.toSet()
            pref[Keys.HOUR] = settings.attemptHour
            pref[Keys.MINUTE] = settings.attemptMinute
        }
    }

    suspend fun setScheduleActive(active: Boolean) {
        context.scheduleDataStore.edit { it[Keys.ACTIVE] = active }
    }

    suspend fun clearSettings() {
        context.scheduleDataStore.edit { it.clear() }
    }
}
