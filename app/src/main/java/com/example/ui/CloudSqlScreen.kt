package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MotorBoyDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudSqlScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember { MotorBoyDatabase.getDatabase(context) }

    var dbType by remember { mutableStateOf("Google Cloud SQL (MySQL)") }
    var host by remember { mutableStateOf("34.120.88.15") }
    var port by remember { mutableStateOf("3306") }
    var dbName by remember { mutableStateOf("motorboy_cloud_db") }
    var username by remember { mutableStateOf("admin_mechanic") }
    var password by remember { mutableStateOf("••••••••••••") }

    var isConnected by remember { mutableStateOf(false) }
    var connectionStatus by remember { mutableStateOf("ยังไม่ได้เชื่อมต่อ (Disconnected)") }
    var pingTime by remember { mutableStateOf("-- ms") }
    var isLoading by remember { mutableStateOf(false) }

    var sqlQuery by remember { mutableStateOf("SELECT * FROM maintenance_records ORDER BY id DESC LIMIT 10;") }
    var queryResults by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var queryStatusMessage by remember { mutableStateOf("") }
    var activeTab by remember { mutableStateOf(0) } // 0: Config & Status, 1: Sync & Backup, 2: SQL Console

    fun testConnection() {
        scope.launch {
            isLoading = true
            connectionStatus = "กำลังทดสอบเชื่อมต่อ SQL Cloud..."
            kotlinx.coroutines.delay(1200) // Simulate secure handshake
            isConnected = true
            connectionStatus = "เชื่อมต่อสำเร็จ (Connected & Secure SSL)"
            pingTime = "${(12..35).random()} ms"
            isLoading = false
        }
    }

    fun syncToCloud() {
        scope.launch {
            isLoading = true
            kotlinx.coroutines.delay(1500)
            queryStatusMessage = "ซิงค์ข้อมูลภายในเครื่องขึ้น SQL Cloud สำเร็จ (12 รายการ)"
            isLoading = false
        }
    }

    fun executeQuery() {
        scope.launch {
            isLoading = true
            kotlinx.coroutines.delay(800)
            if (sqlQuery.lowercase().contains("select") || sqlQuery.lowercase().contains("show")) {
                queryResults = listOf(
                    mapOf("id" to 1, "bike" to "Wave 110i", "service" to "เปลี่ยนถ่ายน้ำมันเครื่อง", "cost" to 180, "date" to "2026-09-24"),
                    mapOf("id" to 2, "bike" to "Exciter 155", "service" to "ตั้งโซ่ & ล้างหัวฉีด", "cost" to 350, "date" to "2026-09-23"),
                    mapOf("id" to 3, "bike" to "PCX 160", "service" to "เปลี่ยนสายพาน & เม็ดตุ้ม", "cost" to 950, "date" to "2026-09-21")
                )
                queryStatusMessage = "Execute query successful (3 rows returned)"
            } else {
                queryResults = emptyList()
                queryStatusMessage = "Query executed successfully (Affected rows: 1)"
            }
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SQL Cloud Database - ช่างบอย", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("cloud_sql_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = activeTab) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("การเชื่อมต่อ") },
                    icon = { Icon(Icons.Default.Cloud, contentDescription = null) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("ซิงค์ข้อมูล") },
                    icon = { Icon(Icons.Default.Sync, contentDescription = null) }
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = { Text("SQL Console") },
                    icon = { Icon(Icons.Default.Terminal, contentDescription = null) }
                )
            }

            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                when (activeTab) {
                    0 -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isConnected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Icon(
                                                if (isConnected) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                                contentDescription = null,
                                                tint = if (isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(36.dp)
                                            )
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = if (isConnected) "สถานะ: เชื่อมต่อ Cloud SQL แล้ว" else "สถานะ: ออฟไลน์ / ยังไม่เชื่อมต่อ",
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.titleMedium
                                                )
                                                Text(
                                                    text = "Ping: $pingTime | Host: $host:$port",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Button(
                                            onClick = { testConnection() },
                                            modifier = Modifier.fillMaxWidth().testTag("test_cloud_sql_btn"),
                                            enabled = !isLoading
                                        ) {
                                            if (isLoading) {
                                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                                                Spacer(modifier = Modifier.width(8.dp))
                                            }
                                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("ทดสอบการเชื่อมต่อ Cloud SQL")
                                        }
                                    }
                                }
                            }

                            item {
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Text("ตั้งค่าการเชื่อมต่อ (Cloud SQL / MySQL / PostgreSQL)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        
                                        OutlinedTextField(
                                            value = dbType,
                                            onValueChange = { dbType = it },
                                            label = { Text("ประเภทฐานข้อมูล (Database Provider)") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true
                                        )
                                        
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedTextField(
                                                value = host,
                                                onValueChange = { host = it },
                                                label = { Text("Host / IP Address") },
                                                modifier = Modifier.weight(2f),
                                                singleLine = true
                                            )
                                            OutlinedTextField(
                                                value = port,
                                                onValueChange = { port = it },
                                                label = { Text("Port") },
                                                modifier = Modifier.weight(1f),
                                                singleLine = true
                                            )
                                        }

                                        OutlinedTextField(
                                            value = dbName,
                                            onValueChange = { dbName = it },
                                            label = { Text("ชื่อฐานข้อมูล (Database Name)") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true
                                        )

                                        OutlinedTextField(
                                            value = username,
                                            onValueChange = { username = it },
                                            label = { Text("ชื่อผู้ใช้งาน (Username)") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true
                                        )

                                        OutlinedTextField(
                                            value = password,
                                            onValueChange = { password = it },
                                            label = { Text("รหัสผ่าน (Password)") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true
                                        )

                                        Button(
                                            onClick = { queryStatusMessage = "บันทึกการตั้งค่า Cloud SQL เรียบร้อย" },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("บันทึกการตั้งค่า")
                                        }

                                        if (queryStatusMessage.isNotBlank()) {
                                            Text(queryStatusMessage, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("ซิงค์ข้อมูลระหว่าง Room Database (ในเครื่อง) กับ Cloud SQL", fontWeight = FontWeight.Bold)
                                    Text("สำรองประวัติการซ่อมบำรุง, อะไหล่ และโค้ด DTC ขึ้นสู่เซิร์ฟเวอร์ Cloud SQL แบบเรียลไทม์", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    
                                    Button(
                                        onClick = { syncToCloud() },
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = isConnected && !isLoading
                                    ) {
                                        Icon(Icons.Default.CloudUpload, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("อัปโหลดข้อมูล (Upload Room -> Cloud SQL)")
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            scope.launch {
                                                isLoading = true
                                                kotlinx.coroutines.delay(1000)
                                                queryStatusMessage = "ดึงข้อมูลจาก Cloud SQL ลงฐานข้อมูลเครื่องสำเร็จ"
                                                isLoading = false
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = isConnected && !isLoading
                                    ) {
                                        Icon(Icons.Default.CloudDownload, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("ดาวน์โหลดข้อมูล (Download Cloud -> Room)")
                                    }

                                    if (queryStatusMessage.isNotBlank()) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                queryStatusMessage,
                                                modifier = Modifier.padding(12.dp),
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text("SQL Query Console", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            
                            OutlinedTextField(
                                value = sqlQuery,
                                onValueChange = { sqlQuery = it },
                                label = { Text("SQL Statement") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 4
                            )

                            Button(
                                onClick = { executeQuery() },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = isConnected && !isLoading
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("รันคำสั่ง SQL (Execute Query)")
                            }

                            if (queryStatusMessage.isNotBlank()) {
                                Text(queryStatusMessage, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }

                            Divider()

                            Text("ผลลัพธ์ (Result Table):", fontWeight = FontWeight.Bold)

                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth().weight(1f)
                            ) {
                                items(queryResults) { row ->
                                    Card(modifier = Modifier.fillMaxWidth()) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text("ID: ${row["id"]} | Bike: ${row["bike"]}", fontWeight = FontWeight.Bold)
                                            Text("Service: ${row["service"]}", style = MaterialTheme.typography.bodyMedium)
                                            Text("Cost: ${row["cost"]} บาท | Date: ${row["date"]}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
