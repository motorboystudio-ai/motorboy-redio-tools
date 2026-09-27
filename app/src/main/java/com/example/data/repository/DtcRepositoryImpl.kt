package com.example.data.repository

import android.content.Context
import com.example.MotorBoyDatabase
import com.example.DtcCodeEntity
import com.example.domain.DtcCode
import com.example.domain.DtcRepository
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class DtcRepositoryImpl(private val context: Context) : DtcRepository {

    private val db = MotorBoyDatabase.getDatabase(context)
    private val dtcDao = db.dtcDao()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            // Seed common motorcycle EFI codes; insert only the ones missing
            // so existing installs get the new codes too.
            val initialData = listOf(
                DtcCodeEntity("P0107", "วงจรเซ็นเซอร์แรงดันอากาศ MAP ต่ำ (Manifold Absolute Pressure Low)", "High", "Engine Management"),
                DtcCodeEntity("P0110", "วงจรเซ็นเซอร์อุณหภูมิอากาศ IAT ผิดปกติ (Intake Air Temperature)", "Medium", "Engine Management"),
                DtcCodeEntity("P0115", "วงจรเซ็นเซอร์อุณหภูมิน้ำมันเครื่อง/น้ำหล่อเย็น ECT/EOT ผิดปกติ", "Medium", "Cooling"),
                DtcCodeEntity("P0120", "วงจรเซ็นเซอร์ลิ้นเร่ง TPS ผิดปกติ (Throttle Position Sensor)", "High", "Engine Management"),
                DtcCodeEntity("P0130", "วงจรเซ็นเซอร์ออกซิเจน O2 Sensor ผิดปกติ", "Medium", "Exhaust"),
                DtcCodeEntity("P0200", "วงจรหัวฉีดน้ำมัน Injector ผิดปกติ", "High", "Fuel System"),
                DtcCodeEntity("P0230", "วงจรปั๊มติ๊ก Fuel Pump ผิดปกติ", "High", "Fuel System"),
                DtcCodeEntity("P0300", "เครื่องสะดุด/จุดระเบิดพลาดหลายสูบ (Random Misfire Detected)", "Medium", "Engine"),
                DtcCodeEntity("P0335", "สัญญาณเซ็นเซอร์เพลาข้อเหวี่ยง CKP/Pulsar Coil ผิดปกติ", "High", "Ignition"),
                DtcCodeEntity("P0350", "วงจรคอยล์จุดระเบิด Ignition Coil ผิดปกติ", "High", "Ignition"),
                DtcCodeEntity("P0500", "สัญญาณเซ็นเซอร์ความเร็วรถ Speed Sensor ผิดปกติ", "Low", "Meter"),
                DtcCodeEntity("P0560", "แรงดันไฟแบตเตอรี่ผิดปกติ (สูง/ต่ำเกิน)", "Medium", "Charging"),
                DtcCodeEntity("P0600", "กล่อง ECU สื่อสารล้มเหลว/หน่วยความจำผิดปกติ", "High", "ECU"),
                DtcCodeEntity("P0650", "วงจรหลอดไฟเตือนเครื่องยนต์ MIL ผิดปกติ", "Low", "Meter"),
                DtcCodeEntity("P0420", "ประสิทธิภาพแคทตาไลติกต่ำ (Catalyst Efficiency Below Threshold)", "Low", "Exhaust")
            )
            initialData.forEach { entity ->
                if (dtcDao.getDtcByCode(entity.code) == null) {
                    dtcDao.insertAllDtc(listOf(entity))
                }
            }
        }
    }

    override suspend fun getDtcDescription(code: String): DtcCode? {
        val entity = dtcDao.getDtcByCode(code.uppercase()) ?: return null
        return DtcCode(entity.code, entity.description, entity.severity, entity.component)
    }

    override suspend fun scanDtcFromImage(imagePath: String): DtcCode? {
        val file = File(imagePath)
        if (!file.exists()) return null

        val image = InputImage.fromFilePath(context, android.net.Uri.fromFile(file))
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        return suspendCoroutine { continuation ->
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val detectedText = visionText.text
                    // Simple logic to find a code like Pxxxx
                    val regex = Regex("P\\d{4}")
                    val match = regex.find(detectedText)
                    if (match != null) {
                        val code = match.value
                        CoroutineScope(Dispatchers.IO).launch {
                            val entity = dtcDao.getDtcByCode(code.uppercase())
                            continuation.resume(entity?.let { DtcCode(it.code, it.description, it.severity, it.component) })
                        }
                    } else {
                        continuation.resume(null)
                    }
                }
                .addOnFailureListener {
                    continuation.resume(null)
                }
        }
    }
}
