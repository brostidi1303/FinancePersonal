package com.example.financeapp.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ══════════════════════════════════════
//  Bộ màu cho mỗi theme
// ══════════════════════════════════════

@Immutable
data class AppColors(
    val background: Color,
    val cardBackground: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val divider: Color,
    val isDark: Boolean
)

val DarkAppColors = AppColors(
    background     = Color(0xFF0A0E27),
    cardBackground = Color(0xFF1A1F3A),
    textPrimary    = Color.White,
    textSecondary  = Color.White.copy(alpha = 0.6f),
    divider        = Color.White.copy(alpha = 0.08f),
    isDark         = true
)

val LightAppColors = AppColors(
    background     = Color(0xFFF0F2F8),
    cardBackground = Color.White,
    textPrimary    = Color(0xFF0D1117),
    textSecondary  = Color(0xFF6B7280),
    divider        = Color(0xFFE5E7EB),
    isDark         = false
)

// ══════════════════════════════════════
//  CompositionLocal — truyền theme xuống toàn bộ cây Composable
// ══════════════════════════════════════

val LocalAppColors = staticCompositionLocalOf { DarkAppColors }

// Dùng trong bất kỳ Composable nào:  val colors = AppTheme.colors
object AppTheme {
    val colors: AppColors
        @Composable
        get() = LocalAppColors.current
}

@Composable
fun FinanceAppTheme(
    isDark: Boolean,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalAppColors provides if (isDark) DarkAppColors else LightAppColors,
        content = content
    )
}