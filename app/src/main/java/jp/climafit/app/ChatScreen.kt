package jp.climafit.app

import android.content.ActivityNotFoundException
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AiScreen(model: ClimaFitViewModel) {
    var mode by rememberSaveable { mutableStateOf<AiMode?>(null) }
    val selectedMode = mode
    if (selectedMode == null) {
        AiEntrance { mode = it }
    } else {
        fun backToEntrance() { model.clearPhoto(); mode = null }
        BackHandler { backToEntrance() }
        AiConversation(model, selectedMode, ::backToEntrance)
    }
}
@Composable
private fun AiEntrance(onSelect: (AiMode) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text("今日は、どちらから？", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
        Text("写真で相談しても、提案から選んでも。\nあなたに合うコーデを一緒に見つけよう。", color = Muted)
        AiChoiceCard("写真でコーデ診断", "服の写真を撮る・選ぶ → コーデを評価してもらう", R.drawable.ic_camera,
            ClimatePalette(OrangeSoft, Orange, OrangeLine)) { onSelect(AiMode.PHOTO) }
        AiChoiceCard("AIにコーデを提案してもらう", "気温・室温や、過去のコーデから相談する", R.drawable.ic_nav_sun,
            ClimatePalette(BlueSoft, Blue, BlueLine)) { onSelect(AiMode.SUGGEST) }
        Text("UIプレビューです。AIによる評価・提案やデータの保存は行いません。",
            style = MaterialTheme.typography.bodySmall, color = Muted)
    }
}
@Composable
private fun AiChoiceCard(title: String, description: String, icon: Int, palette: ClimatePalette, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth(), color = palette.background,
        shape = RoundedCornerShape(26.dp), border = BorderStroke(1.dp, palette.border)) {
        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(38.dp), tint = palette.foreground)
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = palette.foreground)
            Text(description, style = MaterialTheme.typography.bodyMedium, color = Ink)
            Text("はじめる →", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = palette.foreground)
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AiConversation(model: ClimaFitViewModel, mode: AiMode, onBack: () -> Unit) {
    var text by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue()) }
    var showPhotoOptions by rememberSaveable { mutableStateOf(false) }
    val list = rememberLazyListState()
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> uri?.let(model::loadPhoto) }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap -> bitmap?.let(model::useCameraPreview) }
    val messages = model.messagesFor(mode)
    val messageCount = messages.size
    LaunchedEffect(messageCount) { if (messageCount > 0) list.animateScrollToItem(messageCount - 1) }
    Column(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onBack) {
                    Icon(painterResource(R.drawable.ic_back), contentDescription = "最初の2つの選択肢に戻る")
                }
                Column(Modifier.weight(1f)) {
                    Text(mode.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("${model.roomName} · 室温 ${formatTemp(model.room.temperature)}℃ / 金沢 ${formatTemp(model.outdoorTemperature)}℃",
                        style = MaterialTheme.typography.labelSmall, color = Muted)
                }
            }
            if (mode == AiMode.PHOTO) {
                FilledTonalButton(onClick = { showPhotoOptions = true }, enabled = !model.loadingPhoto, modifier = Modifier.fillMaxWidth()) {
                    Icon(painterResource(R.drawable.ic_camera), contentDescription = null)
                    Spacer(Modifier.width(8.dp)); Text("服の写真を撮る・選ぶ")
                }
            }
        }
        LazyColumn(state = list, modifier = Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)) {
            items(messages, key = { it.id }) { message -> ChatBubble(message) {
                model.message("保存ボタンの表示サンプルです。コーデは保存されません。")
            } }
        }
        Surface(color = Color.White, shadowElevation = 6.dp) {
            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (model.loadingPhoto) LinearProgressIndicator(Modifier.fillMaxWidth())
                model.photo?.let { photo ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Image(photo.asImageBitmap(), contentDescription = "添付予定の写真", modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
                        Text("この写真を添えて相談", Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
                        IconButton(onClick = model::clearPhoto) { Icon(painterResource(R.drawable.ic_close), contentDescription = "添付写真を解除") }
                    }
                }
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(onClick = { showPhotoOptions = true }, enabled = !model.loadingPhoto) {
                        Icon(painterResource(R.drawable.ic_image), contentDescription = "コーデの写真を添付", tint = Orange)
                    }
                    JapaneseMessageField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier.weight(1f),
                        placeholder = if (mode == AiMode.PHOTO) "この服装、どうかな？" else "今日の服装を提案して",
                    )
                    FilledIconButton(onClick = { model.sendPreviewMessage(text.text, mode); text = TextFieldValue() },
                        enabled = !model.loadingPhoto && text.text.length <= 1000 && (text.text.isNotBlank() || model.photo != null), modifier = Modifier.size(48.dp)) {
                        Icon(painterResource(R.drawable.ic_send), contentDescription = "メッセージを送る（画面内プレビューのみ）")
                    }
                }
            }
        }
    }
    if (showPhotoOptions) {
        ModalBottomSheet(onDismissRequest = { showPhotoOptions = false }) {
            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("コーデの写真を添える", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("写真はこの画面でのプレビューにのみ使用します。", style = MaterialTheme.typography.bodySmall, color = Muted)
                FilledTonalButton(onClick = {
                    showPhotoOptions = false
                    try { camera.launch(null) } catch (_: ActivityNotFoundException) { model.message("カメラを起動できませんでした。写真を選択してください。") }
                }, modifier = Modifier.fillMaxWidth()) {
                    Icon(painterResource(R.drawable.ic_camera), contentDescription = null); Spacer(Modifier.width(8.dp)); Text("カメラで撮る")
                }
                OutlinedButton(onClick = { showPhotoOptions = false; picker.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                    Icon(painterResource(R.drawable.ic_image), contentDescription = null); Spacer(Modifier.width(8.dp)); Text("写真から選ぶ（10MB以下）")
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
@Composable
private fun ChatBubble(message: ChatMessageUi, onSave: () -> Unit) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = if (message.fromUser) Alignment.End else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(if (message.fromUser) "あなた" else "ClimaFit AI", style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold, color = if (message.fromUser) Orange else Blue)
        Surface(modifier = Modifier.widthIn(max = 320.dp),
            color = if (message.fromUser) OrangeSoft else Color.White,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = if (message.fromUser) 20.dp else 4.dp, bottomEnd = if (message.fromUser) 4.dp else 20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (message.fromUser) OrangeLine else BlueLine)) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                message.samplePhoto?.let { image -> Image(painterResource(image), contentDescription = "会話サンプルのコーデ写真",
                    modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Fit) }
                message.photo?.let { image -> Image(image.asImageBitmap(), contentDescription = "相談中のコーデ写真",
                    modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Fit) }
                if (message.text.isNotBlank()) Text(message.text, style = MaterialTheme.typography.bodyMedium, color = Ink)
                if (message.showSave) OutlinedButton(onClick = onSave, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Text("このコーデを保存", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
