package com.example

import com.example.domain.ChainSlackState
import com.example.domain.MotorcycleCalculators
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testChainSlackCalculations() {
        // Optimal range 25-35mm, measured 30mm
        val optimal = MotorcycleCalculators.evaluateChainSlack(30f, 25f, 35f)
        assertEquals(ChainSlackState.OPTIMAL, optimal.state)
        assertEquals(0f, optimal.diffMm, 0.01f)

        // Too tight: measured 15mm
        val tight = MotorcycleCalculators.evaluateChainSlack(15f, 25f, 35f, 1.25f)
        assertEquals(ChainSlackState.TOO_TIGHT, tight.state)
        assertEquals(10f, tight.diffMm, 0.01f)
        assertEquals(8f, tight.turnsRecommendation, 0.01f)

        // Too loose: measured 45mm
        val loose = MotorcycleCalculators.evaluateChainSlack(45f, 25f, 35f, 1.25f)
        assertEquals(ChainSlackState.TOO_LOOSE, loose.state)
        assertEquals(10f, loose.diffMm, 0.01f)
    }

    @Test
    fun testSprocketRatio() {
        val result = MotorcycleCalculators.calculateSprocketRatio(14, 42)
        assertEquals(3.0f, result.ratio, 0.01f)
        assertTrue(result.characterThai.isNotEmpty())
    }

    @Test
    fun testCompressionRatioDisplacement() {
        // Wave 110i: Bore 50.0mm, Stroke 55.6mm -> approx 109.1 cc
        val comp = MotorcycleCalculators.calculateCompression(
            boreMm = 50.0f,
            strokeMm = 55.6f,
            chamberCc = 12.0f,
            deckClearanceMm = 0.5f,
            gasketThicknessMm = 0.25f,
            cylinders = 1
        )
        // Vs = (PI/4) * 50^2 * 55.6 / 1000 = 109.17 cc
        assertEquals(109.17f, comp.sweptVolumePerCylinderCc, 0.5f)
        assertTrue(comp.compressionRatio > 8.0f && comp.compressionRatio < 11.0f)
        assertTrue(comp.fuelOctaneThai.isNotEmpty())
    }

    @Test
    fun testAirDensityJetting() {
        // At high altitude and heat, RAD drops and recommended jet size decreases
        val result = MotorcycleCalculators.calculateJettingCorrection(115f, 35f, 1200f)
        assertTrue(result.relativeAirDensityPercent < 100f)
        assertTrue(result.recommendedJetSize < 115f)
    }

    @Test
    fun testUnitConversions() {
        assertEquals(2.0f, MotorcycleCalculators.psiToBar(29.0075f), 0.05f)
        assertEquals(22.12f, MotorcycleCalculators.nmToLbFt(30.0f), 0.1f)
        assertEquals(27.05f, MotorcycleCalculators.ccToFlOz(800.0f), 0.1f)
    }
}
