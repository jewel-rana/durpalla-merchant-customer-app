package com.durpalla.core.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.durpalla.core.theme.Brand
import com.durpalla.core.theme.N

/**
 * Screen shell. Deliberately NOT a Scaffold: the single app-level Scaffold in MainActivity owns
 * all window insets (status bar, nav bar, bottom navigation) so nothing is padded twice.
 */
@Composable
fun Screen(
    title: String? = null, onBack: (() -> Unit)? = null, actions: @Composable RowScope.() -> Unit = {},
    bottomBar: (@Composable () -> Unit)? = null, scroll: Boolean = true, content: @Composable ColumnScope.() -> Unit,
) {
    Column(Modifier.fillMaxSize().background(N.Bg)) {
        if (title != null || onBack != null) TopBar(title ?: "", onBack, actions)
        val m = Modifier.weight(1f).fillMaxWidth().let { if (scroll) it.verticalScroll(rememberScrollState()) else it }
        Column(m.padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
        bottomBar?.let { Surface(color = Color.White, shadowElevation = 10.dp, shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)) { Box(Modifier.fillMaxWidth().padding(16.dp)) { it() } } }
    }
}

@Composable
fun TopBar(title: String, onBack: (() -> Unit)?, actions: @Composable RowScope.() -> Unit = {}) {
    Row(Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        if (onBack != null) IconButton(onClick = onBack, modifier = Modifier.clip(CircleShape).background(Color.White)) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f).padding(start = 8.dp), maxLines = 1)
        actions()
    }
}

@Composable
fun MCard(modifier: Modifier = Modifier, color: Color = Color.White, border: Color? = N.Line, pad: Dp = 16.dp, onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(16.dp); val b = border?.let { BorderStroke(1.dp, it) }; val c = CardDefaults.cardColors(containerColor = color)
    if (onClick != null) Card(onClick, modifier.fillMaxWidth(), shape = shape, border = b, colors = c) { Column(Modifier.padding(pad), content = content) }
    else Card(modifier.fillMaxWidth(), shape = shape, border = b, colors = c) { Column(Modifier.padding(pad), content = content) }
}

@Composable
fun AccentButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) =
    Button(onClick, modifier.fillMaxWidth().height(52.dp), enabled = enabled, shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Brand.colors.accent, contentColor = Brand.colors.onAccent, disabledContainerColor = N.Booked)) { Text(text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp) }

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) =
    Button(onClick, modifier.fillMaxWidth().height(52.dp), enabled = enabled, shape = RoundedCornerShape(14.dp)) { Text(text, fontWeight = FontWeight.SemiBold) }

@Composable
fun OutlineButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, color: Color = Brand.colors.primary) =
    OutlinedButton(onClick, modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, color),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = color)) { Text(text, fontWeight = FontWeight.SemiBold) }

/** Price + label on the left, CTA on the right: the sticky booking bar. */
@Composable
fun TotalBar(caption: String, amount: String, cta: String, enabled: Boolean = true, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) { Text(caption, color = N.Muted, fontSize = 12.sp); Text(amount, style = MaterialTheme.typography.titleLarge) }
        AccentButton(cta, onClick, Modifier.width(160.dp), enabled)
    }
}

@Composable
fun Photo(@DrawableRes res: Int, modifier: Modifier = Modifier, corner: Dp = 14.dp) =
    AsyncImage(model = res, contentDescription = null, contentScale = ContentScale.Crop, modifier = modifier.clip(RoundedCornerShape(corner)).background(N.Line),
        placeholder = painterResource(res))

enum class Tone { PRIMARY, ACCENT, SUCCESS, DANGER, NEUTRAL }

@Composable
fun Pill(text: String, tone: Tone = Tone.PRIMARY) {
    val (bg, fg) = when (tone) {
        Tone.PRIMARY -> Brand.colors.primarySoft to Brand.colors.primaryDark; Tone.ACCENT -> Brand.colors.accentSoft to Brand.colors.accent
        Tone.SUCCESS -> N.SuccessSoft to N.Success; Tone.DANGER -> N.DangerSoft to N.Danger; Tone.NEUTRAL -> N.Bg to N.Muted
    }
    Text(text, color = fg, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clip(RoundedCornerShape(50)).background(bg).padding(horizontal = 10.dp, vertical = 4.dp))
}

