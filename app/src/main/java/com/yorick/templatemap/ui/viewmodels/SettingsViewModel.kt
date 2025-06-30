package com.yorick.templatemap.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yorick.common.data.model.SettingsUiState
import com.yorick.templatemap.data.model.UserData
import com.yorick.templatemap.data.repository.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    val settingsUiState: StateFlow<SettingsUiState> =
        userDataRepository.userDataFlow
            .map { userData ->
                SettingsUiState.Success(userData = userData)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = SettingsUiState.Loading,
            )

    fun updateUserData(userData: UserData) {
        viewModelScope.launch {
            userDataRepository.setUserDate(userData)
        }
    }

    fun clearUserData() {
        updateUserData(UserData())
    }
}