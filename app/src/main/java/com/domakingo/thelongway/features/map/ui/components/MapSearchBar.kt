package com.domakingo.thelongway.features.map.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.domakingo.thelongway.R
import com.domakingo.thelongway.core.network.GeocodingFeature

@Composable
fun MapSearchBar(
    query: String,
    suggestions: List<GeocodingFeature>,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onSuggestionClick: (GeocodingFeature) -> Unit,
    onClearClick: () -> Unit,
    isRoutingMode: Boolean,
    originQuery: String,
    destinationQuery: String,
    onOriginQueryChange: (String) -> Unit,
    onDestinationQueryChange: (String) -> Unit,
    onToggleRouting: () -> Unit,
    onUseCurrentLocation: () -> Unit,
    onCalculateRoute: () -> Unit,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .animateContentSize(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            if (!isRoutingMode) {
                TextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.search_placeholder)) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isSelected) {
                                IconButton(onClick = onToggleRouting) {
                                    Icon(
                                        imageVector = Icons.Default.Directions,
                                        contentDescription = "Directions",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            if (query.isNotEmpty()) {
                                IconButton(onClick = onClearClick) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = stringResource(R.string.clear_search_desc)
                                    )
                                }
                            }
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        onSearch(query)
                        focusManager.clearFocus()
                    })
                )
            } else {
                // Routing Mode UI
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Route",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = onToggleRouting) {
                            Icon(Icons.Default.Close, contentDescription = "Close Routing")
                        }
                    }

                    TextField(
                        value = originQuery,
                        onValueChange = onOriginQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Start position") },
                        leadingIcon = { Icon(Icons.Default.MyLocation, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = onUseCurrentLocation) {
                                Icon(Icons.Default.GpsFixed, contentDescription = "Use Current Location")
                            }
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    TextField(
                        value = destinationQuery,
                        onValueChange = onDestinationQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Destination") },
                        leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            onCalculateRoute()
                            focusManager.clearFocus()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = originQuery.isNotEmpty() && destinationQuery.isNotEmpty()
                    ) {
                        Text("Calculate Route")
                    }
                }
            }

            AnimatedVisibility(visible = suggestions.isNotEmpty()) {
                Column {
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    LazyColumn(modifier = Modifier.heightIn(max = 250.dp)) {
                        items(suggestions) { feature ->
                            SuggestionItem(
                                feature = feature,
                                onClick = {
                                    onSuggestionClick(feature)
                                    focusManager.clearFocus()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SuggestionItem(
    feature: GeocodingFeature,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = feature.properties.name ?: feature.properties.label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            if (feature.properties.name != null) {
                Text(
                    text = feature.properties.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
