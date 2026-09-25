package com.example

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.manual.ZoomableManualImage

data class TechnicalDiagramItem(
    val id: String,
    val title: String,
    val category: String, // e.g. "MotorIndy", "CDI Pinout", "Regulator", "ECU/Wiring"
    val source: String,   // "MotorIndy (fb.com/motorindy)" or "Standard OEM"
    val description: String,
    val drawableRes: Int,
    val badge: String = "ไดอะแกรมช่าง",
    val technicalNotes: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("ทั้งหมด") }
    var viewingDiagram by remember { mutableStateOf<TechnicalDiagramItem?>(null) }

    val allDiagrams = listOf(
        // MotorIndy Special Highlights
        TechnicalDiagramItem(
            id = "motorindy_blowby",
            title = "ไดอะแกรมแรงดันสะสม Blow-by Gas & ระบบระบายไอแคร้ง",
            category = "MotorIndy ไดอะแกรม",
            source = "MotorIndy (fb.com/motorindy)",
            description = "วงจรระบบแรงดันไอน้ำมันเครื่องตกค้าง (Blow-by Gas) ไหลผ่านแหวนลูกสูบลงสู่ห้องแคร้ง การไหลเวียนผ่านท่อหายใจเข้าสู่หม้อกรองอากาศเพื่อป้องกันแรงดันสะสมดันซีลข้อเหวี่ยงรั่ว",
            drawableRes = R.drawable.motorindy_blowby_diagram_1788612106836,
            badge = "แนะนำจาก MotorIndy",
            technicalNotes = "• สาเหตุของแรงดันเกิน: แหวนลูกสูบหลวม, วาล์วรั่ว, หรือเติมน้ำมันเครื่องเกินระดับก้านวัด\n• ผลกระทบ: น้ำมันดันออกท่อหายใจ, กำลังอัดตก, เครื่องยนต์กินน้ำมันเครื่อง\n• การแก้ไข: ตรวจเช็คท่อระบายไอแคร้งไม่ให้อุดตัน หรือติดตั้งถังดักไอ (Oil Catch Tank)"
        ),
        TechnicalDiagramItem(
            id = "motorindy_ecu_wiring",
            title = "ไดอะแกรมสายไฟกล่อง ECU & ระบบหัวฉีด EFI มอเตอร์ไซค์",
            category = "MotorIndy ไดอะแกรม",
            source = "MotorIndy (fb.com/motorindy)",
            description = "วงจรเชื่อมต่อสายไฟกล่องควบคุมเครื่องยนต์ ECU กับชุดเซนเซอร์หลัก: TPS (ลิ้นเร่ง), EOT (อุณหภูมิน้ำมันเครื่อง), O2 Sensor (ออกซิเจน), คอยล์จุดระเบิด และปั๊มติ๊ก",
            drawableRes = R.drawable.motorindy_ecu_wiring_1788612122251,
            badge = "MotorIndy EFI",
            technicalNotes = "• สัญญาณ TPS: ขา 5V, Ground, และ Signal (ปกติปิดลิ้นเร่ง 0.5V บิดหมด 4.2-4.5V)\n• เซนเซอร์ EOT: ชนิด NTC ยิ่งร้อนความต้านทานยิ่งลดลง\n• O2 Sensor: จ่ายไฟสลับ 0.1V - 0.9V ให้กล่องปรับอัตราส่วน A/F 14.7:1"
        ),
        TechnicalDiagramItem(
            id = "motorindy_ignition",
            title = "ไดอะแกรมระบบจุดระเบิด CDI, พัลเซอร์คอยล์ & สเตเตอร์",
            category = "MotorIndy ไดอะแกรม",
            source = "MotorIndy (fb.com/motorindy)",
            description = "ผังวงจรการกำเนิดกระแสไฟสถิตจากขดลวดแม่เหล็ก ส่งผ่านพัลเซอร์คอยล์สร้างจังหวะจุดระเบิดเข้ากล่อง CDI แล้วส่งต่อไปยังคอยล์ใต้ถังและหัวเทียน",
            drawableRes = R.drawable.motorindy_ignition_cdi_1788612138206,
            badge = "MotorIndy Ignition",
            technicalNotes = "• ค่าความต้านทานพัลเซอร์คอยล์มาตรฐาน: 120 - 240 โอห์ม\n• หากไฟไม่ออกหัวเทียน: ให้เช็กสายดินกล่อง CDI, เช็กไฟจากพัลเซอร์คอยล์ และคอยล์ใต้ถังตามลำดับ"
        ),

        // CDI & Pinout System
        TechnicalDiagramItem(
            id = "cdi_5pin_ac",
            title = "ไดอะแกรมพินเอาท์ กล่อง CDI 5 พิน ระบบ AC (ไฟกระแสสลับ)",
            category = "CDI Pinout",
            source = "มาตรฐานช่างทั่วไป",
            description = "แผนภาพขาเสียบกล่อง CDI 5 พินแบบ AC ใช้ในรุ่น Wave 100/110 รุ่นเก่า, Dream คุรุสภา ใช้ไฟจากขดลวดสตาร์ทใต้จานไฟโดยตรง",
            drawableRes = R.drawable.cdi_5pin_ac_1786046574754,
            badge = "AC CDI",
            technicalNotes = "ขา 1: ดับเครื่อง (สวิตช์กุญแจลงกราวด์)\nขา 2: คอยล์หัวเทียน\nขา 3: พัลเซอร์คอยล์\nขา 4: กราวด์ (สายดิน)\nขา 5: ไฟชาร์จขดลวดสตาร์ท (AC Ignition)"
        ),
        TechnicalDiagramItem(
            id = "cdi_6pin_dc",
            title = "ไดอะแกรมพินเอาท์ กล่อง CDI 6 พิน ระบบ DC (ไฟแบตเตอรี่)",
            category = "CDI Pinout",
            source = "มาตรฐานช่างทั่วไป",
            description = "แผนภาพขาเสียบกล่อง CDI 6 พินระบบ DC ใช้ไฟ 12V ตรงจากแบตเตอรี่ ให้ประกายไฟแรงสม่ำเสมอในทุกย่านรอบ",
            drawableRes = R.drawable.cdi_6pin_dc_1786046562280,
            badge = "DC CDI",
            technicalNotes = "ขา 1: สัญญาณพัลเซอร์\nขา 2: สายไฟ 12V เลี้ยงกล่องจากแบต\nขา 3: สัญญาณส่งออกคอยล์หัวเทียน\nขา 4: กราวด์ลงตัวถัง"
        ),
        TechnicalDiagramItem(
            id = "cdi_pin_diagram",
            title = "ผังเทียบวงจรขาต่อกล่อง CDI อเนกประสงค์",
            category = "CDI Pinout",
            source = "มาตรฐานช่างทั่วไป",
            description = "ตารางเปรียบเทียบพินเอาท์กล่องจุดระเบิด CDI มอเตอร์ไซค์สี่จังหวะรุ่นยอดนิยมในประเทศไทย",
            drawableRes = R.drawable.cdi_pin_diagram_1786040438198,
            badge = "Universal Pinout",
            technicalNotes = "ใช้สำหรับแปลงขากล่องจุดระเบิดข้ามรุ่น หรือเทียบสเปกกล่องแต่งรอบจัด"
        ),

        // Regulators
        TechnicalDiagramItem(
            id = "regulator_4wire",
            title = "ไดอะแกรมแผ่นชาร์จ (Regulator/Rectifier) 4 สาย Wave/Sonic",
            category = "แผ่นชาร์จไฟ",
            source = "มาตรฐานโรงงาน OEM",
            description = "วงจรแผ่นชาร์จและเรกูเลเตอร์ 4 สาย แปลงไฟ AC จากมัดไฟเป็นไฟ DC 12V ชาร์จลงแบตเตอรี่และควบคุมไฟหน้าไม่ให้ขาด",
            drawableRes = R.drawable.regulator_4wire_1786046591955,
            badge = "Regulator 4P",
            technicalNotes = "• สายขาว: ไฟชาร์จเข้าจากมัดไฟ\n• สายเหลือง: คุมแรงดันไฟส่องสว่างหน้า\n• สายแดง: ไฟชาร์จ DC ออกไปแบตเตอรี่ 13.5-14.8V\n• สายเขียว: กราวด์ตัวถัง"
        ),
        TechnicalDiagramItem(
            id = "regulator_5wire_honda",
            title = "ไดอะแกรมแผ่นชาร์จ เรกูเลเตอร์ 5 สาย Honda Click / AirBlade",
            category = "แผ่นชาร์จไฟ",
            source = "มาตรฐานโรงงาน OEM",
            description = "ไดอะแกรมพินเอาท์แผ่นชาร์จ 5 สาย สำหรับรถสายพานออโตเมติก พร้อมระบบเซนเซอร์ตรวจจับแรงดันไฟกุญแจ",
            drawableRes = R.drawable.regulator_5wire_honda_1786046604058,
            badge = "Regulator 5P",
            technicalNotes = "มีสายตรวจจับโวลต์ (Sense Wire สีดำ) เพื่อควบคุมไฟชาร์จให้เสถียรยิ่งขึ้น ป้องกันแบตเตอรี่บวม"
        ),
        TechnicalDiagramItem(
            id = "regulator_pinout_diagram",
            title = "ผังตำแหน่งปลั๊กและพินแผ่นชาร์จไฟมอเตอร์ไซค์",
            category = "แผ่นชาร์จไฟ",
            source = "มาตรฐานโรงงาน OEM",
            description = "วิธีวัดแรงดันไฟชาร์จด้วยมัลติมิเตอร์ และตำแหน่งขาเสียบเพื่อตรวจสอบปัญหาไฟไม่ชาร์จหรือไฟหน้าขาดบ่อย",
            drawableRes = R.drawable.regulator_pinout_diagram_example_1786043432848,
            badge = "Pinout Guide",
            technicalNotes = "ถ้าเร่งเครื่องแล้วไฟออกเกิน 15V แสดงว่าแผ่นชาร์จเสีย ต้องเปลี่ยนทันทีก่อนแบตเตอรี่ระเบิด"
        )
    )

    val categories = listOf("ทั้งหมด", "MotorIndy ไดอะแกรม", "CDI Pinout", "แผ่นชาร์จไฟ")

    val filteredDiagrams = if (selectedCategory == "ทั้งหมด") {
        allDiagrams
    } else {
        allDiagrams.filter { it.category == selectedCategory }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "คลังไดอะแกรม & วงจรไฟฟ้าช่าง",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "รวมไดอะแกรมจาก MotorIndy & วงจร OEM",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("library_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://facebook.com/motorindy"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.testTag("btn_visit_motorindy_fb")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Open MotorIndy Facebook",
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
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Facebook MotorIndy Credit Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://facebook.com/motorindy"))
                        context.startActivity(intent)
                    }
                    .testTag("motorindy_credit_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.White)
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                "ไดอะแกรม & เคล็ดลับจากเพจ MotorIndy",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Text(
                            "รวมความรู้ทางเทคนิค ไดอะแกรม Blow-by Gas, ระบบไฟ & สายไฟ ECU (fb.com/motorindy)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Icon(
                        Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = "Go to FB",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 12.sp, fontWeight = if (selectedCategory == cat) FontWeight.Bold else FontWeight.Normal) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Diagrams Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredDiagrams) { item ->
                    DiagramCard(
                        diagram = item,
                        onClick = { viewingDiagram = item }
                    )
                }
            }
        }
    }

    // High-Resolution Zoomable Dialog
    if (viewingDiagram != null) {
        val currentDiagram = viewingDiagram!!
        Dialog(
            onDismissRequest = { viewingDiagram = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Column {
                                    Text(
                                        text = currentDiagram.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "แหล่งที่มา: ${currentDiagram.source}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            navigationIcon = {
                                IconButton(onClick = { viewingDiagram = null }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close")
                                }
                            },
                            actions = {
                                IconButton(onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://facebook.com/motorindy"))
                                    context.startActivity(intent)
                                }) {
                                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = "Visit Page")
                                }
                            }
                        )
                    }
                ) { pad ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(pad)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Zoomable Image
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.Black)
                        ) {
                            ZoomableManualImage(
                                model = currentDiagram.drawableRes,
                                contentDescription = currentDiagram.title,
                                modifier = Modifier.fillMaxSize(),
                                showControls = true,
                                allowFullScreen = false
                            )
                        }

                        // Technical Explanation Notes
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "📌 คำอธิบายและเทคนิคช่าง",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            currentDiagram.source,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Text(
                                    currentDiagram.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (currentDiagram.technicalNotes.isNotBlank()) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    Text(
                                        currentDiagram.technicalNotes,
                                        style = MaterialTheme.typography.bodySmall,
                                        lineHeight = 20.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DiagramCard(
    diagram: TechnicalDiagramItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("diagram_card_${diagram.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.DarkGray)
            ) {
                Image(
                    painter = painterResource(id = diagram.drawableRes),
                    contentDescription = diagram.title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )

                Surface(
                    shape = RoundedCornerShape(bottomEnd = 12.dp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        diagram.badge,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                        .size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.ZoomIn,
                            contentDescription = "Zoom",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    diagram.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    diagram.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "ที่มา: ${diagram.source}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "แตะเพื่อดูแบบซูมขยาย 🔍",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
