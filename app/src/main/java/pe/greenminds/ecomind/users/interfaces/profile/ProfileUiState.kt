package pe.greenminds.ecomind.users.interfaces.profile

import pe.greenminds.ecomind.users.domain.model.FamilyOverview
import pe.greenminds.ecomind.users.domain.model.FriendsOverview
import pe.greenminds.ecomind.users.domain.model.UserProfile
import pe.greenminds.ecomind.monetization.domain.model.EquippedCosmetics
import pe.greenminds.ecomind.gamification.interfaces.acl.MyAchievement

// Order of this enum is the order of the tabs on screen
enum class ProfileTab {
    PROFILE,
    FRIENDS,
    FAMILY
}

data class ProfileUiState(
    val selectedTab: ProfileTab = ProfileTab.PROFILE,

    // Header and Profile tab
    val isLoadingProfile: Boolean = true,
    val profile: UserProfile? = null,
    val profileFailed: Boolean = false,
    val equippedCosmetics: EquippedCosmetics = EquippedCosmetics(),

    // Medals of the Profile tab, read from Gamification
    val isLoadingAchievements: Boolean = true,
    val myAchievements: List<MyAchievement> = emptyList(),
    val achievementsFailed: Boolean = false,

    // Friends tab
    val isLoadingFriends: Boolean = true,
    val friends: FriendsOverview? = null,
    val friendsFailed: Boolean = false,

    // Family tab: when it finished loading and family is null, the user has no family
    val isLoadingFamily: Boolean = true,
    val family: FamilyOverview? = null,
    val familyFailed: Boolean = false
)
