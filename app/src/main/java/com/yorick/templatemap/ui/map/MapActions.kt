package com.yorick.templatemap.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.LayersClear
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.melody.map.baidu_compose.poperties.MapProperties

@Composable
fun MapActions(
    modifier: Modifier,
    mapProperties: MapProperties,
    onClickLocation: () -> Unit,
    onToggleMapType: () -> Unit,
    onToggleMapLabels: () -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit
) {
    Column(
        modifier = modifier.padding(bottom = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Map type toggle
        Box(
            modifier = Modifier.background(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = MaterialTheme.shapes.small
            ),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                modifier = Modifier.size(36.dp),
                onClick = onToggleMapType,
            ) {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = "Toggle Map Type",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

//        Text(
//            text = stringResource(id = R.string.switch_map),
//            style = MaterialTheme.typography.labelSmall
//        )

        // Map labels toggle
        Box(
            modifier = Modifier.background(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = MaterialTheme.shapes.small
            ),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                modifier = Modifier.size(36.dp),
                onClick = onToggleMapLabels,
            ) {
                Icon(
                    imageVector = if (mapProperties.isShowMapLabels) {
                        Icons.Outlined.Layers
                    } else {
                        Icons.Outlined.LayersClear
                    },
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = "Toggle Map Labels"
                )
            }
        }

//        Text(
//            text = stringResource(id = R.string.map_label),
//            style = MaterialTheme.typography.labelSmall
//        )

        // Location button
        Box(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = MaterialTheme.shapes.small
                ),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                modifier = Modifier.size(36.dp),
                onClick = onClickLocation
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = "MyLocation"
                )
            }
        }

//        Text(
//            text = stringResource(id = R.string.location),
//            style = MaterialTheme.typography.labelSmall
//        )

        // Zoom buttons
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = MaterialTheme.shapes.small
                )
        ) {
            IconButton(
                onClick = onZoomIn,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = "Zoom In"
                )
            }
            HorizontalDivider(
                modifier = Modifier.width(30.dp),
                thickness = 2.dp
            )
            IconButton(
                onClick = onZoomOut,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = "Zoom Out"
                )
            }
        }
    }
}