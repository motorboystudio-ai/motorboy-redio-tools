package com.example.ui.calculator

import androidx.compose.foundation.background
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
import com.example.domain.ChainSlackResult
import com.example.domain.ChainSlackState
import com.example.domain.MotorcycleCalculators

data class BikeChainPreset(
    val name: String,
    val minSlack: Float,
    val maxSlack: Float,
    val description: String
)

val chainPresets = listOf(
    BikeChainPreset("Street / แม่บ้าน", 25f, 35f, "สตรีททั่วไป เวฟ ฟีโน่ แดช สปาร์ค"),
    BikeChainPreset("Sport / บิ๊กไบค์", 20f, 30f, "สปอร์ตไบค์ สวิงอาร์มสั้น ยุบตัวน้อย"),
    BikeChainPreset("Enduro / วิบาก", 45f, 55f, "รถวิบาก สวิงอาร์มยาว ต้องเผื่อยุบตัว"),
    BikeChainPreset("Cruiser / ทัวร์ริ่ง", 30f, 40f, "ครุยเซอร์ โช้คคู่ ท่องเที่ยว")
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChainCalculatorTab() {
    var selectedPresetIndex by remember { mutableIntStateOf(0) }
    var minSlackText by remember { mutableStateOf(chainPresets[0].minSlack.toString()) }
    var maxSlackText by remember { mutableStateOf(chainPresets[0].maxSlack.toString()) }
    var measuredSlackText by remember { mutableStateOf("30.0") }
    var frontSprocketText by remember { mutableStateOf("14") }
    var rearSprocketText by remember { mutableStateOf("36") }
    var adjusterPitchText by remember { mutableStateOf("1.25") }

    val minSlack = minSlackText.toFloatOrNull() ?: 25f
    val maxSlack = maxSlackText.toFloatOrNull() ?: 35f
    val measuredSlack = measuredSlackText.toFloatOrNull() ?: 30f
    val frontTeeth = frontSprocketText.toIntOrNull() ?: 14
    val rearTeeth = rearSprocketText.toIntOrNull() ?: 36
    val adjusterPitch = adjusterPitchText.toFloatOrNull() ?: 1.25f

    val slackResult: ChainSlackResult = remember(measuredSlack, minSlack, maxSlack, adjusterPitch) {
        MotorcycleCalculators.evaluateChainSlack(measuredSlack, minSlack, maxSlack, adjusterPitch)
    }

    val sprocketResult = remember(frontTeeth, rearTeeth) {
        MotorcycleCalculators.calculateSprocketRatio(frontTeeth, rearTeeth)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Presets Header Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "🏍️ สเปกประเภทรถ (Quick Presets)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "เลือกรุ่นรถเพื่อโหลดค่ามาตรฐานระยะหย่อนโซ่ (Chain Slack)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    chainPresets.forEachIndexed { index, preset ->
                        FilterChip(
                            selected = selectedPresetIndex == index,
                            onClick = {
                                selectedPresetIndex = index
                                minSlackText = preset.minSlack.toString()
                                maxSlackText = preset.maxSlack.toString()
                            },
                            label = { Text(preset.name) },
                            modifier = Modifier.testTag("chain_preset_$index")
                        )
                    }
                }
            }
        }

        // Measurement Inputs
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "📏 ตรวจวัดระยะหย่อนโซ่ (Midpoint Slack)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "วัดระยะการแกว่งตัวของโซ่กึ่งกลางด้านล่าง ระหว่างจุดล่างสุดถึงจุดยกสูงสุด (มม.)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = measuredSlackText,
                    onValueChange = { measuredSlackText = it },
                    label = { Text("ระยะหย่อนที่วัดได้ (มม. mm)") },
                    trailingIcon = { Text("mm", modifier = Modifier.padding(end = 12.dp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("measured_slack_input")
                )

                // Quick increment/decrement buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            val current = measuredSlackText.toFloatOrNull() ?: 25f
                            measuredSlackText = String.format("%.1f", (current - 2.5f).coerceAtLeast(0f))
                        },
                        modifier = Modifier.weight(1f).testTag("slack_minus_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Text("- 2.5 mm", color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                    Button(
                        onClick = {
                            val current = measuredSlackText.toFloatOrNull() ?: 25f
                            measuredSlackText = String.format("%.1f", current + 2.5f)
                        },
                        modifier = Modifier.weight(1f).testTag("slack_plus_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Text("+ 2.5 mm", color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = minSlackText,
                        onValueChange = { minSlackText = it },
                        label = { Text("สเปกต่ำสุด (Min)") },
                        modifier = Modifier.weight(1f).testTag("min_slack_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = maxSlackText,
                        onValueChange = { maxSlackText = it },
                        label = { Text("สเปกสูงสุด (Max)") },
                        modifier = Modifier.weight(1f).testTag("max_slack_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
            }
        }

        // Result Diagnostic Card
        val (badgeColor, statusIcon) = when (slackResult.state) {
            ChainSlackState.OPTIMAL -> Pair(Color(0xFF2E7D32), Icons.Default.CheckCircle)
            ChainSlackState.TOO_TIGHT -> Pair(Color(0xFFC62828), Icons.Default.Warning)
            ChainSlackState.TOO_LOOSE -> Pair(Color(0xFFEF6C00), Icons.Default.ErrorOutline)
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = badgeColor.copy(alpha = 0.12f)),
            modifier = Modifier.fillMaxWidth().testTag("chain_result_card")
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(statusIcon, contentDescription = null, tint = badgeColor, modifier = Modifier.size(28.dp))
                    Text(
                        text = slackResult.statusSummaryThai,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
                HorizontalDivider(color = badgeColor.copy(alpha = 0.3f))
                Text(
                    text = slackResult.detailAdviceThai,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp
                )
            }
        }

        // Sprocket Gear Ratio Calculator
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "⚙️ คำนวณอัตราทดสเตอร์ (Sprocket Ratio)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = frontSprocketText,
                        onValueChange = { frontSprocketText = it },
                        label = { Text("สเตอร์หน้า (T)") },
                        modifier = Modifier.weight(1f).testTag("front_sprocket_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = rearSprocketText,
                        onValueChange = { rearSprocketText = it },
                        label = { Text("สเตอร์หลัง (T)") },
                        modifier = Modifier.weight(1f).testTag("rear_sprocket_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("อัตราทดขั้นสุดท้าย (Final Ratio):", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "${String.format("%.2f", sprocketResult.ratio)} : 1",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = "ลักษณะ: ${sprocketResult.characterThai}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = sprocketResult.torqueDescriptionThai,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
