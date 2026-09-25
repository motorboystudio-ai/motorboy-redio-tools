package com.example.data.gemini

import android.util.Log
import com.example.BuildConfig
import com.example.data.RadioManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

enum class ScreenDestination(val route: String, val titleTh: String, val description: String) {
    DASHBOARD("dashboard", "หน้าหลักแดชบอร์ด", "หน้ารวมฟีเจอร์หลักทั้งหมด"),
    MANUAL_VIEWER("manual_viewer", "คู่มือซ่อมบำรุง & ไดอะแกรม", "คู่มือซ่อมรถจักรยานยนต์ ซูมดูสเต็ป & ไดอะแกรม"),
    SERVICE_REMINDER("service_reminder", "แจ้งเตือนถ่ายน้ำมันเครื่อง", "บันทึกเลขไมล์ ตรวจสอบรอบเปลี่ยนน้ำมันเครื่อง"),
    CALCULATOR("calculator", "เครื่องคำนวณช่างมอเตอร์ไซค์", "คำนวณกำลังอัด ความตึงโซ่ จูนนมหนู แปลงหน่วย"),
    RADIO_SCANNER("radio_scanner", "วิทยุช่าง FM สด", "สตรีมวิทยุทั่วไทย สแกนคลื่นสด"),
    ONLINE_TV("online_tv", "ทีวีช่าง ออนไลน์", "ดูทีวีสด 24 ชม."),
    CAMERA_SCANNER("camera_scanner", "สแกนกล้องอ่านโค้ด DTC", "สแกนรหัสไฟกระพริบ โค้ดกล่อง ECU"),
    PAINT_GUIDE("paint_guide", "คู่มือซ่อม & ทำสี 2K", "ขั้นตอนขัด พ่นสี รองพื้น เคลียร์โค้ท 2K"),
    PAINT_MIXING("paint_mixing", "ผสมสี & อัตราส่วนช่าง", "คำนวณอัตราส่วนผสมสี 2K, 4:1, 2:1, ตัวทำละลาย"),
    PINOUT_LIBRARY("pinout_library", "คลังไดอะแกรม & MotorIndy", "วงจรกล่อง CDI, แผ่นชาร์จ, วงจร ECU MotorIndy")
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "user" or "gemini"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionCommand: AppActionCommand? = null
)

sealed class AppActionCommand {
    data class Navigate(val destination: ScreenDestination) : AppActionCommand()
    data class RadioControl(val play: Boolean, val stationName: String? = null) : AppActionCommand()
}

