package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RadioManager
import com.example.data.gemini.AppActionCommand
import com.example.data.gemini.ChatMessage
import com.example.data.gemini.GeminiMechanicService
import com.example.data.gemini.ScreenDestination
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MechanicChatScreen(
    onNavigateTo: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                sender = "gemini",
                text = "สวัสดีครับช่าง! ผมคือ **ช่างบอย Gemini AI สายช่าง** 🔧\n\nพร้อมช่วยวิเคราะห์ปัญหาซ่อมรถ สเปกเครื่องยนต์ เทคนิคผ่าเครื่อง โค้ดไฟกระพริบ และสั่งเปิด-ปิดรันหน้าต่างฟังก์ชันในแอปให้ทันทีครับ!\n\nลองพิมพ์ถาม หรือแตะปุ่มคำสั่งลัดด้านล่างได้เลยครับ 👇"
            )
        )
    }

    val quickCommands = listOf(
        "เปิดวิทยุช่าง 📻",
        "ดูไดอะแกรม MotorIndy ⚡",
        "เช็คน้ำมันเครื่อง 🛢️",
        "คำนวณกำลังอัด 🧮",
        "สแกนกล้อง DTC 📷",
        "คู่มือผ่าเครื่อง 📖",
        "สูตรผสมสี 2K 🎨",
        "ปิดวิทยุ 🔇"
    )

    fun handleActionCommand(action: AppActionCommand) {
        when (action) {
            is AppActionCommand.Navigate -> {
                Toast.makeText(context, "กำลังเปิดหน้า: ${action.destination.titleTh}", Toast.LENGTH_SHORT).show()
                onNavigateTo(action.destination.route)
            }
            is AppActionCommand.RadioControl -> {
                if (action.play) {
                    if (!RadioManager.isPlaying) {
                        RadioManager.togglePlayPause()
                    }
                    Toast.makeText(context, "เปิดวิทยุแล้ว 🔊", Toast.LENGTH_SHORT).show()
                } else {
                    if (RadioManager.isPlaying) {
                        RadioManager.togglePlayPause()
                    }
                    Toast.makeText(context, "ปิดวิทยุแล้ว 🔇", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun sendMessage(textToSend: String) {
        if (textToSend.isBlank() || isLoading) return
        val userMsg = ChatMessage(sender = "user", text = textToSend.trim())
        messages.add(userMsg)
        inputText = ""
        isLoading = true

        coroutineScope.launch {
            listState.animateScrollToItem(messages.size - 1)

            val (replyText, actionCommand) = GeminiMechanicService.askMechanic(
                userPrompt = userMsg.text,
                history = messages
            )

            val geminiMsg = ChatMessage(
                sender = "gemini",
                text = replyText,
                actionCommand = actionCommand
            )
            messages.add(geminiMsg)
            isLoading = false
            listState.animateScrollToItem(messages.size - 1)

            // Execute action automatically if detected
            actionCommand?.let { cmd ->
                handleActionCommand(cmd)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.SmartToy,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "ช่างบอย AI (Gemini สายช่าง)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.tertiaryContainer
                                ) {
                                    Text(
                                        "สั่งการแอปได้",
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = "ผู้ช่วยช่างอัจฉริยะ ตอบคำถาม & สั่งเปิดปิดฟังก์ชัน",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("chat_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            messages.clear()
                            messages.add(
                                ChatMessage(
                                    sender = "gemini",
                                    text = "ล้างประวัติการสนทนาเรียบร้อยครับ! มีเรื่องซ่อมรถ หรือต้องการสั่งเปิดฟังก์ชันไหน แจ้งช่างบอย AI ได้เลยครับ 🔧"
                                )
                            )
                        },
                        modifier = Modifier.testTag("btn_clear_chat")
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Chat")
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
        ) {
            // Chat Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                items(messages) { msg ->
                    ChatBubble(
                        message = msg,
                        onActionClick = { cmd -> handleActionCommand(cmd) }
                    )
                }

                if (isLoading) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Text(
                                "ช่างบอย AI กำลังวิเคราะห์และตรวจสอบคำสั่ง...",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }
            }

            // Quick Shortcut Action Chips
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        text = "⚡ คำสั่งลัดด่วน (แตะเพื่อถามหรือสั่งรันหน้าต่าง):",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(quickCommands) { cmd ->
                            SuggestionChip(
                                onClick = { sendMessage(cmd) },
                                label = { Text(cmd, fontSize = 12.sp) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                        }
                    }
                }
            }

            // Input Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("ถามเรื่องซ่อม หรือสั่ง 'เปิดวิทยุ', 'เปิดคู่มือ'...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_chat_prompt"),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    FilledIconButton(
                        onClick = { sendMessage(inputText) },
                        enabled = inputText.isNotBlank() && !isLoading,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_send_chat"),
                        shape = CircleShape
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    onActionClick: (AppActionCommand) -> Unit
) {
    val isUser = message.sender == "user"

    val bubbleColor = if (isUser) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (isUser) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val alignment = if (isUser) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            if (!isUser) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .size(28.dp)
                        .padding(end = 4.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.PrecisionManufacturing,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                colors = CardDefaults.cardColors(containerColor = bubbleColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor,
                        lineHeight = 22.sp
                    )

                    // If an actionable command is attached to the response
                    message.actionCommand?.let { cmd ->
                        HorizontalDivider(
                            color = textColor.copy(alpha = 0.2f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        when (cmd) {
                            is AppActionCommand.Navigate -> {
                                FilledTonalButton(
                                    onClick = { onActionClick(cmd) },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        Icons.Default.Launch,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("กดเพื่อไปยัง: ${cmd.destination.titleTh}")
                                }
                            }
                            is AppActionCommand.RadioControl -> {
                                Button(
                                    onClick = { onActionClick(cmd) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (cmd.play) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        if (cmd.play) Icons.Default.PlayArrow else Icons.Default.Pause,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (cmd.play) "เปิดวิทยุตอนนี้" else "ปิดวิทยุตอนนี้")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
