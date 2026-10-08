package pe.greenminds.ecomind.gamification.domain.services

import pe.greenminds.ecomind.gamification.domain.model.RankingPeriod
import java.util.Calendar

object RankingPeriodRange {

    // First instant of the period, in the time zone of the device; null means "no lower limit".
    // Calendar is used instead of java.time because the app supports API 24.
    fun startOf(period: RankingPeriod, nowMillis: Long): Long? {
        if (period == RankingPeriod.ALL_TIME) return null

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = nowMillis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        when (period) {
            RankingPeriod.WEEKLY -> {
                // The week starts on Monday
                calendar.firstDayOfWeek = Calendar.MONDAY
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            }

            RankingPeriod.MONTHLY -> calendar.set(Calendar.DAY_OF_MONTH, 1)

            else -> Unit
        }
        return calendar.timeInMillis
    }
}
