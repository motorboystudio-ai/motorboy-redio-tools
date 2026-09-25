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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CameraScannerScreen(
    onBack: () -> Unit,
    dtcRepository: DtcRepository
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    var detectedDtc by remember { mutableStateOf<DtcCode?>(null) }
    var scanError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    // Single shared ML Kit client + single analysis executor for the whole composition.
    val recognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }
    val analysisExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    val lastAnalyzed = remember { AtomicLong(0L) }
    var cameraProviderRef by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    val previewView = remember(context) { PreviewView(context) }
    val dtcRegex = remember { Regex("P\\d{4}") }

    // Dispose long-lived resources exactly once. Camera unbind follows the provider ref.
    val currentProvider by rememberUpdatedState(cameraProviderRef)
    DisposableEffect(Unit) {
        onDispose {
            try {
                currentProvider?.unbindAll()
            } catch (_: Exception) {
            }
            try {
                analysisExecutor.shutdown()
            } catch (_: Exception) {
            }
            try {
                recognizer.close()
            } catch (_: Exception) {
            }
        }
    }
    DisposableEffect(cameraProviderRef) {
        onDispose {
            try {
                cameraProviderRef?.unbindAll()
            } catch (_: Exception) {
            }
        }
    }

    LaunchedEffect(Unit) {
        cameraPermissionState.launchPermissionRequest()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Camera Scanner DTC",
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            "สแกนรหัสไฟกระพริบ / โค้ดกล่อง ECU",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("camera_scanner_back_btn")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "ย้อนกลับ"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (cameraPermissionState.status.isGranted) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    AndroidView(
                        factory = { ctx ->
                            val mainExecutor = ContextCompat.getMainExecutor(ctx)
                            val providerFuture = ProcessCameraProvider.getInstance(ctx)
                            providerFuture.addListener({
                                try {
                                    val cameraProvider = providerFuture.get()
                                    cameraProviderRef = cameraProvider

                                    val preview = Preview.Builder().build().also {
                                        it.setSurfaceProvider(previewView.surfaceProvider)
                                    }

                                    val imageAnalysis = ImageAnalysis.Builder()
                                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                        .build()

                                    imageAnalysis.setAnalyzer(analysisExecutor) { imageProxy ->
                                        val currentTime = System.currentTimeMillis()
                                        if (currentTime - lastAnalyzed.get() < 500) {
                                            imageProxy.close()
                                            return@setAnalyzer
                                        }
                                        lastAnalyzed.set(currentTime)

                                        val mediaImage = getMediaImage(imageProxy)
                                        if (mediaImage != null) {
                                            val image = InputImage.fromMediaImage(
                                                mediaImage,
                                                imageProxy.imageInfo.rotationDegrees
                                            )
                                            recognizer.process(image)
                                                .addOnSuccessListener { visionText ->
                                                    val match = dtcRegex.find(visionText.text)
                                                    if (match != null) {
                                                        val code = match.value
                                                        scope.launch {
                                                            try {
                                                                detectedDtc =
                                                                    dtcRepository.getDtcDescription(code)
                                                                scanError = null
                                                            } catch (e: Exception) {
                                                                scanError =
                                                                    "ค้นหารหัส $code ไม่สำเร็จ: ${e.message}"
                                                            }
                                                        }
                                                    }
                                                }
                                                .addOnFailureListener { e ->
                                                    scope.launch {
                                                        scanError =
                                                            "สแกนภาพไม่สำเร็จ: ${e.message}"
                                                    }
                                                }
                                                .addOnCompleteListener { imageProxy.close() }
                                        } else {
                                            imageProxy.close()
                                        }
                                    }

                                    try {
                                        cameraProvider.unbindAll()
                                        cameraProvider.bindToLifecycle(
                                            lifecycleOwner,
                                            CameraSelector.DEFAULT_BACK_CAMERA,
                                            preview,
                                            imageAnalysis
                                        )
                                    } catch (e: Exception) {
                                        scanError = "เปิดกล้องไม่สำเร็จ: ${e.message}"
                                    }
                                } catch (e: Exception) {
                                    scanError = "เปิดกล้องไม่สำเร็จ: ${e.message}"
                                }
                            }, mainExecutor)
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

            if (scanError != null) {
                Card(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            scanError ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { scanError = null }) {
                            Icon(Icons.Default.Close, contentDescription = "ปิด")
                        }
                    }
                }
            }

            if (detectedDtc != null) {
                Card(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Code: ${detectedDtc!!.code}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("Description: ${detectedDtc!!.description}")
                                if (detectedDtc!!.component.isNotBlank() || detectedDtc!!.severity.isNotBlank()) {
                                    Text(
                                        "${detectedDtc!!.component} • ${detectedDtc!!.severity}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            IconButton(onClick = { detectedDtc = null }) {
                                Icon(Icons.Default.Close, contentDescription = "ล้างผลสแกน")
                            }
                        }
                    }
                }
            }
        }
    }
}

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
private fun getMediaImage(imageProxy: ImageProxy): android.media.Image? {
    return imageProxy.image
}
