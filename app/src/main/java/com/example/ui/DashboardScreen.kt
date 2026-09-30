package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.R
import com.example.data.RadioManager
import com.example.data.ServiceReminderPreferences
import com.example.domain.OilChangeReminder

@Composable
fun DashboardScreen(
    onFeatureClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val reminderPrefs = remember { ServiceReminderPreferences.getInstance(context) }
    val reminder by reminderPrefs.reminderFlow.collectAsState()

    val currentStation = RadioManager.currentStation
    val isPlaying = RadioManager.isPlaying
    val isBookmarked = RadioManager.isBookmarked(currentStation.id)

    val features = listOf(
        FeatureItem("Product Price List", "รายการสินค้า & ราคาช่าง น้ำมัน อะไหล่ ค่าแรง", Icons.Default.Inventory),
        FeatureItem("Movie Online", "ดูหนังออนไลน์ ซีรีส์ HD จาก Movie911HD", Icons.Default.Movie),
        FeatureItem("Motorcycle Manuals", "คู่มือซ่อมบำรุงตามระยะ ซูมไดอะแกรม & ทีละสเต็ป", Icons.AutoMirrored.Filled.MenuBook),
        FeatureItem("Maintenance Calculator", "ความตึงโซ่ กำลังอัด จูนอากาศ-น้ำมัน & แปลงหน่วย", Icons.Default.Calculate),
        FeatureItem("Thai Online TV", "ทีวีออนไลน์สด IPTV-Org ทั่วไทย 24 ชม.", Icons.Default.LiveTv),
        FeatureItem("Camera Scan DTC", "สแกนรหัสข้อผิดพลาดรถ", Icons.Default.CameraAlt),
        FeatureItem("Repair Guides", "คู่มือซ่อม & ทำสีมอเตอร์ไซค์ 2K", Icons.Default.FormatPaint),
        FeatureItem("Paint Mixing & Tools", "เทียบสี ซ่อมผิว ผสม 2K & เครื่องมือ", Icons.Default.ColorLens),
        FeatureItem("CDI/ECU Pinout", "Wiring diagrams & ไดอะแกรมกล่อง", Icons.Default.Bolt),
        FeatureItem("Cloud SQL Database", "เชื่อมต่อฐานข้อมูล SQL Cloud (MySQL / PostgreSQL)", Icons.Default.Cloud)
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            
            // Professional Workshop Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "MotorBoy Pro Workshop",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    "☁️ Cloud SQL Active",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer
                            ) {
                                Text(
                                    "🤖 ช่างบอย AI พร้อมใช้งาน",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Build,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp)
             ) {

            // Service Reminder Visual Status Card
            item(span = { GridItemSpan(2) }) {
                val isDue = reminder.isDue
                val isNearDue = reminder.isNearDue

                val cardBgColor = when {
                    isDue -> MaterialTheme.colorScheme.errorContainer
                    isNearDue -> MaterialTheme.colorScheme.tertiaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
                val cardContentColor = when {
                    isDue -> MaterialTheme.colorScheme.onErrorContainer
                    isNearDue -> MaterialTheme.colorScheme.onTertiaryContainer
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onFeatureClick("Service Reminder") }
                        .testTag("dashboard_oil_reminder_banner"),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBgColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isDue) MaterialTheme.colorScheme.error else if (isNearDue) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            if (isDue) Icons.Default.Warning else if (isNearDue) Icons.Default.NotificationsActive else Icons.Default.OilBarrel,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = if (isDue) "เตือน: ถึงรอบถ่ายน้ำมันเครื่องแล้ว!" else if (isNearDue) "ใกล้ถึงรอบถ่ายน้ำมันเครื่อง" else "ระบบแจ้งเตือนเปลี่ยนน้ำมันเครื่อง",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = cardContentColor
                                    )
                                    Text(
                                        text = "${reminder.bikeModel} • ไมล์ล่าสุด: ${reminder.lastOilChangeMileage} กม.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = cardContentColor.copy(alpha = 0.85f)
                                    )
                                }
                            }

                            FilledTonalButton(
                                onClick = { onFeatureClick("Service Reminder") },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("ดูรายละเอียด", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        LinearProgressIndicator(
                            progress = { reminder.progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = if (isDue) MaterialTheme.colorScheme.error else if (isNearDue) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                            trackColor = cardContentColor.copy(alpha = 0.15f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "ปัจจุบัน: ${reminder.currentMileage} กม.",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = cardContentColor
                            )
                            Text(
                                text = if (isDue) "⚠️ เกินกำหนดแล้ว ${(reminder.currentMileage - reminder.nextDueMileage).coerceAtLeast(0)} กม."
                                       else "เหลืออีก ${reminder.kmRemaining} กม. (กำหนด ${reminder.nextDueMileage} กม.)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isDue) MaterialTheme.colorScheme.error else cardContentColor
                            )
                        }
                    }
                }
            }
            item(span = { GridItemSpan(2) }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onFeatureClick("Thai Radio Scanner") }
                        .testTag("dashboard_radio_banner"),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        MaterialTheme.colorScheme.surfaceVariant
                                    )
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Radio,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        "วิทยุออนไลน์ประเทศไทย",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(if (isPlaying) Color.Green else Color.Gray)
                                        )
                                        Text(
                                            if (isPlaying) "ON-AIR: ${currentStation.frequency}" else "พร้อมสตรีมมิ่ง 24 ชม.",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFF4CAF50).copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                "🔊 ไม่ต้องเสียบหูฟัง",
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFF4CAF50),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp
                                            )
                                        }
                                    }
                                }
                            }

                            // Action buttons
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { RadioManager.toggleBookmark(currentStation.id) },
                                    modifier = Modifier.testTag("banner_bookmark_btn")
                                ) {
                                    Icon(
                                        if (isBookmarked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                        contentDescription = "Bookmark",
                                        tint = if (isBookmarked) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        RadioManager.scanFrequencies()
                                        onFeatureClick("Thai Radio Scanner")
                                    },
                                    modifier = Modifier.testTag("banner_scan_btn")
                                ) {
                                    Icon(
                                        Icons.Default.Radar,
                                        contentDescription = "Scan",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Currently playing info card inside banner
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        currentStation.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    Text(
                                        "${currentStation.category} • ${currentStation.province}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilledIconButton(
                                        onClick = { RadioManager.togglePlayPause() },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = "Toggle Play",
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item(span = { GridItemSpan(2) }) {
                Text(
                    "Core Features",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(features) { feature ->
                FeatureCard(feature) {
                    onFeatureClick(feature.title)
                }
            }
        }
    }

        ExtendedFloatingActionButton(
            onClick = { onFeatureClick("Gemini AI Mechanic") },
            icon = { Icon(Icons.Default.SmartToy, contentDescription = "AI") },
            text = { Text("ช่างบอย AI", fontWeight = FontWeight.Bold) },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp)
                .testTag("fab_gemini_ai")
        )
    }
}

@Composable
fun FeatureCard(
    feature: FeatureItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .testTag("feature_card_${feature.title.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = feature.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = feature.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = feature.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
    }
}

data class FeatureItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector
)
