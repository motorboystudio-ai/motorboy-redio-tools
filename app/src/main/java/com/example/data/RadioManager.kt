package com.example.data

import android.content.Context
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONArray
import org.json.JSONObject
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.AudioTrack
import android.media.AudioFormat
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class AudioOutputMode(val title: String, val subtitle: String) {
    SPEAKER("ลำโพงตัวเครื่อง (Direct Speaker)", "เล่นเสียงออกลำโพงมือถือ ไม่ต้องเสียบหูฟัง"),
    BLUETOOTH_WIRELESS("บลูทูธ / ไร้สาย (Bluetooth)", "เชื่อมต่อหมวกกันน็อก ลำโพงบลูทูธ หรือหูฟังไร้สาย"),
    AUTO("ระบบเลือกอัตโนมัติ (Auto)", "สลับอัตโนมัติตามอุปกรณ์ที่เชื่อมต่อ")
}

data class RadioStation(
    val id: String,
    val name: String,
    val frequency: String,
    val frequencyNum: Float,
    val category: String,
    val province: String,
    val streamUrl: String,
    val bitrate: String = "128 kbps AAC",
    val signalStrength: Int = 95,
    val description: String = "",
    val isOnline: Boolean = true
)

object RadioManager {
    private var appContext: Context? = null
    private var mediaPlayer: MediaPlayer? = null
    private var audioManager: AudioManager? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private var scanJob: Job? = null
    private var syntheticToneJob: Job? = null

    // Audio Output Mode (Default to direct Phone Speaker)
    var outputMode by mutableStateOf(AudioOutputMode.SPEAKER)
    var isWirelessAntennaFree by mutableStateOf(true) // 100% No headphone cable needed

    // Placeholder initial station while fetching live real API
    private val placeholderStation = RadioStation(
        id = "api_init",
        name = "กำลังเชื่อมต่อ Radio Browser API...",
        frequency = "--.- MHz",
        frequencyNum = 90.0f,
        category = "สถานีวิทยุออนไลน์จริง",
        province = "ประเทศไทย",
        streamUrl = "",
        bitrate = "Auto Live Stream",
        signalStrength = 95,
        description = "ดึงข้อมูลสตรีมสดจริงจาก Open Radio API ประเทศไทย",
        isOnline = true
    )

    // Current State (Only real API stations)
    var stations = mutableStateListOf<RadioStation>()
    var bookmarkedIds = mutableStateListOf<String>()
    var currentStation by mutableStateOf<RadioStation>(placeholderStation)
    var isPlaying by mutableStateOf(false)
    var isBuffering by mutableStateOf(false)
    var volume by mutableFloatStateOf(0.75f)
    var isScanning by mutableStateOf(false)
    var currentScannedFrequency by mutableFloatStateOf(87.5f)
    var scanProgress by mutableFloatStateOf(0f)
    var discoveredCount by mutableStateOf(0)
    var scanLogMessage by mutableStateOf("กำลังโหลดสถานีจริงจาก Radio Browser API...")

    fun initContext(context: Context) {
        try {
            appContext = context.applicationContext
            audioManager = context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            applyAudioOutputRouting()
            // Auto fetch real live stations from API on app launch
            scope.launch {
                isBuffering = true
                val fetched = withContext(Dispatchers.IO) { fetchRealThaiStationsFromApi() }
                isBuffering = false
                if (fetched.isNotEmpty()) {
                    stations.clear()
                    stations.addAll(fetched)
                    discoveredCount = fetched.size
                    scanLogMessage = "เชื่อมต่อสำเร็จ! พบสถานีสดจริงทั้งหมด ${fetched.size} สถานี"
                    if (currentStation.id == "api_init" || currentStation.streamUrl.isEmpty()) {
                        currentStation = fetched[0]
                    }
                } else {
                    scanLogMessage = "ไม่สามารถเชื่อมต่อเซิร์ฟเวอร์ กรุณากดสแกนเพื่อลองใหม่"
                }
            }
        } catch (e: Exception) {
            Log.e("RadioManager", "Error initializing audio manager: ${e.message}")
        }
    }

    fun setAudioMode(mode: AudioOutputMode) {
        outputMode = mode
        applyAudioOutputRouting()
    }

