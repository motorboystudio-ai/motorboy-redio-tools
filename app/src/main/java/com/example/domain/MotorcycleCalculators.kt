package com.example.domain

import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Domain calculation models and pure logic for motorcycle workshop calculations.
 */

enum class ChainSlackState {
    TOO_TIGHT,
    OPTIMAL,
    TOO_LOOSE
}

data class ChainSlackResult(
    val state: ChainSlackState,
    val diffMm: Float,
    val turnsRecommendation: Float,
    val statusSummaryThai: String,
    val detailAdviceThai: String
)

data class SprocketRatioResult(
    val ratio: Float,
    val characterThai: String,
    val torqueDescriptionThai: String
)

data class CompressionResult(
    val sweptVolumePerCylinderCc: Float,
    val totalDisplacementCc: Float,
    val clearanceVolumeCc: Float,
    val totalCombustionChamberCc: Float,
    val totalCylinderVolumeCc: Float,
    val compressionRatio: Float,
    val fuelOctaneThai: String,
    val sparkPlugHeatRangeThai: String,
    val engineSafetyLevelThai: String
)

data class OversizeRow(
    val name: String,
    val boreMm: Float,
    val displacementCc: Float,
    val compressionRatio: Float
)

data class AirDensityResult(
    val relativeAirDensityPercent: Float,
    val recommendedJetSize: Float,
    val sizeDifference: Float,
    val afrStateThai: String,
    val tuningTipsThai: String,
    val airScrewTurnsThai: String
)

object MotorcycleCalculators {

    /**
     * Calculates chain tension condition and bolt adjustment recommendation.
     */
    fun evaluateChainSlack(
        measuredMm: Float,
        minSlackMm: Float,
        maxSlackMm: Float,
        adjusterThreadPitchMm: Float = 1.25f
    ): ChainSlackResult {
        return when {
            measuredMm < minSlackMm -> {
                val deficit = minSlackMm - measuredMm
                val turns = deficit / adjusterThreadPitchMm
                ChainSlackResult(
                    state = ChainSlackState.TOO_TIGHT,
                    diffMm = deficit,
                    turnsRecommendation = turns,
                    statusSummaryThai = "โซ่ตึงเกินไป (อันตรายต่อลูกปืนและแกนเกียร์)",
                    detailAdviceThai = "ระยะหย่อนน้อยกว่าสเปก ${String.format("%.1f", deficit)} มม. ควรคลายน็อตแกนล้อ แล้วหมุนสลักตั้งโซ่ทวนเข็มนาฬิกาประมาณ ${String.format("%.1f", turns)} รอบ เพื่อดันเพลาล้อไปข้างหน้า ป้องกันโซ่ขาดหรือดึงแกนสเตอร์หน้าเสียหายเมื่อโช้คยุบตัว"
                )
            }
            measuredMm > maxSlackMm -> {
                val excess = measuredMm - maxSlackMm
                val turns = excess / adjusterThreadPitchMm
                ChainSlackResult(
                    state = ChainSlackState.TOO_LOOSE,
                    diffMm = excess,
                    turnsRecommendation = turns,
                    statusSummaryThai = "โซ่หย่อนเกินไป (เสี่ยงโซ่ตก/ตีสวิงอาร์ม)",
                    detailAdviceThai = "ระยะหย่อนเกินสเปก ${String.format("%.1f", excess)} มม. ควรขันน็อตตั้งโซ่ตามเข็มนาฬิกาประมาณ ${String.format("%.1f", turns)} รอบ ถอยเพลาล้อหลังให้ได้ระยะ พร้อมตรวจสอบขีดตั้งโซ่ทั้งสองฝั่งให้ตรงกัน"
                )
            }
            else -> {
                ChainSlackResult(
                    state = ChainSlackState.OPTIMAL,
                    diffMm = 0f,
                    turnsRecommendation = 0f,
                    statusSummaryThai = "ระยะหย่อนโซ่พอดีตามเกณฑ์มาตรฐาน",
                    detailAdviceThai = "ระยะหย่อน ${String.format("%.1f", measuredMm)} มม. อยู่ในช่วงแนะนำ (${String.format("%.0f", minSlackMm)}-${String.format("%.0f", maxSlackMm)} มม.) ให้หยอดน้ำมันหล่อลื่นโซ่และตรวจเช็กความตึงทุก 500-1,000 กม."
                )
            }
        }
    }

