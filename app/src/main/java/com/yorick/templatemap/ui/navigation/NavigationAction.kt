package com.yorick.templatemap.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Settings
import com.yorick.common.R
import com.yorick.common.ui.navigation.AppTopLevelDestination

object AppRoute {
    const val REGISTER = "Register"
    const val LOGIN = "Login"
    const val MAIN = "Main"
    const val MIME = "Mine"
    const val HOME = "Home"
    const val MAP = "Map"
    const val SETTINGS = "Settings"
    const val ABOUT = "About"
}

val TOP_LEVEL_DESTINATIONS = listOf(
    AppTopLevelDestination(
        AppRoute.HOME,
        Icons.Filled.Home,
        Icons.Outlined.Home,
        R.string.home
    ),
    AppTopLevelDestination(
        AppRoute.MAP,
        Icons.Filled.Map,
        Icons.Outlined.Map,
        R.string.map
    ),
    AppTopLevelDestination(
        AppRoute.SETTINGS,
        Icons.Filled.Settings,
        Icons.Outlined.Settings,
        R.string.settings
    )
)