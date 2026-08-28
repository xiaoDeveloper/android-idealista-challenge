package com.xiao.idealistachallenge.ui.listing

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
fun ListingDiscoveryControls(
    state: ListingDiscoveryUiState,
    onCategorySelected: (ListingCategory) -> Unit,
    onPriceSortDirectionSelected: (PriceSortDirection) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = state.category == ListingCategory.ALL,
                onClick = { onCategorySelected(ListingCategory.ALL) },
                label = { Text(text = stringResource(R.string.listing_category_all)) },
            )
            FilterChip(
                selected = state.category == ListingCategory.SALE,
                onClick = { onCategorySelected(ListingCategory.SALE) },
                label = { Text(text = stringResource(R.string.listing_category_sale)) },
            )
            FilterChip(
                selected = state.category == ListingCategory.RENT,
                onClick = { onCategorySelected(ListingCategory.RENT) },
                label = { Text(text = stringResource(R.string.listing_category_rent)) },
            )
        }

        if (state.category != ListingCategory.ALL) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = state.priceSortDirection == PriceSortDirection.ASCENDING,
                    onClick = { onPriceSortDirectionSelected(PriceSortDirection.ASCENDING) },
                    label = { Text(text = stringResource(R.string.listing_sort_price_ascending)) },
                )
                FilterChip(
                    selected = state.priceSortDirection == PriceSortDirection.DESCENDING,
                    onClick = { onPriceSortDirectionSelected(PriceSortDirection.DESCENDING) },
                    label = { Text(text = stringResource(R.string.listing_sort_price_descending)) },
                )
            }
        }
    }
}
