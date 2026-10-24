package jp.climafit.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

private enum class Tab(val label: String, val icon: Int) {
    DASHBOARD("提案", R.drawable.ic_nav_sun), AI("コーデAI", R.drawable.ic_nav_chat),
    LOGS("気温別コーデ", R.drawable.ic_nav_outfits), REVIEWS("みんなのひろば", R.drawable.ic_nav_people)
}
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { ClimaFitTheme { ClimaFitApp() } }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClimaFitApp(model: ClimaFitViewModel = viewModel()) {
    var tab by rememberSaveable { mutableStateOf(Tab.DASHBOARD) }
    val snackbar = remember { SnackbarHostState() }
    val screens = rememberSaveableStateHolder()
    val notice = model.notice
    LaunchedEffect(notice) { notice?.let { snackbar.showSnackbar(it); model.dismissNotice() } }
    Scaffold(
        containerColor = Paper,
        topBar = { TopAppBar(colors = TopAppBarDefaults.topAppBarColors(containerColor = Paper), title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Image(painterResource(R.drawable.ic_climafit_mark), contentDescription = null, modifier = Modifier.size(38.dp))
                Column(Modifier.weight(1f)) {
                    Text("ClimaFit", fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.7).sp)
                    Text("金沢の毎日に、心地よい一着。", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Muted)
                }
            }
        }, actions = {
            Badge("UIプレビュー")
            IconButton(onClick = { model.message("表示確認用のサンプルデータです。") }) {
                Icon(painterResource(R.drawable.ic_refresh), contentDescription = "表示データについて", tint = Orange)
            }
        }) },
        bottomBar = {
            NavigationBar(containerColor = androidx.compose.ui.graphics.Color.White) {
                Tab.entries.forEach { destination ->
                    NavigationBarItem(selected = tab == destination, onClick = { tab = destination },
                        icon = { Icon(painterResource(destination.icon), contentDescription = null,
                            tint = Color.Unspecified, modifier = Modifier.size(30.dp)) },
                        label = { Text(destination.label, fontSize = 10.sp, maxLines = 1,
                            fontWeight = if (tab == destination) FontWeight.Bold else FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = Orange, selectedTextColor = Orange,
                            indicatorColor = OrangeSoft, unselectedIconColor = Muted, unselectedTextColor = Muted))
                }
            }
        }, snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding).imePadding()) {
            screens.SaveableStateProvider(tab.name) {
                when (tab) {
                    Tab.DASHBOARD -> Dashboard(model) { tab = Tab.AI }
                    Tab.AI -> AiScreen(model)
                    Tab.LOGS -> OutfitsScreen(model)
                    Tab.REVIEWS -> CommunityScreen(model)
                }
            }
        }
    }
}
