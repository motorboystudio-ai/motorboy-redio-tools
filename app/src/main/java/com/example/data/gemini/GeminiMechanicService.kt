package com.example.data.gemini

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.ProductPriceEntity
import com.example.data.repository.ProductPriceRepository
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
    PINOUT_LIBRARY("pinout_library", "คลังไดอะแกรม & MotorIndy", "วงจรกล่อง CDI, แผ่นชาร์จ, วงจร ECU MotorIndy"),
    CLOUD_SQL("cloud_sql", "SQL Cloud Database", "เชื่อมต่อฐานข้อมูล SQL Cloud (MySQL / PostgreSQL)"),
    MOVIE_ONLINE("movie_online", "หนังออนไลน์ Movie911HD", "ดูหนังออนไลน์ ซีรีส์ความคมชัดสูง"),
    PRODUCT_PRICES("product_prices", "รายการสินค้า & ราคาช่าง", "ดู แก้ไข เพิ่ม-ลบ ราคาน้ำมันเครื่อง อะไหล่ และค่าแรงช่าง")
}

enum class AiProvider(val id: String, val displayName: String, val modelName: String) {
    POOLSIDE("poolside", "Poolside Laguna 2.1", "poolside/laguna-xs-2.1"),
    GEMINI("gemini", "Gemini 3.6 Flash", "gemini-3.6-flash")
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "user", "gemini", or "poolside"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionCommand: AppActionCommand? = null,
    val providerUsed: AiProvider? = null
)

sealed class AppActionCommand {
    data class Navigate(val destination: ScreenDestination) : AppActionCommand()
    data class RadioControl(val play: Boolean, val stationName: String? = null) : AppActionCommand()
    data class PriceUpdated(val productName: String, val message: String) : AppActionCommand()
    data class PriceAdded(val productName: String, val message: String) : AppActionCommand()
    data class PriceDeleted(val productName: String, val message: String) : AppActionCommand()
}

object GeminiMechanicService {
    private const val TAG = "GeminiMechanicService"

    // Poolside AI Configuration
    const val DEFAULT_POOLSIDE_KEY = "sky_MtRuvdIw.PkWSWRcAhe0auTW0YgWWrx8JeGqhFRpg"
    const val POOLSIDE_BASE_URL = "https://inference.poolside.ai/v1"
    const val POOLSIDE_MODEL = "poolside/laguna-xs-2.1"

