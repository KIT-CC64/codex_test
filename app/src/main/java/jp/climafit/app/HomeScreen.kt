package jp.climafit.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun HomeScreen(model: LayeRoomViewModel, onPhoto: () -> Unit, onChooseRoom: () -> Unit, onSuggest: () -> Unit) {
    val room = model.room
    val palette = temperaturePalette(room.temperature)
    val guide = PreviewData.clothingGuides.getValue(room.name)
    LazyColumn(Modifier.fillMaxSize().testTag("home-screen"), contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Surface(shape = RoundedCornerShape(28.dp), color = Color.White, border = BorderStroke(1.dp, palette.border),
                modifier = Modifier.fillMaxWidth().testTag("home-recommendation")) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("今日の服装の目安", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    Text("何を着よう？ まずは、この3つ。", style = MaterialTheme.typography.bodySmall, color = Muted)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ClothingGuideRow("トップス", guide.top, R.drawable.guide_tshirt, OrangeSoft)
                        ClothingGuideRow("ボトムス", guide.bottom, R.drawable.guide_trousers, BlueSoft)
                        ClothingGuideRow("羽織り", guide.layer, R.drawable.guide_cardigan, Color(0xFFF2E9DA))
                    }
                    Text(guide.note, style = MaterialTheme.typography.bodySmall, color = Ink)
                    Badge(room.name, palette)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Badge("金沢 ${formatTemp(model.outdoorTemperature)}℃", temperaturePalette(model.outdoorTemperature))
                        Badge("室温 ${formatTemp(room.temperature)}℃", palette)
                    }
                    Text("湿度 ${room.humidity}% · ${room.discomfort}", style = MaterialTheme.typography.labelSmall, color = Muted)
                    Text("服装の目安は表示サンプルです", style = MaterialTheme.typography.labelSmall, color = Muted)
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(onClick = onPhoto, modifier = Modifier.weight(1f).fillMaxHeight().testTag("home-photo"),
                    color = OrangeSoft, shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, OrangeLine)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(painterResource(R.drawable.ic_camera), contentDescription = null, tint = Orange, modifier = Modifier.size(32.dp))
                        Text("写真で\nコーデ診断", fontSize = 19.sp, lineHeight = 26.sp, fontWeight = FontWeight.Bold, color = Orange)
                        Text("写真を選んですぐ診断へ", style = MaterialTheme.typography.labelSmall, color = Muted)
                        Text("写真を選ぶ →", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Orange)
                    }
                }
                Surface(onClick = onSuggest, modifier = Modifier.weight(1f).fillMaxHeight().testTag("home-suggestion"),
                    color = BlueSoft, shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, BlueLine)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(painterResource(R.drawable.ic_nav_sun), contentDescription = null, tint = Blue, modifier = Modifier.size(32.dp))
                        TextButton(onClick = onChooseRoom, modifier = Modifier.testTag("home-room-picker").semantics { contentDescription = "おすすめの場所を選ぶ" },
                            contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                            colors = ButtonDefaults.textButtonColors(contentColor = Blue)) {
                            Text(room.name, fontSize = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                                textDecoration = TextDecoration.Underline, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f, fill = false))
                            Text(" ⌄", fontSize = 18.sp)
                        }
                        Text("のおすすめ", fontSize = 19.sp, lineHeight = 26.sp, fontWeight = FontWeight.Bold, color = Blue)
                        Text("場所名で変更できます", style = MaterialTheme.typography.labelSmall, color = Muted)
                        Text("提案を見る →", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Blue)
                    }
                }
            }
        }
    }
}

@Composable
private fun ClothingGuideRow(label: String, clothing: String, image: Int, background: Color) {
    Surface(color = background.copy(alpha = 0.45f), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            // The adjacent label conveys the same information, so the illustration is decorative.
            Image(painterResource(image), contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.size(52.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = Muted)
                Text(clothing, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}
