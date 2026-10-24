package jp.climafit.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LocationPickerSheet(title: String, rooms: List<RoomUi>, selected: String?, onDismiss: () -> Unit, onSelect: (String) -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        LazyColumn(Modifier.fillMaxWidth().testTag("location-sheet"), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)) {
            item {
                CenteredHeading(title, "登録されている場所から1つ選んでください。", R.drawable.ic_pin)
                Spacer(Modifier.height(12.dp))
            }
            items(rooms, key = { it.name }) { room ->
                ListItem(
                    leadingContent = { Icon(painterResource(R.drawable.ic_pin), contentDescription = null, tint = if (selected == room.name) Orange else Muted) },
                    headlineContent = { Text(room.name, fontWeight = FontWeight.SemiBold) },
                    supportingContent = { Text("室温 ${formatTemp(room.temperature)}℃ · 湿度 ${room.humidity}%") },
                    trailingContent = { RadioButton(selected = selected == room.name, onClick = null) },
                    modifier = Modifier.fillMaxWidth().testTag("location-${room.name}")
                        .selectable(selected = selected == room.name, role = Role.RadioButton, onClick = { onSelect(room.name) }),
                    colors = ListItemDefaults.colors(containerColor = if (selected == room.name) OrangeSoft else MaterialTheme.colorScheme.surfaceContainerLow),
                )
            }
            item { Spacer(Modifier.height(20.dp)) }
        }
    }
}