    // Gemini Configuration
    const val GEMINI_MODEL = "gemini-3.6-flash"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private const val BASE_SYSTEM_PROMPT = """
        คุณคือ 'ช่างบอย AI (MotorBoy Assistant)' ผู้ช่วยช่างซ่อมมอเตอร์ไซค์อัจฉริยะประจำแอป MotorBoy Tech
        คุณมีความเชี่ยวชาญระดับมาสเตอร์ด้าน:
        1. รถจักรยานยนต์ 4 จังหวะ และ 2 จังหวะ (Honda, Yamaha, Suzuki, Kawasaki, GPX ฯลฯ)
        2. ระบบหัวฉีด PGM-FI, กล่อง ECU, โค้ดไฟกระพริบ MIL/DTC, เซนเซอร์ TPS, EOT, O2 Sensor
        3. ไดอะแกรมระบบไฟ กล่อง CDI (AC/DC), ขดลวดแม่เหล็ก สเตเตอร์ แผ่นชาร์จเรกูเลเตอร์
        4. ไดอะแกรม MotorIndy (แรงดันไอแคร้ง Blow-by Gas, การดักไอ, กำลังอัดเครื่องยนต์)
        5. งานผสมสีพ่น 2K อัตราส่วนเคลียร์ การคำนวณกำลังอัด ซีซี อัตราทดเกียร์ นมหนูคาร์บูเรเตอร์
        6. ราคาสินค้า อะไหล่ น้ำมันเครื่อง น้ำมัน 2T ลูกปืน และค่าบริการ/ค่าแรงซ่อมบำรุงในอู่

        ความสามารถพิเศษในการควบคุมแอปพลิเคชัน & จัดการราคาสินค้า:
        คุณสามารถสั่งแอปเปิดหน้าต่างต่างๆ หรือตอบราคาสินค้า อะไหล่ ค่าแรง และจัดการแก้ไข/เพิ่ม/ลบราคาสินค้าได้โดยตรง!
        หากผู้ใช้ถามราคา เช่น "ปะยาง กี่บาท", "เปลี่ยนลูกปืนแผงคอ กี่บาท", "น้ำมันเครื่องฮอนด้า 0.8L เท่าไหร่" ให้ตอบราคาขายปลีกและราคาพร้อมติดตั้งอย่างชัดเจนตามตารางข้อมูลปัจจุบัน
        
        หากผู้ใช้สั่งให้ "แก้ไขราคา", "เพิ่มสินค้า", หรือ "ลบสินค้า" ให้ตอบยืนยันและใส่ Action Tag พิเศษท้ายข้อความในรูปแบบนี้:
        - แก้ไขราคา: [ACTION:PRICE:UPDATE:name=ชื่อสินค้า:retail=ราคาขายปลีก:cost=ราคาต้นทุน:installed=ราคาพร้อมติดตั้ง]
          (หากระบุเฉพาะราคาขายปลีก ให้ใส่เฉพาะ retail เช่น [ACTION:PRICE:UPDATE:name=ปะยาง:retail=60])
        - เพิ่มสินค้า: [ACTION:PRICE:ADD:category=หมวดหมู่:name=ชื่อสินค้า:retail=ราคาขายปลีก:cost=ราคาต้นทุน:installed=ราคาพร้อมติดตั้ง]
        - ลบสินค้า: [ACTION:PRICE:DELETE:name=ชื่อสินค้า]
        - เปิดหน้าแคตตาล็อกสินค้า: [ACTION:NAVIGATE:product_prices]
        
        Action Tag คำสั่งอื่นๆ สำหรับควบคุมแอป:
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
        [ACTION:NAVIGATE:cloud_sql] -> เปิดหน้าฐานข้อมูล SQL Cloud
        [ACTION:NAVIGATE:movie_online] -> เปิดหน้าดูหนังออนไลน์
        [ACTION:NAVIGATE:product_prices] -> เปิดหน้ารายการสินค้า & ราคาช่าง
        [ACTION:RADIO:PLAY] -> สั่งเปิดวิทยุ
        [ACTION:RADIO:PAUSE] -> สั่งปิด/หยุดวิทยุ

        ตอบอย่างเป็นกันเอง สไตล์ช่างผู้ชำนาญการ กระชับ ถูกต้อง ปลอดภัย เข้าใจง่ายภาษาไทย
    """

    fun getPoolsideApiKey(): String {
        return try {
            val key = BuildConfig.POOLSIDE_API_KEY
            if (!key.isNullOrBlank() && key != "MY_POOLSIDE_API_KEY") key else DEFAULT_POOLSIDE_KEY
        } catch (_: Throwable) {
            DEFAULT_POOLSIDE_KEY
        }
    }

    suspend fun askMechanic(
        userPrompt: String,
        history: List<ChatMessage>,
        provider: AiProvider = AiProvider.POOLSIDE,
        context: Context? = null
    ): Pair<String, AppActionCommand?> = withContext(Dispatchers.IO) {
        val priceRepo = context?.let { ProductPriceRepository.getInstance(it) }
        val priceSummary = priceRepo?.getPriceSummaryForAi() ?: ""

        val systemPrompt = if (priceSummary.isNotBlank()) {
            "$BASE_SYSTEM_PROMPT\n\n$priceSummary"
        } else {
            BASE_SYSTEM_PROMPT
        }

        var result: Pair<String, AppActionCommand?>? = null

        if (provider == AiProvider.POOLSIDE) {
            try {
                result = callPoolside(userPrompt, history, systemPrompt, priceRepo)
            } catch (e: Exception) {
                Log.w(TAG, "Poolside call failed: ${e.message}, falling back to Gemini")
                try {
                    result = callGemini(userPrompt, history, systemPrompt, priceRepo)
                } catch (e2: Exception) {
                    Log.e(TAG, "Gemini fallback also failed: ${e2.message}")
                    result = processOfflineRuleBased(userPrompt, priceRepo)
                }
            }
        } else {
            try {
                result = callGemini(userPrompt, history, systemPrompt, priceRepo)
            } catch (e: Exception) {
                Log.w(TAG, "Gemini call failed: ${e.message}, falling back to Poolside")
                try {
                    result = callPoolside(userPrompt, history, systemPrompt, priceRepo)
                } catch (e2: Exception) {
                    Log.e(TAG, "Poolside fallback also failed: ${e2.message}")
                    result = processOfflineRuleBased(userPrompt, priceRepo)
                }
            }
        }

        return@withContext result ?: processOfflineRuleBased(userPrompt, priceRepo)
    }

