package com.example

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.calculator.ChainCalculatorTab
import com.example.ui.calculator.CompressionCalculatorTab
import com.example.ui.calculator.FuelAirTuningTab
import com.example.ui.calculator.UnitConverterTab

data class CalcTabItem(
    val title: String,
    val icon: ImageVector,
    val testTag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceCalculatorScreen(onBack: () -> Unit) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val tabs = listOf(
        CalcTabItem("ความตึงโซ่ & สเตอร์", Icons.Default.LinearScale, "tab_chain"),
        CalcTabItem("กำลังอัด & ซีซี", Icons.Default.Speed, "tab_compression"),
        CalcTabItem("จูนอากาศ-น้ำมัน", Icons.Default.Air, "tab_fuel_air"),
        CalcTabItem("แปลงหน่วย & สเปกขัน", Icons.Default.Calculate, "tab_unit_converter")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "MotorBoy Tech Calculator",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "คำนวณงานซ่อม & จูนเครื่องยนต์มอเตอร์ไซค์",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("calc_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 16.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("calc_tab_row"),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(tab.title, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (selectedTabIndex) {
                    0 -> ChainCalculatorTab()
                    1 -> CompressionCalculatorTab()
                    2 -> FuelAirTuningTab()
                    3 -> UnitConverterTab()
                }
            }
        }
    }
}
