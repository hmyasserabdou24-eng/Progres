package dz.mesrs.webetu.automeals.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import dz.mesrs.webetu.automeals.ui.theme.WebetuAutoMealsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WebetuAutoMealsTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    val vm: MainViewModel = viewModel(factory = MainViewModel.Factory)
                    NotificationPermissionRequest()
                    WebetuRoot(vm)
                }
            }
        }
    }
}
