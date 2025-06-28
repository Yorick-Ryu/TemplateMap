package com.yorick.common.data.model

import androidx.compose.runtime.Stable

@Stable
sealed interface SettingsUiState {
    data object Loading : SettingsUiState
    data class Success(val userData: BaseUserData) : SettingsUiState
}