object GeminiMechanicService {
    private const val TAG = "GeminiMechanicService"
    private const val MODEL = "gemini-2.5-flash"
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val SYSTEM_PROMPT = """
        คุณคือ 'ช่างบอย AI (MotorBoy Gemini Assistant)' ผู้ช่วยช่างซ่อมมอเตอร์ไซค์อัจฉริยะประจำแอป MotorBoy Tech
        คุณมีความเชี่ยวชาญระดับมาสเตอร์ด้าน:
        1. รถจักรยานยนต์ 4 จังหวะ และ 2 จังหวะ (Honda, Yamaha, Suzuki, Kawasaki, GPX ฯลฯ)
        2. ระบบหัวฉีด PGM-FI, กล่อง ECU, โค้ดไฟกระพริบ MIL/DTC, เซนเซอร์ TPS, EOT, O2 Sensor
        3. ไดอะแกรมระบบไฟ กล่อง CDI (AC/DC), ขดลวดแม่เหล็ก สเตเตอร์ แผ่นชาร์จเรกูเลเตอร์
        4. ไดอะแกรม MotorIndy (แรงดันไอแคร้ง Blow-by Gas, การดักไอ, กำลังอัดเครื่องยนต์)
        5. งานผสมสีพ่น 2K อัตราส่วนเคลียร์ การคำนวณกำลังอัด ซีซี อัตราทดเกียร์ นมหนูคาร์บูเรเตอร์
        
        ความสามารถพิเศษในการควบคุมแอปพลิเคชัน:
        คุณสามารถสั่งแอปเปิดหน้าต่างต่างๆ หรือควบคุมวิทยุได้ โดยต้องตอบเป็นข้อความคำตอบปกติ และหากผู้ใช้ต้องการเปิดหน้า หรือคำถามเข้ากับฟีเจอร์นั้น ให้ใส่ Action Tag ท้ายข้อความในรูปแบบแท็กคำสั่งพิเศษนี้:
        
        [ACTION:NAVIGATE:dashboard] -> เปิดหน้าหลัก
        [ACTION:NAVIGATE:manual_viewer] -> เปิดหน้าคู่มือซ่อมรถจักรยานยนต์
        [ACTION:NAVIGATE:service_reminder] -> เปิดหน้าแจ้งเตือนเปลี่ยนน้ำมันเครื่อง
        [ACTION:NAVIGATE:calculator] -> เปิดหน้าเครื่องคำนวณช่าง
        [ACTION:NAVIGATE:radio_scanner] -> เปิดหน้าวิทยุ FM ช่าง
        [ACTION:NAVIGATE:online_tv] -> เปิดหน้าทีวีออนไลน์
        [ACTION:NAVIGATE:camera_scanner] -> เปิดหน้ากล้องสแกนโค้ด DTC
        [ACTION:NAVIGATE:paint_guide] -> เปิดหน้าคู่มือทำสี 2K
        [ACTION:NAVIGATE:paint_mixing] -> เปิดหน้าสูตรผสมสี
        [ACTION:NAVIGATE:pinout_library] -> เปิดหน้าคลังไดอะแกรม & MotorIndy
        [ACTION:RADIO:PLAY] -> สั่งเปิดวิทยุ
        [ACTION:RADIO:PAUSE] -> สั่งปิด/หยุดวิทยุ

        ตอบอย่างเป็นกันเอง สไตล์ช่างผู้ชำนาญการ กระชับ ถูกต้อง ปลอดภัย เข้าใจง่ายภาษาไทย
    """.trimIndent()

    suspend fun askMechanic(
        userPrompt: String,
        history: List<ChatMessage>
    ): Pair<String, AppActionCommand?> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY

        // Fallback local smart assistant if API Key is not yet configured
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext processOfflineRuleBased(userPrompt)
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent?key=$apiKey"

            val contentsArray = JSONArray()

