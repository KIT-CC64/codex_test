package jp.climafit.app

import android.content.ActivityNotFoundException
import android.graphics.Bitmap
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
    var showPhotoPicker by rememberSaveable { mutableStateOf(false) }
    var roomPicker by rememberSaveable { mutableStateOf<RoomPicker?>(null) }
    var communityLocation by rememberSaveable { mutableStateOf<String?>(null) }
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
        showPhotoPicker = false
        model.loadPhoto(uri)
        aiMode = AiMode.PHOTO
    }
    fun acceptCameraPhoto(bitmap: Bitmap) {
        showPhotoPicker = false
        model.useCameraPreview(bitmap)
        model.requestPhotoPreview()
        aiMode = AiMode.PHOTO
    }
    val photoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) acceptPhoto(uri)
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) acceptCameraPhoto(bitmap)
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
            Badge("UIプレビュー")
            IconButton(onClick = { model.message("表示確認用のサンプルデータです。") }) {
                Icon(painterResource(R.drawable.ic_refresh), contentDescription = "表示データについて", tint = Orange)
            }
        }) },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                Tab.entries.forEach { destination ->
                    NavigationBarItem(selected = tab == destination, onClick = {
                        if (aiMode == AiMode.PHOTO) model.clearPhoto()
                        aiMode = null; showPhotoPicker = false; tab = destination
                        roomPicker = if (destination == Tab.COMMUNITY) RoomPicker.COMMUNITY else null
                    }, modifier = Modifier.testTag("tab-${destination.name}"),
                        icon = { Icon(painterResource(destination.icon), contentDescription = null,
                            tint = if (destination == Tab.HOME) Orange else Color.Unspecified, modifier = Modifier.size(30.dp)) },
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
                AiStatusScreen(model, selectedAiMode, onBack = ::backHome, onRetryPhoto = { showPhotoPicker = true })
            } else {
                screens.SaveableStateProvider(tab.name) {
                    when (tab) {
                        Tab.HOME -> HomeScreen(model,
                            onPhoto = { showPhotoPicker = true },
                            onChooseRoom = { roomPicker = RoomPicker.HOME },
                            onSuggest = { model.requestSuggestionPreview(); aiMode = AiMode.SUGGEST })
                        Tab.OUTFITS -> OutfitsScreen(model)
                        Tab.COMMUNITY -> CommunityScreen(model, communityLocation) { roomPicker = RoomPicker.COMMUNITY }
                    }
                }
            }
        }
    }
    if (showPhotoPicker) {
        ModalBottomSheet(onDismissRequest = { showPhotoPicker = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("服の写真を選ぶ", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("写真を1枚選ぶと、そのまま診断画面へ進みます。", color = Muted)
                FilledTonalButton(onClick = {
                    showPhotoPicker = false
                    try { photoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
                    catch (_: ActivityNotFoundException) { model.message("写真選択を開けませんでした。カメラでの撮影をお試しください。") }
                }, modifier = Modifier.fillMaxWidth().testTag("launch-photo-picker")) {
                    Icon(painterResource(R.drawable.ic_image), contentDescription = null)
                    Spacer(Modifier.width(8.dp)); Text("写真を選ぶ")
                }
                OutlinedButton(onClick = {
                    showPhotoPicker = false
                    try { cameraLauncher.launch(null) }
                    catch (_: ActivityNotFoundException) { model.message("カメラを起動できませんでした。写真から選択してください。") }
                }, modifier = Modifier.fillMaxWidth()) {
                    Icon(painterResource(R.drawable.ic_camera), contentDescription = null)
                    Spacer(Modifier.width(8.dp)); Text("カメラで撮る")
                }
                Text("プレビューでは写真を送信・保存しません。", style = MaterialTheme.typography.bodySmall, color = Muted)
                Spacer(Modifier.height(12.dp))
            }
        }
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
