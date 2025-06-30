package com.yorick.templatemap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.yorick.common.data.model.SettingsUiState
import com.yorick.common.ui.theme.AppTheme
import com.yorick.common.ui.theme.shouldUseDarkTheme
import com.yorick.common.ui.theme.useDynamicTheming
import com.yorick.templatemap.ui.TemplateMapApp
import com.yorick.templatemap.ui.viewmodels.SettingViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val settingViewModel: SettingViewModel by viewModels()
        var uiState: SettingsUiState by mutableStateOf(SettingsUiState.Loading)
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingViewModel.settingsUiState
                    .collect {
                        uiState = it
                    }
            }
        }

        setContent {
            val darkTheme = shouldUseDarkTheme(uiState)
            AppTheme(
                darkTheme = darkTheme,
                dynamicColor = useDynamicTheming(uiState)
            ) {
                TemplateMapApp(
                    modifier = Modifier.fillMaxSize(),
                    settingViewModel = settingViewModel,
                    uiState = uiState
                )
            }
            SideEffect {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        lightScrim = android.graphics.Color.TRANSPARENT,
                        darkScrim = android.graphics.Color.TRANSPARENT,
                    ) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(
                        lightScrim = lightScrim,
                        darkScrim = darkScrim,
                    ) { darkTheme },
                )
            }
        }
    }
}


private val lightScrim = android.graphics.Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
private val darkScrim = android.graphics.Color.argb(0x80, 0x1b, 0x1b, 0x1b)