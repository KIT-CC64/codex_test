package jp.climafit.app

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

/** Registration form preview only. Credentials remain local and are discarded on dismissal. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RegistrationSheet(onDismiss: () -> Unit, onSubmitPreview: () -> Unit) {
    var name by remember { mutableStateOf(TextFieldValue()) }
    var email by remember { mutableStateOf(TextFieldValue()) }
    var password by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    val validEmail = Patterns.EMAIL_ADDRESS.matcher(email.text.trim()).matches()
    val mismatch = confirmation.isNotEmpty() && confirmation != password
    val ready = name.text.isNotBlank() && validEmail && password.isNotBlank() && password == confirmation

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().imePadding().verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp).padding(bottom = 24.dp).testTag("registration-sheet"),
            verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("ユーザー登録", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("あなたの名前で、今日のひろばへ。", color = Muted)
            OutlinedTextField(name, { name = it }, label = { Text("名前（表示名）") }, singleLine = true,
                keyboardOptions = JapaneseKeyboardOptions, modifier = Modifier.fillMaxWidth().testTag("registration-name"))
            OutlinedTextField(email, { email = it }, label = { Text("メールアドレス") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = email.text.isNotEmpty() && !validEmail,
                modifier = Modifier.fillMaxWidth().testTag("registration-email"))
            OutlinedTextField(password, { password = it }, label = { Text("パスワード") }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth().testTag("registration-password"))
            OutlinedTextField(confirmation, { confirmation = it }, label = { Text("パスワード（確認）") }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = mismatch, supportingText = { if (mismatch) Text("パスワードが一致しません") },
                modifier = Modifier.fillMaxWidth().testTag("registration-confirmation"))
            Button(onClick = onSubmitPreview, enabled = ready,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("registration-submit")) {
                Text("登録する", fontWeight = FontWeight.Bold)
            }
            Text("入力画面のプレビューです。内容は送信・保存されず、アカウントは作成されません。",
                style = MaterialTheme.typography.bodySmall, color = Muted)
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("閉じる") }
        }
    }
}
