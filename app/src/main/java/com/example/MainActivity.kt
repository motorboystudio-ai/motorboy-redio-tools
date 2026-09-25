package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RadioManager
import com.example.data.TvManager
import com.example.data.repository.DtcRepositoryImpl
import com.example.ui.CameraScannerScreen
import com.example.ui.MechanicChatScreen
import com.example.ui.ServiceReminderScreen
import com.example.ui.theme.MyApplicationTheme

// Global App Settings
object AppSettings {
    var isThai by mutableStateOf(true)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RadioManager.initContext(this)
        TvManager.initContext(this)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true) {
                MotorBoyTechApp()
            }
        }
    }
}

@Composable
fun MiniPlayerBar(
    onOpenScanner: () -> Unit
) {
    val currentStation = RadioManager.currentStation
    val isPlaying = RadioManager.isPlaying
    val volume = RadioManager.volume

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clickable { onOpenScanner() }
            .testTag("mini_player_bar"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Radio,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        currentStation.name,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "${currentStation.frequency} • ${currentStation.category}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = {
                        val current = RadioManager.currentStation.frequencyNum
                        val newFreq = (current + 0.5f).let { if (it > 108f) 87.5f else it }
                        RadioManager.tuneToFrequency(newFreq)
                    }
                ) {
                    Icon(
                        Icons.Default.SkipNext,
                        contentDescription = "Next Station",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FilledIconButton(
                    onClick = { RadioManager.togglePlayPause() },
                    modifier = Modifier
                        .size(42.dp)
                        .testTag("mini_player_play_btn"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MotorBoyTechApp() {
    var currentScreen by remember { mutableStateOf("dashboard") }

    Scaffold(
        bottomBar = {
            if (currentScreen != "radio_scanner" && currentScreen != "online_tv") {
                MiniPlayerBar(
                    onOpenScanner = { currentScreen = "radio_scanner" }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (currentScreen) {
                "dashboard" -> com.example.ui.DashboardScreen(onFeatureClick = { feature ->
                    when (feature) {
                        "Gemini AI Mechanic" -> currentScreen = "gemini_chat"
                        "Service Reminder" -> currentScreen = "service_reminder"
                        "Motorcycle Manuals" -> currentScreen = "manual_viewer"
                        "Thai Online TV" -> currentScreen = "online_tv"
                        "Live Online TV" -> currentScreen = "online_tv"
                        "Maintenance Calculator" -> currentScreen = "calculator"
                        "Calculation Tools" -> currentScreen = "calculator"
                        "Thai Radio Scanner" -> currentScreen = "radio_scanner"
                        "Camera Scan DTC" -> currentScreen = "camera_scanner"
                        "Repair Guides" -> currentScreen = "paint_guide"
                        "Paint Mixing & Tools" -> currentScreen = "paint_mixing"
                        "CDI/ECU Pinout" -> currentScreen = "pinout_library"
                        "Reference Library" -> currentScreen = "pinout_library"
                        else -> {
                            currentScreen = "manual_viewer"
                        }
                    }
                })
                "gemini_chat" -> MechanicChatScreen(
                    onNavigateTo = { targetRoute -> currentScreen = targetRoute },
                    onBack = { currentScreen = "dashboard" }
                )
                "service_reminder" -> ServiceReminderScreen(onBack = { currentScreen = "dashboard" })
                "manual_viewer" -> com.example.ui.ManualViewerScreen(onBack = { currentScreen = "dashboard" })
                "online_tv" -> ThaiOnlineTvScreen(onBack = { currentScreen = "dashboard" })
                "calculator" -> MaintenanceCalculatorScreen(onBack = { currentScreen = "dashboard" })
                "radio_scanner" -> ThaiRadioScannerScreen(onBack = { currentScreen = "dashboard" })
                "camera_scanner" -> {
                    val context = LocalContext.current
                    CameraScannerScreen(onBack = { currentScreen = "dashboard" }, dtcRepository = DtcRepositoryImpl(context))
                }
                "paint_guide" -> PaintGuideScreen(onBack = { currentScreen = "dashboard" })
                "paint_mixing" -> PaintMaintenanceMixingScreen(onBack = { currentScreen = "dashboard" })
                "pinout_library" -> LibraryScreen(onBack = { currentScreen = "dashboard" })
            }
        }
    }
}
