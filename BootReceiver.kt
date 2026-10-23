package dz.mesrs.webetu.automeals.work

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dz.mesrs.webetu.automeals.WebetuAutoMealsApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val app = context.applicationContext as WebetuAutoMealsApp
            CoroutineScope(Dispatchers.IO).launch {
                val settings = app.settingsRepository.settingsFlow.first()
                if (settings.isScheduleActive && settings.isValid) {
                    app.schedulingManager.syncSchedule(settings)
                }
            }
        }
    }
}
