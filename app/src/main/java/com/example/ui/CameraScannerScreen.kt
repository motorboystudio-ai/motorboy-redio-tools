package com.example.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.domain.DtcCode
import com.example.domain.DtcRepository
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScannerScreen(
    onBack: () -> Unit,
    dtcRepository: DtcRepository
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    var detectedDtc by remember { mutableStateOf<DtcCode?>(null) }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        cameraPermissionState.launchPermissionRequest()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Camera Scanner DTC", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(16.dp))

        if (cameraPermissionState.status.isGranted) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                val previewView = remember { PreviewView(context) }
                val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
                val lastAnalyzed = remember { mutableLongStateOf(0L) }

                AndroidView(
                    factory = {
                        val executor = ContextCompat.getMainExecutor(context)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()

                            imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                                val currentTime = System.currentTimeMillis()
                                if (currentTime - lastAnalyzed.longValue < 500) {
                                    imageProxy.close()
                                    return@setAnalyzer
                                }
                                lastAnalyzed.longValue = currentTime

                                val mediaImage = getMediaImage(imageProxy)
                                if (mediaImage != null) {
                                    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                                    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                                    recognizer.process(image)
                                        .addOnSuccessListener { visionText ->
                                            val regex = Regex("P\\d{4}")
                                            val match = regex.find(visionText.text)
                                            if (match != null) {
                                                val code = match.value
                                                scope.launch {
                                                    detectedDtc = dtcRepository.getDtcDescription(code)
                                                }
                                            }
                                        }
                                        .addOnCompleteListener { imageProxy.close() }
                                } else {
                                    imageProxy.close()
                                }
                            }

                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }, executor)
                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Visual Overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(width = 300.dp, height = 120.dp)
                        .border(2.dp, Color.Green.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = "จัดวางป้าย VIN หรือ Engine Tag ให้อยู่ในกรอบ",
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp),
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (cameraPermissionState.status.shouldShowRationale) {
                    Text("เราจำเป็นต้องใช้กล้องเพื่อสแกนรหัสข้อผิดพลาดรถจักรยานยนต์ของคุณ", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                        Text("อนุญาตให้เข้าถึงกล้อง")
                    }
                } else {
                    Text("กล้องถูกปิดกั้น กรุณาอนุญาตในตั้งค่าของแอป", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = Uri.fromParts("package", context.packageName, null)
                        context.startActivity(intent)
                    }) {
                        Text("ไปที่ตั้งค่า")
                    }
                }
            }
        }

        if (detectedDtc != null) {
            Card(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Code: ${detectedDtc!!.code}", style = MaterialTheme.typography.titleMedium)
                    Text("Description: ${detectedDtc!!.description}")
                }
            }
        }
    }
}

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
private fun getMediaImage(imageProxy: ImageProxy): android.media.Image? {
    return imageProxy.image
}
