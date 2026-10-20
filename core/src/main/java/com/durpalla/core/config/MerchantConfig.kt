package com.durpalla.core.config

import androidx.annotation.DrawableRes
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class Vertical { BUS, HOTEL }

/**
 * Everything that makes a white-label build belong to one merchant.
 * Each product flavor provides one instance (see app/src/<flavor>/.../FlavorSetup.kt).
 */
data class MerchantConfig(
    val vertical: Vertical,
    val merchantId: String,
    val merchantName: String,
    val shortName: String,
    val tagline: String,
    val apiBaseUrl: String,
    val supportPhone: String,
    val supportEmail: String,
    @DrawableRes val logoRes: Int,
    @DrawableRes val splashImageRes: Int,
    val colors: BrandColors,
    val currency: String = "৳",
)

data class BrandColors(
    val primary: Color,
    val primaryDark: Color,
    val primarySoft: Color,
    val accent: Color,
    val accentSoft: Color,
    val onAccent: Color = Color.White,
)

val LocalMerchant = staticCompositionLocalOf<MerchantConfig> { error("MerchantConfig not provided") }
