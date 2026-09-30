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
import com.example.data.gemini.AiProvider
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
    var selectedProvider by remember { mutableStateOf(AiProvider.POOLSIDE) }

    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                sender = "poolside",
                text = "สวัสดีครับช่าง! ผมคือ **ช่างบอย AI สายช่าง** 🔧 (ขับเคลื่อนด้วย **Poolside Laguna 2.1** & Gemini)\n\n" +
                        "ผมทราบฐานข้อมูลราคาสินค้า อะไหล่ และค่าแรงในอู่ทั้งหมดแล้วครับ!\n\n" +
                        "คุณสามารถ:\n" +
                        "• **ถามราคาได้โดยตรง:** เช่น 'ปะยางกี่บาท', 'เปลี่ยนลูกปืนแผงคอกี่บาท', 'ลูกปืน 6201 ราคาเท่าไหร่'\n" +
                        "• **สั่งแก้ราคาได้ทันที:** เช่น 'แก้ราคาปะยาง เป็น 60 บาท'\n" +
                        "• **สั่งเพิ่ม/ลบสินค้า:** เช่น 'เพิ่มสินค้า ...', 'ลบสินค้า ...'\n" +
                        "• หรือสั่งรันหน้าต่างต่างๆ ในแอปได้เลยครับ 👇",
                providerUsed = AiProvider.POOLSIDE
            )
        )
    }

    val quickCommands = listOf(
        "ปะยาง กี่บาท 🛵",
        "เปลี่ยนลูกปืนแผงคอ กี่บาท ⚙️",
        "ตารางราคาสินค้า 📋",
        "ลูกปืน 6201 ราคา 🔩",
        "เปิดวิทยุช่าง 📻",
        "ดูไดอะแกรม MotorIndy ⚡",
        "เช็คน้ำมันเครื่อง 🛢️",
        "คำนวณกำลังอัด 🧮",
        "สแกนกล้อง DTC 📷",
        "คู่มือผ่าเครื่อง 📖",
        "ดูหนังออนไลน์ 🎬",
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
            is AppActionCommand.PriceUpdated -> {
                Toast.makeText(context, "✅ ${action.message}", Toast.LENGTH_LONG).show()
            }
            is AppActionCommand.PriceAdded -> {
                Toast.makeText(context, "✅ ${action.message}", Toast.LENGTH_LONG).show()
            }
            is AppActionCommand.PriceDeleted -> {
                Toast.makeText(context, "🗑️ ${action.message}", Toast.LENGTH_LONG).show()
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
                history = messages,
                provider = selectedProvider,
                context = context
            )

            val aiMsg = ChatMessage(
                sender = if (selectedProvider == AiProvider.POOLSIDE) "poolside" else "gemini",
                text = replyText,
                actionCommand = actionCommand,
                providerUsed = selectedProvider
            )
            messages.add(aiMsg)
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
                                    if (selectedProvider == AiProvider.POOLSIDE) Icons.Default.Bolt else Icons.Default.SmartToy,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "ช่างบอย AI",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (selectedProvider == AiProvider.POOLSIDE) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer
                                ) {
                                    Text(
                                        if (selectedProvider == AiProvider.POOLSIDE) "Poolside Laguna" else "Gemini 3.6",
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = if (selectedProvider == AiProvider.POOLSIDE) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onTertiaryContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = "ถามราคา อะไหล่ สั่งแก้ราคา & สั่งเปิดปิดฟังก์ชัน",
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
                        onClick = { onNavigateTo(ScreenDestination.PRODUCT_PRICES.route) },
                        modifier = Modifier.testTag("btn_go_prices")
                    ) {
                        Icon(Icons.Default.Inventory, contentDescription = "รายการสินค้า")
                    }
                    IconButton(
                        onClick = {
                            messages.clear()
                            messages.add(
                                ChatMessage(
                                    sender = if (selectedProvider == AiProvider.POOLSIDE) "poolside" else "gemini",
                                    text = "ล้างประวัติการสนทนาเรียบร้อยครับ! มีเรื่องราคาอะไหล่ ซ่อมรถ หรือต้องการสั่งเปิดฟังก์ชันไหน แจ้งช่างบอย AI ได้เลยครับ 🔧",
                                    providerUsed = selectedProvider
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
            // Model Selector Bar
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "โมเดล AI:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    FilterChip(
                        selected = selectedProvider == AiProvider.POOLSIDE,
                        onClick = { selectedProvider = AiProvider.POOLSIDE },
                        label = { Text("⚡ Poolside Laguna 2.1", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.height(32.dp)
                    )
                    FilterChip(
                        selected = selectedProvider == AiProvider.GEMINI,
                        onClick = { selectedProvider = AiProvider.GEMINI },
                        label = { Text("✨ Gemini 3.6 Flash", fontSize = 11.sp) },
                        modifier = Modifier.height(32.dp)
                    )
                }
            }

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
                                if (selectedProvider == AiProvider.POOLSIDE)
                                    "Poolside Laguna กำลังค้นหาราคาและวิเคราะห์คำสั่ง..."
                                else
                                    "Gemini กำลังค้นหาราคาและวิเคราะห์คำสั่ง...",
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
                        text = "⚡ คำสั่งลัดด่วน (แตะเพื่อถามราคาหรือสั่งรันหน้าต่าง):",
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
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.clickable { sendMessage(cmd) }
                            ) {
                                Text(
                                    text = cmd,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Chat Input Bar
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
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_chat_prompt"),
                        placeholder = {
                            Text(
                                "ถามราคา เช่น 'ปะยางกี่บาท' หรือสั่งแก้ราคา...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 4,
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
                            if (message.providerUsed == AiProvider.POOLSIDE || message.sender == "poolside")
                                Icons.Default.Bolt
                            else
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
                    if (!isUser) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = if (message.providerUsed == AiProvider.POOLSIDE || message.sender == "poolside")
                                    "⚡ poolside/laguna-xs-2.1"
                                else
                                    "✨ gemini-3.6-flash",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

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
                            is AppActionCommand.PriceUpdated,
                            is AppActionCommand.PriceAdded,
                            is AppActionCommand.PriceDeleted -> {
                                FilledTonalButton(
                                    onClick = { onActionClick(AppActionCommand.Navigate(ScreenDestination.PRODUCT_PRICES)) },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        Icons.Default.Inventory,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("เปิดดูตารางราคาสินค้า & ค่าแรง")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