    /**
     * Calculates final sprocket drive ratio and vehicle driving characteristics.
     */
    fun calculateSprocketRatio(frontTeeth: Int, rearTeeth: Int): SprocketRatioResult {
        if (frontTeeth <= 0 || rearTeeth <= 0) {
            return SprocketRatioResult(0f, "-", "-")
        }
        val ratio = rearTeeth.toFloat() / frontTeeth.toFloat()
        val character = when {
            ratio > 3.0f -> "เน้นอัตราเร่งต้น / ขึ้นทางชัน / บรรทุกหนัก"
            ratio in 2.6f..3.0f -> "สมดุลมาตรฐาน ต้นจัด ปลายไหลสม่ำเสมอ"
            else -> "เน้นความเร็วปลาย / ประหยัดน้ำมันรอบเดินทางไกล"
        }
        val desc = "สเตอร์หน้า 1 ฟัน หมุนเท่ากับสเตอร์หลังหมุน ${String.format("%.2f", ratio)} รอบ (ทดสเตอร์หน้า 1 ฟันเทียบเท่าสเตอร์หลัง ~2.5-3 ฟัน)"
        return SprocketRatioResult(ratio, character, desc)
    }

    /**
     * Calculates engine displacement and static compression ratio.
     * Formula:
     * Cylinder Swept Volume Vs = (π / 4) * Bore^2 * Stroke / 1000  (in cc)
     * Clearance Volume V_clearance = (π / 4) * Bore^2 * (Deck + Gasket) / 1000
     * Total Compressed Volume V_combustion = ChamberCc + V_clearance
     * Compression Ratio = (Vs + V_combustion) / V_combustion
     */
    fun calculateCompression(
        boreMm: Float,
        strokeMm: Float,
        chamberCc: Float,
        deckClearanceMm: Float = 0.5f,
        gasketThicknessMm: Float = 0.25f,
        cylinders: Int = 1
    ): CompressionResult {
        if (boreMm <= 0f || strokeMm <= 0f || chamberCc <= 0f) {
            return CompressionResult(0f, 0f, 0f, 0f, 0f, 0f, "-", "-", "-")
        }

        val cylinderAreaMm2 = (PI.toFloat() / 4.0f) * (boreMm * boreMm)
        val sweptVolumePerCylinderCc = (cylinderAreaMm2 * strokeMm) / 1000f
        val totalDisplacementCc = sweptVolumePerCylinderCc * cylinders.coerceAtLeast(1)

        val totalClearanceMm = (deckClearanceMm.coerceAtLeast(0f) + gasketThicknessMm.coerceAtLeast(0f))
        val clearanceVolumeCc = (cylinderAreaMm2 * totalClearanceMm) / 1000f
        val totalChamberCc = chamberCc + clearanceVolumeCc

        val totalCylinderVolumeCc = sweptVolumePerCylinderCc + totalChamberCc
        val compressionRatio = if (totalChamberCc > 0.001f) {
            totalCylinderVolumeCc / totalChamberCc
        } else 0f

        val (fuel, spark, safety) = when {
            compressionRatio < 9.5f -> Triple(
                "แก๊สโซฮอล์ 91 หรือ 95 ทั่วไป (เผาไหม้ง่าย ไม่สะดุด)",
                "หัวเทียนเบอร์ร้อน (เช่น NGK เบอร์ 6 หรือ 7)",
                "ปลอดภัยสูง ทนทาน เหมาะกับการใช้งานประจำวันทั่วไป"
            )
            compressionRatio in 9.5f..11.2f -> Triple(
                "แก๊สโซฮอล์ 95 หรือ E20 (ป้องกันอาการชิงจุดระเบิด)",
                "หัวเทียนเบอร์มาตรฐาน (เช่น NGK เบอร์ 7 หรือ 8)",
                "กำลังเครื่องยนต์ยอดเยี่ยม อัตราเร่งดีและประหยัดน้ำมัน"
            )
            compressionRatio in 11.2f..12.5f -> Triple(
                "เบนซิน 95 เพียว หรือ แก๊สโซฮอล์ 95 ค่าออกเทนสูง",
                "หัวเทียนเบอร์เย็น (เช่น NGK เบอร์ 8 หรือ 9 / อิริเดียม)",
                "กำลังอัดสูงมาก แรงบิดสูง ต้องระวังความร้อนสะสมและการน็อค (Knock)"
            )
            else -> Triple(
                "น้ำมันเกรดแข่ง 100+ ออกเทน หรือ E85 จูนองศาจุดระเบิด",
                "หัวเทียนเบอร์เย็นจัด (เช่น NGK เบอร์ 9 หรือ 10 อิริเดียม)",
                "ระดับรถแข่ง (Racing) เสี่ยงลูกสูบละลายหรือน็อคหากใช้น้ำมันออกเทนต่ำ"
            )
        }

        return CompressionResult(
            sweptVolumePerCylinderCc = sweptVolumePerCylinderCc,
            totalDisplacementCc = totalDisplacementCc,
            clearanceVolumeCc = clearanceVolumeCc,
            totalCombustionChamberCc = totalChamberCc,
            totalCylinderVolumeCc = totalCylinderVolumeCc,
            compressionRatio = compressionRatio,
            fuelOctaneThai = fuel,
            sparkPlugHeatRangeThai = spark,
            engineSafetyLevelThai = safety
        )
    }