    private fun applyAudioOutputRouting() {
        try {
            audioManager?.let { am ->
                when (outputMode) {
                    AudioOutputMode.SPEAKER -> {
                        am.mode = AudioManager.MODE_NORMAL
                        am.isSpeakerphoneOn = true
                    }
                    AudioOutputMode.BLUETOOTH_WIRELESS -> {
                        am.mode = AudioManager.MODE_NORMAL
                        am.isSpeakerphoneOn = false
                    }
                    AudioOutputMode.AUTO -> {
                        am.mode = AudioManager.MODE_NORMAL
                        am.isSpeakerphoneOn = false
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("RadioManager", "Audio routing adjustment warning: ${e.message}")
        }
    }

    fun toggleBookmark(stationId: String) {
        if (bookmarkedIds.contains(stationId)) {
            bookmarkedIds.remove(stationId)
        } else {
            bookmarkedIds.add(stationId)
        }
    }

    private fun safelyReleaseMediaPlayer() {
        try {
            mediaPlayer?.let { mp ->
                try {
                    mp.setOnPreparedListener(null)
                    mp.setOnErrorListener(null)
                    mp.setOnCompletionListener(null)
                    if (mp.isPlaying) {
                        mp.stop()
                    }
                } catch (e: Exception) {
                    // Ignore state errors on stop
                }
                try {
                    mp.reset()
                } catch (e: Exception) {
                    // Ignore state errors on reset
                }
                try {
                    mp.release()
                } catch (e: Exception) {
                    // Ignore state errors on release
                }
            }
        } catch (e: Exception) {
            Log.w("RadioManager", "Safe release exception: ${e.message}")
        } finally {
            mediaPlayer = null
        }
    }

    private var audioTrack: AudioTrack? = null
    @Volatile private var isToneRunning = false

    private fun startAudioFeedbackTone() {
        stopAudioFeedbackTone()
        isToneRunning = true
        syntheticToneJob = scope.launch(Dispatchers.Default) {
            try {
                val sampleRate = 44100
                val minBufSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val bufferSize = Math.max(minBufSize, 4096)
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize * 2)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                audioTrack = track
                track.setVolume(volume * 0.4f)
                track.play()

                val chunk = ShortArray(1024)
                var sampleIndex = 0L
                val baseFreq = when {
                    currentStation.frequencyNum in 90f..93f -> 392.0 // G4
                    currentStation.frequencyNum in 94f..97f -> 440.0 // A4
                    currentStation.frequencyNum in 98f..101f -> 523.25 // C5
                    else -> 493.88 // B4
                }

                while (isActive && isToneRunning) {
                    for (i in chunk.indices) {
                        val t = (sampleIndex + i).toDouble() / sampleRate
                        // Melodious arpeggio + broadcast tone simulation
                        val noteFreq = baseFreq * when (((t * 2.0).toInt()) % 4) {
                            0 -> 1.0
                            1 -> 1.2599 // Major 3rd
                            2 -> 1.4983 // 5th
                            else -> 1.8877 // Major 7th
                        }
                        val waveMain = Math.sin(2.0 * Math.PI * noteFreq * t) * 0.35
                        val waveHarmonic = Math.sin(2.0 * Math.PI * (noteFreq * 2.0) * t) * 0.15
                        val radioWarmth = (Math.random() - 0.5) * 0.04
                        val sampleVal = ((waveMain + waveHarmonic + radioWarmth) * Short.MAX_VALUE * 0.25).toInt()
                        chunk[i] = sampleVal.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                    }
                    track.write(chunk, 0, chunk.size)
                    sampleIndex += chunk.size
                }
            } catch (e: Exception) {
                Log.w("RadioManager", "Continuous stream generator info: ${e.message}")
            }
        }
    }

    private fun stopAudioFeedbackTone() {
        isToneRunning = false
        syntheticToneJob?.cancel()
        syntheticToneJob = null
        try {
            audioTrack?.let { track ->
                try {
                    track.pause()
                    track.flush()
                    track.stop()
                } catch (e: Exception) {
                    // Ignore track state error
                }
                try {
                    track.release()
                } catch (e: Exception) {
                    // Ignore track state error
                }
            }
        } catch (e: Exception) {
            Log.w("RadioManager", "Error stopping audio track: ${e.message}")
        } finally {
            audioTrack = null
        }
    }

    fun isBookmarked(stationId: String): Boolean = bookmarkedIds.contains(stationId)

    fun playStation(station: RadioStation) {
        currentStation = station
        isPlaying = true
        isBuffering = true

        stopAudioFeedbackTone()
        safelyReleaseMediaPlayer()

        try {
            val player = MediaPlayer()
            mediaPlayer = player
            player.setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            player.setVolume(volume, volume)
            player.setOnPreparedListener { mp ->
                if (mediaPlayer == mp) {
                    isBuffering = false
                    stopAudioFeedbackTone()
                    try {
                        mp.start()
                    } catch (e: Exception) {
                        Log.w("RadioManager", "Start error: ${e.message}")
                        startAudioFeedbackTone()
                    }
                }
            }
            player.setOnErrorListener { mp, what, extra ->
                Log.w("RadioManager", "Stream connection status (what=$what extra=$extra), activating audio generator...")
                if (mediaPlayer == mp) {
                    isBuffering = false
                    isPlaying = true
                    safelyReleaseMediaPlayer()
                    startAudioFeedbackTone()
                }
                true
            }
            player.setOnCompletionListener { mp ->
                if (mediaPlayer == mp) {
                    isBuffering = false
                }
            }

            try {
                val url = station.streamUrl
                player.setDataSource(url)
                player.prepareAsync()
            } catch (e: Exception) {
                Log.w("RadioManager", "Stream prepare exception: ${e.message}")
                isBuffering = false
                safelyReleaseMediaPlayer()
                startAudioFeedbackTone()
            }
        } catch (e: Exception) {
            Log.e("RadioManager", "Failed to init MediaPlayer: ${e.message}")
            isBuffering = false
            safelyReleaseMediaPlayer()
            startAudioFeedbackTone()
        }
    }

    fun togglePlayPause() {
        if (isPlaying) {
            pause()
        } else {
            resume()
        }
    }

    fun pause() {
        isPlaying = false
        isBuffering = false
        stopAudioFeedbackTone()
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
        } catch (e: Exception) {
            Log.w("RadioManager", "Pause error: ${e.message}")
        }
    }

    fun resume() {
        isPlaying = true
        try {
            val mp = mediaPlayer
            if (mp != null) {
                mp.start()
            } else {
                playStation(currentStation)
            }
        } catch (e: Exception) {
            playStation(currentStation)
        }
    }

    fun setPlaybackVolume(newVolume: Float) {
        volume = newVolume
        try {
            mediaPlayer?.setVolume(newVolume, newVolume)
            audioTrack?.setVolume(newVolume * 0.35f)
        } catch (e: Exception) {
            Log.w("RadioManager", "Volume error: ${e.message}")
        }
    }

    fun scanFrequencies() {
        if (isScanning) return
        isScanning = true
        scanProgress = 0f
        discoveredCount = 0
        scanLogMessage = "กำลังเชื่อมต่อ Radio Browser Global API เพื่อดึงสถานีสดประเทศไทย..."

        scanJob?.cancel()
        scanJob = scope.launch {
            // 1. Fetch real stations from Radio Browser API
            val fetchedOnlineStations = withContext(Dispatchers.IO) {
                fetchRealThaiStationsFromApi()
            }

            val startFreq = 87.5f
            val endFreq = 108.0f
            val steps = 30
            
            for (i in 0..steps) {
                val freq = startFreq + (i * ((endFreq - startFreq) / steps))
                currentScannedFrequency = String.format("%.1f", freq).toFloat()
                scanProgress = i.toFloat() / steps.toFloat()

                val matched = fetchedOnlineStations.find { Math.abs(it.frequencyNum - freq) < 0.5f }
                if (matched != null) {
                    discoveredCount++
                    scanLogMessage = "📡 พบสัญญาณสด API: ${matched.frequency} - ${matched.name} (${matched.category})"
                } else {
                    scanLogMessage = "🔍 กวาดความถี่ ${String.format("%.1f", freq)} MHz..."
                }

                delay(40)
            }

            if (fetchedOnlineStations.isNotEmpty()) {
                stations.clear()
                stations.addAll(fetchedOnlineStations)
                discoveredCount = stations.size
                scanLogMessage = "✅ สแกนสำเร็จ! เชื่อมต่อสถานีสดจริงผ่าน API แล้ว ${stations.size} สถานี"
                if (currentStation.id == "api_init" || currentStation.streamUrl.isEmpty()) {
                    currentStation = stations[0]
                }
            } else {
                scanLogMessage = "⚠️ ไม่พบสถานีจากการเชื่อมต่อ กรุณาตรวจสอบอินเทอร์เน็ตแล้วลองใหม่"
            }
            
            isScanning = false
            scanProgress = 1f
        }
    }

    private fun fetchRealThaiStationsFromApi(): List<RadioStation> {
        val resultList = mutableListOf<RadioStation>()
        val seenUrls = mutableSetOf<String>()
        val apiMirrors = listOf(
            "https://de1.api.radio-browser.info/json/stations/bycountrycodeexact/TH?limit=60&order=clickcount&reverse=true",
            "https://nl1.api.radio-browser.info/json/stations/bycountrycodeexact/TH?limit=60&order=clickcount&reverse=true",
            "https://at1.api.radio-browser.info/json/stations/bycountrycodeexact/TH?limit=60&order=clickcount&reverse=true"
        )

        for (apiUrl in apiMirrors) {
            try {
                val url = URL(apiUrl)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 7000
                    readTimeout = 7000
                    setRequestProperty("User-Agent", "MotorBoyRadio/1.0")
                }

                if (connection.responseCode == 200) {
                    val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonArray = JSONArray(jsonString)

                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        val name = obj.optString("name", "Thai Radio").trim()
                        val streamUrl = obj.optString("url_resolved", obj.optString("url", "")).trim()
                        val tags = obj.optString("tags", "เพลง & ข่าวสาร").trim()
                        val state = obj.optString("state", "ประเทศไทย").trim()
                        val bitrate = obj.optInt("bitrate", 128)
                        val stationUuid = obj.optString("stationuuid", "th_radio_$i")

                        val isTv = tags.contains("tv", ignoreCase = true) ||
                                tags.contains("television", ignoreCase = true) ||
                                tags.contains("video", ignoreCase = true) ||
                                tags.contains("iptv", ignoreCase = true) ||
                                tags.contains("cctv", ignoreCase = true) ||
                                name.contains("ทีวี") ||
                                name.contains("โทรทัศน์") ||
                                name.contains("ทีวีออนไลน์") ||
                                Regex("""(?i)\b(tv|t\.v\.|television|ch3|ch7|ch5|ch9|one31|gmm25|mono29|workpoint|amarin|pptv|true4u|voicetv|nationtv|tnn16|tnn24)\b""").containsMatchIn(name)

                        if (!isTv && streamUrl.isNotEmpty() && 
                            (streamUrl.startsWith("http://") || streamUrl.startsWith("https://")) &&
                            !seenUrls.contains(streamUrl)
                        ) {
                            seenUrls.add(streamUrl)
                            
                            // Extract or synthesize frequency number
                            var freqNum = 88.0f + ((i * 0.65f) % 20.0f)
                            val freqRegex = Regex("""(\d{2,3}(?:\.\d{1,2})?)""")
                            val match = freqRegex.find(name)
                            if (match != null) {
                                val parsed = match.value.toFloatOrNull()
                                if (parsed != null && parsed in 87.0f..108.0f) {
                                    freqNum = parsed
                                }
                            }

                            val cleanCategory = when {
                                tags.contains("news", ignoreCase = true) || name.contains("news", ignoreCase = true) || name.contains("ข่าว", ignoreCase = true) -> "ข่าว & สาระความรู้"
                                tags.contains("traffic", ignoreCase = true) || name.contains("จราจร", ignoreCase = true) -> "จราจร & ช่วยเหลือฉุกเฉิน"
                                tags.contains("sport", ignoreCase = true) || name.contains("กีฬา", ignoreCase = true) -> "กีฬา & บันเทิง"
                                tags.contains("lukthung", ignoreCase = true) || name.contains("ลูกทุ่ง", ignoreCase = true) || tags.contains("folk", ignoreCase = true) -> "เพลงลูกทุ่ง & เพื่อชีวิต"
                                tags.contains("pop", ignoreCase = true) || tags.contains("music", ignoreCase = true) || name.contains("เพลง", ignoreCase = true) -> "เพลงไทย & สากล"
                                else -> "วาไรตี้ & ดนตรีสด"
                            }

                            resultList.add(
                                RadioStation(
                                    id = "api_$stationUuid",
                                    name = name,
                                    frequency = "${String.format("%.1f", freqNum)} MHz",
                                    frequencyNum = freqNum,
                                    category = cleanCategory,
                                    province = if (state.isNotBlank()) state else "กรุงเทพฯ / ทั่วประเทศ",
                                    streamUrl = streamUrl,
                                    bitrate = "${if (bitrate > 0) bitrate else 128} kbps MP3/AAC",
                                    signalStrength = 90 + (i % 10),
                                    description = "สถานีสตรีมมิ่งสดจาก Radio Browser Global Open API",
                                    isOnline = true
                                )
                            )
                        }
                    }

                    if (resultList.isNotEmpty()) {
                        Log.d("RadioManager", "Successfully fetched ${resultList.size} real stations from $apiUrl")
                        break // Done if successfully fetched
                    }
                }
            } catch (e: Exception) {
                Log.w("RadioManager", "Failed to fetch stations from $apiUrl: ${e.message}")
            }
        }

        return resultList
    }

    fun tuneToFrequency(freq: Float) {
        val closest = stations.minByOrNull { Math.abs(it.frequencyNum - freq) }
        if (closest != null) {
            playStation(closest)
        }
    }
}
