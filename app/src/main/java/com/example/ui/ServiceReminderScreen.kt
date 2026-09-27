package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.ServiceReminderPreferences
import com.example.domain.OilChangeReminder
import com.example.domain.ServiceReminderNotificationHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceReminderScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val reminderPrefs = remember { ServiceReminderPreferences.getInstance(context) }
    val reminder by reminderPrefs.reminderFlow.collectAsState()

    var showEditDialog by rememberSaveable { mutableStateOf(false) }
    var showQuickMileageDialog by rememberSaveable { mutableStateOf(false) }

    // Notification Permission Launcher (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            ServiceReminderNotificationHelper.sendReminderNotification(context, reminder)
            Toast.makeText(context, "ส่งการแจ้งเตือนสถานะน้ำมันเครื่องแล้ว", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "ไม่ได้รับอนุญาตการส่ง Notification", Toast.LENGTH_SHORT).show()
        }
    }

    fun triggerNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED
            ) {
                ServiceReminderNotificationHelper.sendReminderNotification(context, reminder)
                Toast.makeText(context, "ส่งการแจ้งเตือน Push Notification เรียบร้อย", Toast.LENGTH_SHORT).show()
            } else {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            ServiceReminderNotificationHelper.sendReminderNotification(context, reminder)
            Toast.makeText(context, "ส่งการแจ้งเตือนเรียบร้อย", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ระบบแจ้งเตือนเปลี่ยนน้ำมันเครื่อง",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Oil Change & Service Reminder",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("reminder_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { triggerNotification() },
                        modifier = Modifier.testTag("btn_trigger_notification")
                    ) {
                        Icon(
                            Icons.Default.NotificationsActive,
                            contentDescription = "Send Notification",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Visual Status Banner (Primary Indicator)
            ServiceStatusBanner(
                reminder = reminder,
                onUpdateMileage = { showQuickMileageDialog = true },
                onResetOilChange = { showEditDialog = true }
            )

            // Current Stats Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📊 ข้อมูลระยะทางและการบริการ",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedButton(
                            onClick = { showEditDialog = true },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("btn_edit_reminder")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("แก้ไขข้อมูล", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("รุ่นรถจักรยานยนต์", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(reminder.bikeModel, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("วันที่เปลี่ยนล่าสุด", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(reminder.lastChangeDate, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("ไมล์เปลี่ยนล่าสุด", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${reminder.lastOilChangeMileage} กม.", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("ไมล์ปัจจุบัน", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${reminder.currentMileage} กม.", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("รอบกำหนดเปลี่ยน", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("ทุกๆ ${reminder.intervalKm} กม.", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("สเปกน้ำมันเครื่อง", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(reminder.oilBrandGrade, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    if (reminder.notes.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Text("บันทึก: ${reminder.notes}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            // Quick Actions & Notification Test
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "🔔 การแจ้งเตือน & การจัดการรวดเร็ว",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = { triggerNotification() },
                        modifier = Modifier.fillMaxWidth().testTag("btn_test_notification"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ทดสอบส่ง Push Notification ตอนนี้")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showQuickMileageDialog = true },
                            modifier = Modifier.weight(1f).testTag("btn_quick_update_mileage"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("อัปเดตไมล์")
                        }

                        Button(
                            onClick = {
                                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                val today = sdf.format(Date())
                                reminderPrefs.recordNewOilChange(
                                    mileage = reminder.currentMileage,
                                    date = today
                                )
                                Toast.makeText(context, "บันทึกการเปลี่ยนถ่ายน้ำมันใหม่เรียบร้อยแล้ว", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.weight(1f).testTag("btn_record_oil_done"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("เพิ่งเปลี่ยนมา")
                        }
                    }
                }
            }

            // Standard Interval Recommendations Table
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "📖 คำแนะนำรอบระยะเปลี่ยนถ่ายน้ำมันเครื่อง",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "• มอเตอร์ไซค์ครอบครัว (Wave, Super Cub): ทุก 2,000 - 3,000 กม. (หรือ 6 เดือน)\n" +
                               "• มอเตอร์ไซค์ออโตเมติก (Scoopy, Click, PCX): ทุก 2,000 - 3,000 กม. (และเปลี่ยนน้ำมันเฟืองท้ายทุก 6,000 กม.)\n" +
                               "• มอเตอร์ไซค์สายสปอร์ต / บิ๊กไบค์: ทุก 3,000 - 5,000 กม. (น้ำมันเครื่องสังเคราะห์ 100% Fully Synthetic)\n" +
                               "• รถใช้งานหนัก ขับขี่ส่งของ บิดแช่รอบสูง: แนะนำเปลี่ยนทุก 1,500 - 2,000 กม.",
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Edit/Record Oil Change Dialog
    if (showEditDialog) {
        var inputBikeModel by rememberSaveable { mutableStateOf(reminder.bikeModel) }
        var inputLastMileage by rememberSaveable { mutableStateOf(reminder.lastOilChangeMileage.toString()) }
        var inputCurrentMileage by rememberSaveable { mutableStateOf(reminder.currentMileage.toString()) }
        var inputInterval by rememberSaveable { mutableStateOf(reminder.intervalKm.toString()) }
        var inputOilGrade by rememberSaveable { mutableStateOf(reminder.oilBrandGrade) }
        var inputDate by rememberSaveable { mutableStateOf(reminder.lastChangeDate) }
        var inputNotes by rememberSaveable { mutableStateOf(reminder.notes) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("บันทึกการเปลี่ยนถ่ายน้ำมันเครื่อง") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = inputBikeModel,
                        onValueChange = { inputBikeModel = it },
                        label = { Text("รุ่นรถมอเตอร์ไซค์") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = inputLastMileage,
                        onValueChange = { inputLastMileage = it.filter { char -> char.isDigit() } },
                        label = { Text("เลขไมล์ตอนเปลี่ยนน้ำมันล่าสุด (กม.)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = inputCurrentMileage,
                        onValueChange = { inputCurrentMileage = it.filter { char -> char.isDigit() } },
                        label = { Text("เลขไมล์ปัจจุบัน (กม.)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = inputInterval,
                        onValueChange = { inputInterval = it.filter { char -> char.isDigit() } },
                        label = { Text("รอบระยะกำหนดเปลี่ยน (กม. เช่น 2000)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = inputOilGrade,
                        onValueChange = { inputOilGrade = it },
                        label = { Text("เกรดยี่ห้อน้ำมันเครื่อง (เช่น 10W-30 JASO MA)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = inputDate,
                        onValueChange = { inputDate = it },
                        label = { Text("วันที่เปลี่ยน (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = inputNotes,
                        onValueChange = { inputNotes = it },
                        label = { Text("หมายเหตุเพิ่มเติม (ถ้ามี)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val lastM = inputLastMileage.toIntOrNull() ?: reminder.lastOilChangeMileage
                        val currM = inputCurrentMileage.toIntOrNull() ?: reminder.currentMileage
                        val interval = inputInterval.toIntOrNull() ?: reminder.intervalKm

                        val updated = reminder.copy(
                            bikeModel = inputBikeModel.ifBlank { "มอเตอร์ไซค์" },
                            lastOilChangeMileage = lastM,
                            currentMileage = currM,
                            intervalKm = interval,
                            oilBrandGrade = inputOilGrade,
                            lastChangeDate = inputDate,
                            notes = inputNotes
                        )
                        reminderPrefs.saveReminder(updated)
                        showEditDialog = false
                        Toast.makeText(context, "บันทึกข้อมูลเรียบร้อย", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("บันทึก")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("ยกเลิก")
                }
            }
        )
    }

    // Quick Mileage Update Dialog
    if (showQuickMileageDialog) {
        var inputMileage by rememberSaveable { mutableStateOf(reminder.currentMileage.toString()) }

        AlertDialog(
            onDismissRequest = { showQuickMileageDialog = false },
            title = { Text("อัปเดตเลขไมล์ปัจจุบัน") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("กรอกเลขไมล์หน้าปัดรถล่าสุดเพื่อตรวจสอบระยะการใช้งาน:")
                    OutlinedTextField(
                        value = inputMileage,
                        onValueChange = { inputMileage = it.filter { char -> char.isDigit() } },
                        label = { Text("เลขไมล์ปัจจุบัน (กม.)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newMileage = inputMileage.toIntOrNull() ?: reminder.currentMileage
                        reminderPrefs.updateCurrentMileage(newMileage)
                        showQuickMileageDialog = false
                        Toast.makeText(context, "อัปเดตไมล์เป็น $newMileage กม. เรียบร้อย", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("อัปเดต")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuickMileageDialog = false }) {
                    Text("ยกเลิก")
                }
            }
        )
    }
}

@Composable
fun ServiceStatusBanner(
    reminder: OilChangeReminder,
    onUpdateMileage: () -> Unit,
    onResetOilChange: () -> Unit
) {
    val isDue = reminder.isDue
    val isNearDue = reminder.isNearDue

    val bannerColor = when {
        isDue -> MaterialTheme.colorScheme.errorContainer
        isNearDue -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.primaryContainer
    }

    val contentColor = when {
        isDue -> MaterialTheme.colorScheme.onErrorContainer
        isNearDue -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onPrimaryContainer
    }

    val icon = when {
        isDue -> Icons.Default.Warning
        isNearDue -> Icons.Default.NotificationImportant
        else -> Icons.Default.CheckCircle
    }

    val statusTitle = when {
        isDue -> "⚠️ ถึงกำหนดเปลี่ยนถ่ายน้ำมันเครื่องแล้ว!"
        isNearDue -> "🔔 ใกล้ถึงกำหนดเปลี่ยนถ่ายน้ำมันเครื่อง"
        else -> "✅ สภาพน้ำมันเครื่องอยู่ในเกณฑ์ปกติ"
    }

    val statusSubtitle = when {
        isDue -> {
            val overdue = (reminder.currentMileage - reminder.nextDueMileage).coerceAtLeast(0)
            if (overdue > 0) "เกินกำหนดมาแล้ว $overdue กม. (กำหนดที่ ${reminder.nextDueMileage} กม.)"
            else "ถึงระยะกำหนดพอดีที่ ${reminder.nextDueMileage} กม."
        }
        isNearDue -> "เหลือระยะทางอีกเพียง ${reminder.kmRemaining} กม. จะถึงรอบ ${reminder.nextDueMileage} กม."
        else -> "วิ่งไปแล้ว ${reminder.kmDrivenSinceLastChange} กม. เหลืออีก ${reminder.kmRemaining} กม. (กำหนดถัดไป ${reminder.nextDueMileage} กม.)"
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = bannerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier.fillMaxWidth().testTag("service_status_banner")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = contentColor.copy(alpha = 0.2f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(26.dp))
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = statusTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                    Text(
                        text = statusSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = contentColor.copy(alpha = 0.85f)
                    )
                }
            }

            // Visual Progress Gauge
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "ไมล์เดิม: ${reminder.lastOilChangeMileage} กม.",
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.75f)
                    )
                    Text(
                        text = "กำหนดถัดไป: ${reminder.nextDueMileage} กม.",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                }

                LinearProgressIndicator(
                    progress = { reminder.progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(CircleShape),
                    color = if (isDue) MaterialTheme.colorScheme.error else if (isNearDue) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                    trackColor = contentColor.copy(alpha = 0.15f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "ใช้ไปแล้ว ${(reminder.progressFraction * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = contentColor
                    )
                    Text(
                        text = "ปัจจุบัน: ${reminder.currentMileage} กม.",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                }
            }

            // Banner Quick Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = onUpdateMileage,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("อัปเดตไมล์ล่าสุด")
                }

                Button(
                    onClick = onResetOilChange,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("บันทึกการเปลี่ยน")
                }
            }
        }
    }
}
