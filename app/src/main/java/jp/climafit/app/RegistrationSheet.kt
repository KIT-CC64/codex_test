package jp.climafit.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

/** Registration form preview only. The username stays local and is discarded on dismissal. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RegistrationSheet(onDismiss: () -> Unit, onSubmitPreview: () -> Unit) {
    var name by remember { mutableStateOf(TextFieldValue()) }
    val ready = name.text.isNotBlank()

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().imePadding().verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp).padding(bottom = 24.dp).testTag("registration-sheet"),
            verticalArrangement = Arrangement.spacedBy(14.dp)) {
            CenteredHeading("ユーザー登録", "あなたの名前で、今日のひろばへ。", R.drawable.ic_person)
            OutlinedTextField(name, { name = it }, label = { Text("ユーザー名") }, singleLine = true,
                leadingIcon = { Icon(painterResource(R.drawable.ic_person), contentDescription = null) },
                shape = MaterialTheme.shapes.medium, keyboardOptions = JapaneseKeyboardOptions, modifier = Modifier.fillMaxWidth().testTag("registration-name"))
            Button(onClick = onSubmitPreview, enabled = ready,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("registration-submit")) {
                Text("登録する", fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Icon(painterResource(R.drawable.ic_close), contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp)); Text("閉じる")
            }
        }
    }
}