    /**
     * Generates oversize piston bore comparison (+0.25, +0.50, +1.00, +2.00 mm).
     */
    fun calculateOversizeComparisons(
        stdBoreMm: Float,
        strokeMm: Float,
        chamberCc: Float,
        deckClearanceMm: Float,
        gasketThicknessMm: Float
    ): List<OversizeRow> {
        val steps = listOf(
            "STD (เดิม)" to 0.0f,
            "+0.25 (ไซซ์ 1)" to 0.25f,
            "+0.50 (ไซซ์ 2)" to 0.50f,
            "+0.75 (ไซซ์ 3)" to 0.75f,
            "+1.00 (ไซซ์ 4)" to 1.00f,
            "+2.00 (ไซซ์พิเศษ)" to 2.00f
        )
        return steps.map { (label, delta) ->
            val bore = stdBoreMm + delta
            val comp = calculateCompression(bore, strokeMm, chamberCc, deckClearanceMm, gasketThicknessMm, 1)
            OversizeRow(label, bore, comp.sweptVolumePerCylinderCc, comp.compressionRatio)
        }
    }

    /**
     * Calculates Relative Air Density (RAD) and recommended Carburetor / Fuel Injection jetting.
     * Uses barometric atmospheric formula based on altitude and ambient temperature.
     */
    fun calculateJettingCorrection(
        baseMainJet: Float,
        tempCelsius: Float,
        altitudeMeters: Float
    ): AirDensityResult {
        // Standard atmospheric pressure: Sea level = 1013.25 hPa, standard temp = 15°C (288.15 K)
        val tempKelvin = (tempCelsius + 273.15f).coerceAtLeast(200f)
        val standardTempKelvin = 288.15f

        // Barometric pressure variation with altitude: P = P0 * exp(-alt / 8400)
        val pressureRatio = exp(-altitudeMeters / 8400.0).toFloat()
        val tempRatio = standardTempKelvin / tempKelvin

        // Relative Air Density relative to standard day
        val rad = (pressureRatio * tempRatio) * 100f

        // Corrected main jet: Air density changes required fuel mass flow proportionally to sqrt of air density
        val correctionFactor = sqrt(rad / 100f)
        val targetJet = if (baseMainJet > 0f) baseMainJet * correctionFactor else 0f
        val diff = targetJet - baseMainJet

        val (afrState, tips, screw) = when {
            rad < 93f -> Triple(
                "อากาศเบาบาง (High Altitude/Hot) - ส่วนผสมเดิมจะหนาขึ้น",
                "อากาศมีความหนาแน่นต่ำกว่าปกติ แนะนำลดเบอร์นมหนูเมนลง ${String.format("%.0f", (baseMainJet - targetJet).coerceAtLeast(0f))} เบอร์ เพื่อป้องกันหัวเทียนดำและเร่งสะดุด",
                "ขันสกรูอากาศออก 1.75 - 2.25 รอบ (เพิ่มปริมาณอากาศเดินเบา)"
            )
            rad in 93f..105f -> Triple(
                "อากาศปกติปานกลาง (Standard Atmospheric Condition)",
                "ความหนาแน่นของอากาศอยู่ในเกณฑ์มาตรฐาน นมหนูเดิมเบอร์ ${String.format("%.0f", baseMainJet)} ใช้งานได้ดี สมดุลทั้งรอบต้นและปลาย",
                "ขันสกรูอากาศออก 1.5 - 1.75 รอบมาตรฐาน"
            )
            else -> Triple(
                "อากาศหนาแน่นสูง (Cold/Sea Level) - ส่วนผสมเดิมจะบางลง",
                "อากาศหนาวหรืออยู่ใกล้ระดับน้ำทะเล อากาศหนาแน่น มีออกซิเจนมาก แนะนำเพิ่มเบอร์นมหนูขึ้น ${String.format("%.0f", (targetJet - baseMainJet).coerceAtLeast(0f))} เบอร์ ป้องกันลูกสูบแห้ง/ร้อนจัด",
                "ขันสกรูอากาศเข้าเล็กน้อยเหลือ 1.0 - 1.25 รอบ"
            )
        }

        return AirDensityResult(
            relativeAirDensityPercent = rad,
            recommendedJetSize = targetJet,
            sizeDifference = diff,
            afrStateThai = afrState,
            tuningTipsThai = tips,
            airScrewTurnsThai = screw
        )
    }

    // Common Unit Conversions
    fun psiToBar(psi: Float): Float = psi * 0.0689476f
    fun barToPsi(bar: Float): Float = bar / 0.0689476f
    fun psiToKpa(psi: Float): Float = psi * 6.89476f

    fun nmToLbFt(nm: Float): Float = nm * 0.737562f
    fun lbFtToNm(lbFt: Float): Float = lbFt / 0.737562f
    fun nmToKgfM(nm: Float): Float = nm * 0.101972f

    fun ccToFlOz(cc: Float): Float = cc * 0.033814f
    fun flOzToCc(oz: Float): Float = oz / 0.033814f
    fun ccToLiters(cc: Float): Float = cc / 1000f
}
