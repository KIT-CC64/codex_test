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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun HomeScreen(model: LayeRoomViewModel, onPhoto: () -> Unit, onChooseRoom: () -> Unit, onSuggest: () -> Unit) {
    val room = model.room
    val palette = temperaturePalette(model.outdoorTemperature)
    val guide = PreviewData.kanazawaClothingGuide
    LazyColumn(Modifier.fillMaxSize().testTag("home-screen"), contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Surface(shape = RoundedCornerShape(28.dp), color = Color.White, border = BorderStroke(1.dp, palette.border),
                modifier = Modifier.fillMaxWidth().testTag("home-recommendation")) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    CenteredHeading("今日の服装の目安", "金沢の天気に合わせた、今日の3つ。", R.drawable.ic_sun)
                    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ClothingGuideTile("トップス", guide.top, R.drawable.guide_tshirt, OrangeSoft)
                        ClothingGuideTile("ボトムス", guide.bottom, R.drawable.guide_trousers, BlueSoft)
                        ClothingGuideTile("羽織り", guide.layer, R.drawable.guide_cardigan, Color(0xFFF2E9DA))
                    }
                    Text(guide.note, Modifier.fillMaxWidth(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Ink, textAlign = TextAlign.Center)
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(onClick = onPhoto, modifier = Modifier.weight(1f).fillMaxHeight().testTag("home-photo"),
                    color = OrangeSoft, shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, OrangeLine)) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SymbolBadge(R.drawable.ic_camera, Orange, Color.White.copy(alpha = 0.75f))
                        Text("写真で\nコーデ診断", Modifier.fillMaxWidth().heightIn(min = 56.dp),
                            fontSize = 19.sp, lineHeight = 26.sp, fontWeight = FontWeight.Bold, color = Orange, textAlign = TextAlign.Center)
                        Surface(color = Color.White.copy(alpha = 0.7f), shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().heightIn(min = 68.dp)) {
                            Box(Modifier.padding(8.dp), contentAlignment = Alignment.Center) {
                                Text("写真を1枚選んで\nすぐに診断", style = MaterialTheme.typography.labelMedium,
                                    color = Orange, textAlign = TextAlign.Center)
                            }
                        }
                        ActionLabel("写真を選ぶ", Orange)
                    }
                }
                Surface(onClick = onSuggest, modifier = Modifier.weight(1f).fillMaxHeight().testTag("home-suggestion"),
                    color = BlueSoft, shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, BlueLine)) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SymbolBadge(R.drawable.ic_nav_sun, Blue, Color.White.copy(alpha = 0.75f))
                        Text("${room.name}の\nおすすめ", Modifier.fillMaxWidth().heightIn(min = 56.dp),
                            fontSize = 19.sp, lineHeight = 26.sp, fontWeight = FontWeight.Bold, color = Blue, textAlign = TextAlign.Center)
                        OutlinedButton(onClick = onChooseRoom,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 68.dp).testTag("home-room-picker")
                                .semantics { contentDescription = "おすすめの場所を選ぶ" },
                            shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, Blue),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Blue, containerColor = Color.White.copy(alpha = 0.7f))) {
                            Icon(painterResource(R.drawable.ic_pin), contentDescription = null, modifier = Modifier.size(18.dp))
                            Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("場所を選ぶ", style = MaterialTheme.typography.labelSmall)
                                Text(room.name, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
                            }
                            Icon(painterResource(R.drawable.ic_expand_more), contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                        ActionLabel("提案を見る", Blue)
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.ClothingGuideTile(label: String, clothing: String, image: Int, background: Color) {
    Surface(color = background.copy(alpha = 0.45f), shape = RoundedCornerShape(16.dp),
        modifier = Modifier.weight(1f).fillMaxHeight()) {
        Column(Modifier.padding(horizontal = 8.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Image(painterResource(image), contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.size(52.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = Muted)
            Text(clothing, Modifier.fillMaxWidth(), style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}
