package com.xiao.idealistachallenge.ui.listing

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ListingDiscoveryControlsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun allCategoryShowsSegmentedCategoriesAndHidesSortControl() {
        composeTestRule.setContent {
            ListingTheme {
                ListingDiscoveryControls(
                    state = ListingDiscoveryUiState(category = ListingCategory.ALL),
                    onCategorySelected = {},
                    onPriceSortDirectionSelected = {},
                    onFavoritesOnlyToggled = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Viviendas").assertIsDisplayed()
        composeTestRule.onNodeWithText("Todos").assertIsDisplayed().assertIsSelected()
        composeTestRule.onNodeWithText("Venta").assertIsDisplayed()
        composeTestRule.onNodeWithText("Alquiler").assertIsDisplayed()
        composeTestRule.onNodeWithText("Solo favoritos").assertIsDisplayed().assertIsNotSelected()
        composeTestRule.onNodeWithText("Ordenar").assertDoesNotExist()
        composeTestRule.onNodeWithText("Precio más bajo").assertDoesNotExist()
        composeTestRule.onNodeWithText("Precio más alto").assertDoesNotExist()
    }

    @Test
    fun saleCategoryShowsSortMenuAndPropagatesSelectionCallbacks() {
        var selectedCategory: ListingCategory? = null
        var selectedDirection: PriceSortDirection? = null
        var currentState by mutableStateOf(
            ListingDiscoveryUiState(
                category = ListingCategory.SALE,
                priceSortDirection = PriceSortDirection.ASCENDING,
            ),
        )

        composeTestRule.setContent {
            ListingTheme {
                ListingDiscoveryControls(
                    state = currentState,
                    onCategorySelected = {
                        selectedCategory = it
                        currentState = currentState.copy(category = it)
                    },
                    onPriceSortDirectionSelected = {
                        selectedDirection = it
                        currentState = currentState.copy(priceSortDirection = it)
                    },
                    onFavoritesOnlyToggled = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Venta").assertIsSelected()
        composeTestRule.onNodeWithText("Solo favoritos").assertIsDisplayed().assertIsNotSelected()
        composeTestRule.onNodeWithText("Ordenar").assertIsDisplayed()
        composeTestRule.onNodeWithText("Precio más bajo").assertDoesNotExist()
        composeTestRule.onNodeWithText("Precio más alto").assertDoesNotExist()

        composeTestRule.onNodeWithText("Ordenar").performClick()
        composeTestRule.onNodeWithText("Precio más bajo").assertIsDisplayed().assertIsSelected()
        composeTestRule.onNodeWithText("Precio más alto").assertIsDisplayed()
        composeTestRule.onNodeWithText("Precio más alto").performClick()
        assertEquals(PriceSortDirection.DESCENDING, selectedDirection)

        composeTestRule.onNodeWithText("Alquiler").performClick()
        assertEquals(ListingCategory.RENT, selectedCategory)
    }

    @Test
    fun favoritesOnlyFilterChipTogglesSelectionAndInvokesCallback() {
        var toggledValue: Boolean? = null
        var currentState by mutableStateOf(
            ListingDiscoveryUiState(
                category = ListingCategory.ALL,
                favoritesOnly = false,
            ),
        )

        composeTestRule.setContent {
            ListingTheme {
                ListingDiscoveryControls(
                    state = currentState,
                    onCategorySelected = {},
                    onPriceSortDirectionSelected = {},
                    onFavoritesOnlyToggled = {
                        toggledValue = it
                        currentState = currentState.copy(favoritesOnly = it)
                    },
                )
            }
        }

        val favoritesNode = composeTestRule.onNodeWithText("Solo favoritos")
        favoritesNode.assertIsDisplayed().assertIsNotSelected()

        favoritesNode.performClick()
        assertEquals(true, toggledValue)
        favoritesNode.assertIsSelected()
    }
}
