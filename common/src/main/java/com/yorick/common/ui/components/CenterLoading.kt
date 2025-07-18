package com.yorick.common.ui.components

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BoxScope.CenterLoading() {
    CircularProgressIndicator(
        modifier = Modifier
            .size(40.dp)
            .align(Alignment.Center),
        color = MaterialTheme.colorScheme.primary
    )
}