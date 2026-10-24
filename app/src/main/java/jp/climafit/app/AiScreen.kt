package jp.climafit.app

import android.content.ActivityNotFoundException
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun AiScreen(model: LayeRoomViewModel) {
    var mode by rememberSaveable { mutableStateOf<AiMode?>(null) }
    val selectedMode = mode
    if (selectedMode == null) {
        AiEntrance { mode = it }
    } else {
        val onBack = { mode = null }
        BackHandler(onBack = onBack)
        AiActionScreen(model, selectedMode, onBack)
    }
}
@Composable
private fun AiEntrance(onSelect: (AiMode) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text("今日は、どちらから？", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
        Text("写真を選ぶか、ボタンをひと押し。\n文字を入力せずに、コーデを見つけよう。", color = Muted)
        AiChoiceCard("写真でコーデ診断", "服の写真を撮る・選ぶ → 写真を送って評価してもらう", R.drawable.ic_camera,
            ClimatePalette(OrangeSoft, Orange, OrangeLine)) { onSelect(AiMode.PHOTO) }
        AiChoiceCard("AIにコーデを提案してもらう", "ボタンを押すだけで、今日の服装をリクエスト", R.drawable.ic_nav_sun,
            ClimatePalette(BlueSoft, Blue, BlueLine)) { onSelect(AiMode.SUGGEST) }
        Text("UIプレビューです。実際の送信・AI処理・保存は行いません。",
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
private fun AiActionScreen(model: LayeRoomViewModel, mode: AiMode, onBack: () -> Unit) {
    var showPhotoOptions by rememberSaveable { mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> uri?.let(model::loadPhoto) }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap -> bitmap?.let(model::useCameraPreview) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = onBack) { Icon(painterResource(R.drawable.ic_back), contentDescription = "最初の2つの選択肢に戻る") }
            Text(mode.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        UiCard(color = BlueSoft, border = BlueLine) {
            Text("${model.roomName} · 室温 ${formatTemp(model.room.temperature)}℃", color = Blue, fontWeight = FontWeight.Bold)
            Text("金沢の気温 ${formatTemp(model.outdoorTemperature)}℃", color = Blue)
        }
        if (mode == AiMode.PHOTO) {
            UiCard {
                Text("今日のコーデを写真でチェック", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("服の写真を選んで、診断ボタンを押してください。文章の入力は不要です。", color = Muted)
                model.photo?.let { photo ->
                    Image(photo.asImageBitmap(), contentDescription = "診断する服の写真",
                        modifier = Modifier.fillMaxWidth().height(270.dp).clip(RoundedCornerShape(16.dp)), contentScale = ContentScale.Fit)
                    TextButton(onClick = model::clearPhoto) { Text("写真を解除") }
                }
                if (model.loadingPhoto) { LinearProgressIndicator(Modifier.fillMaxWidth()); Text("写真を読み込み中…") }
                OutlinedButton(onClick = { showPhotoOptions = true }, enabled = !model.loadingPhoto, modifier = Modifier.fillMaxWidth()) {
                    Icon(painterResource(R.drawable.ic_camera), contentDescription = null)
                    Spacer(Modifier.width(8.dp)); Text(if (model.photo == null) "服の写真を撮る・選ぶ" else "別の写真に変更する")
                }
                Button(onClick = model::requestPhotoPreview, enabled = model.photo != null && !model.loadingPhoto,
                    modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(14.dp)) { Text("この写真で診断する") }
                Text("プレビューのため、写真は送信されません。", style = MaterialTheme.typography.bodySmall, color = Muted)
            }
            if (model.photoResultVisible) AiResultCard(model, mode)
        } else {
            UiCard {
                Text("今日の一着を、ワンタップで。", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("気温・室温や過去のコーデを使った提案を、ボタンひとつでリクエストします。", color = Muted)
                Button(onClick = model::requestSuggestionPreview, modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp), colors = ButtonDefaults.buttonColors(containerColor = Blue)) {
                    Icon(painterResource(R.drawable.ic_nav_outfits), contentDescription = null, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(8.dp)); Text(TODAY_OUTFIT_PROMPT, fontWeight = FontWeight.Bold)
                }
                Text("文字入力は不要です。プレビューでは送信せず、表示サンプルを開きます。", style = MaterialTheme.typography.bodySmall, color = Muted)
            }
            if (model.suggestionResultVisible) AiResultCard(model, mode)
        }
    }
    if (showPhotoOptions) {
        ModalBottomSheet(onDismissRequest = { showPhotoOptions = false }) {
            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("服の写真を選ぶ", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
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
private fun AiResultCard(model: LayeRoomViewModel, mode: AiMode) {
    UiCard {
        Badge("表示サンプル")
        Text(if (mode == AiMode.PHOTO) "診断結果" else "今日のおすすめコーデ",
            style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        if (mode == AiMode.SUGGEST) {
            Image(painterResource(R.drawable.outfit_warm), contentDescription = "提案結果のサンプル写真",
                modifier = Modifier.fillMaxWidth().height(280.dp).clip(RoundedCornerShape(16.dp)), contentScale = ContentScale.Fit)
        }
        Text(if (mode == AiMode.PHOTO) "写真に対する評価やアドバイスがここに表示されます。"
            else "AIから提案されたコーデや、おすすめの理由がここに表示されます。", color = Muted)
        OutlinedButton(onClick = { model.message("保存ボタンの表示サンプルです。コーデは保存されません。") }, modifier = Modifier.fillMaxWidth()) {
            Text("このコーデを保存")
        }
    }
}
