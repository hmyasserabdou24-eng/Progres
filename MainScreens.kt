package dz.mesrs.webetu.automeals.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List as ListIcon
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import dz.mesrs.webetu.automeals.core.util.TimeUtils
import dz.mesrs.webetu.automeals.data.local.entity.BookingAttemptEntity
import dz.mesrs.webetu.automeals.data.model.DayOfWeekOption
import dz.mesrs.webetu.automeals.data.model.MealType
import dz.mesrs.webetu.automeals.data.model.ScheduleSettings
import dz.mesrs.webetu.automeals.data.model.VerifiedResidenceCatalog
import java.time.format.DateTimeFormatter
import java.util.Locale

private data class TabItem(val label: String, val icon: ImageVector)

private val TABS = listOf(
    TabItem("الرئيسية", Icons.Filled.Home),
    TabItem("الإعداد", Icons.Filled.Settings),
    TabItem("السجل", ListIcon),
    TabItem("الأمان", Icons.Filled.Lock)
)

@Composable
fun NotificationPermissionRequest() {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            if (!granted) launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Composable
fun WebetuRoot(vm: MainViewModel) {
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    val message by vm.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        val text = message
        if (text != null) {
            vm.consumeMessage()
            snackbarHostState.showSnackbar(text)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                TABS.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (selectedTab) {
                0 -> DashboardScreen(vm)
                1 -> SetupScreen(vm)
                2 -> HistoryScreen(vm)
                else -> SecurityScreen(vm)
            }
        }
    }
}

@Composable
private fun DashboardScreen(vm: MainViewModel) {
    val state by vm.dashboard.collectAsState()
    val formatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.ENGLISH) }
    val s = state.settings

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("لوحة التحكم", style = MaterialTheme.typography.headlineSmall)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(if (s.isScheduleActive) "الحالة: مفعلة" else "الحالة: متوقفة", fontWeight = FontWeight.Bold)
                Text("الإقامة: " + s.selectedResidenceName.ifBlank { "غير محددة" })
                Text("الوجبات: " + s.selectedMeals.joinToString("، ") { it.arabicName }.ifBlank { "غير محددة" })
                Text("موعد المحاولة التالي: " + (state.nextAttemptTime?.format(formatter) ?: "—"))
                Text("وجبات مؤكدة اليوم: ${state.todayConfirmedCount}")
                state.latestOperation?.let { Text("آخر عملية: ${it.message}") }
            }
        }
        Button(onClick = { vm.resumeSchedule() }, modifier = Modifier.fillMaxWidth()) { Text("تفعيل الجدولة") }
        OutlinedButton(onClick = { vm.emergencyStop() }, modifier = Modifier.fillMaxWidth()) { Text("إيقاف طارئ") }
        Text(
            "ملاحظة: التطبيق لا يسجل الدخول إلى منصة Webetu ولا يحجز نيابةً عنك. في موعد المحاولة يرسل لك تنبيهًا لإتمام الحجز بنفسك.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun SetupScreen(vm: MainViewModel) {
    var initial by remember { mutableStateOf<ScheduleSettings?>(null) }
    LaunchedEffect(Unit) { initial = vm.loadSettings() }
    val loaded = initial
    if (loaded == null) {
        Text("جارٍ التحميل...", modifier = Modifier.padding(16.dp))
    } else {
        SetupForm(loaded, vm)
    }
}

