package jp.climafit.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Locale
import kotlin.math.abs

private val Warm = Color(0xFFFDFBF7)
private val Brown = Color(0xFF433425)
private val Peach = Color(0xFFE05D38)
private val Sage = Color(0xFF638666)
private enum class Tab(val label: String, val symbol: String) {
    DASHBOARD("提案", "☀"), AI("コーデAI", "✦"), LOGS("記録", "▤"), REVIEWS("口コミ", "☏")
}
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(
                primary = Peach, onPrimary = Color.White, secondary = Sage,
                background = Warm, surface = Warm, onSurface = Brown,
                surfaceVariant = Color(0xFFF7F2EA), onSurfaceVariant = Color(0xFF67523D),
            )) { ClimaFitApp() }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClimaFitApp(model: ClimaFitViewModel = viewModel()) {
    var tab by rememberSaveable { mutableStateOf(Tab.DASHBOARD) }
    val snackbar = remember { SnackbarHostState() }
    val screens = rememberSaveableStateHolder()
    val notice = model.notice
    LaunchedEffect(notice) {
        notice?.let { snackbar.showSnackbar(it); model.dismissNotice() }
    }
    Scaffold(
        containerColor = Warm,
        topBar = { TopAppBar(title = {
            Column {
                Text("ClimaFit AI", fontWeight = FontWeight.Bold)
                Text("快適な服装を提案 · デモデータ", style = MaterialTheme.typography.labelSmall, color = Sage)
            }
        }, actions = {
            TextButton(onClick = { model.message("サンプルデータを表示しています。センサー・天気APIは未接続です。") }) { Text("更新") }
        }) },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFFF7F2EA)) {
                Tab.entries.forEach { destination ->
                    NavigationBarItem(selected = tab == destination, onClick = { tab = destination },
                        icon = { Text(destination.symbol) }, label = { Text(destination.label) })
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding).imePadding()) {
            screens.SaveableStateProvider(tab.name) {
                when (tab) {
                    Tab.DASHBOARD -> Dashboard(model)
                    Tab.AI -> AiScreen(model)
                    Tab.LOGS -> LogsScreen(model)
                    Tab.REVIEWS -> ReviewsScreen(model)
                }
            }
        }
    }
}
@Composable
private fun Panel(title: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = Brown)
            content()
        }
    }
}
@Composable
private fun Selector(label: String, selected: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Box {
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Text("$selected  ▾")
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { expanded = false; onSelect(option) }) }
            }
        }
    }
}
@Composable
private fun Dashboard(model: ClimaFitViewModel) {
    val room = model.room
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Panel("行き先を選択") { Selector("場所", room.name, model.rooms.map { it.name }, model::selectRoom) } }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Panel("指定場所の室温", Modifier.weight(1f)) {
                    Text("${formatTemp(room.temperature)}℃", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("湿度 ${room.humidity}%", style = MaterialTheme.typography.bodySmall)
                }
                Panel("外気温", Modifier.weight(1f)) {
                    Text("${formatTemp(model.outdoorTemperature)}℃", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("サンプル / 天気未接続", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        item {
            Panel("快適さ指標（不快指数）") {
                Text(room.discomfort, color = Sage, fontWeight = FontWeight.Bold)
                HorizontalDivider(color = Sage)
                Text("表示確認用のサンプルです。体感には個人差があります。", style = MaterialTheme.typography.bodySmall)
            }
        }
        item {
            Panel("温度に合わせた服装提案") {
                Text("外気 ${formatTemp(model.outdoorTemperature)}℃ × 室内 ${formatTemp(room.temperature)}℃", style = MaterialTheme.typography.labelMedium, color = Peach)
                Text(room.recommendationTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(room.recommendationDescription, style = MaterialTheme.typography.bodyMedium)
                room.layers.forEachIndexed { index, layer ->
                    Text("${index + 1}. $layer", Modifier.fillMaxWidth().background(Warm, RoundedCornerShape(12.dp)).padding(12.dp))
                }
                Button(onClick = { model.message("画面サンプルです。記録の保存処理は実装していません。") }, modifier = Modifier.fillMaxWidth()) {
                    Text("この服装提案を記録する")
                }
            }
        }
    }
}
@Composable
private fun AiScreen(model: ClimaFitViewModel) {
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> uri?.let(model::loadPhoto) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Panel("今日のコーデ AI判定") {
                Text("着ている服の写真を選んで、室温・外気温に合うか相談できます。")
                Text("画面プレビュー版です。写真の選択と表示を確認できます。", color = Peach, style = MaterialTheme.typography.bodySmall)
                OutlinedButton(onClick = { picker.launch("image/*") }, enabled = !model.loadingPhoto, modifier = Modifier.fillMaxWidth()) { Text("写真を選択（10MB以下）") }
                if (model.loadingPhoto) { CircularProgressIndicator(); Text("画像を読み込み中…") }
                model.photo?.let { bitmap ->
                    Image(bitmap.asImageBitmap(), contentDescription = "選択したコーデの写真", modifier = Modifier.fillMaxWidth().height(300.dp), contentScale = ContentScale.Fit)
                    TextButton(onClick = model::clearPhoto) { Text("写真を解除") }
                }
                Button(enabled = model.photo != null && !model.loadingPhoto, onClick = {
                    model.message("画面プレビュー版のため、AI診断は利用できません。写真は送信していません。")
                }, modifier = Modifier.fillMaxWidth()) { Text("AIでコーデを診断・評価する") }
            }
        }
    }
}
@Composable
private fun LogsScreen(model: ClimaFitViewModel) {
    var mode by rememberSaveable { mutableStateOf(TemperatureMode.ALL) }
    var custom by rememberSaveable { mutableStateOf("22") }
    val temperature = if (mode == TemperatureMode.CURRENT) model.room.temperature else custom.toDoubleOrNull()?.takeIf { it.isFinite() && it in -50.0..60.0 }
    val visible = model.logs.filter { mode == TemperatureMode.ALL || (temperature != null && abs(it.indoorTemperature - temperature) <= 2.0) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Panel("コーデ記録（サンプル） · ${visible.size} / ${model.logs.size}件") {
                Selector("温度条件で絞り込む", mode.label, TemperatureMode.entries.map { it.label }) { label -> mode = TemperatureMode.entries.first { it.label == label } }
                if (mode == TemperatureMode.CUSTOM) {
                    OutlinedTextField(custom, { custom = it }, label = { Text("指定した温度（℃）") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true,
                        isError = temperature == null, modifier = Modifier.fillMaxWidth())
                }
                if (mode != TemperatureMode.ALL) Text(if (temperature == null) "−50〜60℃の温度を入力してください。" else "${formatTemp(temperature)}℃ ±2℃の室温で記録したコーデを表示")
            }
        }
        if (visible.isEmpty()) item { Text("条件に合うサンプル記録はありません。", modifier = Modifier.padding(12.dp)) }
        items(visible, key = { it.id }) { log ->
            Panel(log.title) {
                Text("${log.date} · ${log.location}", style = MaterialTheme.typography.labelMedium)
                Text("室内 ${formatTemp(log.indoorTemperature)}℃ / 外気 ${formatTemp(log.outdoorTemperature)}℃", color = Peach)
                Text(log.comment)
            }
        }
    }
}
@Composable
private fun ReviewsScreen(model: ClimaFitViewModel) {
    var location by rememberSaveable { mutableStateOf(model.roomName) }
    var feel by rememberSaveable { mutableStateOf("快適") }
    var comment by rememberSaveable { mutableStateOf("") }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Panel("教室の寒さ・暑さを投稿") {
                Selector("場所", location, model.rooms.map { it.name }) { location = it }
                Selector("体感", feel, listOf("快適", "少し寒い", "かなり寒い", "少し暑い")) { feel = it }
                OutlinedTextField(comment, { if (it.length <= 500) comment = it }, label = { Text("コメント") },
                    supportingText = { Text("${comment.length} / 500文字") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                Button(onClick = { model.message("画面サンプルです。投稿・保存・送信は行いません。") },
                    enabled = comment.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("口コミを投稿する") }
                Text("入力フォームの見た目と操作の確認用です。データは保存・送信しません。", style = MaterialTheme.typography.bodySmall)
            }
        }
        item { Text("みんなの口コミ（サンプル）", fontWeight = FontWeight.Bold) }
        items(model.reviews, key = { it.id }) { review ->
            Panel("${review.location} · ${review.feel}") {
                Text(review.comment)
                Text(review.date, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
private fun formatTemp(value: Double): String = String.format(Locale.JAPAN, "%.1f", value)
