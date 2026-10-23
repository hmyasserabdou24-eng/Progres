package dz.mesrs.webetu.automeals.data.model

data class ResidenceOption(val id: String, val nameArabic: String, val wilaya: String, val restaurants: List<RestaurantOption>)
data class RestaurantOption(val id: String, val nameArabic: String)

object VerifiedResidenceCatalog {
    val RESIDENCES: List<ResidenceOption> = listOf(
        ResidenceOption("res_alger_ouled_fayet_1", "الإقامة الجامعية أولاد فايت 1 (الجزائر)", "الجزائر", listOf(RestaurantOption("resto_of1_central", "المطعم المركزي أولاد فايت 1"))),
        ResidenceOption("res_alger_bab_ezzouar_1", "الإقامة الجامعية باب الزوار 1 (الجزائر)", "الجزائر", listOf(RestaurantOption("resto_bz1_central", "المطعم المركزي باب الزوار 1")))
    )
    fun findResidence(id: String): ResidenceOption? = RESIDENCES.find { it.id == id }
}

data class ScheduleSettings(
    val isScheduleActive: Boolean = false,
    val selectedResidenceId: String = "",
    val selectedResidenceName: String = "",
    val selectedRestaurantId: String = "",
    val selectedRestaurantName: String = "",
    val selectedMeals: Set<MealType> = emptySet(),
    val selectedDays: Set<DayOfWeekOption> = emptySet(),
    val attemptHour: Int = 19,
    val attemptMinute: Int = 0,
    val startDateEpochDay: Long = java.time.LocalDate.now().toEpochDay(),
    val endDateEpochDay: Long? = null,
    val notificationsEnabled: Boolean = true,
    val studentLocalId: String = "student_default"
) {
    val isValid: Boolean get() = selectedMeals.isNotEmpty() && selectedDays.isNotEmpty() && selectedResidenceId.isNotBlank()
}
