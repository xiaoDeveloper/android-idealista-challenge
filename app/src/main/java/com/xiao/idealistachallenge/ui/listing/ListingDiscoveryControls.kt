package com.xiao.idealistachallenge.ui.listing

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xiao.idealistachallenge.R

@Composable
fun ListingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = colorResource(R.color.colorBrandPrimary),
            onPrimary = colorResource(R.color.colorOnPrimary),
            primaryContainer = colorResource(R.color.colorPrimaryContainer),
            onPrimaryContainer = colorResource(R.color.colorOnPrimaryContainer),
            secondary = colorResource(R.color.colorSecondary),
            onSecondary = colorResource(R.color.colorOnSecondary),
            background = colorResource(R.color.colorBackground),
            surface = colorResource(R.color.colorSurface),
            surfaceVariant = colorResource(R.color.colorSurfaceVariant),
            onSurface = colorResource(R.color.colorOnSurface),
            onSurfaceVariant = colorResource(R.color.colorOnSurfaceVariant),
            outline = colorResource(R.color.colorOutline),
            error = colorResource(R.color.colorError),
            onError = colorResource(R.color.colorOnError),
        )
    } else {
        lightColorScheme(
            primary = colorResource(R.color.colorBrandPrimary),
            onPrimary = colorResource(R.color.colorOnPrimary),
            primaryContainer = colorResource(R.color.colorPrimaryContainer),
            onPrimaryContainer = colorResource(R.color.colorOnPrimaryContainer),
            secondary = colorResource(R.color.colorSecondary),
            onSecondary = colorResource(R.color.colorOnSecondary),
            background = colorResource(R.color.colorBackground),
            surface = colorResource(R.color.colorSurface),
            surfaceVariant = colorResource(R.color.colorSurfaceVariant),
            onSurface = colorResource(R.color.colorOnSurface),
            onSurfaceVariant = colorResource(R.color.colorOnSurfaceVariant),
            outline = colorResource(R.color.colorOutline),
            error = colorResource(R.color.colorError),
            onError = colorResource(R.color.colorOnError),
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ListingDiscoveryControls(
    state: ListingDiscoveryUiState,
    onCategorySelected: (ListingCategory) -> Unit,
    onPriceSortDirectionSelected: (PriceSortDirection) -> Unit,
    onFavoritesOnlyToggled: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var sortMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(R.string.listing_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 24.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.height(16.dp))

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            ListingCategory.entries.forEachIndexed { index, category ->
                SegmentedButton(
                    selected = state.category == category,
                    onClick = { onCategorySelected(category) },
                    shape = SegmentedButtonDefaults.itemShape(index, ListingCategory.entries.size),
                    modifier = Modifier.weight(1f),
                    label = {
                        Text(
                            text = stringResource(
                                when (category) {
                                    ListingCategory.ALL -> R.string.listing_category_all
                                    ListingCategory.SALE -> R.string.listing_category_sale
                                    ListingCategory.RENT -> R.string.listing_category_rent
                                },
                            ),
                        )
                    },
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.height(48.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                FilterChip(
                    selected = state.favoritesOnly,
                    onClick = { onFavoritesOnlyToggled(!state.favoritesOnly) },
                    modifier = Modifier.height(40.dp),
                    label = { Text(text = stringResource(R.string.listing_filter_favorites_only)) },
                    leadingIcon = {
                        Text(text = if (state.favoritesOnly) "♥" else "♡")
                    },
                )
            }

            if (state.category != ListingCategory.ALL) {
                Box(
                    modifier = Modifier.height(48.dp),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    OutlinedButton(
                        onClick = { sortMenuExpanded = true },
                        modifier = Modifier.height(40.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_swap_vert),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = stringResource(R.string.listing_sort),
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                    DropdownMenu(
                        expanded = sortMenuExpanded,
                        onDismissRequest = { sortMenuExpanded = false },
                    ) {
                        PriceSortDirection.entries.forEach { direction ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(
                                            if (direction == PriceSortDirection.ASCENDING) {
                                                R.string.listing_sort_price_ascending
                                            } else {
                                                R.string.listing_sort_price_descending
                                            },
                                        ),
                                    )
                                },
                                onClick = {
                                    onPriceSortDirectionSelected(direction)
                                    sortMenuExpanded = false
                                },
                                leadingIcon = {
                                    Text(text = if (direction == PriceSortDirection.ASCENDING) "↑" else "↓")
                                },
                                modifier = Modifier.semantics {
                                    selected = state.priceSortDirection == direction
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
