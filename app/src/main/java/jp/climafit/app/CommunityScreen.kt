package jp.climafit.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CommunityScreen(model: LayeRoomViewModel, selectedLocation: String?, onChooseLocation: () -> Unit) {
    var showForm by rememberSaveable { mutableStateOf(false) }
    var location by rememberSaveable(selectedLocation) { mutableStateOf(selectedLocation ?: model.roomName) }
    var feel by rememberSaveable { mutableStateOf("快適") }
    var comment by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue()) }
    val reviews = model.reviews.filter { it.location == selectedLocation }
    Column(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxWidth().weight(1f).testTag("community-screen"), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("今日のひろば", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                    Text("教室の「ちょっと寒い」を、シェアしよう。", style = MaterialTheme.typography.bodyMedium, color = Muted)
                    OutlinedButton(onClick = onChooseLocation, modifier = Modifier.fillMaxWidth().testTag("community-location")) {
                        Icon(painterResource(R.drawable.ic_pin), contentDescription = null)
                        Spacer(Modifier.width(8.dp)); Text(selectedLocation?.let { "$it · 場所を変更" } ?: "口コミを見る場所を選ぶ")
                    }
                }
            }
            if (selectedLocation == null) {
                item { UiCard { Text("場所を選ぶと、その場所の口コミだけを表示します。", color = Muted) } }
            } else {
                item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${selectedLocation}の口コミ · ${reviews.size}件", fontWeight = FontWeight.Bold)
                } }
                if (reviews.isEmpty()) item { UiCard { Text("この場所の口コミはまだありません。", color = Muted) } }
                items(reviews, key = { it.id }) { review ->
                    UiCard(modifier = Modifier.testTag("review-${review.id}"), border = feelPalette(review.feel).border) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(review.location, fontWeight = FontWeight.Bold)
                            Badge(review.feel, feelPalette(review.feel))
                        }
                        Text(review.comment, style = MaterialTheme.typography.bodyMedium)
                        Text(review.date, style = MaterialTheme.typography.labelSmall, color = Muted)
                    }
                }
            }
        }
        if (selectedLocation != null) {
            Surface(color = Paper, modifier = Modifier.fillMaxWidth()) {
                FilledTonalButton(
                    onClick = { showForm = true },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).fillMaxWidth()
                        .heightIn(min = 60.dp).testTag("community-post"),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = BlueSoft, contentColor = Blue),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                ) {
                    Icon(painterResource(R.drawable.ic_add), contentDescription = null, modifier = Modifier.size(26.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("いまの体感を投稿する", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
    if (showForm) {
        ModalBottomSheet(onDismissRequest = { showForm = false }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
            Column(Modifier.fillMaxWidth().imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("いまの体感をシェア", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("どこで、どんなふうに感じた？", color = Muted)
                Selector("場所", location, model.rooms.map { it.name }) { location = it }
                Selector("体感", feel, listOf("快適", "少し寒い", "かなり寒い", "少し暑い")) { feel = it }
                OutlinedTextField(comment, { comment = it }, label = { Text("ひとこと") },
                    placeholder = { Text("窓際は少しひんやり、など") }, supportingText = { Text("${comment.text.length} / 500文字") },
                    keyboardOptions = JapaneseKeyboardOptions, isError = comment.text.length > 500,
                    modifier = Modifier.fillMaxWidth(), minLines = 3, shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                Button(enabled = comment.text.isNotBlank() && comment.text.length <= 500, onClick = {
                    showForm = false
                }, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(14.dp)) { Text("投稿する", fontWeight = FontWeight.Bold) }
            }
        }
    }
}
