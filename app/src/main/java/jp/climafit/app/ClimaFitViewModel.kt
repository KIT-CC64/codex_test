package jp.climafit.app

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Frontend presentation state only. Nothing is saved or sent to a server. */
class ClimaFitViewModel(application: Application) : AndroidViewModel(application) {
    val rooms = PreviewData.rooms
    val outdoorTemperature = PreviewData.outdoorTemperature
    val logs = PreviewData.logs
    val reviews = PreviewData.reviews
    var roomName by mutableStateOf(rooms.first().name); private set
    val room: RoomUi get() = rooms.first { it.name == roomName }
    var notice by mutableStateOf<String?>(null); private set
    var photo by mutableStateOf<Bitmap?>(null); private set
    var loadingPhoto by mutableStateOf(false); private set
    private val photoMessages = mutableStateListOf(
        ChatMessageUi("photo-intro", "今日のコーデを見せてください。写真を撮るか、写真から選んで、気になることを添えて相談できます。", false),
    )
    private val suggestionMessages = mutableStateListOf(
        ChatMessageUi("suggest-intro", "今日着る服を一緒に考えましょう。行き先や、どんな服装にしたいかを教えてください。気温や室温、過去のコーデを使った提案の画面サンプルです。", false),
    )
    fun messagesFor(mode: AiMode): List<ChatMessageUi> = if (mode == AiMode.PHOTO) photoMessages else suggestionMessages
    private var nextMessage = 1
    fun sendPreviewMessage(text: String, mode: AiMode) {
        if ((text.isBlank() && photo == null) || loadingPhoto) return
        val messages = if (mode == AiMode.PHOTO) photoMessages else suggestionMessages
        val attachedPhoto = photo
        messages.add(ChatMessageUi("user-${nextMessage++}", text.trim(), true, photo = attachedPhoto))
        messages.add(if (mode == AiMode.PHOTO) {
            ChatMessageUi("assistant-${nextMessage++}", "【診断画面のサンプル応答】写真や文章は送信していません。実際の評価・アドバイスはAI連携後に表示されます。", false, showSave = attachedPhoto != null)
        } else {
            ChatMessageUi("assistant-${nextMessage++}", "【提案画面のサンプル応答】カーディガンとシャツを組み合わせた表示例です。実際には、気温や室温、過去のコーデをもとにしたAIの提案をここに表示します。", false, samplePhoto = R.drawable.outfit_warm, showSave = true)
        })
        clearPhoto()
    }
    fun useCameraPreview(bitmap: Bitmap) { clearPhoto(); photo = bitmap }
    private var photoJob: Job? = null
    private var photoVersion = 0
    fun message(text: String) { notice = text }
    fun dismissNotice() { notice = null }
    fun selectRoom(name: String) { if (rooms.any { it.name == name }) roomName = name }
    fun clearPhoto() { photoVersion++; photoJob?.cancel(); photo = null; loadingPhoto = false }
    fun loadPhoto(uri: Uri) {
        clearPhoto(); loadingPhoto = true
        val version = photoVersion
        photoJob = viewModelScope.launch {
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    val resolver = getApplication<Application>().contentResolver
                    require(resolver.getType(uri)?.startsWith("image/") == true)
                    val data = resolver.openInputStream(uri)?.use { input ->
                        val output = ByteArrayOutputStream()
                        val buffer = ByteArray(8192)
                        var total = 0
                        while (true) {
                            val read = input.read(buffer)
                            if (read < 0) break
                            total += read; require(total <= 10 * 1024 * 1024)
                            output.write(buffer, 0, read)
                        }
                        output.toByteArray()
                    } ?: error("Unreadable image")
                    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeByteArray(data, 0, data.size, bounds)
                    require(bounds.outWidth > 0 && bounds.outHeight > 0)
                    val options = BitmapFactory.Options().apply { inSampleSize = 1 }
                    while (maxOf(bounds.outWidth, bounds.outHeight) / options.inSampleSize > 1400) options.inSampleSize *= 2
                    BitmapFactory.decodeByteArray(data, 0, data.size, options) ?: error("Unsupported image")
                }
                if (version == photoVersion) photo = bitmap
            } catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
            catch (_: Exception) { message("画像を表示できませんでした。10MB以下のJPEG・PNG等を選んでください。") }
            finally { if (version == photoVersion) loadingPhoto = false }
        }
    }
}