    /**
     * Calls Poolside AI (OpenAI-compatible /v1/chat/completions)
     */
    private suspend fun callPoolside(
        userPrompt: String,
        history: List<ChatMessage>,
        systemPrompt: String,
        priceRepo: ProductPriceRepository?
    ): Pair<String, AppActionCommand?> {
        val apiKey = getPoolsideApiKey()
        val url = "$POOLSIDE_BASE_URL/chat/completions"

        val messagesArray = JSONArray()

        // System prompt
        messagesArray.put(JSONObject().apply {
            put("role", "system")
            put("content", systemPrompt)
        })

        // Recent history
        val recentHistory = history.takeLast(6)
        for (msg in recentHistory) {
            val role = if (msg.sender == "user") "user" else "assistant"
            messagesArray.put(JSONObject().apply {
                put("role", role)
                put("content", msg.text)
            })
        }

        // User current message
        messagesArray.put(JSONObject().apply {
            put("role", "user")
            put("content", userPrompt)
        })

        val jsonBody = JSONObject().apply {
            put("model", POOLSIDE_MODEL)
            put("messages", messagesArray)
            put("max_tokens", 1200)
            put("temperature", 0.4)
        }

        val requestBody = jsonBody.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: ""

        if (!response.isSuccessful) {
            Log.e(TAG, "Poolside API Error: ${response.code} - $responseBody")
            throw IllegalStateException("Poolside HTTP ${response.code}: $responseBody")
        }

        val jsonResponse = JSONObject(responseBody)
        val choices = jsonResponse.optJSONArray("choices")
        if (choices != null && choices.length() > 0) {
            val firstChoice = choices.getJSONObject(0)
            val message = firstChoice.optJSONObject("message")
            val content = message?.optString("content")
            val reasoning = message?.optString("reasoning_content")

            val rawText = when {
                !content.isNullOrBlank() && content != "null" -> content
                !reasoning.isNullOrBlank() && reasoning != "null" -> reasoning
                else -> ""
            }

            if (rawText.isNotBlank()) {
                return parseActionAndCleanText(rawText, priceRepo)
            }
        }
        throw IllegalStateException("Empty choices from Poolside response")
    }

    /**
     * Calls Google Gemini API
     */
    private suspend fun callGemini(
        userPrompt: String,
        history: List<ChatMessage>,
        systemPrompt: String,
        priceRepo: ProductPriceRepository?
    ): Pair<String, AppActionCommand?> {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            throw IllegalStateException("Gemini API key is not configured")
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$GEMINI_MODEL:generateContent?key=$apiKey"

        val contentsArray = JSONArray()

        val systemObj = JSONObject().apply {
            put("role", "user")
            put("parts", JSONArray().put(JSONObject().put("text", systemPrompt)))
        }
        val systemAck = JSONObject().apply {
            put("role", "model")
            put("parts", JSONArray().put(JSONObject().put("text", "รับทราบครับผม ช่างบอย AI พร้อมวิเคราะห์ปัญหา ตอบราคาสินค้าและสั่งการแอป MotorBoy Tech ครับ!")))
        }
        contentsArray.put(systemObj)
        contentsArray.put(systemAck)

        val recentHistory = history.takeLast(6)
        for (msg in recentHistory) {
            val role = if (msg.sender == "user") "user" else "model"
            val msgObj = JSONObject().apply {
                put("role", role)
                put("parts", JSONArray().put(JSONObject().put("text", msg.text)))
            }
            contentsArray.put(msgObj)
        }

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
            throw IllegalStateException("Gemini HTTP ${response.code}: $responseBody")
        }

