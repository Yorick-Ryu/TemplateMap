package com.yorick.templatemap.ui.viewmodels

import android.app.Application
import androidx.compose.runtime.Stable
import androidx.lifecycle.AndroidViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val application: Application,
) : AndroidViewModel(application) {

    companion object {
        const val TAG = "AppViewModel"
    }

    private val _uiState = MutableStateFlow(AppUiState(loading = false))
    val uiState: StateFlow<AppUiState> = _uiState


    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

}

@Stable
data class AppUiState(
    val loading: Boolean = false,
    val error: String? = null,
)