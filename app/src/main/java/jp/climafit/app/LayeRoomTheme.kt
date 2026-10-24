package jp.climafit.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.annotation.DrawableRes
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

internal val Paper = Color(0xFFFAF7F2)
internal val Ink = Color(0xFF332C29)
internal val Muted = Color(0xFF71665F)
internal val Orange = Color(0xFFAD4827)
internal val OrangeSoft = Color(0xFFFFE1CC)
internal val OrangeLine = Color(0xFFE8AD87)
internal val Blue = Color(0xFF225C7E)
internal val BlueSoft = Color(0xFFDDEFF9)
internal val BlueLine = Color(0xFFA7CEE4)
internal val Green = Color(0xFF32634D)
internal val GreenSoft = Color(0xFFDDEDE3)
internal val Outline = Color(0xFFDDD3C9)
internal data class ClimatePalette(val background: Color, val foreground: Color, val border: Color)
// Presentation colors only; this does not calculate comfort or clothing recommendations.
internal fun temperaturePalette(temperature: Double) = if (temperature < 22.0)
    ClimatePalette(BlueSoft, Blue, BlueLine) else ClimatePalette(OrangeSoft, Orange, OrangeLine)
internal fun feelPalette(feel: String) = when {
    "寒" in feel -> ClimatePalette(BlueSoft, Blue, BlueLine)
    "暑" in feel -> ClimatePalette(OrangeSoft, Orange, OrangeLine)
    else -> ClimatePalette(GreenSoft, Green, Color(0xFFA6C8B2))
}
@Composable
internal fun LayeRoomTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = lightColorScheme(
        primary = Orange, onPrimary = Color.White, primaryContainer = OrangeSoft,
        onPrimaryContainer = Ink, secondary = Blue, secondaryContainer = BlueSoft,
        onSecondaryContainer = Blue, background = Paper, surface = Paper,
        onSurface = Ink, onSurfaceVariant = Muted, outline = Outline,
        surfaceVariant = Color(0xFFF0E9E1), tertiary = Green,
        surfaceContainerLow = Color(0xFFF5EFE7), surfaceContainer = Color(0xFFF0E9E1),
        surfaceContainerHigh = Color(0xFFECE4DB),
    ), shapes = Shapes(
        extraSmall = RoundedCornerShape(8.dp), small = RoundedCornerShape(12.dp),
        medium = RoundedCornerShape(16.dp), large = RoundedCornerShape(24.dp), extraLarge = RoundedCornerShape(28.dp),
    ), typography = Typography(
        headlineMedium = TextStyle(fontSize = 28.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold),
        headlineSmall = TextStyle(fontSize = 24.sp, lineHeight = 32.sp, fontWeight = FontWeight.Bold),
        titleLarge = TextStyle(fontSize = 22.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold),
        titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold),
        titleSmall = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
        bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 26.sp),
        bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 22.sp),
        bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 19.sp),
        labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
        labelMedium = TextStyle(fontSize = 12.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium),
        labelSmall = TextStyle(fontSize = 11.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),
    ), content = content)
}
@Composable
internal fun UiCard(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    border: Color = Outline,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = color,
        border = BorderStroke(1.dp, border)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
    }
}
@Composable
internal fun Badge(text: String, palette: ClimatePalette = ClimatePalette(OrangeSoft, Orange, OrangeLine)) {
    Surface(color = palette.background, shape = RoundedCornerShape(50), border = BorderStroke(1.dp, palette.border)) {
        Text(text, Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = palette.foreground)
    }
}
@Composable
internal fun Selector(label: String, selected: String, options: List<String>, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (label.isNotBlank()) Text(label, style = MaterialTheme.typography.labelMedium, color = Muted)
        Box {
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, Outline),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Ink), contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)) {
                Text(selected, Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                Icon(painterResource(R.drawable.ic_expand_more), contentDescription = null, modifier = Modifier.size(24.dp))
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { expanded = false; onSelect(option) }) }
            }
        }
    }
}


@Composable
internal fun SymbolBadge(@DrawableRes symbol: Int, color: Color = Orange, background: Color = OrangeSoft) {
    Surface(shape = RoundedCornerShape(16.dp), color = background) {
        Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
            Icon(painterResource(symbol), contentDescription = null, tint = color, modifier = Modifier.size(26.dp))
        }
    }
}

@Composable
internal fun ActionLabel(label: String, color: Color) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)) {
        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = color)
        Icon(painterResource(R.drawable.ic_arrow_forward), contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
    }
}


@Composable
internal fun CenteredHeading(title: String, subtitle: String, @DrawableRes symbol: Int,
    color: Color = Orange, background: Color = OrangeSoft) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            SymbolBadge(symbol, color, background)
            Text(title, Modifier.weight(1f).padding(horizontal = 4.dp),
                style = MaterialTheme.typography.titleLarge, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Spacer(Modifier.width(48.dp))
        }
        if (subtitle.isNotBlank()) Text(subtitle, Modifier.fillMaxWidth(), color = Muted,
            style = MaterialTheme.typography.bodySmall, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}