        val jsonResponse = JSONObject(responseBody)
        val candidates = jsonResponse.optJSONArray("candidates")
        if (candidates != null && candidates.length() > 0) {
            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.getJSONObject("content")
            val parts = content.getJSONArray("parts")
            val rawText = parts.getJSONObject(0).getString("text")

            return parseActionAndCleanText(rawText, priceRepo)
        }
        throw IllegalStateException("Empty candidates from Gemini response")
    }

    private suspend fun parseActionAndCleanText(
        rawText: String,
        priceRepo: ProductPriceRepository?
    ): Pair<String, AppActionCommand?> {
        var actionCommand: AppActionCommand? = null
        var cleanedText = rawText

        // 1. Check for Price Update Tag: [ACTION:PRICE:UPDATE:name=...:retail=...:cost=...:installed=...]
        val updatePriceRegex = Regex("\\[ACTION:PRICE:UPDATE:(.*?)\\]")
        val updateMatch = updatePriceRegex.find(cleanedText)
        if (updateMatch != null) {
            val paramsStr = updateMatch.groupValues[1]
            val params = parseKeyValues(paramsStr)
            val name = params["name"] ?: ""
            val retail = params["retail"]?.toDoubleOrNull()
            val cost = params["cost"]?.toDoubleOrNull()
            val installed = params["installed"]?.toDoubleOrNull()

            if (name.isNotBlank() && priceRepo != null) {
                val (success, updated) = priceRepo.updatePriceByName(name, retail, cost, installed)
                if (success && updated != null) {
                    actionCommand = AppActionCommand.PriceUpdated(
                        productName = updated.name,
                        message = "อัปเดตราคา ${updated.name} เรียบร้อยแล้ว (ขายปลีก: ${updated.retailPrice.toInt()} บ. / ติดตั้ง: ${updated.installedPrice.toInt()} บ.)"
                    )
                }
            }
            cleanedText = cleanedText.replace(updateMatch.value, "").trim()
        }

        // 2. Check for Price Add Tag: [ACTION:PRICE:ADD:category=...:name=...:retail=...:cost=...:installed=...]
        val addPriceRegex = Regex("\\[ACTION:PRICE:ADD:(.*?)\\]")
        val addMatch = addPriceRegex.find(cleanedText)
        if (addMatch != null) {
            val paramsStr = addMatch.groupValues[1]
            val params = parseKeyValues(paramsStr)
            val category = params["category"] ?: "อะไหล่เปลี่ยนประจำ"
            val name = params["name"] ?: ""
            val retail = params["retail"]?.toDoubleOrNull() ?: 0.0
            val cost = params["cost"]?.toDoubleOrNull() ?: 0.0
            val installed = params["installed"]?.toDoubleOrNull() ?: 0.0

            if (name.isNotBlank() && priceRepo != null) {
                val newEntity = ProductPriceEntity(
                    category = category,
                    name = name,
                    costPrice = cost,
                    retailPrice = retail,
                    installedPrice = installed
                )
                priceRepo.insertProduct(newEntity)
                actionCommand = AppActionCommand.PriceAdded(
                    productName = name,
                    message = "เพิ่มสินค้า $name ในหมวด $category เรียบร้อยแล้ว"
                )
            }
            cleanedText = cleanedText.replace(addMatch.value, "").trim()
        }

        // 3. Check for Price Delete Tag: [ACTION:PRICE:DELETE:name=...]
        val deletePriceRegex = Regex("\\[ACTION:PRICE:DELETE:(.*?)\\]")
        val deleteMatch = deletePriceRegex.find(cleanedText)
        if (deleteMatch != null) {
            val paramsStr = deleteMatch.groupValues[1]
            val params = parseKeyValues(paramsStr)
            val name = params["name"] ?: paramsStr.replace("name=", "").trim()

            if (name.isNotBlank() && priceRepo != null) {
                val deleted = priceRepo.deleteByName(name)
                actionCommand = AppActionCommand.PriceDeleted(
                    productName = name,
                    message = if (deleted) "ลบรายการ $name เรียบร้อยแล้ว" else "ไม่พบรายการ $name ในระบบ"
                )
            }
            cleanedText = cleanedText.replace(deleteMatch.value, "").trim()
        }

        // 4. Navigation Tags: [ACTION:NAVIGATE:...]
        val navRegex = Regex("\\[ACTION:NAVIGATE:([a-zA-Z_]+)\\]")
        val navMatch = navRegex.find(cleanedText)
        if (navMatch != null) {
            val route = navMatch.groupValues[1]
            val dest = ScreenDestination.values().find { it.route.equals(route, ignoreCase = true) }
            if (dest != null && actionCommand == null) {
                actionCommand = AppActionCommand.Navigate(dest)
            }
            cleanedText = cleanedText.replace(navMatch.value, "").trim()
        }

        // 5. Radio Tags: [ACTION:RADIO:...]
        val radioRegex = Regex("\\[ACTION:RADIO:(PLAY|PAUSE)\\]")
        val radioMatch = radioRegex.find(cleanedText)
        if (radioMatch != null) {
            val cmd = radioMatch.groupValues[1]
            if (actionCommand == null) {
                actionCommand = AppActionCommand.RadioControl(play = (cmd == "PLAY"))
            }
            cleanedText = cleanedText.replace(radioMatch.value, "").trim()
        }

        return Pair(cleanedText, actionCommand)
    }

    private fun parseKeyValues(input: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        val parts = input.split(":")
        for (p in parts) {
            val kv = p.split("=")
            if (kv.size == 2) {
                result[kv[0].trim()] = kv[1].trim()
            }
        }
        return result
    }

    // Smart fallback pattern matching so the assistant works reliably even without network/secrets
    suspend fun processOfflineRuleBased(
        query: String,
        priceRepo: ProductPriceRepository? = null
    ): Pair<String, AppActionCommand?> {
        val q = query.lowercase().trim()

        // 1. Direct price asking: "ปะยาง กี่บาท / เท่าไหร่"
        if (q.contains("ปะยาง")) {
            val product = priceRepo?.searchProducts("ปะยาง")?.firstOrNull()
            val retail = product?.retailPrice?.toInt() ?: 50
            val installed = product?.installedPrice?.toInt() ?: 50
            return Pair(
                "บริการ **ปะยาง** (ปะสตรีม / ตัวหนอน) ราคาอยู่ที่ **$retail บาท** ครับ (ค่าบริการพร้อมปะ $installed บาท) 🛠️",
                AppActionCommand.Navigate(ScreenDestination.PRODUCT_PRICES)
            )
        }

        // 2. Direct price asking: "เปลี่ยนลูกปืนแผงคอ / ลูกปืนแผงคอ กี่บาท"
        if (q.contains("ลูกปืนแผงคอ") || q.contains("ถ้วยคอ") || q.contains("แผงคอ")) {
            val product = priceRepo?.searchProducts("ลูกปืนแผงคอ")?.firstOrNull()
            val retail = product?.retailPrice?.toInt() ?: 180
            val installed = product?.installedPrice?.toInt() ?: 280
            return Pair(
                "ค่าบริการ **เปลี่ยนลูกปืนแผงคอ** (ชุดถ้วยคอ + ลูกปืนเต้าคอ):\n• ราคาเฉพาะอะไหล่: **$retail บาท**\n• ราคาพร้อมถอดติดตั้งตั้งคอให้อย่างดี: **$installed บาท** ครับ 🛵",
                AppActionCommand.Navigate(ScreenDestination.PRODUCT_PRICES)
            )
        }

        // 3. Asking about Ball Bearing (ลูกปืน)
        val bearingMatch = Regex("6[23]0[0-5]").find(q)
        if (bearingMatch != null || q.contains("ลูกปืน")) {
            val code = bearingMatch?.value ?: "6201"
            val product = priceRepo?.searchProducts(code)?.firstOrNull()
            val retail = product?.retailPrice?.toInt() ?: 50
            val installed = product?.installedPrice?.toInt() ?: 150
            return Pair(
                "ลูกปืน Ball Bearing เบอร์ **$code**:\n• ราคาขายปลีก: **$retail บาท**/ตลับ\n• ค่าแรงพร้อมอัดเปลี่ยนลูกปืนล้อ: **$installed บาท** (คิดราคาต่อคู่/ต่อครั้งครับ) ⚙️",
                AppActionCommand.Navigate(ScreenDestination.PRODUCT_PRICES)
            )
        }

        // 4. Edit price command in natural language: e.g. "แก้ราคาปะยาง เป็น 60"
        if ((q.contains("แก้ราคา") || q.contains("เปลี่ยนราคา") || q.contains("ปรับราคา")) && priceRepo != null) {
            val priceNum = Regex("\\d+").find(q)?.value?.toDoubleOrNull()
            if (priceNum != null) {
                // Find matched product name
                val all = priceRepo.getAllProductsList()
                val matched = all.find { q.contains(it.name.lowercase()) || it.name.lowercase().contains(q.replace("แก้ราคา", "").replace("เปลี่ยนราคา", "").trim()) }
                if (matched != null) {
                    val (success, updated) = priceRepo.updatePriceByName(matched.name, newRetailPrice = priceNum, newInstalledPrice = priceNum)
                    if (success && updated != null) {
                        return Pair(
                            "ได้ทำการแก้ไขราคา **${updated.name}** เป็น **${priceNum.toInt()} บาท** เรียบร้อยแล้วครับ! ✅",
                            AppActionCommand.PriceUpdated(updated.name, "แก้ไขราคาสำเร็จ")
                        )
                    }
                }
            }
        }

        // 5. Delete product command: "ลบสินค้า 2t มอลลี่"
        if ((q.contains("ลบสินค้า") || q.contains("ลบรายการ")) && priceRepo != null) {
            val targetName = q.replace("ลบสินค้า", "").replace("ลบรายการ", "").trim()
            if (targetName.isNotBlank()) {
                val all = priceRepo.getAllProductsList()
                val matched = all.find { it.name.lowercase().contains(targetName) }
                if (matched != null) {
                    priceRepo.deleteById(matched.id)
                    return Pair(
                        "ได้ทำการลบรายการ **${matched.name}** ออกจากฐานข้อมูลสินค้าเรียบร้อยแล้วครับ 🗑️",
                        AppActionCommand.PriceDeleted(matched.name, "ลบสำเร็จ")
                    )
                }
            }
        }

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

            // Products / Price list
            q.contains("ราคา") || q.contains("สินค้า") || q.contains("อะไหล่") || q.contains("ค่าแรง") || q.contains("ตารางราคา") -> {
                Pair(
                    "เปิดหน้ารายการสินค้า อะไหล่ และตารางราคาช่างให้แล้วครับ! คุณสามารถค้นหา ดูราคาต้นทุน ขายปลีก พร้อมติดตั้ง หรือสั่งผมแก้ไขราคาได้ทันทีครับ 📋",
                    AppActionCommand.Navigate(ScreenDestination.PRODUCT_PRICES)
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

            // Movie
            q.contains("หนัง") || q.contains("ดูหนัง") || q.contains("movie") || q.contains("ภาพยนตร์") -> {
                Pair(
                    "เปิดหน้าหนังออนไลน์ Movie911HD ให้แล้วครับผม! 🎬",
                    AppActionCommand.Navigate(ScreenDestination.MOVIE_ONLINE)
                )
            }

            // General greeting & Mechanic Help
            else -> {
                Pair(
                    "สวัสดีครับ! ช่างบอย AI สายช่างประจำแอปยินดีให้บริการครับ 🔧\n\nคุณสามารถถามราคา สเปกเครื่อง หรือสั่งแก้ไขราคาสินค้าได้โดยตรง เช่น:\n• 'ปะยาง กี่บาท'\n• 'เปลี่ยนลูกปืนแผงคอ กี่บาท'\n• 'น้ำมันเครื่อง 4T ฮอนด้า 0.8L ขายเท่าไหร่'\n• 'แก้ราคาปะยาง เป็น 60 บาท'\n• 'เพิ่มสินค้า ...'\n• 'ดูรายการสินค้าทั้งหมด'",
                    AppActionCommand.Navigate(ScreenDestination.PRODUCT_PRICES)
                )
            }
        }
    }
}
