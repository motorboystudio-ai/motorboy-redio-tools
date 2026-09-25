package com.example

import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.example.data.TvChannel
import com.example.data.TvManager
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThaiOnlineTvScreen(
    onBack: () -> Unit = {}
) {
    val currentChannel = TvManager.currentChannel
    val isPlaying = TvManager.isPlaying
    val isBuffering = TvManager.isBuffering
    val isSyncing = TvManager.isSyncingApi
    val channels = TvManager.channels

    val categories = listOf("ทั้งหมด", "ข่าวสาร & สาระ", "บันเทิง & วาไรตี้", "ละคร & บันเทิง", "ภาพยนตร์ & ซีรีส์", "การศึกษา & รัฐสภา")
    var selectedCategory by rememberSaveable { mutableStateOf("ทั้งหมด") }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var viewMode by rememberSaveable { mutableStateOf("grid") } // "grid" or "list"
    var isFullScreen by rememberSaveable { mutableStateOf(false) }

    // System back exits fullscreen first instead of leaving the whole screen.
    BackHandler(enabled = isFullScreen) {
        isFullScreen = false
    }

    val filteredChannels = remember(channels.toList(), selectedCategory, searchQuery) {
        channels.filter { channel ->
            val matchCat = if (selectedCategory == "ทั้งหมด") true else channel.category.contains(selectedCategory) || selectedCategory.contains(channel.category)
            val matchQuery = if (searchQuery.isBlank()) true else {
                channel.name.contains(searchQuery, ignoreCase = true) ||
                        channel.channelNumber.contains(searchQuery, ignoreCase = true) ||
                        channel.category.contains(searchQuery, ignoreCase = true)
            }
            matchCat && matchQuery
        }
    }

    if (isFullScreen) {
        // Fullscreen Player Overlay
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            TvVideoPlayerCard(
                channel = currentChannel,
                isPlaying = isPlaying,
                onTogglePlay = { TvManager.togglePlayPause() },
                isFullScreen = true,
                onToggleFullScreen = { isFullScreen = false }
            )
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.LiveTv,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    "ทีวีออนไลน์สด (Thai Live TV)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "สตรีมสด IPTV-Org Open API (${channels.size} ช่อง)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("tv_back_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "กลับ")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { TvManager.syncLiveThaiTvFromApi() },
                            enabled = !isSyncing,
                            modifier = Modifier.testTag("tv_sync_button")
                        ) {
                            if (isSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Icon(Icons.Default.CloudSync, contentDescription = "ซิงค์ API สด")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Live Video Player Screen
                item {
                    TvVideoPlayerCard(
                        channel = currentChannel,
                        isPlaying = isPlaying,
                        onTogglePlay = { TvManager.togglePlayPause() },
                        isFullScreen = false,
                        onToggleFullScreen = { isFullScreen = true }
                    )
                }

                // 2. Channel Quick Number Presets (รีโมทปุ่มกดช่องด่วน)
                item {
                    TvRemotePresetsBar(
                        channels = channels,
                        currentChannel = currentChannel,
                        onSelectChannel = { TvManager.selectChannel(it) }
                    )
                }

                // 3. API Status & Scanner Banner
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isSyncing) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(10.dp)
                                ) {}
                                Text(
                                    TvManager.syncLogMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            FilledTonalButton(
                                onClick = { TvManager.syncLiveThaiTvFromApi() },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ซิงค์สด", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }

                // 4. Search & Filter Bar
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("tv_search_input"),
                            placeholder = { Text("ค้นหาช่องทีวี เช่น ช่อง 3, ไทยรัฐ, เวิร์คพอยท์...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "ล้าง")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp)
                        )

                        // Category Tabs
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(categories) { cat ->
                                FilterChip(
                                    selected = selectedCategory == cat,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(cat, style = MaterialTheme.typography.labelMedium) }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "รายการช่องดิจิทัลทีวี (${filteredChannels.size} ช่อง)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(onClick = { viewMode = "grid" }) {
                                    Icon(
                                        Icons.Default.GridView,
                                        contentDescription = "Grid",
                                        tint = if (viewMode == "grid") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { viewMode = "list" }) {
                                    Icon(
                                        Icons.Default.ViewList,
                                        contentDescription = "List",
                                        tint = if (viewMode == "list") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // 5. Channel List / Grid
                if (filteredChannels.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.TvOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "ไม่พบช่องทีวีที่ตรงกับคำค้นหา",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(filteredChannels) { ch ->
                        TvChannelRowCard(
                            channel = ch,
                            isSelected = currentChannel.id == ch.id,
                            onClick = { TvManager.selectChannel(ch) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun TvVideoPlayerCard(
    channel: TvChannel,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    isFullScreen: Boolean = false,
    onToggleFullScreen: () -> Unit = {}
) {
    val context = LocalContext.current
    var hasError by remember { mutableStateOf(false) }

    Card(
        modifier = if (isFullScreen) {
            Modifier
                .fillMaxSize()
                .testTag("tv_video_player_card")
        } else {
            Modifier
                .fillMaxWidth()
                .testTag("tv_video_player_card")
        },
        shape = if (isFullScreen) RoundedCornerShape(0.dp) else RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isFullScreen) 0.dp else 6.dp
        )
    ) {
        Column(modifier = if (isFullScreen) Modifier.fillMaxSize() else Modifier.fillMaxWidth()) {
            // Video Frame: true fullscreen fills the whole screen,
            // embedded mode keeps the 16:9 aspect ratio.
            Box(
                modifier = if (isFullScreen) {
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color.Black)
                } else {
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(Color.Black)
                }
            ) {
                // Live Stream Video View via AndroidView
                key(channel.streamUrl, isPlaying) {
                    if (isPlaying) {
                        AndroidView(
                            factory = { ctx ->
                                VideoView(ctx).apply {
                                    layoutParams = FrameLayout.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                    val mediaController = MediaController(ctx)
                                    mediaController.setAnchorView(this)
                                    setMediaController(null) // Use custom UI controls

                                    setOnPreparedListener { mp ->
                                        mp.isLooping = true
                                        TvManager.isBuffering = false
                                        hasError = false
                                        start()
                                    }
                                    setOnErrorListener { _, what, extra ->
                                        Log.e("ThaiOnlineTvScreen", "MediaPlayer Error: $what, $extra")
                                        hasError = true
                                        TvManager.isBuffering = false
                                        
                                        // Stop playback to prevent infinite error loops
                                        try {
                                            stopPlayback()
                                        } catch (e: Exception) {
                                            Log.e("ThaiOnlineTvScreen", "stopPlayback failed: ${e.message}")
                                        }
                                        
                                        // Only try fallback once
                                        if (channel.backupStreamUrl.isNotEmpty() && channel.streamUrl != channel.backupStreamUrl) {
                                            try {
                                                setVideoURI(Uri.parse(channel.backupStreamUrl))
                                                start()
                                                hasError = false // Reset error if fallback succeeds
                                            } catch (e: Exception) {
                                                Log.e("ThaiOnlineTvScreen", "Fallback failed: ${e.message}")
                                            }
                                        }
                                        true
                                    }
                                    try {
                                        setVideoURI(Uri.parse(channel.streamUrl))
                                    } catch (e: Exception) {
                                        hasError = true
                                    }
                                }
                            },
                            update = { videoView ->
                                if (isPlaying) {
                                    if (!videoView.isPlaying) {
                                        videoView.start()
                                    }
                                } else {
                                    if (videoView.isPlaying) {
                                        videoView.pause()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Error / Simulation Visual Overlay
                if (hasError || !isPlaying) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Black.copy(alpha = 0.6f),
                                        MaterialTheme.colorScheme.surfaceTint.copy(alpha = 0.2f),
                                        Color.Black.copy(alpha = 0.9f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        if (!isPlaying) Icons.Default.PlayArrow else Icons.Default.Tv,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            Text(
                                channel.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                when {
                                    hasError -> "ไม่สามารถเชื่อมต่อสัญญาณสตรีมมิ่งได้ (ลองเปลี่ยนช่อง)"
                                    !isPlaying -> "กดปุ่มเล่นเพื่อรับชมการถ่ายทอดสด"
                                    else -> "กำลังเชื่อมโยงสัญญาณสตรีมมิ่งสด (Live Broadcast)..."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // Top Overlay Badges (LIVE & Quality)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // LIVE Red Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFD32F2F)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White,
                                modifier = Modifier.size(6.dp)
                            ) {}
                            Text(
                                "สด (LIVE)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Resolution Quality Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.Black.copy(alpha = 0.6f)
                    ) {
                        Text(
                            channel.quality,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Bottom Control Bar
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                            )
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledIconButton(
                                onClick = onTogglePlay,
                                modifier = Modifier.size(36.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "เล่น/หยุด",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(onClick = onToggleFullScreen) {
                                Icon(
                                    if (isFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                    contentDescription = "เต็มจอ",
                                    tint = Color.White
                                )
                            }
                            Column {
                                Text(
                                    channel.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1
                                )
                                Text(
                                    "ช่องหมายเลข ${channel.channelNumber} • ${channel.category}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }

            // Channel Description & Details (hidden in true fullscreen)
            if (!isFullScreen) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp)
                ) {
                    Text(
                        channel.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun TvRemotePresetsBar(
    channels: List<TvChannel>,
    currentChannel: TvChannel,
    onSelectChannel: (TvChannel) -> Unit
) {
    val topChannels = channels.take(7)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "รีโมทเลือกช่องด่วน (Quick Channels)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "กดเปลี่ยนช่องทันที",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(topChannels) { ch ->
                val isSelected = currentChannel.id == ch.id
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.clickable { onSelectChannel(ch) }
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "CH ${ch.channelNumber}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                        )
                        Text(
                            ch.name.take(12),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TvChannelRowCard(
    channel: TvChannel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("tv_channel_item_${channel.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Channel Number Box
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            channel.channelNumber,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            channel.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isSelected) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    "กำลังดู",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        "${channel.category} • ${channel.quality}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            FilledTonalIconButton(
                onClick = onClick,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                )
            ) {
                Icon(
                    if (isSelected) Icons.Default.PlayArrow else Icons.Default.LiveTv,
                    contentDescription = "ดูช่องนี้",
                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
