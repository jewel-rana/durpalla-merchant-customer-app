package com.durpalla.core.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.durpalla.core.config.LocalMerchant
import com.durpalla.core.config.MerchantConfig

object N {
    val Ink = Color(0xFF0F172A); val Muted = Color(0xFF64748B); val Line = Color(0xFFE2E8F0); val Bg = Color(0xFFF4F7FB)
    val Success = Color(0xFF16A34A); val SuccessSoft = Color(0xFFE7F6EC); val Danger = Color(0xFFDC2626); val DangerSoft = Color(0xFFFDECEC)
    val Warning = Color(0xFFEAB308); val Ladies = Color(0xFFFBCFE8); val LadiesInk = Color(0xFF9D174D); val Booked = Color(0xFFCBD5E1); val Blocked = Color(0xFF475569)
}

private val type = Typography(
    headlineMedium = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold),
    headlineSmall = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 19.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
    titleSmall = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
    bodyMedium = TextStyle(fontSize = 14.sp), bodySmall = TextStyle(fontSize = 12.sp),
)

@Composable
fun MerchantTheme(config: MerchantConfig, content: @Composable () -> Unit) {
    val c = config.colors
    val scheme = lightColorScheme(
        primary = c.primary, onPrimary = Color.White, primaryContainer = c.primarySoft, onPrimaryContainer = c.primaryDark,
        secondary = c.accent, onSecondary = c.onAccent, secondaryContainer = c.accentSoft,
        background = N.Bg, onBackground = N.Ink, surface = Color.White, onSurface = N.Ink,
        surfaceVariant = N.Bg, onSurfaceVariant = N.Muted, outline = N.Line, outlineVariant = N.Line, error = N.Danger,
        surfaceContainerLow = Color.White, surfaceContainer = Color.White, surfaceContainerHigh = Color.White,
    )
    CompositionLocalProvider(LocalMerchant provides config) {
        MaterialTheme(scheme, shapes = Shapes(small = RoundedCornerShape(10.dp), medium = RoundedCornerShape(16.dp), large = RoundedCornerShape(20.dp)), typography = type, content = content)
    }
}

object Brand {
    val colors @Composable get() = LocalMerchant.current.colors
}
