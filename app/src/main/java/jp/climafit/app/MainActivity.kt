package jp.climafit.app

import android.content.ActivityNotFoundException
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

private enum class Tab(val label: String, val icon: Int) {
    OUTFITS("気温別コーデ", R.drawable.ic_nav_outfits),
    HOME("ホーム", R.drawable.ic_home),
    COMMUNITY("今日のひろば", R.drawable.ic_nav_people)
}
private enum class RoomPicker { HOME, COMMUNITY }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { LayeRoomTheme { LayeRoomApp() } }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LayeRoomApp(model: LayeRoomViewModel = viewModel()) {
    var tab by rememberSaveable { mutableStateOf(Tab.HOME) }
    var aiMode by rememberSaveable { mutableStateOf<AiMode?>(null) }
    var roomPicker by rememberSaveable { mutableStateOf<RoomPicker?>(null) }
    var communityLocation by rememberSaveable { mutableStateOf<String?>(null) }
    var showRegistration by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val screens = rememberSaveableStateHolder()
    val notice = model.notice
    LaunchedEffect(notice) { notice?.let { snackbar.showSnackbar(it); model.dismissNotice() } }

    fun backHome() {
        if (aiMode == AiMode.PHOTO) model.clearPhoto()
        aiMode = null
        tab = Tab.HOME
    }
    fun acceptPhoto(uri: Uri) {
        model.loadPhoto(uri)
        aiMode = AiMode.PHOTO
    }
    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) acceptPhoto(uri)
    }
    fun openPhotoPicker() {
        try { photoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
        catch (_: ActivityNotFoundException) { model.message("写真選択を開けませんでした。端末の写真アプリをご確認ください。") }
    }
    BackHandler(enabled = aiMode != null) { backHome() }
    Scaffold(
        containerColor = Paper,
        topBar = { TopAppBar(colors = TopAppBarDefaults.topAppBarColors(containerColor = Paper), title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Image(painterResource(R.drawable.ic_layeroom_mark), contentDescription = null, modifier = Modifier.size(38.dp))
                Column(Modifier.weight(1f)) {
                    Text(androidx.compose.ui.res.stringResource(R.string.app_name), fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.7).sp)
                    Text("レイルーム · 今日の一着を、心地よく。", style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Muted)
                }
            }
        }, actions = {
            FilledTonalIconButton(onClick = { showRegistration = true }, modifier = Modifier.padding(end = 12.dp).testTag("open-registration"),
                colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = OrangeSoft, contentColor = Orange)) {
                Icon(painterResource(R.drawable.ic_settings), contentDescription = "設定・ユーザー登録", tint = Orange)
            }
        }) },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                Tab.entries.forEach { destination ->
                    NavigationBarItem(selected = tab == destination, onClick = {
                        if (aiMode == AiMode.PHOTO) model.clearPhoto()
                        aiMode = null; tab = destination
                        roomPicker = if (destination == Tab.COMMUNITY) RoomPicker.COMMUNITY else null
                    }, modifier = Modifier.testTag("tab-${destination.name}"),
                        icon = { Icon(painterResource(destination.icon), contentDescription = null,
                            tint = if (tab == destination) Orange else Muted, modifier = Modifier.size(26.dp)) },
                        label = { Text(destination.label, fontSize = 12.sp, maxLines = 1,
                            fontWeight = if (tab == destination) FontWeight.Bold else FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = Orange, selectedTextColor = Orange,
                            indicatorColor = OrangeSoft, unselectedIconColor = Muted, unselectedTextColor = Muted))
                }
            }
        }, snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding).imePadding()) {
            val selectedAiMode = aiMode
            if (selectedAiMode != null) {
                AiStatusScreen(model, selectedAiMode, onBack = ::backHome, onRetryPhoto = ::openPhotoPicker)
            } else {
                screens.SaveableStateProvider(tab.name) {
                    when (tab) {
                        Tab.HOME -> HomeScreen(model,
                            onPhoto = ::openPhotoPicker,
                            onChooseRoom = { roomPicker = RoomPicker.HOME },
                            onSuggest = { model.requestSuggestionPreview(); aiMode = AiMode.SUGGEST })
                        Tab.OUTFITS -> OutfitsScreen(model)
                        Tab.COMMUNITY -> CommunityScreen(model, communityLocation) { roomPicker = RoomPicker.COMMUNITY }
                    }
                }
            }
        }
    }
    if (showRegistration) {
        RegistrationSheet(onDismiss = { showRegistration = false }, onSubmitPreview = {
            showRegistration = false
        })
    }
    val pickerTarget = roomPicker
    if (pickerTarget != null) {
        LocationPickerSheet(
            title = if (pickerTarget == RoomPicker.HOME) "おすすめの場所を選ぶ" else "今日のひろばの場所を選ぶ",
            rooms = model.rooms,
            selected = if (pickerTarget == RoomPicker.HOME) model.roomName else communityLocation,
            onDismiss = { roomPicker = null },
            onSelect = { location ->
                if (pickerTarget == RoomPicker.HOME) model.selectRoom(location) else communityLocation = location
                roomPicker = null
            },
        )
    }
}
