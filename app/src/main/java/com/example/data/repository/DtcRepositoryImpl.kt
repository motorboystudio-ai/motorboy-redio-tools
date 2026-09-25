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
            // Pre-populate database if empty
            if (dtcDao.getDtcByCode("P0300") == null) {
                val initialData = listOf(
                    DtcCodeEntity("P0300", "Random/Multiple Cylinder Misfire Detected", "Medium", "Engine"),
                    DtcCodeEntity("P0115", "Engine Coolant Temperature Circuit Malfunction", "Medium", "Cooling"),
                    DtcCodeEntity("P0107", "Manifold Absolute Pressure/Barometric Pressure Circuit Low Input", "High", "Engine Management")
                )
                dtcDao.insertAllDtc(initialData)
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