            // System prompt as first user/model turn if needed, or structured
            val systemObj = JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().put(JSONObject().put("text", SYSTEM_PROMPT)))
            }
            val systemAck = JSONObject().apply {
                put("role", "model")
                put("parts", JSONArray().put(JSONObject().put("text", "รับทราบครับผม ช่างบอย AI พร้อมวิเคราะห์ปัญหาและสั่งการแอป MotorBoy Tech ครับ!")))
            }
            contentsArray.put(systemObj)
            contentsArray.put(systemAck)

            // Previous chat context (last 6 messages)
            val recentHistory = history.takeLast(6)
            for (msg in recentHistory) {
                val role = if (msg.sender == "user") "user" else "model"
                val msgObj = JSONObject().apply {
                    put("role", role)
                    put("parts", JSONArray().put(JSONObject().put("text", msg.text)))
                }
                contentsArray.put(msgObj)
            }

            // Current prompt
            val currentObj = JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().put(JSONObject().put("text", userPrompt)))
            }
            contentsArray.put(currentObj)

            val jsonBody = JSONObject().apply {
                put("contents", contentsArray)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.4)
                    put("maxOutputTokens", 1200)
                })
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "Gemini API Error: ${response.code} - $responseBody")
                return@withContext processOfflineRuleBased(userPrompt)
            }

            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.getJSONObject("content")
                val parts = content.getJSONArray("parts")
                val rawText = parts.getJSONObject(0).getString("text")

                parseActionAndCleanText(rawText)
            } else {
                processOfflineRuleBased(userPrompt)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in Gemini API call", e)
            processOfflineRuleBased(userPrompt)
        }
    }

    private fun parseActionAndCleanText(rawText: String): Pair<String, AppActionCommand?> {
        var actionCommand: AppActionCommand? = null
        var cleanedText = rawText

        val navRegex = Regex("\\[ACTION:NAVIGATE:([a-zA-Z_]+)\\]")
        val navMatch = navRegex.find(rawText)
        if (navMatch != null) {
            val route = navMatch.groupValues[1]
            val dest = ScreenDestination.values().find { it.route.equals(route, ignoreCase = true) }
            if (dest != null) {
                actionCommand = AppActionCommand.Navigate(dest)
            }
            cleanedText = cleanedText.replace(navMatch.value, "").trim()
        }

        val radioRegex = Regex("\\[ACTION:RADIO:(PLAY|PAUSE)\\]")
        val radioMatch = radioRegex.find(rawText)
        if (radioMatch != null) {
            val cmd = radioMatch.groupValues[1]
            actionCommand = AppActionCommand.RadioControl(play = (cmd == "PLAY"))
            cleanedText = cleanedText.replace(radioMatch.value, "").trim()
        }

        return Pair(cleanedText, actionCommand)
    }

    // Smart fallback pattern matching so the assistant works reliably even without network/secrets
    private fun processOfflineRuleBased(query: String): Pair<String, AppActionCommand?> {
        val q = query.lowercase().trim()

        return when {
            // Radio controls
            q.contains("เปิดเพลง") || q.contains("เปิดวิทยุ") || q.contains("ฟังวิทยุ") || q.contains("เล่นวิทยุ") -> {
                Pair(
                    "เปิดวิทยุช่างออนไลน์เรียบร้อยครับ! กำลังเล่นคลื่นเพลงสบายๆ ระหว่างซ่อมรถครับผม 📻",
                    AppActionCommand.RadioControl(play = true)
                )
            }
            q.contains("ปิดเพลง") || q.contains("ปิดวิทยุ") || q.contains("หยุดวิทยุ") || q.contains("หยุดเพลง") -> {
                Pair(
                    "หยุดการเล่นวิทยุเรียบร้อยครับผม 🔇",
                    AppActionCommand.RadioControl(play = false)
                )
            }
            q.contains("วิทยุ") || q.contains("คลื่น") || q.contains("สถานี") -> {
                Pair(
                    "เปิดหน้าสถานีวิทยุช่าง FM สดให้แล้วครับ คุณสามารถสแกนคลื่นและฟังเพลงคลายเครียดตอนซ่อมรถได้เลยครับ 📻",
                    AppActionCommand.Navigate(ScreenDestination.RADIO_SCANNER)
                )
            }

            // Service / Oil Change
            q.contains("น้ำมันเครื่อง") || q.contains("ถ่ายน้ำมัน") || q.contains("เช็คไมล์") || q.contains("เตือน") -> {
                Pair(
                    "เปิดระบบแจ้งเตือนการเปลี่ยนถ่ายน้ำมันเครื่อง & เซอร์วิสให้แล้วครับ! คุณสามารถเช็คเลขไมล์คงเหลือและรอบเปลี่ยนถัดไปได้ทันทีครับ 🛢️",
                    AppActionCommand.Navigate(ScreenDestination.SERVICE_REMINDER)
                )
            }

            // Manuals
            q.contains("คู่มือ") || q.contains("manual") || q.contains("wave") || q.contains("pcx") || q.contains("click") -> {
                Pair(
                    "เปิดหน้าคลังคู่มือซ่อมบำรุงรถจักรยานยนต์ให้เรียบร้อยครับ! มีทั้งสเต็ปการผ่าเครื่อง ตั้งวาล์ว และไดอะแกรมละเอียดพร้อมซูมครับ 📖",
                    AppActionCommand.Navigate(ScreenDestination.MANUAL_VIEWER)
                )
            }

            // Calculation
            q.contains("คำนวณ") || q.contains("กำลังอัด") || q.contains("ความตึงโซ่") || q.contains("cc") || q.contains("ซีซี") || q.contains("นมหนู") -> {
                Pair(
                    "เปิดเครื่องคิดเลขช่างให้แล้วครับ! สามารถคำนวณกำลังอัดเครื่องยนต์ (CR), ซีซีลูกสูบ, ความตึงโซ่ และสูตรจูนนมหนูคาร์บูได้ทันทีครับ 🧮",
                    AppActionCommand.Navigate(ScreenDestination.CALCULATOR)
                )
            }

            // DTC / Scan Camera
            q.contains("โค้ด") || q.contains("dtc") || q.contains("ไฟโชว์") || q.contains("ไฟเครื่อง") || q.contains("กระพริบ") || q.contains("สแกน") -> {
                Pair(
                    "เปิดระบบกล้องสแกนรหัสข้อผิดพลาด DTC และตารางวิเคราะห์ไฟกระพริบ MIL ให้แล้วครับ พร้อมวิธีแก้ไขทีละจุด 📷",
                    AppActionCommand.Navigate(ScreenDestination.CAMERA_SCANNER)
                )
            }

            // MotorIndy / Pinout / Diagrams
            q.contains("ไดอะแกรม") || q.contains("motorindy") || q.contains("blow by") || q.contains("สายไฟ") || q.contains("cdi") || q.contains("แผ่นชาร์จ") || q.contains("พิน") -> {
                Pair(
                    "เปิดคลังไดอะแกรมช่างและผังระบบไฟ MotorIndy (fb.com/motorindy) ให้แล้วครับ! มีทั้งผังไอแคร้ง Blow-by Gas, กล่อง CDI 5-6 พิน, และระบบไฟ ECU ครบครัน ⚡",
                    AppActionCommand.Navigate(ScreenDestination.PINOUT_LIBRARY)
                )
            }

            // Paint
            q.contains("ทำสี") || q.contains("พ่นสี") || q.contains("2k") || q.contains("ผสมสี") || q.contains("แล็กเกอร์") || q.contains("เคลียร์") -> {
                Pair(
                    "เปิดคู่มือการทำสีมอเตอร์ไซค์ 2K และเครื่องคำนวณอัตราส่วนผสมสีให้แล้วครับ! สูตร 4:1, 2:1 และคำแนะนำการพ่นรองพื้นเคลียร์โค้ทครบถ้วนครับ 🎨",
                    AppActionCommand.Navigate(ScreenDestination.PAINT_MIXING)
                )
            }

            // TV
            q.contains("ทีวี") || q.contains("ดูทีวี") || q.contains("ถ่ายทอด") || q.contains("ข่าว") -> {
                Pair(
                    "เปิดทีวีออนไลน์สำหรับเปิดดูในอู่ให้เรียบร้อยครับผม! 📺",
                    AppActionCommand.Navigate(ScreenDestination.ONLINE_TV)
                )
            }

            // General greeting & Mechanic Help
            else -> {
                Pair(
                    "สวัสดีครับ! ช่างบอย Gemini AI สายช่างประจำแอปยินดีให้บริการครับ 🔧\n\nคุณสามารถปรึกษาปัญหาซ่อมรถ สเปกเครื่องยนต์ รหัสโค้ด หรือบอกให้ผม **สั่งเปิดหน้าต่างฟังก์ชันต่างๆ** ได้เลย เช่น:\n• 'เปิดวิทยุคลื่นเพลง'\n• 'เช็คระยะถ่ายน้ำมันเครื่อง'\n• 'ดูไดอะแกรม MotorIndy'\n• 'คำนวณกำลังอัดเครื่องยนต์'\n• 'สแกนโค้ดไฟเครื่องกระพริบ'",
                    null
                )
            }
        }
    }
}
