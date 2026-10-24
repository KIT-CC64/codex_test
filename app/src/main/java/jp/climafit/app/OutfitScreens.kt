package jp.climafit.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.abs

@Composable
internal fun Dashboard(model: ClimaFitViewModel, onConsult: () -> Unit) {
    val room = model.room
    val palette = temperaturePalette(room.temperature)
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(painterResource(R.drawable.ic_pin), contentDescription = null, tint = Orange)
                Box(Modifier.weight(1f)) { Selector("今日の行き先", room.name, model.rooms.map { it.name }, model::selectRoom) }
            }
        }
        item {
            Surface(shape = RoundedCornerShape(28.dp), color = palette.background) {
                Column(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(palette.background, if (room.temperature < 22.0) Color(0xFFF0F8FD) else Color(0xFFFFF2E8)))).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Badge("TODAY’S OUTFIT · 今日のおすすめ", palette)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(Modifier.weight(1f).align(Alignment.CenterVertically), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(room.recommendationTitle, fontSize = 23.sp, lineHeight = 32.sp, fontWeight = FontWeight.ExtraBold, color = Ink)
                            Text("${room.name}に行くあなたへ", style = MaterialTheme.typography.labelMedium, color = palette.foreground)
                        }
                        Image(painterResource(room.photo), contentDescription = "おすすめコーデの生成サンプル写真",
                            modifier = Modifier.width(108.dp).height(156.dp).clip(RoundedCornerShape(18.dp)), contentScale = ContentScale.Crop)
                    }
                    Text(room.recommendationDescription, style = MaterialTheme.typography.bodyMedium, lineHeight = 22.sp, color = Ink)
                    Text(room.layers.joinToString(" ＋ "), style = MaterialTheme.typography.labelLarge, color = palette.foreground, fontWeight = FontWeight.SemiBold)
                    Button(onClick = onConsult, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = palette.foreground),
                        contentPadding = PaddingValues(14.dp)) {
                        Icon(painterResource(R.drawable.ic_chat), contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp)); Text("写真診断・AI提案を使う", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TemperatureTile("${room.name}の室温", room.temperature, "湿度 ${room.humidity}%", Modifier.weight(1f))
                TemperatureTile("金沢の気温", model.outdoorTemperature, "屋外の気温", Modifier.weight(1f))
            }
        }
        item {
            UiCard(color = GreenSoft, border = Color(0xFFA6C8B2)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("今の室内の快適さ", fontWeight = FontWeight.SemiBold, color = Green)
                    Text(room.discomfort, style = MaterialTheme.typography.labelLarge, color = Green)
                }
                Text("温度・提案は表示確認用のサンプルです。体感に合わせて調節しましょう。", style = MaterialTheme.typography.bodySmall, color = Green)
            }
        }
    }
}
@Composable
private fun TemperatureTile(title: String, temperature: Double, detail: String, modifier: Modifier) {
    val palette = temperaturePalette(temperature)
    UiCard(modifier, color = palette.background, border = palette.border) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = palette.foreground, fontWeight = FontWeight.Bold)
        Text("${formatTemp(temperature)}℃", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold, color = palette.foreground)
        Text(detail, style = MaterialTheme.typography.labelSmall, color = palette.foreground)
    }
}
@Composable
internal fun OutfitsScreen(model: ClimaFitViewModel) {
    var mode by rememberSaveable { mutableStateOf(TemperatureMode.ALL) }
    var custom by rememberSaveable { mutableStateOf("18") }
    val temperature = when (mode) {
        TemperatureMode.ALL -> null
        TemperatureMode.OUTDOOR -> model.outdoorTemperature
        TemperatureMode.CURRENT -> model.room.temperature
        TemperatureMode.CUSTOM -> custom.toDoubleOrNull()?.takeIf { it.isFinite() && it in -50.0..60.0 }
    }
    val visible = model.logs.filter { log -> mode == TemperatureMode.ALL || (temperature != null &&
        abs((if (mode == TemperatureMode.CURRENT) log.indoorTemperature else log.outdoorTemperature) - temperature) <= 2.0) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("気温別コーデ", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                Text("あの日の一着を、今日のヒントに。", color = Muted)
            }
        }
        item {
            UiCard {
                Text("気温から、コーデを探す", fontWeight = FontWeight.Bold)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TemperatureMode.entries.forEach { option -> FilterChip(selected = mode == option, onClick = { mode = option }, label = { Text(option.label) }) }
                }
                if (mode == TemperatureMode.CURRENT) {
                    Selector(
                        label = "室温を確認する場所",
                        selected = model.roomName,
                        options = model.rooms.map { it.name },
                        onSelect = model::selectRoom,
                    )
                }
                if (mode == TemperatureMode.CUSTOM) {
                    OutlinedTextField(custom, { custom = it }, label = { Text("金沢の気温（℃）") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true,
                        isError = temperature == null, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp))
                }
                Text(when {
                    mode == TemperatureMode.ALL -> "写真は生成したサンプルです · ${visible.size}コーデ"
                    temperature == null -> "−50〜60℃の温度を入力してください。"
                    mode == TemperatureMode.CURRENT -> "${model.roomName}の室温 ${formatTemp(temperature)}℃ ±2℃"
                    else -> "金沢の気温 ${formatTemp(temperature)}℃ ±2℃"
                }, style = MaterialTheme.typography.bodySmall, color = Muted)
            }
        }
        if (visible.isEmpty()) item { UiCard { Text("条件に合うサンプルコーデがありません。温度条件を変えてみてください。", color = Muted) } }
        items(visible, key = { it.id }) { outfit ->
            Surface(color = Color.White, shape = RoundedCornerShape(24.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Outline)) {
                Column {
                    Box {
                        Image(painterResource(outfit.photo), contentDescription = "${outfit.title}の生成サンプル写真",
                            modifier = Modifier.fillMaxWidth().height(320.dp).background(temperaturePalette(outfit.outdoorTemperature).background), contentScale = ContentScale.Fit)
                        Box(Modifier.padding(14.dp).align(Alignment.TopStart)) { Badge("金沢 ${formatTemp(outfit.outdoorTemperature)}℃", temperaturePalette(outfit.outdoorTemperature)) }
                    }
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(outfit.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("${outfit.date} · ${outfit.location}", style = MaterialTheme.typography.bodySmall, color = Muted)
                        Badge("室温 ${formatTemp(outfit.indoorTemperature)}℃", temperaturePalette(outfit.indoorTemperature))
                        Text(outfit.comment, style = MaterialTheme.typography.labelSmall, color = Muted)
                    }
                }
            }
        }
    }
}
internal fun formatTemp(value: Double): String = String.format(Locale.JAPAN, "%.1f", value)
