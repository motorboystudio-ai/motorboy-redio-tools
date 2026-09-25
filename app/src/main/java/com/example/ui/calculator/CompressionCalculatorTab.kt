package com.example.ui.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.MotorcycleCalculators

data class EnginePreset(
    val model: String,
    val bore: Float,
    val stroke: Float,
    val chamberCc: Float,
    val cylinders: Int = 1
)

val enginePresets = listOf(
    EnginePreset("Wave 110i", 50.0f, 55.6f, 12.0f),
    EnginePreset("Wave 125i", 52.4f, 57.9f, 13.5f),
    EnginePreset("Aerox / NMAX 155", 58.0f, 58.7f, 14.6f),
    EnginePreset("Click / PCX 160", 60.0f, 55.5f, 14.3f),
    EnginePreset("CBR 150R (DOHC)", 57.3f, 57.8f, 13.2f)
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CompressionCalculatorTab() {
    var selectedPresetIndex by remember { mutableIntStateOf(0) }
    var boreText by remember { mutableStateOf(enginePresets[0].bore.toString()) }
    var strokeText by remember { mutableStateOf(enginePresets[0].stroke.toString()) }
    var chamberText by remember { mutableStateOf(enginePresets[0].chamberCc.toString()) }
    var deckClearanceText by remember { mutableStateOf("0.5") }
    var gasketThicknessText by remember { mutableStateOf("0.25") }
    var cylindersCount by remember { mutableIntStateOf(1) }

    val bore = boreText.toFloatOrNull() ?: 50f
    val stroke = strokeText.toFloatOrNull() ?: 55.6f
    val chamber = chamberText.toFloatOrNull() ?: 12f
    val deck = deckClearanceText.toFloatOrNull() ?: 0.5f
    val gasket = gasketThicknessText.toFloatOrNull() ?: 0.25f

    val result = remember(bore, stroke, chamber, deck, gasket, cylindersCount) {
        MotorcycleCalculators.calculateCompression(bore, stroke, chamber, deck, gasket, cylindersCount)
    }

    val oversizeList = remember(bore, stroke, chamber, deck, gasket) {
        MotorcycleCalculators.calculateOversizeComparisons(bore, stroke, chamber, deck, gasket)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Presets
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "🏍️ เลือกรุ่นเครื่องยนต์มาตรฐาน (Engine Presets)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    enginePresets.forEachIndexed { index, preset ->
                        FilterChip(
                            selected = selectedPresetIndex == index,
                            onClick = {
                                selectedPresetIndex = index
                                boreText = preset.bore.toString()
                                strokeText = preset.stroke.toString()
                                chamberText = preset.chamberCc.toString()
                                cylindersCount = preset.cylinders
                            },
                            label = { Text(preset.model) },
                            modifier = Modifier.testTag("engine_preset_$index")
                        )
                    }
                }
            }
        }

        // Bore & Stroke Parameters Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "⚙️ ขนาดกระบอกสูบและระยะชัก (Bore & Stroke)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = boreText,
                        onValueChange = { boreText = it },
                        label = { Text("ขนาดลูกสูบ Bore (mm)") },
                        trailingIcon = { Text("mm", modifier = Modifier.padding(end = 12.dp)) },
                        modifier = Modifier.weight(1f).testTag("bore_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = strokeText,
                        onValueChange = { strokeText = it },
                        label = { Text("ระยะชัก Stroke (mm)") },
                        trailingIcon = { Text("mm", modifier = Modifier.padding(end = 12.dp)) },
                        modifier = Modifier.weight(1f).testTag("stroke_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }

                // Quick Oversize Increment
                Text("เพิ่มขนาดลูกสูบ (Oversize Quick Adjust):", style = MaterialTheme.typography.bodySmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(0.25f, 0.50f, 1.00f).forEach { step ->
                        OutlinedButton(
                            onClick = {
                                val current = boreText.toFloatOrNull() ?: 50f
                                boreText = String.format("%.2f", current + step)
                            },
                            modifier = Modifier.weight(1f).testTag("oversize_plus_${step}_btn"),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            Text("+$step", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Button(
                        onClick = {
                            val preset = enginePresets.getOrNull(selectedPresetIndex) ?: enginePresets[0]
                            boreText = preset.bore.toString()
                        },
                        modifier = Modifier.weight(1f).testTag("oversize_reset_btn"),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                    ) {
                        Text("STD", color = MaterialTheme.colorScheme.onTertiaryContainer, style = MaterialTheme.typography.labelSmall)
                    }
                }

                HorizontalDivider()

                // Combustion Chamber Volume (Vc) and clearances
                Text(
                    text = "ฝาสูบและระยะเคลียแรนซ์ (Chamber & Clearances)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                OutlinedTextField(
                    value = chamberText,
                    onValueChange = { chamberText = it },
                    label = { Text("ปริมาตรเบ้าฝาสูบ Vc (cc)") },
                    trailingIcon = { Text("cc", modifier = Modifier.padding(end = 12.dp)) },
                    modifier = Modifier.fillMaxWidth().testTag("chamber_cc_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = deckClearanceText,
                        onValueChange = { deckClearanceText = it },
                        label = { Text("ลูกสูบต่ำกว่าปากเสื้อ (mm)") },
                        modifier = Modifier.weight(1f).testTag("deck_clearance_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = gasketThicknessText,
                        onValueChange = { gasketThicknessText = it },
                        label = { Text("ความหนาปะเก็นฝา (mm)") },
                        modifier = Modifier.weight(1f).testTag("gasket_thickness_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
            }
        }

        // Calculation Results Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth().testTag("compression_result_card")
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("ปริมาตรกระบอกสูบสุทธิ (Displacement):", style = MaterialTheme.typography.bodySmall)
                        Text(
                            "${String.format("%.1f", result.totalDisplacementCc)} cc",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("อัตราส่วนกำลังอัด (CR):", style = MaterialTheme.typography.bodySmall)
                        Text(
                            "${String.format("%.2f", result.compressionRatio)} : 1",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))

                // Breakdown details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("ความจุห้องเผาไหม้รวม:", style = MaterialTheme.typography.bodySmall)
                    Text("${String.format("%.2f", result.totalCombustionChamberCc)} cc", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("ปริมาตรรวมเมื่อลูกสูบอยู่ล่างสุด (BDC):", style = MaterialTheme.typography.bodySmall)
                    Text("${String.format("%.2f", result.totalCylinderVolumeCc)} cc", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.LocalGasStation, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Text("คำแนะนำน้ำมันเชื้อเพลิง:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Text(result.fuelOctaneThai, style = MaterialTheme.typography.bodyMedium)

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                            Text("หัวเทียนที่เหมาะสม:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Text(result.sparkPlugHeatRangeThai, style = MaterialTheme.typography.bodyMedium)

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(18.dp))
                            Text("ระดับความปลอดภัย:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Text(result.engineSafetyLevelThai, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        // Oversize Comparison Table
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "📊 ตารางเปรียบเทียบการคว้านไซซ์ (Oversize Comparison)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("ไซซ์ลูกสูบ", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Text("Bore (mm)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Text("ความจุ (cc)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Text("กำลังอัด CR", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }

                oversizeList.forEach { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(row.name, style = MaterialTheme.typography.bodySmall)
                        Text(String.format("%.2f", row.boreMm), style = MaterialTheme.typography.bodySmall)
                        Text("${String.format("%.1f", row.displacementCc)} cc", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        Text("${String.format("%.2f", row.compressionRatio)}:1", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
                }
            }
        }
    }
}