@Composable
fun ChoiceChips(options: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.horizontalScrollable(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEachIndexed { i, s ->
            val sel = i == selected
            Text(s, color = if (sel) Color.White else N.Ink, fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                modifier = Modifier.clip(RoundedCornerShape(50)).background(if (sel) Brand.colors.primary else Color.White)
                    .border(1.dp, if (sel) Brand.colors.primary else N.Line, RoundedCornerShape(50)).clickable { onSelect(i) }.padding(horizontal = 14.dp, vertical = 8.dp))
        }
    }
}

@Composable
private fun Modifier.horizontalScrollable() = this.then(Modifier.horizontalScroll(rememberScrollState()))

@Composable
fun KV(key: String, value: String, valueColor: Color = N.Ink, bold: Boolean = false) =
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(key, color = N.Muted, fontSize = 14.sp, modifier = Modifier.weight(1f, fill = false))
        Text(value, color = valueColor, fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium, fontSize = 14.sp, textAlign = TextAlign.End)
    }

@Composable
fun SectionTitle(title: String, action: String? = null, onAction: () -> Unit = {}) =
    Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        if (action != null) Text(action, color = Brand.colors.primary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, modifier = Modifier.clickable(onClick = onAction))
    }

@Composable
fun MenuRow(icon: ImageVector, title: String, subtitle: String? = null, onClick: () -> Unit) =
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(Brand.colors.primarySoft), contentAlignment = Alignment.Center) { Icon(icon, null, tint = Brand.colors.primary) }
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall); if (subtitle != null) Text(subtitle, color = N.Muted, fontSize = 12.sp)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = N.Muted)
    }

@Composable
fun IconTile(icon: ImageVector, label: String, modifier: Modifier = Modifier) =
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(52.dp).clip(RoundedCornerShape(14.dp)).background(Color.White).border(1.dp, N.Line, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) { Icon(icon, null, tint = Brand.colors.primary) }
        Text(label, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
    }

/** Deterministic QR-like matrix (visual stand-in; render a real QR from the API's ticket token in production). */
@Composable
fun QrCode(data: String, modifier: Modifier = Modifier.size(170.dp)) {
    val n = 25
    val bits = BooleanArray(n * n) { i -> ((data.hashCode() * 31 + i * 7919) xor (i * i * 131)).let { (it shr 3) and 1 == 1 } }
    Canvas(modifier.background(Color.White).padding(8.dp)) {
        val c = size.width / n
        fun finder(x: Int, y: Int) {
            drawRect(Color.Black, Offset(x * c, y * c), Size(7 * c, 7 * c)); drawRect(Color.White, Offset((x + 1) * c, (y + 1) * c), Size(5 * c, 5 * c))
            drawRect(Color.Black, Offset((x + 2) * c, (y + 2) * c), Size(3 * c, 3 * c))
        }
        for (y in 0 until n) for (x in 0 until n) {
            val inFinder = (x < 8 && y < 8) || (x >= n - 8 && y < 8) || (x < 8 && y >= n - 8)
            if (!inFinder && bits[y * n + x]) drawRect(Color.Black, Offset(x * c, y * c), Size(c, c))
        }
        finder(0, 0); finder(n - 7, 0); finder(0, n - 7)
    }
}

@Composable
fun Avatar(name: String, size: Dp = 44.dp) =
    Box(Modifier.size(size).clip(CircleShape).background(Brand.colors.primarySoft), contentAlignment = Alignment.Center) {
        Text(name.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString(""), color = Brand.colors.primaryDark, fontWeight = FontWeight.Bold)
    }

@Composable
fun EmptyState(text: String) = Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) { Text(text, color = N.Muted) }

@Composable
fun SuccessBadge() = Box(Modifier.size(72.dp).clip(CircleShape).background(N.Success), contentAlignment = Alignment.Center) {
    Icon(androidx.compose.material.icons.Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(40.dp))
}
