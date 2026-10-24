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
internal fun OutfitsScreen(model: LayeRoomViewModel) {
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
