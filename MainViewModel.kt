package dz.mesrs.webetu.automeals.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dz.mesrs.webetu.automeals.WebetuAutoMealsApp
import dz.mesrs.webetu.automeals.core.security.KeystoreSecretManager
import dz.mesrs.webetu.automeals.core.util.AppResult
import dz.mesrs.webetu.automeals.data.local.entity.BookingAttemptEntity
import dz.mesrs.webetu.automeals.data.model.ScheduleSettings
import dz.mesrs.webetu.automeals.domain.usecase.DashboardState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(private val app: WebetuAutoMealsApp) : ViewModel() {
    val dashboard: StateFlow<DashboardState> = app.getDashboardDataUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardState())

    val history: StateFlow<List<BookingAttemptEntity>> = app.bookingRepository.getAllAttempts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun showMessage(text: String) { _message.value = text }
    fun consumeMessage() { _message.value = null }

    suspend fun loadSettings(): ScheduleSettings = app.settingsRepository.settingsFlow.first()

    fun saveSettings(settings: ScheduleSettings) {
        viewModelScope.launch {
            when (val result = app.saveScheduleSettingsUseCase(settings)) {
                is AppResult.Success -> showMessage("تم حفظ الإعداد وجدولة التنبيهات")
                is AppResult.Error -> showMessage(result.message)
                AppResult.Loading -> Unit
            }
        }
    }

    fun resumeSchedule() {
        viewModelScope.launch {
            val settings = app.settingsRepository.settingsFlow.first()
            if (!settings.isValid) {
                showMessage("أكمل إعداد الجدولة أولًا")
                return@launch
            }
            saveSettings(settings.copy(isScheduleActive = true))
        }
    }

    fun emergencyStop() {
        viewModelScope.launch {
            app.emergencyStopUseCase()
            showMessage("تم إيقاف الجدولة")
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            app.clearHistoryUseCase()
            showMessage("تم مسح السجل")
        }
    }

    fun testKeystore(): String = try {
        val manager = KeystoreSecretManager()
        if (manager.decrypt(manager.encrypt("test")) == "test") "نجح" else "فشل"
    } catch (e: Exception) {
        "خطأ: ${e.javaClass.simpleName}"
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as WebetuAutoMealsApp
                MainViewModel(app)
            }
        }
    }
}
