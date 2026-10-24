package jp.climafit.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun AiStatusScreen(model: LayeRoomViewModel, mode: AiMode, onBack: () -> Unit, onRetryPhoto: () -> Unit) {
    val request = model.latestAiRequest?.takeIf { it.mode == mode }
    val location = request?.location ?: model.roomName
    Column(Modifier.fillMaxSize().testTag(if (mode == AiMode.PHOTO) "photo-diagnosis-screen" else "suggestion-screen")
        .verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(painterResource(R.drawable.ic_back), contentDescription = "ホームに戻る") }
            Text(if (mode == AiMode.PHOTO) "コーデ診断" else "${location}のおすすめ",
                style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        UiCard(color = BlueSoft, border = BlueLine) {
            Text("${location} · 室温 ${formatTemp(request?.indoorTemperature ?: model.room.temperature)}℃", color = Blue, fontWeight = FontWeight.Bold)
            Text("金沢の気温 ${formatTemp(request?.outdoorTemperature ?: model.outdoorTemperature)}℃", color = Blue)
        }
        if (mode == AiMode.PHOTO) {
            when {
                model.photoError != null -> UiCard {
                    Text("写真を読み込めませんでした", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(model.photoError.orEmpty(), color = Muted)
                    Button(onClick = onRetryPhoto) { Text("写真を選び直す") }
                }
                model.loadingPhoto -> UiCard {
                    CircularProgressIndicator()
                    Text("写真を読み込み中…", fontWeight = FontWeight.Bold)
                }
                model.photo != null && request != null -> UiCard {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        CircularProgressIndicator(Modifier.size(28.dp))
                        Text("コーデを確認しています", fontWeight = FontWeight.Bold)
                    }
                    Image(model.photo!!.asImageBitmap(), contentDescription = "選択した診断用の写真",
                        modifier = Modifier.fillMaxWidth().height(280.dp).clip(RoundedCornerShape(18.dp)), contentScale = ContentScale.Fit)
                }
                else -> UiCard {
                    Text("診断する写真を選んでください。", color = Muted)
                    Button(onClick = onRetryPhoto) { Text("写真を選び直す") }
                }
            }
        } else {
            UiCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SymbolBadge(R.drawable.ic_nav_sun, Blue, BlueSoft)
                    Text("今日のおすすめコーデ", style = MaterialTheme.typography.titleLarge)
                }
                Image(painterResource(R.drawable.outfit_warm), contentDescription = "おすすめのコーデ写真",
                    modifier = Modifier.fillMaxWidth().height(300.dp).clip(RoundedCornerShape(18.dp)), contentScale = ContentScale.Fit)
                Text(model.rooms.firstOrNull { it.name == location }?.recommendationDescription.orEmpty(), color = Muted)
                OutlinedButton(onClick = { /* Backend save action is not connected. */ }, modifier = Modifier.fillMaxWidth()) {
                    Icon(painterResource(R.drawable.ic_save), contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("このコーデを保存")
                }
            }
        }
    }
}
