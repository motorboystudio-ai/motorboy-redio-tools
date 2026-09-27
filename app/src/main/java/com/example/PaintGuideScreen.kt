package com.example

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// =========================================================================
// DATA MODELS FOR PAINT MASTER MODULE
// =========================================================================

data class PaintStep(
    val stepNumber: Int,
    val titleTh: String,
    val titleEn: String,
    val descriptionTh: String,
    val descriptionEn: String,
    val gritOrTool: String,
    val timeOrWait: String,
    val keyTips: List<String>,
    val tag: String
)

data class PaintTechnique(
    val nameTh: String,
    val nameEn: String,
    val difficulty: String,
    val baseLayer: String,
    val midLayer: String,
    val topLayer: String,
    val instructions: List<String>,
    val proTip: String
)

data class PaintDefect(
    val defectNameTh: String,
    val defectNameEn: String,
    val symptom: String,
    val causes: List<String>,
    val prevention: List<String>,
    val solution: String,
    val severity: String // "สูง", "ปานกลาง", "ต่ำ"
)

// =========================================================================
// MAIN COMPOSABLE: PaintGuideScreen
// =========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaintGuideScreen(
    onBack: () -> Unit = {},
    onOpenMixing: () -> Unit = {}
) {
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    val tabs = listOf(
        "🎨 ขั้นตอนทำสี",
        "🩹 ซ่อมสี & เบลนด์",
        "🧪 สูตรผสม 2K",
        "✨ เทคนิคพิเศษ",
        "⚠️ ข้อควรระวัง"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("คู่มือทำสี & ซ่อมสีมอเตอร์ไซค์ 2K") },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("paint_guide_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ย้อนกลับ")
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = { AppSettings.isThai = !AppSettings.isThai },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("paint_guide_lang_toggle_btn")
                    ) {
                        Icon(
                            Icons.Default.Translate,
                            contentDescription = "สลับภาษา",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            if (AppSettings.isThai) "TH" else "EN",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Palette,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    if (AppSettings.isThai) "🎨 คู่มือทำสี & ซ่อมสีมอเตอร์ไซค์ 2K" else "🎨 2K Motorcycle Paint & Bodywork Master",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    if (AppSettings.isThai) "สูตรผสม 2K, เทคนิคพ่นมุก/แคนดี้ & แก้งานเสียครบวงจร" else "2K mixing formulas, candy/pearl techniques & defect fixes",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Scrollable Sub-tabs
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        edgePadding = 0.dp,
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary,
                        divider = {}
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        title,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                modifier = Modifier.testTag("paint_tab_$index")
                            )
                        }
                    }
                }
            }

            // Screen content based on tab
            AnimatedContent(
                targetState = selectedTab,
                label = "PaintTabAnimation"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> PaintingProcessTab()
                    1 -> SpotRepairTab()
                    2 -> PaintMixingCalculatorTab(
                        showFullVersionLink = true,
                        onOpenFullCalculator = onOpenMixing
                    )
                    3 -> SpecialTechniquesTab()
                    4 -> PaintDefectsTab()
                }
            }
        }
    }
}

// =========================================================================
// TAB 1: ขั้นตอนการทำสีมอเตอร์ไซค์ (PAINTING PROCESS)
// =========================================================================

