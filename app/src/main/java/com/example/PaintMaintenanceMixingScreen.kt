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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// =========================================================================
// DATA MODELS: Paint Maintenance, Mixing, Safety & Tool Glossary
// =========================================================================

data class ColorMatchingStep(
    val id: Int,
    val titleTh: String,
    val titleEn: String,
    val principle: String,
    val procedure: List<String>,
    val warning: String,
    val proTip: String
)

data class TintAdjustmentRule(
    val scenario: String,
    val problem: String,
    val adjustmentAction: String,
    val cautionaryNote: String
)

data class SurfaceRepairStage(
    val stageNumber: Int,
    val stageNameTh: String,
    val stageNameEn: String,
    val objective: String,
    val requiredGritTools: String,
    val stepInstructions: List<String>,
    val qualityCheckPoint: String
)

data class ChemicalSafetyItem(
    val chemicalName: String,
    val hazardType: String,
    val exposureRisk: String,
    val requiredPPE: String,
    val safeHandlingRules: List<String>,
    val emergencyFirstAid: String
)

data class PaintToolGlossaryItem(
    val toolNameTh: String,
    val toolNameEn: String,
    val category: String, // "ปืนพ่นสี", "ระบบลมและกรอง", "เครื่องมือขัดผิว", "อุปกรณ์ผสมและวัด", "อุปกรณ์ตรวจสอบและขัดเงา"
    val purpose: String,
    val technicalSpec: String,
    val maintenanceTip: String
)

// =========================================================================
// MAIN COMPOSABLE: PaintMaintenanceMixingScreen
// =========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaintMaintenanceMixingScreen(
    onBack: () -> Unit = {}
) {
    var selectedSection by remember { mutableStateOf(0) }
    val sectionTabs = listOf(
        "🎨 เทคนิคเทียบสี",
        "🛠️ ขั้นตอนซ่อมผิวโปร",
        "☣️ ความปลอดภัยสารเคมี",
        "🧰 อภิธานศัพท์เครื่องมือ",
        "🧪 เครื่องคำนวณ 2K"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header Banner
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
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "ย้อนกลับ")
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.ColorLens,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                if (AppSettings.isThai) "🎨 คู่มือทำสี & ผสมสี 2K"
                                else "🎨 Paint Maintenance & Mixing",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                if (AppSettings.isThai) "เทคนิคเทียบสี, ซ่อมผิว และเครื่องคำนวณ"
                                else "Color matching, repair & calculator",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Category Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedSection,
                    edgePadding = 0.dp,
                    containerColor = Color.Transparent,
                    divider = {}
                ) {
                    sectionTabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedSection == index,
                            onClick = { selectedSection = index },
                            text = {
                                Text(
                                    title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedSection == index) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag("pmm_tab_$index")
                        )
                    }
                }
            }
        }

        // Section Content
        AnimatedContent(
            targetState = selectedSection,
            label = "PMMSectionAnimation"
        ) { sectionIndex ->
            when (sectionIndex) {
                0 -> ColorMatchingTechniquesSection()
                1 -> ProfessionalSurfaceRepairSection()
                2 -> ChemicalSafetyPrecautionsSection()
                3 -> PaintToolsGlossarySection()
                4 -> PaintMixingCalculatorTab()
            }
        }
    }
}

