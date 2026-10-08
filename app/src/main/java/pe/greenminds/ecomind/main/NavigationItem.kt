package pe.greenminds.ecomind.main

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import pe.greenminds.ecomind.R

// Items of the bottom bar, in the order they are drawn
enum class NavigationItem(
    @DrawableRes val activeIcon: Int,
    @DrawableRes val inactiveIcon: Int,
    @StringRes val label: Int
) {
    STORE(
        activeIcon = R.drawable.ic_nav_store_active,
        inactiveIcon = R.drawable.ic_nav_store_inactive,
        label = R.string.nav_store
    ),
    RANKING(
        activeIcon = R.drawable.ic_nav_ranking_active,
        inactiveIcon = R.drawable.ic_nav_ranking_inactive,
        label = R.string.nav_ranking
    ),
    MAIN_MENU(
        activeIcon = R.drawable.ic_nav_main_menu_active,
        inactiveIcon = R.drawable.ic_nav_main_menu_inactive,
        label = R.string.nav_main_menu
    ),
    PROGRESS(
        activeIcon = R.drawable.ic_nav_progress_active,
        inactiveIcon = R.drawable.ic_nav_progress_inactive,
        label = R.string.nav_progress
    ),
    COMMUNITY(
        activeIcon = R.drawable.ic_nav_community_active,
        inactiveIcon = R.drawable.ic_nav_community_inactive,
        label = R.string.nav_community
    ),
    PROFILE(
        activeIcon = R.drawable.ic_nav_profile_active,
        inactiveIcon = R.drawable.ic_nav_profile_inactive,
        label = R.string.nav_profile
    )
}
