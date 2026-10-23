package dz.mesrs.webetu.automeals.data.model

enum class MealType(val arabicName: String, val frenchName: String, val standardServiceHour: Int) {
    BREAKFAST("الفطور", "Petit Déjeuner", 7),
    LUNCH("الغداء", "Déjeuner", 12),
    DINNER("العشاء", "Dîner", 18);
    val displayName: String get() = "$arabicName ($frenchName)"
}

enum class BookingStatus(val labelArabic: String) {
    SCHEDULED("مجدول"), IN_PROGRESS("قيد التنفيذ"), CONFIRMED("مؤكد"), UNCONFIRMED("غير مؤكد"), FAILED("فشل"), NEEDS_INTERVENTION("يحتاج إلى تدخل المستخدم");
    val isTerminal: Boolean get() = this == CONFIRMED || this == FAILED
}

enum class ExecutionMethod(val labelArabic: String) {
    OFFICIAL_API("واجهة رسمية"), ASSISTED_MANUAL("مساعدة وتوجيه يدوي"), MOCK_SIMULATION("محاكاة واختبار")
}

enum class DayOfWeekOption(val id: Int, val dayOfWeek: java.time.DayOfWeek, val arabicName: String) {
    SUNDAY(1, java.time.DayOfWeek.SUNDAY, "الأحد"),
    MONDAY(2, java.time.DayOfWeek.MONDAY, "الاثنين"),
    TUESDAY(3, java.time.DayOfWeek.TUESDAY, "الثلاثاء"),
    WEDNESDAY(4, java.time.DayOfWeek.WEDNESDAY, "الأربعاء"),
    THURSDAY(5, java.time.DayOfWeek.THURSDAY, "الخميس"),
    FRIDAY(6, java.time.DayOfWeek.FRIDAY, "الجمعة"),
    SATURDAY(7, java.time.DayOfWeek.SATURDAY, "السبت");
}
