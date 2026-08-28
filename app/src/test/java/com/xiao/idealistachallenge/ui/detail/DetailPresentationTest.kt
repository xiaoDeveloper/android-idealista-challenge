package com.xiao.idealistachallenge.ui.detail

import androidx.test.core.app.ApplicationProvider
import com.xiao.idealistachallenge.model.EnergyRating
import com.xiao.idealistachallenge.model.PropertyDetails
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DetailPresentationTest {

    private val presentation = DetailPresentation(ApplicationProvider.getApplicationContext())

    @Test
    fun `translates supported property type and operation and appends the detail currency suffix`() {
        val content = presentation.present(details(propertyType = "flat", operation = "sale"))

        assertEquals("Piso · Venta", content.typeAndOperation)
        assertEquals("1.195.000 €", content.price)
    }

    @Test
    fun `presents essential facts in the approved order including interior and true lift`() {
        val content = presentation.present(
            details(
                constructedAreaSquareMeters = 133,
                rooms = 3,
                bathrooms = 2,
                floor = "2",
                isExterior = false,
                hasLift = true,
            ),
        )

        assertEquals(
            listOf("133 m²", "3 habitaciones", "2 baños", "Planta 2", "Interior", "Con ascensor"),
            content.essentialFacts,
        )
    }

    @Test
    fun `presents exterior and only true additional characteristics without a cost frequency`() {
        val content = presentation.present(
            details(
                isExterior = true,
                hasLift = false,
                hasStorageRoom = true,
                isDuplex = true,
                communityCosts = BigDecimal("330"),
            ),
        )

        assertEquals(listOf("Exterior"), content.essentialFacts)
        assertEquals(
            listOf("Trastero", "Dúplex", "Gastos de comunidad: 330 €"),
            content.additionalCharacteristics,
        )
        assertFalse(content.additionalCharacteristics.any { it.contains("mes", ignoreCase = true) })
        assertFalse(content.additionalCharacteristics.any { it.contains("año", ignoreCase = true) })
    }

    @Test
    fun `omits unknown and unsupported presentation values rather than guessing`() {
        val content = presentation.present(
            details(
                propertyType = "homes",
                operation = "transfer",
                floor = "   ",
                isExterior = null,
                hasLift = false,
                hasStorageRoom = false,
                isDuplex = false,
                communityCosts = null,
            ),
        )

        assertNull(content.typeAndOperation)
        assertEquals(emptyList<String>(), content.essentialFacts)
        assertEquals(emptyList<String>(), content.additionalCharacteristics)
    }

    @Test
    fun `renders only valid normalized energy grades in dedicated rows`() {
        val content = presentation.present(
            details(
                energyConsumptionRating = EnergyRating.E,
                energyEmissionsRating = EnergyRating.G,
            ),
        )

        assertEquals(listOf("Consumo: E", "Emisiones: G"), content.energyRows)
    }

    private fun details(
        propertyType: String? = null,
        operation: String? = null,
        constructedAreaSquareMeters: Int? = null,
        rooms: Int? = null,
        bathrooms: Int? = null,
        floor: String? = null,
        isExterior: Boolean? = null,
        hasLift: Boolean? = null,
        hasStorageRoom: Boolean? = null,
        isDuplex: Boolean? = null,
        communityCosts: BigDecimal? = null,
        energyConsumptionRating: EnergyRating? = null,
        energyEmissionsRating: EnergyRating? = null,
    ) = PropertyDetails(
        selectedAdId = "listing-42",
        remoteAdId = 1,
        price = BigDecimal("1195000"),
        currencySuffix = "€",
        propertyType = propertyType,
        operation = operation,
        constructedAreaSquareMeters = constructedAreaSquareMeters,
        rooms = rooms,
        bathrooms = bathrooms,
        floor = floor,
        isExterior = isExterior,
        hasLift = hasLift,
        hasStorageRoom = hasStorageRoom,
        isDuplex = isDuplex,
        communityCosts = communityCosts,
        energyConsumptionRating = energyConsumptionRating,
        energyEmissionsRating = energyEmissionsRating,
    )
}
