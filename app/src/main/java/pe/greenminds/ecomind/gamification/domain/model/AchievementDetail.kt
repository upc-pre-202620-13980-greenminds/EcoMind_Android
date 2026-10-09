package pe.greenminds.ecomind.gamification.domain.model

data class AchievementDetail(val entry: AchievementEntry, val isSimulated: Boolean)

class AchievementNotFoundException : NoSuchElementException("The achievement is unavailable")
