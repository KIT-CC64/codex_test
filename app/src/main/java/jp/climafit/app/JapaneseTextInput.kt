package jp.climafit.app

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp

internal val JapaneseKeyboardOptions = KeyboardOptions(
    keyboardType = KeyboardType.Text,
    imeAction = ImeAction.Default,
    hintLocales = LocaleList("ja"),
)

/** Preserve selection and the IME composition range while Japanese text is being converted. */
@Composable
internal fun JapaneseMessageField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = { Text(placeholder, style = MaterialTheme.typography.bodySmall) },
        keyboardOptions = JapaneseKeyboardOptions,
        shape = RoundedCornerShape(20.dp),
        maxLines = 4,
        isError = value.text.length > 1000,
        supportingText = if (value.text.length > 1000) ({ Text("1000文字以内で入力してください。") }) else null,
        colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Paper, unfocusedBorderColor = Outline),
    )
}
