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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.domain.MotorcycleCalculators

data class StandardTorqueItem(
    val partNameThai: String,
    val partNameEng: String,
    val specNm: String,
    val note: String
)

val standardTorqueList = listOf(
    StandardTorqueItem("น็อตถ่ายน้ำมันเครื่อง", "Oil Drain Bolt", "24 - 28 Nm", "ควรเปลี่ยนแหวนรองอลูมิเนียมทุกครั้ง"),
    StandardTorqueItem("หัวเทียน", "Spark Plug", "12 - 16 Nm", "ขันด้วยมือจนตึงแล้วกวดต่อ 1/2 รอบ"),
    StandardTorqueItem("น็อตฝาสูบ / เสาเสื้อ", "Cylinder Head Nuts", "28 - 32 Nm", "ขันทะแยงกากบาท 2-3 สเต็ป"),
    StandardTorqueItem("น็อตเพลาล้อหน้า", "Front Axle Nut", "50 - 65 Nm", "ขันแน่นตามสเปกป้องกันล้อส่าย"),
    StandardTorqueItem("น็อตเพลาล้อหลัง", "Rear Axle Nut", "80 - 100 Nm", "อย่าลืมเสียบสลักล็อก (Cotter pin)"),
    StandardTorqueItem("น็อตจานดิสก์เบรก", "Brake Disc Bolts", "20 - 24 Nm", "ควรหยอดน้ำยากันคลาย (Loctite Blue)"),
    StandardTorqueItem("น็อตคาลิปเปอร์เบรก", "Caliper Mounting", "30 - 35 Nm", "ตรวจเช็กความแน่นทุกรอบเปลี่ยนผ้าเบรก")
)

@Composable
fun UnitConverterTab() {
    var psiText by remember { mutableStateOf("29.0") }
    var nmText by remember { mutableStateOf("30.0") }
    var ccText by remember { mutableStateOf("800.0") }

    val psi = psiText.toFloatOrNull() ?: 29f
    val bar = MotorcycleCalculators.psiToBar(psi)
    val kpa = MotorcycleCalculators.psiToKpa(psi)

    val nm = nmText.toFloatOrNull() ?: 30f
    val lbFt = MotorcycleCalculators.nmToLbFt(nm)
    val kgfM = MotorcycleCalculators.nmToKgfM(nm)

    val cc = ccText.toFloatOrNull() ?: 800f
    val flOz = MotorcycleCalculators.ccToFlOz(cc)
    val liters = MotorcycleCalculators.ccToLiters(cc)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tire Pressure
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Speed, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "💨 แรงดันลมยาง (Tire Pressure)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedTextField(
                    value = psiText,
                    onValueChange = { psiText = it },
                    label = { Text("แรงดันลม (PSI ปอนด์/ตร.นิ้ว)") },
                    modifier = Modifier.fillMaxWidth().testTag("psi_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Bar (บาร์):", style = MaterialTheme.typography.labelSmall)
                            Text(String.format("%.2f Bar", bar), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("kPa (กิโลปาสคาล):", style = MaterialTheme.typography.labelSmall)
                            Text(String.format("%.0f kPa", kpa), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        }

        // Torque
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Build, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "🔧 แรงบิดขันน็อต (Torque Units)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedTextField(
                    value = nmText,
                    onValueChange = { nmText = it },
                    label = { Text("แรงบิดนิวตันเมตร (Nm)") },
                    modifier = Modifier.fillMaxWidth().testTag("nm_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("ปอนด์-ฟุต (lb-ft):", style = MaterialTheme.typography.labelSmall)
                            Text(String.format("%.2f lb-ft", lbFt), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("กิโลกรัม-เมตร (kgf-m):", style = MaterialTheme.typography.labelSmall)
                            Text(String.format("%.2f kgf-m", kgfM), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        }

        // Fluid Volume
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Opacity, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        text = "🧪 ปริมาตรของเหลว/น้ำมันเครื่อง (Fluid Volume)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedTextField(
                    value = ccText,
                    onValueChange = { ccText = it },
                    label = { Text("ปริมาตร ซีซี/มิลลิลิตร (cc / mL)") },
                    modifier = Modifier.fillMaxWidth().testTag("cc_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("ออนซ์ของเหลว (US Fl Oz):", style = MaterialTheme.typography.labelSmall)
                            Text(String.format("%.2f oz", flOz), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("ลิตร (Liters):", style = MaterialTheme.typography.labelSmall)
                            Text(String.format("%.3f L", liters), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        }

        // Standard Torque Table Reference
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "📋 ค่าแรงขันมาตรฐานมอเตอร์ไซค์ (Standard Torque Guide)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                standardTorqueList.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.partNameThai, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                            Text("${item.partNameEng} • ${item.note}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = item.specNm,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                }
            }
        }
    }
}