@Composable
private fun SetupForm(initial: ScheduleSettings, vm: MainViewModel) {
    var residenceId by remember { mutableStateOf(initial.selectedResidenceId) }
    var meals by remember { mutableStateOf(initial.selectedMeals) }
    var days by remember { mutableStateOf(initial.selectedDays) }
    var hourText by remember { mutableStateOf(initial.attemptHour.toString()) }
    var minuteText by remember { mutableStateOf(initial.attemptMinute.toString()) }
    val hour = hourText.toIntOrNull()?.takeIf { it in 0..23 }
    val minute = minuteText.toIntOrNull()?.takeIf { it in 0..59 }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("إعداد التنبيهات", style = MaterialTheme.typography.headlineSmall)

        Text("الإقامة الجامعية", style = MaterialTheme.typography.titleMedium)
        VerifiedResidenceCatalog.RESIDENCES.forEach { residence ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = residenceId == residence.id,
                        onClick = { residenceId = residence.id },
                        role = Role.RadioButton
                    )
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = residenceId == residence.id, onClick = null)
                Spacer(Modifier.width(8.dp))
                Text(residence.nameArabic)
            }
        }

        Text("الوجبات", style = MaterialTheme.typography.titleMedium)
        MealType.entries.forEach { meal ->
            CheckRow(meal.displayName, meal in meals) { checked ->
                meals = if (checked) meals + meal else meals - meal
            }
        }

        Text("أيام الوجبات", style = MaterialTheme.typography.titleMedium)
        DayOfWeekOption.entries.forEach { day ->
            CheckRow(day.arabicName, day in days) { checked ->
                days = if (checked) days + day else days - day
            }
        }

        Text("وقت المحاولة (قبل يوم من الوجبة)", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = hourText,
                onValueChange = { hourText = it.filter { c -> c.isDigit() }.take(2) },
                label = { Text("الساعة (0-23)") },
                singleLine = true,
                isError = hour == null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = minuteText,
                onValueChange = { minuteText = it.filter { c -> c.isDigit() }.take(2) },
                label = { Text("الدقيقة (0-59)") },
                singleLine = true,
                isError = minute == null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }

        Button(
            onClick = {
                if (hour == null || minute == null) {
                    vm.showMessage("الوقت غير صالح")
                } else {
                    val residence = VerifiedResidenceCatalog.findResidence(residenceId)
                    val restaurant = residence?.restaurants?.firstOrNull()
                    vm.saveSettings(
                        initial.copy(
                            selectedResidenceId = residence?.id ?: "",
                            selectedResidenceName = residence?.nameArabic ?: "",
                            selectedRestaurantId = restaurant?.id ?: "",
                            selectedRestaurantName = restaurant?.nameArabic ?: "",
                            selectedMeals = meals,
                            selectedDays = days,
                            attemptHour = hour,
                            attemptMinute = minute
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("حفظ الإعداد") }
    }
}

@Composable
private fun CheckRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = onCheckedChange)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = null)
        Spacer(Modifier.width(8.dp))
        Text(label)
    }
}

@Composable
private fun HistoryScreen(vm: MainViewModel) {
    val attempts by vm.history.collectAsState()
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("سجل العمليات", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            TextButton(onClick = { vm.clearHistory() }) { Text("مسح السجل") }
        }
        if (attempts.isEmpty()) {
            Text("لا توجد عمليات بعد.")
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(attempts, key = { it.id }) { attempt -> AttemptCard(attempt) }
        }
    }
}

@Composable
private fun AttemptCard(attempt: BookingAttemptEntity) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("${attempt.mealTypeEnum.displayName} — ${attempt.mealDate}", fontWeight = FontWeight.Bold)
            Text("المطعم: ${attempt.restaurantName}")
            Text("الحالة: ${attempt.statusEnum.labelArabic}")
            Text(attempt.message)
            attempt.confirmationReference?.let { Text("المرجع: $it") }
            Text(
                TimeUtils.formatDateTimeForDisplay(attempt.attemptTimestamp),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun SecurityScreen(vm: MainViewModel) {
    var result by remember { mutableStateOf<String?>(null) }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("الأمان والخصوصية", style = MaterialTheme.typography.headlineSmall)
        Text("• التطبيق لا يطلب ولا يحفظ كلمة مرور حساب Webetu.")
        Text("• لا يسجل الدخول إلى المنصة نيابةً عنك؛ الحجز يتم بنفسك عبر Webetu.")
        Text("• بيانات الجدولة محفوظة محليًا على الجهاز فقط.")
        Text("• البيانات الحساسة تُخفى من سجلات النظام عبر SecurityAudit.")
        Button(onClick = { result = vm.testKeystore() }, modifier = Modifier.fillMaxWidth()) {
            Text("اختبار مفتاح التشفير")
        }
        result?.let { Text("نتيجة الاختبار: $it") }
    }
}
