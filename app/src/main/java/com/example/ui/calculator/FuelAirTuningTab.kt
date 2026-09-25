package com.example.ui.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.MotorcycleCalculators

data class SparkPlugCondition(
    val title: String,
    val colorBadge: Color,
    val status: String,
    val diagnostic: String,
    val action: String
)

val sparkPlugConditions = listOf(
    SparkPlugCondition(
        title = "ขาวซีด / มีคราบเกาะแห้ง (White / Blistered)",
        colorBadge = Color(0xFFEEEEEE),
        status = "ส่วนผสมบางเกินไป (Lean Condition ⚠️)",
        diagnostic = "อากาศมากกว่าน้ำมันมาก หรือหัวเทียนเบอร์ร้อนเกินไป เสี่ยงลูกสูบทะลุหรือวาล์วไหม้เนื่องจากความร้อนห้องเผาไหม้สูงจัด",
        action = "เพิ่มเบอร์นมหนูเมนขึ้น 1-2 เบอร์, ขันสกรูอากาศเข้าเล็กน้อย หรือเลื่อนกิ๊ฟล็อกเข็มเร่งลง 1 ช่อง (ยกเข็มขึ้น)"
    ),
    SparkPlugCondition(
        title = "สีน้ำตาลอ่อน / สีกะลา (Light Tan / Chocolate)",
        colorBadge = Color(0xFF8D6E63),
        status = "ส่วนผสมสมบูรณ์แบบ (Ideal AFR ~ 13.0 - 13.5:1 ✅)",
        diagnostic = "การเผาไหม้หมดจด สะอาด ให้กำลังอัดและแรงบิดสูงสุด ไม่กินน้ำมันเกินจำเป็น",
        action = "ค่าจูนและนมหนูปัจจุบันถูกต้องแล้ว ล็อคตำแหน่งและรักษาระดับไส้กรองอากาศให้สะอาด"
    ),
    SparkPlugCondition(
        title = "ดำแห้ง / มีเขม่าดำเกาะ (Dark Sooty / Dry Black)",
        colorBadge = Color(0xFF212121),
        status = "ส่วนผสมหนาเกินไป (Rich Condition ⚠️)",
        diagnostic = "น้ำมันมากกว่าอากาศ เผาไหม้ไม่หมด เปลืองน้ำมัน อัตราเร่งอืด มีกลิ่นน้ำมันออกปลายท่อ และสตาร์ตติดยากเมื่อเครื่องร้อน",
        action = "ลดเบอร์นมหนูเมนลง 1-2 เบอร์, ตรวจเช็กไส้กรองอากาศว่าตันหรือไม่ และขันสกรูอากาศออก 1/4 - 1/2 รอบ"
    ),
    SparkPlugCondition(
        title = "ดำเปียก / มีคราบน้ำมันเครื่อง (Wet Oily Black)",
        colorBadge = Color(0xFF37474F),
        status = "น้ำมันเครื่องรั่วเข้าห้องเผาไหม้ (Oil Fouling 🛑)",
        diagnostic = "เกิดจากซีลหมวกวาล์วเสื่อมสภาพ, แหวนลูกสูบหลวม หรือปลอกวาล์วสึกหรอ ทำให้ควันขาวออกท่อ",
        action = "ต้องเปิดฝาสูบเปลี่ยนซีลวาล์วหรือเปลี่ยนแหวนลูกสูบ ไม่สามารถแก้ได้ด้วยการจูนคาร์บูเรเตอร์"
    )
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FuelAirTuningTab() {
    var mainJetText by remember { mutableStateOf("115") }
    var tempText by remember { mutableStateOf("32") }
    var altitudeText by remember { mutableStateOf("20") }
    var targetAfrIndex by remember { mutableIntStateOf(0) }
    var selectedPlugIndex by remember { mutableIntStateOf(1) }

    val mainJet = mainJetText.toFloatOrNull() ?: 115f
    val tempC = tempText.toFloatOrNull() ?: 32f
    val altitudeM = altitudeText.toFloatOrNull() ?: 20f

    val airDensityResult = remember(mainJet, tempC, altitudeM) {
        MotorcycleCalculators.calculateJettingCorrection(mainJet, tempC, altitudeM)
    }

    val afrPresets = listOf(
        "12.5:1 (Max Power)" to "กำลังสูงสุด บิดหมดปลอกในสนามแข่ง",
        "13.2:1 (Response)" to "เร่งติดมือ ขี่สนุก ใช้งานทั่วไป",
        "14.7:1 (Stoichiometric)" to "ทฤษฎีสมบูรณ์ ประหยัดน้ำมันสูงสุด",
        "15.0:1 (Lean)" to "บาง ประหยัดแต่วิ่งทางไกลเครื่องร้อน"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Atmospheric & Jetting Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Air, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "💨 ความหนาแน่นอากาศ & นมหนู (Jetting & RAD)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "คำนวณการปรับเบอร์นมหนูตามอุณหภูมิและความสูง (Density Altitude Correction)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = mainJetText,
                        onValueChange = { mainJetText = it },
                        label = { Text("นมหนูเมนเดิม (#)") },
                        modifier = Modifier.weight(1f).testTag("main_jet_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = tempText,
                        onValueChange = { tempText = it },
                        label = { Text("อุณหภูมิ (°C)") },
                        trailingIcon = { Text("°C", modifier = Modifier.padding(end = 12.dp)) },
                        modifier = Modifier.weight(1f).testTag("temp_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }

                OutlinedTextField(
                    value = altitudeText,
                    onValueChange = { altitudeText = it },
                    label = { Text("ความสูงจากระดับน้ำทะเล (เมตร m)") },
                    trailingIcon = { Text("m", modifier = Modifier.padding(end = 12.dp)) },
                    modifier = Modifier.fillMaxWidth().testTag("altitude_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                // Quick Altitude chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "ภาคกลาง (10m)" to "10",
                        "ที่ราบสูง (200m)" to "200",
                        "ภูเขา/ดอย (800m)" to "800",
                        "ดอยสูง (1400m)" to "1400"
                    ).forEach { (label, alt) ->
                        OutlinedButton(
                            onClick = { altitudeText = alt },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 1)
                        }
                    }
                }
            }
        }

        // Air Density Result Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth().testTag("jetting_result_card")
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("ความหนาแน่นอากาศสัมพัทธ์ (RAD):", style = MaterialTheme.typography.bodySmall)
                        Text(
                            "${String.format("%.1f", airDensityResult.relativeAirDensityPercent)} %",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("นมหนูเมนที่แนะนำ:", style = MaterialTheme.typography.bodySmall)
                        Text(
                            "#${String.format("%.0f", airDensityResult.recommendedJetSize)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))

                Text("สถานะ: ${airDensityResult.afrStateThai}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text(airDensityResult.tuningTipsThai, style = MaterialTheme.typography.bodySmall)

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Column {
                            Text("การตั้งสกรูอากาศเดินเบา (Air Screw Baseline):", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Text(airDensityResult.airScrewTurnsThai, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        // Target AFR Selector
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "🎯 เป้าหมายอัตราส่วนผสม (Target AFR Guide)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                afrPresets.forEachIndexed { index, (ratio, desc) ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (targetAfrIndex == index) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { targetAfrIndex = index }
                            .padding(vertical = 2.dp)
                            .testTag("afr_target_$index")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(ratio, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            RadioButton(
                                selected = targetAfrIndex == index,
                                onClick = { targetAfrIndex = index }
                            )
                        }
                    }
                }
            }
        }

        // Spark Plug Diagnostic Guide
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "🔍 ตรวจเช็กสีเขี้ยวหัวเทียน (Spark Plug Diagnostic)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "กดเลือกสีเขี้ยวหัวเทียนที่ถอดจากรถ เพื่อดูผลการวิเคราะห์ส่วนผสมน้ำมัน-อากาศ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                sparkPlugConditions.forEachIndexed { index, plug ->
                    val isSelected = selectedPlugIndex == index
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPlugIndex = index }
                            .testTag("spark_plug_condition_$index")
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(plug.colorBadge)
                                        .border(1.dp, Color.Gray, CircleShape)
                                )
                                Text(plug.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            }
                            Text(plug.status, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)

                            if (isSelected) {
                                Text("สาเหตุ: ${plug.diagnostic}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                                Text("วิธีแก้ไข: ${plug.action}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }
                }
            }
        }
    }
}
