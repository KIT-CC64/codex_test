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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CommunityScreen(model: LayeRoomViewModel) {
    var showForm by rememberSaveable { mutableStateOf(false) }
    var location by rememberSaveable { mutableStateOf(model.roomName) }
    var feel by rememberSaveable { mutableStateOf("快適") }
    var comment by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue()) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("みんなのひろば", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                Text("教室の「ちょっと寒い」を、シェアしよう。", style = MaterialTheme.typography.bodyMedium, color = Muted)
                FilledTonalButton(onClick = { showForm = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(painterResource(R.drawable.ic_add), contentDescription = null)
                    Spacer(Modifier.width(8.dp)); Text("いまの体感を投稿する", fontWeight = FontWeight.Bold)
                }
            }
        }
        item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("みんなの声", fontWeight = FontWeight.Bold)
            Text("表示サンプル", style = MaterialTheme.typography.labelSmall, color = Muted)
        } }
        items(model.reviews, key = { it.id }) { review ->
            UiCard(border = feelPalette(review.feel).border) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(review.location, fontWeight = FontWeight.Bold)
                    Badge(review.feel, feelPalette(review.feel))
                }
                Text(review.comment, style = MaterialTheme.typography.bodyMedium)
                Text(review.date, style = MaterialTheme.typography.labelSmall, color = Muted)
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
                    showForm = false; model.message("投稿画面のプレビューです。内容は送信・保存されません。")
                }, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(14.dp)) { Text("投稿する", fontWeight = FontWeight.Bold) }
                Text("UIプレビューのため、投稿は送信されません。", style = MaterialTheme.typography.bodySmall, color = Muted)
            }
        }
    }
}
