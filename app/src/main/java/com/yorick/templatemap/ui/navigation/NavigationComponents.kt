package com.yorick.templatemap.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.yorick.common.ui.navigation.AppTopLevelDestination

@Composable
fun AppNavigationBar(
    selectedDestination: String,
    navigateToTopLevelDestination: (AppTopLevelDestination) -> Unit,
) {
    NavigationBar {
        TOP_LEVEL_DESTINATIONS.forEach { destination ->
            NavigationBarItem(
                selected = selectedDestination == destination.route,
                onClick = { navigateToTopLevelDestination(destination) },
                icon = {
                    Icon(
                        imageVector = if (selectedDestination == destination.route) destination.selectedIcon
                        else destination.unselectedIcon,
                        contentDescription = stringResource(id = destination.iconTextId)
                    )
                },
                label = { Text(text = stringResource(id = destination.iconTextId)) },
                alwaysShowLabel = true
            )
        }
    }
}