@Composable
fun ColorMatchingTechniquesSection() {
    var selectedSubCategory by remember { mutableStateOf(0) }
    val subTabs = listOf("กระบวนการเทียบสี 5 ขั้นตอน", "ตารางปรับจูนแม่สี (Tinting Rules)", "การตรวจแสง & เมทาเมอริซึม")

    val matchingSteps = listOf(
        ColorMatchingStep(
            id = 1,
            titleTh = "ค้นหารหัสสีโรงงาน (OEM Color Code Lookup)",
            titleEn = "Identify OEM Paint Code",
            principle = "เริ่มต้นด้วยสูตรมาตรฐานจากผู้ผลิตเสมอ เพื่อลดความคลาดเคลื่อนของเบสสี",
            procedure = listOf(
                "ตรวจดูป้ายสติกเกอร์รหัสสีใต้เบาะรถ (เช่น Honda Wave/PCX มักติดไว้ใต้ถังน้ำมันหรือโครงเบาะ)",
                "เทียบชื่อรหัสสีมาตรฐาน เช่น R-340C (Candy Rosy Red), NH-B01M (Matte Bullet Silver), PB-325C (Candy Lightning Blue)",
                "ตรวจสอบประวัติการเปลี่ยนสีหรือการซีดจางจากแสงแดด หากรถใช้งานมานานกว่า 3 ปี สีจริงจะสว่างขึ้น 5-10%"
            ),
            warning = "อย่าผสมสีตามความจำหรือดูจากหน้าจอมือถือเด็ดขาด เพราะค่า Gamma จอภาพไม่ตรงกับเนื้อสีจริง",
            proTip = "หากไม่มีรหัสสี ให้ถอดฝาครอบชิ้นส่วนเล็กๆ ที่ไม่โดนแดด (เช่น ฝาปิดแบตเตอรี่ด้านใน) ไปใช้เป็นชิ้นงานอ้างอิง"
        ),
        ColorMatchingStep(
            id = 2,
            titleTh = "วิเคราะห์มิติสี (Hue, Value, Chroma & Metamerism)",
            titleEn = "Analyze Color Dimensions",
            principle = "สีทุกเฉดประกอบด้วย 3 มิติหลัก: โทนสี (Hue), ความสว่าง/มืด (Value), และความสด/อิ่มตัว (Chroma)",
            procedure = listOf(
                "ตรวจดูโทนสีหลัก (Hue): สีนี้เอียงไปทางโทนแดง เหลือง หรือน้ำเงิน",
                "ตรวจดูความสว่าง (Value): มืดกว่าหรือสว่างกว่าสีเดิม",
                "ตรวจดูความสด (Chroma/Purity): สีดูสดใสหรือดูตุ่น/หม่น",
                "ตรวจดูชนิดเม็ดสีเอฟเฟกต์ (Face & Flop): ดูมุมตรง (Face) สว่างแค่ไหน และดูมุมเฉียง 45 องศา (Flop) มืดลงหรือเปลี่ยนสีอย่างไร"
            ),
            warning = "สีมุกและสีเมทัลลิก แรงดันลมและระยะพ่นจะเปลี่ยนมิติความสว่างทันที (พ่นลมแรง/ระยะห่าง = สีสว่างขึ้น, พ่นชิด/เปียก = สีมืดลง)",
            proTip = "มองชิ้นงานโดยหรี่ตาเล็กน้อย จะช่วยให้สายตาตัดความเงาสะท้อนออก และโฟกัสที่ความสว่าง (Value) ได้ชัดเจนที่สุด"
        ),
        ColorMatchingStep(
            id = 3,
            titleTh = "การทำแผ่นพ่นทดสอบเปรียบเทียบ (Spray-Out Test Cards)",
            titleEn = "Spray-Out Test Card Verification",
            principle = "ต้องพ่นทดสอบบนแผ่นทดสอบและเคลือบแลกเกอร์เงา 2K ก่อนพ่นชิ้นงานจริงทุกครั้ง",
            procedure = listOf(
                "ใช้แผ่นพ่นทดสอบโลหะหรือพลาสติกที่มีตารางตารางขาว-ดำ (Checkered Contrast Card) เพื่อเช็คการกลบมิดของสี",
                "พ่นสีรองพื้นชนิดเดียวกับที่จะใช้บนชิ้นงานจริง",
                "พ่นสีจริงตามจำนวนเที่ยวที่กำหนด (เช่น 2 เที่ยว และ 3 เที่ยว คนละฝั่งของแผ่น)",
                "พ่นแลกเกอร์ 2K ทับและรอแห้งสนิท เพราะแลกเกอร์จะทำให้สีดูเข้มขึ้นและมุกสะท้อนแสงชัดเจนขึ้น"
            ),
            warning = "ห้ามเทียบสีในขณะที่สียังเปียก (Wet State) สีเปียกจะสว่างกว่าสีที่แห้งและเคลือบแลกเกอร์แล้วอย่างมีนัยสำคัญ",
            proTip = "เจาะรูตรงกลางแผ่นพ่นทดสอบ แล้วนำไปทาบแนบสนิทกับตัวถังรถเดิม เพื่อมองเปรียบเทียบรอยต่อแบบไร้ช่องว่าง"
        ),
        ColorMatchingStep(
            id = 4,
            titleTh = "การปรับจูนแม่สีแบบทีละหยด (Micro-Tinting Process)",
            titleEn = "Micro-Tinting & Adjustment",
            principle = "เติมแม่สีปรับแก้ทีละ 0.5% - 1% เสมอ เพราะการใส่เกินจะแก้ไขกลับคืนได้ยากมาก",
            procedure = listOf(
                "จดบันทึกน้ำหนักที่ชั่งด้วยเครื่องชั่งทศนิยม 2 ตำแหน่งทุกครั้งที่มีการเติมแม่สี",
                "หากสีสว่างไป: เติมแม่สีโทนหลักที่เข้มขึ้น หรือเติมดำโปร่งแสงทีละหยด",
                "หากสีมืดไป: เติมแม่สีขาว หรือแม่สีหลักที่มีความสว่าง (ห้ามเติมขาวมากเกินไปเพราะสีจะขุ่น/หม่น)",
                "หากสีสดเกินไป: เติมแม่สีคู่ตรงข้าม (Complementary color) เล็กน้อยเพื่อเบรกความสด"
            ),
            warning = "อย่าใช้สีดำทึบแก้สีมืด/สว่างในสีมุก เพราะจะทำให้ประกายมุกดับ ให้ใช้แม่สีน้ำเงินเข้มหรือน้ำตาลแทน",
            proTip = "คนกวนสีให้เข้ากันอย่างน้อย 2-3 นาทีหลังเติมแม่สี ก่อนนำไปหยดทดสอบ"
        ),
        ColorMatchingStep(
            id = 5,
            titleTh = "เทคนิคการพ่นกลืนรอยต่อ (Blending Transition)",
            titleEn = "Color Blending Application",
            principle = "แม้สีจะเทียบได้แม่นยำ 95% แต่การพ่นตัดขอบแข็งจะมองเห็นรอยต่อได้ การพ่นเบลนด์จึงเป็นหัวใจของงานซ่อมมืออาชีพ",
            procedure = listOf(
                "พ่นสีจริงกลบมิด 100% เฉพาะบริเวณแผลที่ลงรองพื้นไว้",
                "เที่ยวถัดไปให้ขยายรัศมีพ่นออกไป 5-10 ซม. โดยลดแรงกดไกปืนและสะบัดข้อมือออก (Flick-out motion)",
                "เที่ยวสุดท้ายพ่นละอองโปรย (Drop coat) ด้วยแรงดันลมต่ำและระยะห่าง 30 ซม. เพื่อเรียงเกล็ดมุกให้กลืนกับสีเดิม",
                "เคลือบแลกเกอร์ 2K เต็มชิ้นงานเพื่อลบรอยต่อสายตา"
            ),
            warning = "ห้ามพ่นสีจริงเต็มทั้งชิ้นงานหากเป็นการซ่อมเฉพาะจุด เพราะจะทำให้เห็นความต่างระหว่างชิ้นส่วนข้างเคียงชัดเจน",
            proTip = "ใช้น้ำยาเบลนเดอร์ (Blender / Wet-bed resin) พ่นรองพื้นบางๆ ก่อนพ่นสีจริง จะช่วยให้เม็ดมุกไหลกลืนเข้าเนื้อสีเดิมได้อย่างไร้ที่ติ"
        )
    )

    val tintingRules = listOf(
        TintAdjustmentRule(
            scenario = "สีออกแดงเกินไป (Too Red)",
            problem = "เฉดสีมีอันเดอร์โทนอมชมพูหรือแดงเข้มกว่าตัวถังเดิม",
            adjustmentAction = "เติมแม่สีเขียว (Green) เล็กน้อยเพื่อหักล้างสีแดง หรือเติมเหลือง/น้ำเงินตามโทนสีหลัก",
            cautionaryNote = "เติมทีละ 0.5% เพราะแม่สีเขียวมีพลังกลบสีสูงมาก"
        ),
        TintAdjustmentRule(
            scenario = "สีออกเหลืองเกินไป (Too Yellow)",
            problem = "สีดูอมส้มหรือเหลืองเลี่ยน ไม่คมใส",
            adjustmentAction = "เติมแม่สีม่วง (Violet) เพื่อหักล้างสีเหลือง หรือเติมน้ำเงินโปร่งแสง",
            cautionaryNote = "หากเป็นสีขาวมุก การเติมม่วงมากไปจะทำให้ขาวดูหมองเป็นสีควันบุหรี่"
        ),
        TintAdjustmentRule(
            scenario = "สีดูมืด/ทึบเกินไป (Too Dark)",
            problem = "เมื่อมองมุมตรง สีเข้มกว่าชิ้นงานเดิม",
            adjustmentAction = "เติมแม่สีเบสสว่างของเฉดนั้น หรือเติมบรอนซ์เกล็ดละเอียด (Coarse Aluminum) เล็กน้อย",
            cautionaryNote = "หลีกเลี่ยงการเติมแม่สีขาวเดี่ยวๆ ในสีเมทัลลิก เพราะเกล็ดจะจมและสีจะดูขุ่นด้าน"
        ),
        TintAdjustmentRule(
            scenario = "สีดูสดใสเกินไป / ไม่กลืนกับสีเก่า (Too Clean/Vibrant)",
            problem = "สีใหม่ดูสดเด้งกว่าสีรถเดิมที่ผ่านการใช้งานมานาน",
            adjustmentAction = "เติมแม่สีคู่ตรงข้าม (Complementary) หรือเติมดำโปร่งแสง/น้ำตาลทึบ 1-2 หยด เพื่อเบรกความสดให้ออกโทนธรรมชาติ",
            cautionaryNote = "อย่าใช้สีเทาขุ่น เพราะจะทำให้ประกายเงาหายไป"
        ),
        TintAdjustmentRule(
            scenario = "เม็ดมุก/เมทัลลิกมืดเมื่อมองมุมเอียง (Flop too dark)",
            problem = "มุมตรงสีตรง แต่เมื่อมองมุมเฉียง 45 องศา สีกลับมืดดำเกินไป",
            adjustmentAction = "เติม Flop Adjuster / Matting Base หรือปรับเทคนิคพ่นให้แห้งขึ้น (Dry coat) และเพิ่มแรงดันลม",
            cautionaryNote = "การพ่นเปียกเกินไปจะทำให้เม็ดมุกนอนตัวแบนและสะท้อนแสงเฉพาะมุมตรง"
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Sub Tabs selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                subTabs.forEachIndexed { idx, title ->
                    val isSel = selectedSubCategory == idx
                    Button(
                        onClick = { selectedSubCategory = idx },
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        contentPadding = PaddingValues(2.dp)
                    ) {
                        Text(
                            title.split(" ")[0],
                            fontSize = 11.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        when (selectedSubCategory) {
            0 -> {
                item {
                    Text(
                        "🎯 ขั้นตอนการเทียบสีมาตรฐานโรงงาน (Step-by-Step Color Matching)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                items(matchingSteps) { step ->
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
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("${step.id}", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(step.titleTh, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text("💡 หลักการ: ${step.principle}", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.tertiary)

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("ขั้นตอนปฏิบัติการ:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            step.procedure.forEach { p ->
                                Text("• $p", fontSize = 12.sp, lineHeight = 17.sp, modifier = Modifier.padding(vertical = 1.dp))
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                            ) {
                                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("ข้อควรระวัง: ${step.warning}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            ) {
                                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Pro Tip ช่างบอย: ${step.proTip}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                item {
                    Text(
                        "🧪 ตารางสูตรการปรับแก้แม่สี (Color Tinting & Correction Matrix)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                items(tintingRules) { rule ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("🔍 อาการ: ${rule.scenario}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("ปัญหา: ${rule.problem}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("✅ วิธีปรับแต่งแม่สี:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.tertiary)
                                    Text(rule.adjustmentAction, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text("⚠️ คำเตือน: ${rule.cautionaryNote}", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
            2 -> {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.WbSunny, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("☀️ การตรวจสอบแสง & ภาวะเมทาเมอริซึม (Metamerism)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "เมทาเมอริซึม (Metamerism) คือปรากฏการณ์ที่ 'สีดูเหมือนกันเป๊ะเมื่ออยู่ในห้องพ่นสีใต้หลอดไฟนีออน แต่พอนำรถออกไปกลางแดด สีกลับเพี้ยนคนละเฉด'",
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Text("💡 กฎการตรวจเช็คแสงที่ถูกต้อง:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("1. แสงธรรมชาติเวลากลางวัน (Daylight D65): ตรวจดูในช่วงเวลา 10:00 - 14:00 น. ในที่ร่มที่มีแสงแดดส่องถึงสม่ำเสมอ (หลีกเลี่ยงแดดจัดจ้าตรงๆ)", fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
                            Text("2. ไฟตรวจสีความแม่นยำสูง (High CRI LED Sun Gun): ใช้ไฟฉายตรวจสีที่มีค่า CRI ≥ 95 และปรับอุณหภูมิสีได้ 3 ระดับ (2700K Warm, 4500K Neutral, 6500K Daylight)", fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
                            Text("3. ตรวจสอบ 3 มุมมองเสมอ: มุมตรง 90° (Face), มุมเฉียง 45° (Mid), และมุมมองเกือบขนาน 15° (Grazing/Flop)", fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

// =========================================================================
// SECTION 2: PROFESSIONAL SURFACE REPAIR STEPS (ขั้นตอนซ่อมผิวระดับมืออาชีพ)
// =========================================================================

@Composable
fun ProfessionalSurfaceRepairSection() {
    val repairStages = listOf(
        SurfaceRepairStage(
            stageNumber = 1,
            stageNameTh = "การล้างทำความสะอาด & ขจัดคราบไขมัน (Degreasing & Prep)",
            stageNameEn = "Decontamination & Wax/Grease Removal",
            objective = "กำจัดคราบซิลิโคน แว็กซ์ น้ำมันโซ่ และสิ่งสกปรกฝังแน่น 100% ก่อนลงมือขัด",
            requiredGritTools = "น้ำยาซิลิโคนรีมูฟเวอร์ (Wax & Grease Remover) + ผ้าไมโครไฟเบอร์สะอาด 2 ผืน (เช็ดเปียก 1 ผืน / เช็ดแห้งทันที 1 ผืน)",
            stepInstructions = listOf(
                "ล้างชิ้นงานด้วยน้ำผสมแชมพูล้างรถ ขจัดเศษดินโคลนและฝุ่นผงให้หมด",
                "ชโลมน้ำยา Wax & Grease Remover ให้ทั่วบริเวณแผลและพื้นที่โดยรอบ",
                "ใช้ผ้าผืนแรกเช็ดละลายคราบ และใช้ผ้าแห้งผืนที่สองเช็ดตามทันทีก่อนน้ำยาระเหย (เทคนิค Two-Cloth Method)",
                "ห้ามใช้ทินเนอร์ 3A เช็ดล้างคราบไขมัน เพราะทินเนอร์จะละลายคราบแว็กซ์ให้ฝังลึกลงในรอยขูดขีด"
            ),
            qualityCheckPoint = "หยดน้ำสะอาดลงบนผิว หากน้ำแผ่เป็นฟิล์มราบเรียบแปลว่าสะอาดบริสุทธิ์ หากน้ำยังจับตัวเป็นเม็ดกลมแปลว่ายังมีคราบแว็กซ์ตกค้าง"
        ),
        SurfaceRepairStage(
            stageNumber = 2,
            stageNameTh = "การขัดเปิดแผล & สร้างขอบสโลปขนนก (Featheredging)",
            stageNameEn = "Substrate Sanding & Featheredging",
            objective = "ลบสันคมของชั้นสีเดิมให้ลาดเอียงเป็นขอบสโลปนุ่มนวล เพื่อไม่ให้เกิดรอยยุบตัวหลังพ่น",
            requiredGritTools = "กระดาษทรายแห้ง #120 -> #240 -> #320 + บล็อกขัดยางแข็ง",
            stepInstructions = listOf(
                "ขัดเปิดรอยแผลลึกจนถึงเนื้อชิ้นงานเดิม (โลหะหรือพลาสติก) ด้วยกระดาษทราย #180",
                "ขัดขยายรัศมีรอบแผลออกไป 3-5 ซม. เพื่อทำขอบสโลปขนนก (Featheredge) โดยไล่เบอร์ #240 และ #320",
                "รอยต่อระหว่างเนื้อเหล็ก/พลาสติก -> สีรองพื้นเดิม -> สีจริงเดิม -> เคลียร์เดิม ต้องเรียบเนียนไร้สันสะดุดเมื่อลูบด้วยปลายนิ้ว",
                "เป่าลมไล่ฝุ่นผงและเช็ดด้วยผ้าเหนียว (Tack Rag)"
            ),
            qualityCheckPoint = "หลับตาแล้วใช้ปลายนิ้วเปลือยลูบผ่านรอยต่อขอบแผล ต้องไม่รู้สึกถึงสันคมหรือรอยกระโดดของชั้นสีเดิม"
        ),
        SurfaceRepairStage(
            stageNumber = 3,
            stageNameTh = "การโป๊วปรับระนาบ & ขัดบล็อกระนาบ (2K Body Filler & Leveling)",
            stageNameEn = "2K Polyester Putty & Block Leveling",
            objective = "เติมเต็มหลุมรอยบุบ รอยตามด และปรับระนาบความโค้งมนของตัวถังให้คืนรูปเดิม 100%",
            requiredGritTools = "สีโป๊วพลาสติก 2K (Polyester Putty) + ฮาร์ดเดนเนอร์ 2-3% + ยางปาดสีโป๊ว + บล็อกขัดระนาบ + ผงไกด์โค้ทดำ (Dry Guide Coat)",
            stepInstructions = listOf(
                "ผสมสีโป๊วกับฮาร์ดเดนเนอร์ให้เข้ากันเป็นเนื้อเดียวภายใน 1 นาที (ระวังอย่ากวนจนเกิดฟองอากาศ)",
                "ปาดเที่ยวแรกด้วยแรงกดแน่นเพื่อไล่อากาศและสร้างแรงยึดเกาะในร่องลึก",
                "ปาดเที่ยวที่สองและสามสร้างความหนานูนขึ้นมาจากระนาบเดิมประมาณ 1 มม. รอแห้งตัว 20-30 นาที",
                "ทาผงไกด์โค้ทสีดำให้ทั่วบริเวณโป๊ว",
                "ใช้บล็อกขัดระนาบยาวขัดแบบไขว้ทแยงมุม 45 องศา (X-Pattern) ด้วยกระดาษทรายแห้ง #180 -> #240 -> #320",
                "เมื่อผงไกด์โค้ทถูกขัดออกจนหมด ระนาบจะเรียบตรงสมบูรณ์แบบ"
            ),
            qualityCheckPoint = "หากยังมีจุดดำของผงไกด์โค้ทหลงเหลืออยู่ แสดงว่าเป็นหลุมต่ำ ต้องปาดสีโป๊วเก็บซ้ำเฉพาะจุด"
        ),
        SurfaceRepairStage(
            stageNumber = 4,
            stageNameTh = "การพ่นรองพื้น 2K กลบรอย & ขัดเนียน (2K High-Build Primer Surfacer)",
            stageNameEn = "2K Primer Surfacer & Wet Sanding",
            objective = "กลบรอยเส้นกระดาษทราย ปิดกั้นการดูดซึมสี และสร้างฟิล์มรองพื้นที่เรียบเนียนดุจกระจก",
            requiredGritTools = "สีรองพื้น 2K High-Build (4:1) + กาพ่นหัว 1.6-1.8mm + กระดาษทรายน้ำ #800 และ #1000 + บล็อกฟองน้ำนุ่ม",
            stepInstructions = listOf(
                "พ่นรองพื้น 2K เที่ยวแรกแบบโปรยบางๆ พัก 10 นาที",
                "พ่นเที่ยวที่ 2 และ 3 แบบเปียกชุ่มปานกลาง ทิ้งระยะระหว่างเที่ยว 10-15 นาที",
                "ปล่อยให้สีรองพื้นแห้งตัวสมบูรณ์ 3-4 ชั่วโมง หรืออบความร้อน 40 นาที",
                "ทาผงไกด์โค้ทดำบางๆ บนผิวรองพื้น",
                "ใช้น้ำผสมแชมพูเล็กน้อย ขัดลูบด้วยกระดาษทรายน้ำ #800 ตามด้วย #1000 จนผงไกด์โค้ทหมด ผิวจะเนียนนุ่มไม่มีรอยเส้นทราย"
            ),
            qualityCheckPoint = "ส่องไฟตรวจดูว่าไม่มีรูตามด (Pinholes) และผิวสัมผัสต้องลื่นเรียบเสมอกันทุกจุด"
        ),
        SurfaceRepairStage(
            stageNumber = 5,
            stageNameTh = "การพ่นสีจริง & การเรียงเม็ดมุก (Basecoat & Flake Orientation)",
            stageNameEn = "Basecoat & Pearl/Metallic Layering",
            objective = "พ่นเฉดสีจริงให้กลบมิดสม่ำเสมอ และจัดเรียงเกล็ดมุก/บรอนซ์เงินให้สะท้อนแสงตรงตามโรงงาน",
            requiredGritTools = "กาพ่นสีหัว 1.2-1.3mm + แรงดันลม 2.0 Bar + ผ้าเหนียวเช็ดฝุ่น (Tack Cloth)",
            stepInstructions = listOf(
                "เช็ดผิวด้วยผ้าเหนียว Tack Cloth เบาๆ เพื่อดึงละอองฝุ่นเม็ดสุดท้ายออกก่อนพ่น",
                "พ่นเที่ยวที่ 1 แบบโปรย 50-60% (Dust coat) พัก 10 นาที",
                "พ่นเที่ยวที่ 2 แบบกลบมิด 100% สม่ำเสมอ รักษาระยะห่าง 15-20 ซม. ความเร็วเดินมือคงที่",
                "เที่ยวสุดท้ายสำหรับสีมุก/เมทัลลิก: ถอยระยะห่างเป็น 25-30 ซม. พ่นโปรยเม็ดมุกกระจายตัว (Drop coat) เพื่อไม่ให้เกล็ดนอนแบนหรือเกิดรอยทางม้าลาย (Tiger stripes)",
                "พักแห้งตัว Flash-off 15-20 นาทีก่อนเริ่มเคลือบเงา"
            ),
            qualityCheckPoint = "ฟิล์มสีจริงต้องแห้งด้านสนิท ห้ามเอามือแตะ และไม่มีเม็ดฝุ่นขนาดใหญ่"
        ),
        SurfaceRepairStage(
            stageNumber = 6,
            stageNameTh = "การพ่นเคลียร์ 2K & ขัดเงาระดับโชว์รูม (2K Clearcoat & Mirror Polish)",
            stageNameEn = "2K High-Gloss Clearcoat & Wet Sanding/Buffing",
            objective = "สร้างเกราะป้องกัน UV ป้องกันรอยขีดข่วน และให้ความเงางามฉ่ำลึกดุจผิวกระจก",
            requiredGritTools = "แลกเกอร์ 2K High Solid (2:1 หรือ 4:1) + กระดาษทรายน้ำ #2000/#3000 + เครื่องขัดโรตารี่/DA + น้ำยาขัดหยาบและละเอียด",
            stepInstructions = listOf(
                "พ่นแลกเกอร์ 2K เที่ยวแรกแบบกึ่งเปียก (Medium Wet) เพื่อสร้างการยึดเกาะ พัก 10-15 นาที",
                "พ่นเที่ยวที่ 2 แบบเปียกฉ่ำเต็มที่ (Full Gloss Wet) เดินมือสม่ำเสมอ ซ้อนทับแนวพ่น 50%",
                "ทิ้งให้แห้งสนิทข้ามคืน 24-48 ชั่วโมง",
                "ขั้นตอนเก็บผิวส้ม/ฝุ่น: ใช้กระดาษทรายน้ำ #2000 และ #3000 ลูบยอดผิวส้มและขี้ฝุ่นออกเบาๆ ด้วยบล็อกยาง",
                "ปั่นชักเงาด้วยขนแกะแท้ + ยาขัดหยาบ (Heavy Cut Compound) รอบเครื่อง 1200-1500 RPM",
                "ปิดท้ายด้วยฟองน้ำนุ่ม + ยาขัดเงาละเอียด (Finishing Polish) เพื่อลบรอยวง Hologram"
            ),
            qualityCheckPoint = "ส่องไฟมองภาพสะท้อน จะเห็นเส้นหลอดไฟคมกริบไร้รอยคลื่นและไร้รอยวงขนแมว"
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
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("🛠️ 6 ขั้นตอนซ่อมผิวตัวถังและทำสีมาตรฐานช่างศูนย์", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("ปฏิบัติตามลำดับขั้นตอนอย่างเคร่งครัดเพื่อป้องกันปัญหาสีย่น สีพอง ตามด และรอยยุบตัวในระยะยาว", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }

        items(repairStages) { stage ->
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
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        "${stage.stageNumber}",
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(stage.stageNameTh, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                                Text(stage.stageNameEn, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("🎯 เป้าหมาย: ${stage.objective}", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.tertiary)

                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Build, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("อุปกรณ์/เบอร์ทราย: ${stage.requiredGritTools}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("ขั้นตอนการปฏิบัติ:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    stage.stepInstructions.forEach { inst ->
                        Text("• $inst", fontSize = 12.sp, lineHeight = 17.sp, modifier = Modifier.padding(vertical = 1.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("จุดตรวจคุณภาพ (QC Check): ${stage.qualityCheckPoint}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

// =========================================================================
// SECTION 3: SAFETY PRECAUTIONS FOR HANDLING CHEMICALS (ความปลอดภัยสารเคมี)
// =========================================================================

@Composable
fun ChemicalSafetyPrecautionsSection() {
    val chemicalRisks = listOf(
        ChemicalSafetyItem(
            chemicalName = "ฮาร์ดเดนเนอร์ 2K (Isocyanates / ตัวเร่งแข็ง)",
            hazardType = "สารก่อมะเร็ง & ทำลายระบบทางเดินหายใจรุนแรง",
            exposureRisk = "ไอระเหยของ Isocyanate ไม่มีกลิ่นเตือนชัดเจน สามารถสะสมในปอด ทำให้เกิดโรคหอบหืดรุนแรง หลอดลมอักเสบเรื้อรัง และแพ้สัมผัสทางผิวหนัง",
            requiredPPE = "หน้ากากกรองไอสารอินทรีย์ระดับ A2P3 หรือหน้ากากจ่ายอากาศบริสุทธิ์ (Air-fed Respirator) + ถุงมือไนไตรล์ + ชุดพ่นสีคลุมมิดชิด",
            safeHandlingRules = listOf(
                "ห้ามใช้หน้ากากผ้าหรือหน้ากาก N95 ทั่วไปเด็ดขาด เพราะไม่สามารถกรองไอระเหย Isocyanate ได้",
                "ปิดฝากระป๋องฮาร์ดเดนเนอร์ให้สนิททันทีหลังตวง เพราะสารจะทำปฏิกิริยากับความชื้นในอากาศแล้วแข็งตัวและปล่อยก๊าซ",
                "ห้ามสูดดมไอระเหยโดยตรงขณะเทผสม"
            ),
            emergencyFirstAid = "หากสูดดม: รีบย้ายผู้ป่วยไปยังที่ที่มีอากาศบริสุทธิ์ทันที หากหายใจติดขัดให้นำส่งแพทย์พร้อมฉลากสินค้า\nหากเข้าตา: ล้างด้วยน้ำสะอาดไหลผ่านต่อเนื่องอย่างน้อย 15 นาที"
        ),
        ChemicalSafetyItem(
            chemicalName = "ทินเนอร์ 2K & ตัวทำละลายอินทรีย์ระเหยง่าย (VOCs)",
            hazardType = "สารไวไฟสูง (Flammable) & ทำลายระบบประสาทส่วนกลาง",
            exposureRisk = "ไอระเหยของ Toluene, Xylene, Acetone ติดไฟง่ายมากเมื่อกระทบประกายไฟหรือไฟฟ้าสถิต การสูดดมทำให้เวียนศีรษะ คลื่นไส้ มึนงง และทำลายตับไตในระยะยาว",
            requiredPPE = "หน้ากากคาร์บอนกรองก๊าซพิษ (Organic Vapor Cartridge) + แว่นตา Safety Goggles ป้องกันสารเคมีกระเด็น + ถุงมือไนไตรล์หนา",
            safeHandlingRules = listOf(
                "ห้ามมีประกายไฟ ห้ามสูบบุหรี่ และดับเปลวไฟทุกชนิดในรัศมี 10 เมตร",
                "พัดลมดูดอากาศในห้องพ่นสีต้องเป็นชนิด 'มอเตอร์กันระเบิด' (Explosion-Proof Motor) เท่านั้น",
                "ผ้าขี้ริ้วที่เปื้อนทินเนอร์ต้องทิ้งลงในถังขยะโลหะที่มีฝาปิดมิดชิด เพื่อป้องกันการลุกไหม้ได้เอง (Spontaneous Combustion)"
            ),
            emergencyFirstAid = "หากสัมผัสผิวหนัง: ล้างด้วยสบู่และน้ำสะอาดทันที ห้ามใช้ทินเนอร์ล้างสีที่ติดมือ\nหากกลืนกิน: ห้ามทำให้อาเจียน รีบนำส่งแพทย์ทันที"
        ),
        ChemicalSafetyItem(
            chemicalName = "ฝุ่นละอองจากการขัดสี & สีโป๊ว (Toxic Sanding Dust)",
            hazardType = "ฝุ่นละอองขนาดเล็กทำลายเนื้อเยื่อปอด (Pneumoconiosis)",
            exposureRisk = "ฝุ่นผงเรซิ่น, ตะกั่ว, โครเมียม และใยแก้วในสีโป๊ว สามารถแทรกซึมลึกถึงถุงลมปอด ก่อให้เกิดพังผืดในปอดและการระคายเคืองตาอย่างรุนแรง",
            requiredPPE = "หน้ากากกันฝุ่นละอองมาตรฐาน P2 / N95 ขึ้นไป หรือหน้ากากครึ่งหน้าพร้อมตลับกรองฝุ่นอนุภาคละเอียด",
            safeHandlingRules = listOf(
                "ใช้วิธีขัดน้ำ (Wet Sanding) หรือใช้เครื่องขัดลมที่มีระบบดูดฝุ่นในตัว (Dust Extraction System) เสมอ",
                "ห้ามใช้ลมเป่าฝุ่นฟุ้งกระจายในห้องทำงาน ให้ใช้เครื่องดูดฝุ่นอุตสาหกรรมแทน",
                "ล้างมือและล้างหน้าให้สะอาดก่อนรับประทานอาหารหรือดื่มน้ำ"
            ),
            emergencyFirstAid = "หากฝุ่นเข้าตา: ห้ามขยี้ตา ให้ใช้น้ำยาล้างตาหรือน้ำสะอาดล้างออกเบาๆ จนกว่าจะหายเคืองตา"
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
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("☣️ มาตรฐานความปลอดภัยสารเคมีและสุขภาพช่างพ่นสี", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                        Text("สารเคมีระบบ 2K มีพิษสะสมเฉียบพลันและเรื้อรัง การสวมอุปกรณ์ PPE ที่ได้มาตรฐานช่วยปกป้องชีวิตและปอดของคุณ 100%", fontSize = 11.sp, color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f))
                    }
                }
            }
        }

        items(chemicalRisks) { chem ->
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
                        Text(chem.chemicalName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.error)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                    ) {
                        Text(
                            "อันตราย: ${chem.hazardType}",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("⚠️ ความเสี่ยงต่อสุขภาพ: ${chem.exposureRisk}", fontSize = 12.sp, lineHeight = 17.sp)

                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Masks, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("อุปกรณ์ PPE ที่จำเป็นต้องสวมใส่:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(chem.requiredPPE, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("กฎการปฏิบัติงานเพื่อความปลอดภัย:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    chem.safeHandlingRules.forEach { rule ->
                        Text("• $rule", fontSize = 12.sp, lineHeight = 17.sp, modifier = Modifier.padding(vertical = 1.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MedicalServices, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("การปฐมพยาบาลเบื้องต้น (First Aid):", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(chem.emergencyFirstAid, fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

// =========================================================================
// SECTION 4: GLOSSARY OF SPECIALIZED PAINT TOOLS (อภิธานศัพท์เครื่องมือทำสี)
// =========================================================================

@Composable
fun PaintToolsGlossarySection() {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("ทั้งหมด") }

    val categories = listOf("ทั้งหมด", "ปืนพ่นสี", "ระบบลมและกรอง", "เครื่องมือขัดผิว", "อุปกรณ์ผสมและวัด", "อุปกรณ์ตรวจสอบ")

    val toolsList = listOf(
        PaintToolGlossaryItem(
            toolNameTh = "ปืนพ่นสีระบบ HVLP (High Volume Low Pressure)",
            toolNameEn = "HVLP Spray Gun",
            category = "ปืนพ่นสี",
            purpose = "ปืนพ่นสีแรงดันลมต่ำแต่ปริมาตรลมสูง ถ่ายทอดเนื้อสีลงบนชิ้นงานได้มากกว่า 65-75% ลดการฟุ้งกระจายของละอองสี ประหยัดสีได้สูงสุด",
            technicalSpec = "แรงดันลมที่ด้ามจับ 1.8 - 2.2 Bar / แรงดันลมที่หัวแอร์แคป < 0.7 Bar / อัตรากินลม 250-350 L/min",
            maintenanceTip = "ล้างเข็มหัวฉีดและแอร์แคปทันทีหลังพ่น ห้ามแช่ปืนทั้งกระบอกลงในถังทินเนอร์เพราะซีลยางโอริงจะบวมเปื่อย"
        ),
        PaintToolGlossaryItem(
            toolNameTh = "ปืนพ่นสีมินิ / กาพ่นเก็บขอบ (Touch-Up / Mini Gun)",
            toolNameEn = "Mini Gravity Spray Gun",
            category = "ปืนพ่นสี",
            purpose = "กาพ่นสีขนาดกะทัดรัด ถ้วยบน 100-250cc เหมาะสำหรับงานพ่นซ่อมเฉพาะจุด สาดเบลนด์ขอบสี และพ่นชิ้นส่วนแฟริ่งขนาดเล็ก",
            technicalSpec = "ขนาดหัวฉีด (Nozzle Size) 0.8 - 1.0 mm / ม่านสีแคบ 10-15 ซม.",
            maintenanceTip = "ใช้สำหรับพ่นน้ำยาเบลนเดอร์ประสานขอบและพ่นสีจริงเที่ยวเก็บรายละเอียด"
        ),
        PaintToolGlossaryItem(
            toolNameTh = "หัวฉีดกาพ่นสีขนาดต่างๆ (Spray Nozzle Sizes Guide)",
            toolNameEn = "Fluid Tip & Air Cap Setup",
            category = "ปืนพ่นสี",
            purpose = "• 1.0mm: งานเก็บรอยแผลเฉพาะจุด & แอร์บรัช\n• 1.2-1.3mm: สีจริง Basecoat & สีมุก/เมทัลลิก\n• 1.3-1.4mm: แลกเกอร์เคลียร์ 2K เงาฉ่ำ\n• 1.6-1.8mm: สีรองพื้น 2K High-Build Primer\n• 2.0-2.5mm: สีโป๊วพ่น (Spray Polyester Putty)",
            technicalSpec = "ทำจากสแตนเลสหรือทังสเตนคาร์ไบด์ ป้องกันการกัดกร่อนจากสารเคมี",
            maintenanceTip = "ห้ามใช้ลวดเหล็กหรือเข็มแข็งแยงรูหัวพ่นเด็ดขาด ให้ใช้แปรงขนไนลอนเฉพาะทางเพื่อไม่ให้รูหัวพ่นเสียรูปทรง"
        ),
        PaintToolGlossaryItem(
            toolNameTh = "ชุดกรองดักน้ำ & น้ำมัน 3 สเตจ (3-Stage Air Filter & Dryer)",
            toolNameEn = "3-Stage Coalescing Air Filter & Desiccant",
            category = "ระบบลมและกรอง",
            purpose = "กรองดักละอองน้ำ ละอองน้ำมันเครื่อง และสิ่งสกปรกจากปั๊มลม เพื่อป้องกันปัญหาฝ้าขาว ตามด และฟองอากาศใต้ฟิล์มสี 100%",
            technicalSpec = "Stage 1: กรองน้ำ 5 ไมครอน -> Stage 2: กรองไอน้ำมัน 0.01 ไมครอน -> Stage 3: สารดูดความชื้นซิลิกาเจล",
            maintenanceTip = "เดรนถ่ายน้ำที่ก้นถ้วยกรองทุกวัน และเปลี่ยนเม็ดสารดูดความชื้นเมื่อเปลี่ยนสี"
        ),
        PaintToolGlossaryItem(
            toolNameTh = "เกจวัดแรงดันลมติดท้ายปืน (Digital/Analog Air Regulator)",
            toolNameEn = "Mini Air Pressure Gauge at Gun Inlet",
            category = "ระบบลมและกรอง",
            purpose = "ควบคุมและอ่านค่าแรงดันลมที่เข้าสู่ตัวปืนพ่นสีอย่างแม่นยำขณะเหนี่ยวไกพ่น (Dynamic Pressure)",
            technicalSpec = "ย่านวัด 0 - 10 Bar (0 - 150 PSI) / รองรับข้อต่อสวมเร็ว 1/4 นิ้ว",
            maintenanceTip = "ต้องตั้งค่าแรงดันลมในขณะที่ 'เหนี่ยวไกปืนลมจนสุด' ไม่ใช่ตั้งตอนที่ยังไม่กดไก"
        ),
        PaintToolGlossaryItem(
            toolNameTh = "บล็อกขัดระนาบยางแข็ง & บล็อกฟองน้ำ (Rigid & Soft Sanding Blocks)",
            toolNameEn = "Ergonomic Sanding Blocks & Interface Pads",
            category = "เครื่องมือขัดผิว",
            purpose = "บล็อกแข็งใช้สำหรับขัดปรับระนาบสีโป๊วไม่ให้เป็นคลื่น ส่วนบล็อกนุ่ม/ฟองน้ำใช้สำหรับขัดตามส่วนโค้งเว้าของแฟริ่งมอเตอร์ไซค์",
            technicalSpec = "ความยาว 150 - 400 mm รองรับระบบตีนตุ๊กแก Velcro และแถบหนีบกระดาษทราย",
            maintenanceTip = "ห้ามใช้มือกดขัดโดยตรงบนสีโป๊ว เพราะแรงกดนิ้วมือจะทำให้ผิวเป็นคลื่นหลุมตามแนวนิ้ว"
        ),
        PaintToolGlossaryItem(
            toolNameTh = "ผงไกด์โค้ทดำตรวจสอบระนาบ (Dry Guide Coat Applicator)",
            toolNameEn = "Dry Guide Coat Powder",
            category = "เครื่องมือขัดผิว",
            purpose = "ผงคาร์บอนละเอียดใช้ทาลงบนผิวสีโป๊วหรือสีรองพื้นก่อนขัด เพื่อไฮไลท์ให้เห็นรอยคลื่น หลุมตามด และเส้นรอยขูดขีดได้อย่างชัดเจน",
            technicalSpec = "ผงคาร์บอนแห้งไร้ตัวทำละลาย ไม่ทำให้กระดาษทรายตันหรืออุดตันฟิล์มสี",
            maintenanceTip = "ขัดจนผงไกด์โค้ทหายไปหมด แสดงว่าผิวงานระนาบเสมอกัน 100%"
        ),
        PaintToolGlossaryItem(
            toolNameTh = "ถ้วยตวงอัตราส่วนผสมสี 2K (Calibrated Mixing Cups)",
            toolNameEn = "Graduated Paint Mixing Cup",
            category = "อุปกรณ์ผสมและวัด",
            purpose = "ถ้วยพลาสติกใสพร้อมสเกลตวงอัตราส่วนผสมมาตรฐาน (2:1, 4:1, 5:1) และสเกลเปอร์เซ็นต์ทินเนอร์ในตัว",
            technicalSpec = "ผลิตจากพลาสติก PP ทนทานต่อทินเนอร์ ความจุ 400cc / 650cc / 1300cc",
            maintenanceTip = "ใช้ไม้คนกวนก้นถ้วยและขอบถ้วยให้เข้ากันสนิท เพื่อให้ฮาร์ดเดนเนอร์ทำปฏิกิริยากับเรซิ่นอย่างทั่วถึง"
        ),
        PaintToolGlossaryItem(
            toolNameTh = "ถ้วยวัดความหนืดสี (Ford Viscosity Cup #4 / DIN 4)",
            toolNameEn = "Viscosity Flow Cup",
            category = "อุปกรณ์ผสมและวัด",
            purpose = "ใช้วัดความหนืดของสีและแลกเกอร์โดยจับเวลาการไหล เพื่อปรับสัดส่วนทินเนอร์ให้ได้ขนาดละอองสีที่สมบูรณ์แบบ",
            technicalSpec = "Ford Cup #4 รูขนาด 4.12 mm / เวลาไหลมาตรฐานของแลกเกอร์ 2K อยู่ที่ 16 - 19 วินาที",
            maintenanceTip = "จุ่มถ้วยให้สีเต็ม ยกขึ้นแล้วเริ่มจับเวลาทันที หยุดเวลาเมื่อสายสีขาดสายหยดแรก"
        ),
        PaintToolGlossaryItem(
            toolNameTh = "ไฟฉายตรวจเทียบสีความแม่นยำสูง (High-CRI LED Sun Gun)",
            toolNameEn = "High-CRI 96+ Color Match Inspection Light",
            category = "อุปกรณ์ตรวจสอบ",
            purpose = "ไฟส่องตรวจเฉดสี ตรวจหาเม็ดมุก ตรวจรอยขัดกระดาษทราย รอยคลื่น และรอยวง Hologram เสมือนแสงอาทิตย์จริง",
            technicalSpec = "ค่าดัชนีความถูกต้องของสี CRI ≥ 96 / ปรับอุณหภูมิสีได้ 2700K - 4500K - 6500K / ความสว่าง 500-1000 Lumens",
            maintenanceTip = "ใช้ตรวจเช็คเปรียบเทียบ Spray-Out Card กับตัวถังรถในมุมต่างๆ เพื่อป้องกันเมทาเมอริซึม"
        ),
        PaintToolGlossaryItem(
            toolNameTh = "ผ้าเหนียวเช็ดฝุ่นห้องพ่น (Tack Cloth / Tack Rag)",
            toolNameEn = "Anti-Static Tack Cloth",
            category = "อุปกรณ์ตรวจสอบ",
            purpose = "ผ้าตาข่ายชุบเรซิ่นเหนียวพิเศษ ใช้เช็ดลูบผิวชิ้นงานเบาๆ เป็นขั้นตอนสุดท้ายก่อนพ่นสี เพื่อดึงละอองฝุ่นและเส้นใยผ้าออกทั้งหมด",
            technicalSpec = "ไม่ทิ้งคราบกาวเหนียวตกค้างบนผิวชิ้นงาน ป้องกันไฟฟ้าสถิต",
            maintenanceTip = "ลูบชิ้นงานด้วยน้ำหนักมือแผ่วเบา ห้ามกดแรงเพราะอาจทิ้งคราบยางเหนียวบนผิวสี"
        )
    )

    val filteredTools = toolsList.filter { item ->
        (selectedCategoryFilter == "ทั้งหมด" || item.category == selectedCategoryFilter) &&
        (item.toolNameTh.contains(searchQuery, ignoreCase = true) ||
         item.toolNameEn.contains(searchQuery, ignoreCase = true) ||
         item.purpose.contains(searchQuery, ignoreCase = true))
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("🔍 ค้นหาเครื่องมือทำสี (เช่น HVLP, ไกด์โค้ท, กรองดักน้ำ...)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Category chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.take(3).forEach { cat ->
                    val isSel = selectedCategoryFilter == cat
                    FilterChip(
                        selected = isSel,
                        onClick = { selectedCategoryFilter = cat },
                        label = { Text(cat, fontSize = 11.sp) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.drop(3).forEach { cat ->
                    val isSel = selectedCategoryFilter == cat
                    FilterChip(
                        selected = isSel,
                        onClick = { selectedCategoryFilter = cat },
                        label = { Text(cat, fontSize = 11.sp) }
                    )
                }
            }
        }

        item {
            Text(
                "🧰 อภิธานศัพท์เครื่องมือทำสีเฉพาะทาง (${filteredTools.size} รายการ)",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        items(filteredTools) { tool ->
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(tool.toolNameTh, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                            Text(tool.toolNameEn, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                tool.category,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("📖 หน้าที่และการใช้งาน:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(tool.purpose, fontSize = 12.sp, lineHeight = 17.sp)

                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("⚙️ สเปคและพารามิเตอร์เทคนิค: ${tool.technicalSpec}", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.tertiary)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Build, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ข้อแนะนำการบำรุงรักษา: ${tool.maintenanceTip}", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}
