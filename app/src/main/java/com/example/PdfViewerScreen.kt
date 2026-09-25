package com.example

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PdfViewerScreen(fileName: String) {
    // This is a placeholder for the PDF Viewer utility.
    // In a production app, you would use PdfRenderer here to render the pages.
    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("PDF Viewer: แสดงไฟล์ $fileName\n\nหมายเหตุ: โปรดวางไฟล์ PDF ในโฟลเดอร์ assets/manual.pdf เพื่อให้ระบบโหลดไฟล์ออฟไลน์", 
             modifier = Modifier.padding(16.dp))
    }
}