@Composable
fun PaintingProcessTab() {
    var selectedMaterial by rememberSaveable { mutableStateOf("ABS / พลาสติกใหม่") }
    val materials = listOf("ABS / พลาสติกใหม่", "PP / พลาสติกดำเหนียว", "โลหะ / ถังน้ำมัน / โครงเหล็ก")

    val steps = when (selectedMaterial) {
        "PP / พลาสติกดำเหนียว" -> listOf(
            PaintStep(
                stepNumber = 1,
                titleTh = "ล้างคราบไขมัน & ขัดเปิดผิว",
                titleEn = "Degrease & Surface Scuffing",
                descriptionTh = "ล้างด้วยน้ำยาล้างจาน/น้ำยาเช็ดคราบ ซิลิโคนรีมูฟเวอร์ (Wax & Grease Remover) เพื่อขจัดน้ำยาเคลือบเงา จากนั้นขัดด้วยกระดาษทรายน้ำเบอร์ 600-800 ลูบให้ทั่วจนผิวหายมันวาว",
                descriptionEn = "Clean thoroughly with wax & grease remover. Sand with wet 600-800 grit.",
                gritOrTool = "กระดาษทรายน้ำ #600 - #800 + ซิลิโคนรีมูฟเวอร์",
                timeOrWait = "ตากให้แห้งสนิท 15 นาที",
                keyTips = listOf("พลาสติก PP มีความยืดหยุ่นสูง หากไม่ล้างคราบไขมัน สีจะร่อนเป็นแผ่น", "ห้ามใช้ทินเนอร์ 3A เช็ด เพราะพลาสติกจะละลายเสียรูป"),
                tag = "เตรียมผิว"
            ),
            PaintStep(
                stepNumber = 2,
                titleTh = "พ่นน้ำยาประสานพลาสติก (Plastic / PP Primer)",
                titleEn = "Plastic Adhesion Promoter",
                descriptionTh = "พ่นน้ำยาเกาะพลาสติก 1-2 เที่ยวบางๆ เพื่อสร้างฟิล์มเหนียวจับยึดระหว่างโมเลกุลพลาสติกกับสีรองพื้น",
                descriptionEn = "Apply 1-2 light coats of plastic adhesion promoter.",
                gritOrTool = "ปืนพ่นสี / สเปรย์รองพื้นเกาะพลาสติก PP",
                timeOrWait = "รอหมาด 5-10 นาที (ห้ามทิ้งเกิน 15 นาที ต้องพ่นสีรองพื้นต่อทันที)",
                keyTips = listOf("พ่นบางๆ ไม่ต้องฉ่ำเยิ้ม", "ต้องพ่นรองพื้น 2K ตามในขณะที่น้ำยายังเหนียวหนึบ (Wet-on-Tack)"),
                tag = "น้ำยาเกาะ"
            ),
            PaintStep(
                stepNumber = 3,
                titleTh = "พ่นรองพื้น 2K ยืดหยุ่น (2K Primer + Elastic Additive)",
                titleEn = "2K Primer with Flex Agent",
                descriptionTh = "ผสมสีรองพื้น 2K อัตราส่วน 4:1 และเติมน้ำยาเพิ่มความยืดหยุ่น (Flex Agent) 5-10% พ่น 2 เที่ยวเพื่อกลบเส้นทรายและปรับระนาบ",
                descriptionEn = "Apply 2K primer surfacer with flexible additive.",
                gritOrTool = "รองพื้น 2K + ทินเนอร์ 2K ช้า",
                timeOrWait = "ทิ้งให้แห้งตัว 2-3 ชม. หรืออบ 40 นาที",
                keyTips = listOf("ผสมทินเนอร์ 2K เกรดอะครีลิกเท่านั้น", "ขัดเก็บคลื่นด้วยกระดาษทรายน้ำ #800 - #1000"),
                tag = "รองพื้น 2K"
            ),
            PaintStep(
                stepNumber = 4,
                titleTh = "พ่นสีจริง (Base Coat Color)",
                titleEn = "Base Coat Application",
                descriptionTh = "พ่นสีจริง 2-3 เที่ยว เที่ยวแรกพ่นโปรย (Dust coat) เที่ยวที่ 2-3 พ่นเน้นเฉดสีสม่ำเสมอ ทิ้งระยะห่างแต่ละเที่ยว (Flash-off) 10-15 นาที",
                descriptionEn = "Apply basecoat 2-3 coats with 10-15 min flash-off time.",
                gritOrTool = "กาพ่นสีหัว 1.3 - 1.4 mm / แรงดันลม 2.0 - 2.2 Bar",
                timeOrWait = "พักระหว่างเที่ยว 10-15 นาที",
                keyTips = listOf("หากเป็นสีมุก/เมทัลลิก ให้พ่นโปรยเก็บเม็ดมุกระยะห่างขึ้น 25-30 ซม. ในเที่ยวสุดท้าย", "ห้ามจับหรือเอามือแตะฟิล์มสีก่อนพ่นเคลียร์"),
                tag = "สีจริง"
            ),
            PaintStep(
                stepNumber = 5,
                titleTh = "พ่นเคลียร์เงา 2K (2K Clear Coat High Gloss)",
                titleEn = "2K Clear Coat Topcoat",
                descriptionTh = "ผสมแลกเกอร์ 2K อัตราส่วน 2:1 หรือ 4:1 ตามสเปค พ่นเที่ยวแรกแบบกึ่งเปียก (Medium Wet) พัก 10-15 นาที แล้วพ่นเที่ยวที่ 2 แบบฉ่ำเงา (Full Wet)",
                descriptionEn = "Apply 2 coats of 2K clear coat. First medium wet, second full gloss.",
                gritOrTool = "แลกเกอร์ 2K Extra Solid / กาหัว 1.3-1.4mm",
                timeOrWait = "แห้งสัมผัสได้ 4-6 ชม. / แห้งสนิทขัดยาได้ 24 ชม.",
                keyTips = listOf("ผสมทินเนอร์ไม่เกิน 10-15% เพื่อป้องกันตามดและฟองอากาศ", "รักษาระยะห่างกา 15-20 ซม. และความเร็วเดินมือสม่ำเสมอ"),
                tag = "เคลียร์ 2K"
            )
        )
        "โลหะ / ถังน้ำมัน / โครงเหล็ก" -> listOf(
            PaintStep(
                stepNumber = 1,
                titleTh = "ลอกสีเดิม & ขัดเปิดสนิม (Bare Metal Prep)",
                titleEn = "Stripping & Rust Removal",
                descriptionTh = "ใช้น้ำยาลอกสี หรือขัดเปิดผิวด้วยกระดาษทรายเบอร์ #120 - #240 ขจัดคราบสนิมและคราบไขมันออกจนเห็นเนื้อเหล็ก/อะลูมิเนียมขาวใส",
                descriptionEn = "Remove old paint & rust with #120-#240 sandpaper.",
                gritOrTool = "กระดาษทรายแห้ง #180 - #240 / น้ำยาแปลงสนิม",
                timeOrWait = "เช็ดแห้งสนิททันที",
                keyTips = listOf("หลังขัดสนิมเสร็จ ห้ามโดนน้ำหรือเหงื่อมือ เพราะสนิมจะขึ้นใหม่ภายใน 10 นาที", "เช็ดด้วย Wax & Grease Remover ให้สะอาด"),
                tag = "เปิดผิวเหล็ก"
            ),
            PaintStep(
                stepNumber = 2,
                titleTh = "พ่นรองพื้นกันสนิมอีพ็อกซี่ 2K (Epoxy Primer)",
                titleEn = "2K Epoxy Anti-Corrosion Primer",
                descriptionTh = "พ่นรองพื้นอีพ็อกซี่ 2K ป้องกันสนิมและเพิ่มการยึดเกาะกับผิวเหล็ก 1-2 เที่ยว",
                descriptionEn = "Apply 2K Epoxy anti-rust primer coat.",
                gritOrTool = "สีรองพื้นอีพ็อกซี่ 2K (4:1)",
                timeOrWait = "แห้งตัว 4-6 ชม. ก่อนโป๊วหรือพ่นสีต่อ",
                keyTips = listOf("อีพ็อกซี่ 2K ป้องกันความชื้นและสนิมใต้ฟิล์มสีได้ดีที่สุด", "สามารถโป๊วสีทับบนชั้นอีพ็อกซี่ได้เลย"),
                tag = "กันสนิม 2K"
            ),
            PaintStep(
                stepNumber = 3,
                titleTh = "โป๊วเก็บรอยบุบ & ขัดปรับระนาบ (Putty & Leveling)",
                titleEn = "Body Filler & Sanding",
                descriptionTh = "ผสมสีโป๊วพลาสติก/โพลีเอสเตอร์กับฮาร์ดเดนเนอร์ 2-3% ปาดบางๆ ในรอยบุบ รอแห้ง 20-30 นาที แล้วขัดด้วยบล็อกขัดเบอร์ #180 -> #320 -> #400",
                descriptionEn = "Apply body filler and block sand from #180 to #400.",
                gritOrTool = "สีโป๊ว 2K + ยางปาด + บล็อกขัดกระดาษทราย",
                timeOrWait = "แห้งขัดได้ 20-30 นาที",
                keyTips = listOf("ใช้บล็อกขัดแบบแข็งหรือไม้ระนาบเสมอ ห้ามใช้มือกดขัดโดยตรงเพราะจะเป็นคลื่น", "ปาดสีโป๊วเป็นชั้นบางๆ 2-3 รอบ ดีกว่าปาดหนารอบเดียว"),
                tag = "โป๊วสี"
            ),
            PaintStep(
                stepNumber = 4,
                titleTh = "พ่นรองพื้นกลบรอย 2K (2K Primer Surfacer)",
                titleEn = "2K Primer Surfacer",
                descriptionTh = "พ่นรองพื้น 2K หนา 2-3 เที่ยวเพื่อกลบเส้นกระดาษทรายและปรับความเรียบเนียน จากนั้นขัดน้ำด้วย #800 - #1000",
                descriptionEn = "Apply 2-3 coats of 2K primer surfacer, wet sand with #800-#1000.",
                gritOrTool = "รองพื้น 2K Surfacer + กระดาษทรายน้ำ #800/#1000",
                timeOrWait = "แห้งขัดได้ 2-3 ชม.",
                keyTips = listOf("พ่นไกด์โค้ท (Guide Coat) ดำบางๆ ก่อนขัด เพื่อดูหลุมและรอยคลื่น", "เมื่อขัดจนไกด์โค้ทหมด ผิวจะเรียบระดับ 100%"),
                tag = "รองพื้นกลบรอย"
            ),
            PaintStep(
                stepNumber = 5,
                titleTh = "พ่นสีจริง & เคลียร์เงา 2K",
                titleEn = "Basecoat & 2K Clear Coat",
                descriptionTh = "พ่นสีจริง 2-3 เที่ยว พัก 10-15 นาที แล้วตามด้วยแลกเกอร์ 2K ความเงาสูง 2 เที่ยวเต็ม",
                descriptionEn = "Apply basecoat followed by 2 coats of 2K high solid clear coat.",
                gritOrTool = "สีจริง + เคลียร์ 2K",
                timeOrWait = "แห้งสมบูรณ์ 24 ชม.",
                keyTips = listOf("สำหรับถังน้ำมัน เคลียร์ 2K แท้จะทนทานต่อน้ำมันเบนซินหกใส่ ไม่ด่างไม่พอง", "หลีกเลี่ยงการพ่นในวันที่ฝนตกหรือความชื้นเกิน 80%"),
                tag = "สีจริง+เคลียร์"
            )
        )
        else -> listOf(
            PaintStep(
                stepNumber = 1,
                titleTh = "ขัดลบผิวแลกเกอร์เดิม (Scuffing & Sanding)",
                titleEn = "Surface Preparation",
                descriptionTh = "ใช้กระดาษทรายน้ำเบอร์ #600 - #800 ขัดลูบผิวสีเดิมให้ทั่วจนหายเงา เพื่อสร้างร่องยึดเกาะ (Mechanical Key) เช็ดทำความสะอาดด้วยซิลิโคนรีมูฟเวอร์",
                descriptionEn = "Sand original clearcoat with #600-#800 until matte. Clean with degreaser.",
                gritOrTool = "กระดาษทรายน้ำ #600 - #800",
                timeOrWait = "เช็ดแห้งสนิท 10 นาที",
                keyTips = listOf("ไม่ต้องขัดลึกถึงเนื้อพลาสติก ขัดแค่ให้ชั้นเคลียร์เดิมด้านสม่ำเสมอ", "ตามซอกมุมให้ใช้สก็อตไบรท์สีเทาสำหรับงานสีลูบช่วย"),
                tag = "ขัดเปิดผิว"
            ),
            PaintStep(
                stepNumber = 2,
                titleTh = "พ่นรองพื้น 2K ปรับผิว (2K Primer Surfacer)",
                titleEn = "2K Primer Application",
                descriptionTh = "พ่นสีรองพื้น 2K จำนวน 2 เที่ยว ทิ้งช่วงเที่ยวละ 10 นาที เพื่อกลบรอยขูดขีดเดิมและปรับสีพื้นให้สม่ำเสมอ",
                descriptionEn = "Apply 2 coats of 2K primer surfacer.",
                gritOrTool = "รองพื้น 2K 4:1 + ทินเนอร์ 2K",
                timeOrWait = "แห้งขัด 2 ชม. (ขัดน้ำ #1000)",
                keyTips = listOf("เลือกรองพื้นสีเทาสำหรับสีทั่วไป, รองพื้นขาวสำหรับสีสว่าง/มุก, รองพื้นดำสำหรับสีมืด/แคนดี้", "ขัดน้ำเบาๆ ด้วย #1000 ให้เรียบเนียนดุจกระจก"),
                tag = "รองพื้น 2K"
            ),
            PaintStep(
                stepNumber = 3,
                titleTh = "พ่นสีจริง (Base Coat)",
                titleEn = "Basecoat Spraying",
                descriptionTh = "พ่นเที่ยวแรกแบบโปรย (Dust coat) พัก 10 นาที จากนั้นพ่นเที่ยวที่ 2 และ 3 แบบสม่ำเสมอ รักษาระยะห่าง 15-20 ซม.",
                descriptionEn = "Apply basecoat 2-3 coats with 10 min flash off.",
                gritOrTool = "สีกระป๋อง 2K หรือ กาพ่นสีหัว 1.3mm",
                timeOrWait = "พัก 15 นาทีก่อนเคลียร์",
                keyTips = listOf("อย่าพ่นอัดหนาเกินไปในเที่ยวเดียว สีจะย้อยและแห้งช้า", "หากมีเม็ดฝุ่นตกใส่ ให้รอแห้งแล้วใช้กระดาษทราย #1500 ลูบเบาๆ แล้วพ่นสีทับบางๆ"),
                tag = "สีจริง"
            ),
            PaintStep(
                stepNumber = 4,
                titleTh = "พ่นเคลียร์เงา 2K (2K Clear Coat)",
                titleEn = "2K Clear Coat",
                descriptionTh = "พ่นเที่ยวแรก Medium Wet พัก 10 นาที พ่นเที่ยวที่ 2 Full Wet ฉ่ำเงา ทิ้งให้แห้งตัวข้ามคืน",
                descriptionEn = "Apply 2 coats of 2K clear coat. First medium wet, second full gloss wet.",
                gritOrTool = "เคลียร์ 2K เงาฉ่ำพิเศษ",
                timeOrWait = "แห้งสนิท 24 ชม.",
                keyTips = listOf("ควบคุมอุณหภูมิห้องพ่น 25-32°C จะได้ฟิล์มสีที่เงางามที่สุด", "ขัดชักเงาด้วยน้ำยาขัดหยาบและละเอียดหลัง 24-48 ชม."),
                tag = "เคลียร์ 2K"
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("🛠️ เลือกประเภทชิ้นงานที่ต้องการทำสี:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        materials.forEach { mat ->
                            val isSel = selectedMaterial == mat
                            Button(
                                onClick = { selectedMaterial = mat },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                                ),
                                contentPadding = PaddingValues(2.dp)
                            ) {
                                Text(
                                    mat.split(" / ")[0],
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        items(steps) { step ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        "${step.stepNumber}",
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                step.titleTh,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                step.tag,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(step.descriptionTh, fontSize = 13.sp, lineHeight = 18.sp)

                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.tertiary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("อุปกรณ์/เบอร์กระดาษทราย: ${step.gritOrTool}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.tertiary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ระยะเวลารอแห้ง/Flash-off: ${step.timeOrWait}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("💡 เทคนิคสำคัญ (Key Tips):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    step.keyTips.forEach { tip ->
                        Text("• $tip", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

// =========================================================================
// TAB 2: ซ่อมสีเฉพาะจุด & การเบลนด์สี (SPOT REPAIR & BLENDING)
// =========================================================================

@Composable
fun SpotRepairTab() {
    val spotRepairGuides = listOf(
        mapOf(
            "title" to "ซ่อมรอยขูดขีดลึก & รอยหินดีด (Deep Scratch / Stone Chip)",
            "level" to "ระดับง่าย - ปานกลาง",
            "tools" to "กระดาษทราย #800, #1500, #2000, สีแต้มเบอร์ตรง, พู่กันเบอร์ 0, ยาขัดละเอียด",
            "steps" to listOf(
                "1. ล้างทำความสะอาดร่องรอยขูดขีดด้วยแอลกอฮอล์หรือน้ำยาล้างคราบไขมัน",
                "2. หากมีสนิมหรือรอยคม ให้ใช้ปลายกระดาษทรายเบอร์ #800 ม้วนเล็กๆ สะกิดคราบสนิมออก",
                "3. ใช้พู่กันเบอร์ 0 จุ่มสีจริงแต้มลงในร่องทีละน้อย ให้เนื้อสีนูนขึ้นมาจากระนาบเดิมเล็กน้อย (เพราะสีจะยุบตัวเมื่อแห้ง)",
                "4. รอสีแห้งตัว 1-2 ชั่วโมง จากนั้นแต้มแลกเกอร์ 2K ใสทับยอดนูนอีก 1 หยด",
                "5. ทิ้งให้แห้งข้ามคืน 24 ชม. แล้วใช้บล็อกยางพันกระดาษทรายน้ำเบอร์ #1500 และ #2000 ขัดลูบยอดนูนให้เรียบเสมอผิวเดิม",
                "6. ปั่นชักเงาด้วยน้ำยาขัดหยาบ (Compound) ตามด้วยน้ำยาขัดละเอียด (Finishing Polish)"
            ),
            "trick" to "ใช้เทปกระดาษกาวแปะขนาบข้างรอยแผลก่อนขัด เพื่อไม่ให้กระดาษทรายไปกินผิวสีดีรอบข้าง"
        ),
        mapOf(
            "title" to "การสาดเบลนด์สีเฉพาะจุดไม่ให้เห็นขอบ (Color Blending / Fade-Out)",
            "level" to "ระดับช่างมืออาชีพ",
            "tools" to "กาพ่นสีหัวเล็ก (Mini Gun 0.8-1.0mm), ทินเนอร์ประสานขอบแลกเกอร์ (Fade-out Thinner / Blender)",
            "steps" to listOf(
                "1. ขัดเตรียมผิวบริเวณแผลด้วย #800 และขยายวงรอบแผลออกไป 10-15 ซม. ด้วยกระดาษทราย #1500 หรือสก็อตไบรท์เทา",
                "2. พ่นสีรองพื้นเฉพาะบริเวณแผลที่โป๊วไว้ อย่าให้ละอองฟุ้งกว้างเกินไป ขัดเรียบด้วย #1000",
                "3. พ่นสีจริงทับจุดซ่อม โดยในเที่ยวสุดท้ายให้ 'ลดแรงลมและสะบัดข้อมือออก' (Drop coat / Flicking) เพื่อให้เม็ดสีกลืนหายไปกับสีเดิม",
                "4. พ่นแลกเกอร์ 2K ทับบริเวณซ่อมและขยายเลยรอยสีจริงออกมา แต่ยังอยู่ในพื้นที่ที่ขัดด้วย #1500",
                "5. ขั้นตอนเด็ด: พ่น 'น้ำยาประสานขอบแลกเกอร์ (Blender Thinner)' ทันทีบางๆ บริเวณรอยต่อขอบแลกเกอร์ใหม่กับแลกเกอร์เดิม เพื่อละลายขอบฟิล์มให้เรียบเนียนเป็นเนื้อเดียวกัน 100%"
            ),
            "trick" to "ห้ามติดเทปกั้นขอบแข็งเป็นสันเด็ดขาด ให้ใช้เทคนิค 'พับเทปกระดาษครึ่งหนึ่ง (Soft Edge Tape)' เพื่อให้ละอองสีฟุ้งนุ่มนวล"
        ),
        mapOf(
            "title" to "การซ่อมแฟริ่งพลาสติกแตกหัก & ขาไฟหัก (Plastic Welding & Repair)",
            "level" to "ระดับปานกลาง",
            "tools" to "หัวแร้งบัดกรี, ลวดตาข่ายสแตนเลสเสริมแรง (Stainless Mesh), แท่งเชื่อมพลาสติก ABS/PP, กาวมหาอุด 2K Epoxy",
            "steps" to listOf(
                "1. บากขอบรอยแตกด้านในเป็นร่องตัว V (V-Groove) เพื่อเพิ่มพื้นที่ยึดเกาะ",
                "2. วางลวดตาข่ายสแตนเลสด้านหลังรอยแตก ใช้หัวแร้งกดความร้อนให้ตาข่ายฝังจมลงในเนื้อพลาสติก",
                "3. เติมเนื้อพลาสติกแท่งชนิดเดียวกับชิ้นงาน (ABS หรือ PP) หลอมประสานให้เต็มร่อง",
                "4. ด้านหน้าชิ้นงาน: ขัดเปิดร่องตัว V ตื้นๆ แล้วโป๊วด้วยสีโป๊วพลาสติกยืดหยุ่น (Flex Putty)",
                "5. ขัดปรับระนาบด้วยกระดาษทราย #320 -> #600 -> #800 พร้อมเข้าสู่กระบวนการพ่นสีตามปกติ"
            ),
            "trick" to "ดูสัญลักษณ์ชนิดพลาสติกใต้แฟริ่งเสมอ (ABS หรือ PP) หากใช้แท่งเชื่อมผิดประเภท พลาสติกจะไม่หลอมติดกัน"
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                "🩹 เทคนิคการซ่อมสีเฉพาะจุด & เก็บงานระดับโปร",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        items(spotRepairGuides) { guide ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            guide["title"] as String,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                guide["level"] as String,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("🛠️ เครื่องมือที่ใช้: ${guide["tools"]}", fontSize = 12.sp, color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Medium)

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("ขั้นตอนการซ่อม:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    (guide["steps"] as List<String>).forEach { step ->
                        Text(step, fontSize = 12.sp, lineHeight = 17.sp, modifier = Modifier.padding(vertical = 2.dp))
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("เคล็ดลับช่าง: ${guide["trick"]}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

// =========================================================================
// TAB 3: สูตรผสมสี & เครื่องคำนวณ 2K (MIXING & CALCULATOR)
// =========================================================================

@Composable
fun PaintMixingCalculatorTab(
    showFullVersionLink: Boolean = false,
    onOpenFullCalculator: () -> Unit = {}
) {
    var inputMode by rememberSaveable { mutableStateOf("Volume") } // "Volume" or "Area"
    var inputValue by rememberSaveable { mutableStateOf("150") }
    var selectedFormulaType by rememberSaveable { mutableStateOf("แลกเกอร์ 2K (ระบบ 2:1)") }
    var thinnerPercentage by rememberSaveable { mutableFloatStateOf(10f) } // %

    val totalVolume = if (inputMode == "Volume") {
        inputValue.toFloatOrNull() ?: 150f
    } else {
        val area = inputValue.toFloatOrNull() ?: 0f
        area * 83.33f
    }

    val (basePart, hardenerPart, thinnerPart) = when (selectedFormulaType) {
        "แลกเกอร์ 2K (ระบบ 2:1)" -> {
            val netMix = totalVolume / (1f + (thinnerPercentage / 100f))
            val base = (netMix * 2f / 3f)
            val hardener = (netMix * 1f / 3f)
            val thinner = netMix * (thinnerPercentage / 100f)
            Triple(base, hardener, thinner)
        }
        "แลกเกอร์ 2K (ระบบ 4:1)" -> {
            val netMix = totalVolume / (1f + (thinnerPercentage / 100f))
            val base = (netMix * 4f / 5f)
            val hardener = (netMix * 1f / 5f)
            val thinner = netMix * (thinnerPercentage / 100f)
            Triple(base, hardener, thinner)
        }
        "สีรองพื้น 2K (ระบบ 4:1:1)" -> {
            val onePart = totalVolume / 6f
            Triple(onePart * 4f, onePart * 1f, onePart * 1f)
        }
        else -> {
            val netMix = totalVolume / (1f + (thinnerPercentage / 100f))
            Triple(netMix * 0.6f, netMix * 0.3f, netMix * (thinnerPercentage / 100f))
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("🧪 เครื่องคำนวณอัตราส่วนผสมสี (2K Calculator)", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
        }

        // Link to the full Mixing & Tools screen (avoids maintaining two separate calculators).
        if (showFullVersionLink) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenFullCalculator() }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Default.OpenInNew,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "ต้องการสูตรเทียบสี–ซ่อมผิว–คลังเครื่องมือแบบเต็ม?",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                "แตะเพื่อเปิดหน้า Paint Mixing & Tools",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("โหมดการคำนวณ:", fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = inputMode == "Volume", onClick = { inputMode = "Volume"; inputValue = "150" }, label = { Text("ปริมาณสี (ml)") })
                        FilterChip(selected = inputMode == "Area", onClick = { inputMode = "Area"; inputValue = "1" }, label = { Text("พื้นที่ (ตร.ม.)") })
                    }

                    OutlinedTextField(
                        value = inputValue,
                        onValueChange = { inputValue = it },
                        label = { Text(if (inputMode == "Volume") "ต้องการปริมาณรวม (ml)" else "พื้นที่ผิว (ตร.ม.)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Text("เลือกสูตรผสม:", fontWeight = FontWeight.SemiBold)
                    DropdownMenuBox(
                        selectedOption = selectedFormulaType,
                        options = listOf("แลกเกอร์ 2K (ระบบ 2:1)", "แลกเกอร์ 2K (ระบบ 4:1)", "สีรองพื้น 2K (ระบบ 4:1:1)"),
                        onOptionSelected = { selectedFormulaType = it }
                    )

                    if (selectedFormulaType != "สีรองพื้น 2K (ระบบ 4:1:1)") {
                        Text("เปอร์เซ็นต์ทินเนอร์ (${thinnerPercentage.toInt()}%):", fontWeight = FontWeight.SemiBold)
                        Slider(value = thinnerPercentage, onValueChange = { thinnerPercentage = it }, valueRange = 0f..50f)
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("ผลลัพธ์การผสม (รวม: ${"%.1f".format(basePart + hardenerPart + thinnerPart)} ml):", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("• สี/แลกเกอร์หลัก:")
                        Text("${"%.1f".format(basePart)} ml", fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("• ฮาร์ดเดนเนอร์:")
                        Text("${"%.1f".format(hardenerPart)} ml", fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("• ทินเนอร์:")
                        Text("${"%.1f".format(thinnerPart)} ml", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// =========================================================================
// TAB 4: เทคนิคพิเศษ (SPECIAL PAINT EFFECTS)
// =========================================================================

@Composable
fun SpecialTechniquesTab() {
    val techniques = listOf(
        PaintTechnique(
            nameTh = "สีแคนดี้โทน / สีแก้วโปร่งแสง (Candy Tone)",
            nameEn = "Candy Tone Translucent Effect",
            difficulty = "⭐⭐⭐⭐ สูง",
            baseLayer = "รองพื้นบรอนซ์เงินเกล็ดสว่าง (Fine Silver) หรือบรอนซ์ทอง",
            midLayer = "สีแคนดี้โทนใส (Candy Red, Candy Blue, Candy Green) พ่น 3-4 เที่ยว",
            topLayer = "แลกเกอร์ 2K High Solid 2-3 เที่ยว",
            instructions = listOf(
                "1. พ่นสีรองพื้นบรอนซ์เงินให้เรียบเนียนสม่ำเสมอ 100% (ความสว่างของบรอนซ์จะเป็นตัวสะท้อนแสงให้สีแคนดี้เปล่งประกาย)",
                "2. ผสมสีแคนดี้โทนกับทินเนอร์ 2K ตามสัดส่วน",
                "3. พ่นสีแคนดี้โปรยบางๆ สม่ำเสมอ ซ้อนทับ 50% ของแนวกาในแต่ละเที่ยว",
                "4. จำนวนเที่ยวพ่นจะกำหนดความเข้มของสี (พ่น 2 เที่ยว = สว่างสดใส, พ่น 4 เที่ยว = สีลึกเข้มมิติสูง)",
                "5. พ่นเคลียร์เงา 2K ทับ 2 เที่ยวฉ่ำๆ เพื่อดึงมิติความลึก (Depth)"
            ),
            proTip = "ต้องเดินมือกาพ่นสีด้วยความเร็วและระยะห่างที่เป๊ะมาก หากหยุดมือหรือพ่นซ้ำจุดเดิม สีจะเข้มเป็นด่างดวงไม่เท่ากัน"
        ),
        PaintTechnique(
            nameTh = "สีเหลือบมุกเปลี่ยนสี (Chameleon / Flip-Flop)",
            nameEn = "Color Shift Chameleon Effect",
            difficulty = "⭐⭐⭐ ปานกลาง",
            baseLayer = "สีดำเงา หรือ ดำด้าน 2K (Solid Black)",
            midLayer = "ผงมุกคาเมเลียนผสมในเคลียร์ใส (Intercoat Clear) พ่น 2-3 เที่ยว",
            topLayer = "แลกเกอร์ 2K เคลียร์เงาสูง 2 เที่ยว",
            instructions = listOf(
                "1. พ่นสีพื้นด้วย 'สีดำสนิท' เท่านั้น เพราะพื้นดำจะดูดกลืนแสงและสะท้อนเฉพาะคลื่นแสงของผงมุกเปลี่ยนสี",
                "2. ผสมผงมุกคาเมเลียน 15-25 กรัม ต่อสารเคลียร์ผสมเสร็จ 1 ลิตร",
                "3. พ่นแบบโปรยกระจายตัว (Mist/Dust coat) 2-3 เที่ยว จนเห็นการเปลี่ยนสีเมื่อเปลี่ยนมุมมอง",
                "4. ปิดท้ายด้วยแลกเกอร์ 2K แบบ High Gloss เพื่อให้สะท้อนแสงเต็มที่"
            ),
            proTip = "อย่าพ่นผงมุกหนาเกินไปจนทึบ เพราะจะทำให้สูญเสียมิติการเปลี่ยนสีตามมุมตกกระทบของแสง"
        ),
        PaintTechnique(
            nameTh = "ลายเคฟล่าร์คาร์บอนด้วยผ้าตาข่าย (Carbon Fiber Mesh Effect)",
            nameEn = "Mesh Carbon Fiber Stencil",
            difficulty = "⭐⭐ ง่าย-ปานกลาง",
            baseLayer = "สีดำเงา (Gloss Black)",
            midLayer = "วางผ้าตาข่ายกันลื่นตารางถี่ แล้วพ่นสีเทาควันบุหรี่/บรอนซ์เงินเข้ม",
            topLayer = "สีดำโปร่งแสง Tint Clear + แลกเกอร์ 2K",
            instructions = listOf(
                "1. พ่นสีพื้นดำเงาให้แห้งสนิท",
                "2. ขึงผ้าตาข่ายกันลื่นลายตารางสี่เหลี่ยมให้ตึงแนบสนิทกับชิ้นงาน ยึดด้วยแม่เหล็กหรือเทปกาว",
                "3. พ่นสีบรอนซ์เงินหรือสีเทากราไฟต์โปรยบางๆ ผ่านตาข่ายในมุม 45 องศา",
                "4. ดึงตาข่ายออกทันที จะได้ลายตารางหมากรุกคาร์บอนเคฟล่าร์",
                "5. พ่นเคลียร์ใสผสมสีดำโปร่งแสง 5% ทับ 1 เที่ยวเพื่อลดความสว่างของลาย ให้ดูเหมือนเส้นใยคาร์บอนแท้",
                "6. เคลือบแลกเกอร์ 2K ฉ่ำ 2 เที่ยว"
            ),
            proTip = "ผ้าตาข่ายต้องแนบสนิทกับผิวงาน 100% อย่าให้ขยับขณะพ่น จะได้ขอบตารางคมกริบไม่เบลอ"
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                "✨ เทคนิคพ่นสีพิเศษระดับ Custom & Show Bike",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        items(techniques) { tech ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            tech.nameTh,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Text(
                                tech.difficulty,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Layer Stack Breakdown
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("โครงสร้างชั้นสี (Layer Stack):", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("1. ชั้นพื้น (Base): ${tech.baseLayer}", fontSize = 12.sp)
                            Text("2. ชั้นกลาง (Mid): ${tech.midLayer}", fontSize = 12.sp)
                            Text("3. ชั้นเคลือบ (Top): ${tech.topLayer}", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("ขั้นตอนการทำ:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    tech.instructions.forEach { inst ->
                        Text(inst, fontSize = 12.sp, lineHeight = 17.sp, modifier = Modifier.padding(vertical = 2.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pro Tip: ${tech.proTip}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

// =========================================================================
// TAB 5: ข้อควรระวัง & แก้ไขตำหนิงานสี (DEFECTS & PRECAUTIONS)
// =========================================================================

@Composable
fun PaintDefectsTab() {
    val defects = listOf(
        PaintDefect(
            defectNameTh = "สีย่น / สีพองย่น (Wrinkling / Lifting)",
            defectNameEn = "Wrinkling / Lifting",
            symptom = "ฟิล์มสีชั้นบนเกิดรอยย่นคล้ายหนังคางคก หรือพองบวมเป็นริ้วรอยหลังพ่นทับ",
            causes = listOf(
                "พ่นสีชั้นใหม่ทับลงไปขณะที่สีชั้นล่างยังแห้งไม่สนิท (ทินเนอร์จากชั้นบนซึมลงไปละลายสีล่าง)",
                "ใช้ทินเนอร์แรงเกินไป (เช่น ใช้ทินเนอร์ 3A พ่นทับสีสูตร 1K เดิม)",
                "ความหนาของฟิล์มสีมากเกินไปในเที่ยวเดียว"
            ),
            prevention = listOf(
                "เว้นระยะ Flash-off time แต่ละเที่ยวให้แห้งหมาดตามเวลาที่กำหนด",
                "ใช้ทินเนอร์ 2K คุณภาพสูงที่เข้ากันได้กับระบบสี",
                "หากเป็นสีเดิมโรงงานที่ไม่แน่ใจ ให้พ่นสีรองพื้นบาริเออร์ (Barrier Coat) กั้นชั้นสีก่อน"
            ),
            solution = "ต้องรอให้ฟิล์มสีแห้งสนิท 100% แล้วขัดลอกส่วนที่ย่นออกจนถึงชั้นที่เรียบ จากนั้นลงรองพื้น 2K ใหม่",
            severity = "สูง"
        ),
        PaintDefect(
            defectNameTh = "ตามด / รูเข็ม (Pinholing / Solvent Pop)",
            defectNameEn = "Solvent Popping",
            symptom = "มีรูพรุนขนาดเล็กคล้ายรอยเข็มแทงกระจายอยู่ทั่วผิวฟิล์มสีหรือแลกเกอร์",
            causes = listOf(
                "ทินเนอร์ระเหยออกไม่ทัน ถูกฟิล์มสีชั้นนอกที่แห้งก่อนกักไว้ข้างใต้ แล้วดันทะลุผิวออกมา",
                "พ่นเคลียร์หนาเกินไปในเที่ยวแรก หรือไม่เว้นระยะ Flash-off",
                "ใช้ทินเนอร์แห้งเร็วเกินไปในขณะที่อากาศร้อนจัด"
            ),
            prevention = listOf(
                "พ่นเที่ยวแรกแบบ Medium Wet อย่าพ่นฉ่ำหนาเกินไป",
                "เลือกใช้ทินเนอร์แห้งช้า (Slow Thinner) ในช่วงหน้าร้อน",
                "เว้นระยะเวลารอหมาด 10-15 นาทีก่อนพ่นเที่ยวถัดไป"
            ),
            solution = "ขัดน้ำด้วยกระดาษทรายเบอร์ #1200 - #1500 จนรูตามดหายไป แล้วพ่นเคลียร์ 2K ทับใหม่อีก 1 เที่ยว",
            severity = "ปานกลาง"
        ),
        PaintDefect(
            defectNameTh = "สีเยิ้ม / ไหลย้อย (Runs & Sags)",
            defectNameEn = "Runs and Sags",
            symptom = "สีหรือแลกเกอร์ไหลย้อยเป็นหยดน้ำหรือเป็นแนวสันตามแนวดิ่งของชิ้นงาน",
            causes = listOf(
                "ถือกาพ่นสีใกล้ชิ้นงานเกินไป (<15 ซม.)",
                "เดินมือกาพ่นสีช้าเกินไป หรือพ่นแช่จุดเดิมนาน",
                "ผสมทินเนอร์มากเกินไปจนสีเหลวเกินพิกัด",
                "แรงดันลมต่ำเกินไปทำให้สีกองเป็นเม็ดใหญ่"
            ),
            prevention = listOf(
                "รักษาระยะห่างกา 15-20 ซม. และความเร็วเดินมือให้สม่ำเสมอ 90 องศากับผิวงาน",
                "ผสมอัตราส่วนทินเนอร์ตามสเปค 10-15% ไม่เกินนี้",
                "ปรับแรงดันลมให้ได้ 2.0 - 2.2 Bar"
            ),
            solution = "รอให้สีแห้งสนิท 24-48 ชม. ใช้บล็อกแข็งขัดด้วยกระดาษทราย #1500 เฉพาะสันที่ย้อยจนเรียบเสมอผิวเดิม แล้วปั่นชักเงา",
            severity = "ปานกลาง"
        ),
        PaintDefect(
            defectNameTh = "ผิวเปลือกส้ม / ผิวมะระ (Orange Peel)",
            defectNameEn = "Orange Peel",
            symptom = "ผิวหน้าฟิล์มสีไม่เรียบตึง มีลวดลายขรุขระคล้ายผิวเปลือกส้ม",
            causes = listOf(
                "ละอองสีไม่แตกตัว (แรงดันลมต่ำเกินไป หรือหัวพ่นใหญ่เกิน)",
                "ทินเนอร์แห้งเร็วเกินไป สีแห้งตัวก่อนที่จะไหลแผ่เรียบ (Poor Flow-out)",
                "ถือกาพ่นสีห่างจากชิ้นงานมากเกินไป (>25 ซม.)"
            ),
            prevention = listOf(
                "เพิ่มแรงดันลม หรือปรับขนาดเม็ดสีให้เล็กลง",
                "ใช้ทินเนอร์เกรดแห้งช้า เพื่อให้เนื้อแลกเกอร์มีเวลาไหลปรับระนาบตัวเอง (Self-Leveling)",
                "รักษาระยะห่างในการพ่นให้พอเหมาะ 15-20 ซม."
            ),
            solution = "ขัดกระดาษทรายน้ำเบอร์ #1500 และ #2000 ลูบผิวส้มออกให้เรียบ แล้วใช้เครื่องขัดโรตารี่ปั่นยาขัดชักเงา",
            severity = "ต่ำ"
        ),
        PaintDefect(
            defectNameTh = "ฝ้าขาว / สีด้านขุ่น (Blushing / Moisture Bloom)",
            defectNameEn = "Moisture Blushing",
            symptom = "ฟิล์มสีหรือแลกเกอร์เกิดฝ้าขาวขุ่น ไม่มีความเงาใสหลังพ่นเสร็จ",
            causes = listOf(
                "พ่นสีในวันที่ความชื้นในอากาศสูง (>80%) หรือฝนตก",
                "มีน้ำหรือละอองความชื้นปนมากับสายลมจากปั๊มลม (ไม่ได้เดรนน้ำในถังลม)",
                "ใช้ทินเนอร์ระเหยเร็วเกินไปทำให้อุณหภูมิผิวงานเย็นจัดจนดึงไอน้ำในอากาศมาเกาะ"
            ),
            prevention = listOf(
                "ติดตั้งตัวดักน้ำ/กรองลม (Air Filter & Water Trap) ก่อนเข้ากาพ่นสี",
                "เดรนน้ำออกจากถังปั๊มลมทุกวันก่อนเริ่มงาน",
                "หลีกเลี่ยงการพ่นสีในวันฝนตก หรือใช้ทินเนอร์ช้าพิเศษผสมสารกันฝ้า (Retarder Thinner)"
            ),
            solution = "หากเกิดขณะพ่น ให้พ่นทินเนอร์ช้าหรือ Blender Thinner โปรยบางๆ ทันทีเพื่อไล่ความชื้น หากแห้งแล้วต้องขัดผิวแล้วพ่นใหม่",
            severity = "สูง"
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("⚠️ ข้อควรระวังความปลอดภัยในห้องพ่นสี (Safety First)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                        Text("• สวมหน้ากากกรองสารเคมีคาร์บอน (Organic Vapor Mask) เสมอ ฮาร์ดเดนเนอร์ 2K มีสาร Isocyanate ที่เป็นอันตรายต่อปอด\n• ดับประกายไฟ ห้ามสูบบุหรี่ในบริเวณพ่นสี\n• ต่อสายดินปั๊มลมและกาพ่นสีป้องกันไฟฟ้าสถิต", fontSize = 11.sp, color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f))
                    }
                }
            }
        }

        items(defects) { defect ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            defect.defectNameTh,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (defect.severity == "สูง") MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                "ความรุนแรง: ${defect.severity}",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (defect.severity == "สูง") MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text("🔍 อาการ: ${defect.symptom}", fontSize = 12.sp, fontWeight = FontWeight.Medium)

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("❌ สาเหตุที่พบบ่อย:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                    defect.causes.forEach { cause ->
                        Text("• $cause", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("✅ วิธีป้องกัน:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    defect.prevention.forEach { prev ->
                        Text("• $prev", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Build, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("🛠️ วิธีแก้ไขหน้างาน: ${defect.solution}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.tertiary)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}
