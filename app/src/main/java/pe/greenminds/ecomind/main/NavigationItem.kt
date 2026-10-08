package pe.greenminds.ecomind.main

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.community.interfaces.navigation.CommunityRoute
import pe.greenminds.ecomind.gamification.interfaces.navigation.RankingRoute
import pe.greenminds.ecomind.monetization.interfaces.navigation.StoreRoute
import pe.greenminds.ecomind.quests.interfaces.navigation.ProgressRoute
import pe.greenminds.ecomind.users.interfaces.navigation.ProfileRoute

// Items of the bottom bar, in the order they are drawn
enum class NavigationItem(
    val route: Any,
    @DrawableRes val activeIcon: Int,
    @DrawableRes val inactiveIcon: Int,
    @StringRes val label: Int
) {
    STORE(
        route = StoreRoute,
        activeIcon = R.drawable.ic_nav_store_active,
        inactiveIcon = R.drawable.ic_nav_store_inactive,
        label = R.string.nav_store
    ),
    RANKING(
        route = RankingRoute,
        activeIcon = R.drawable.ic_nav_ranking_active,
        inactiveIcon = R.drawable.ic_nav_ranking_inactive,
        label = R.string.nav_ranking
    ),
    MAIN_MENU(
        route = MainMenuRoute,
        activeIcon = R.drawable.ic_nav_main_menu_active,
        inactiveIcon = R.drawable.ic_nav_main_menu_inactive,
        label = R.string.nav_main_menu
    ),
    PROGRESS(
        route = ProgressRoute,
        activeIcon = R.drawable.ic_nav_progress_active,
        inactiveIcon = R.drawable.ic_nav_progress_inactive,
        label = R.string.nav_progress
    ),
    COMMUNITY(
        route = CommunityRoute,
        activeIcon = R.drawable.ic_nav_community_active,
        inactiveIcon = R.drawable.ic_nav_community_inactive,
        label = R.string.nav_community
    ),
    PROFILE(
        route = ProfileRoute,
        activeIcon = R.drawable.ic_nav_profile_active,
        inactiveIcon = R.drawable.ic_nav_profile_inactive,
        label = R.string.nav_profile
    )
}
