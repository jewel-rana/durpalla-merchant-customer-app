package com.durpalla.merchant

import androidx.compose.ui.graphics.Color
import com.durpalla.core.config.BrandColors
import com.durpalla.core.config.MerchantConfig
import com.durpalla.core.config.Vertical
import com.durpalla.core.data.FakeAccountRepository
import com.durpalla.merchant.bus.BusData
import com.durpalla.merchant.bus.BusFeature

/** Green Line Paribahan (bus merchant). Change these values to re-brand for another operator. */
object Flavor : FlavorSetup {
    override val config = MerchantConfig(
        vertical = Vertical.BUS,
        merchantId = BuildConfig.MERCHANT_ID,
        merchantName = "Green Line Paribahan",
        shortName = "Green Line",
        tagline = "Your journey, our priority",
        apiBaseUrl = BuildConfig.DURPALLA_API_BASE_URL,
        supportPhone = "16xxx",
        supportEmail = "support@greenline.example",
        logoRes = R.drawable.merchant_logo,
        splashImageRes = R.drawable.img_bus,
        colors = BrandColors(
            primary = Color(0xFF0B3D91), primaryDark = Color(0xFF072A66), primarySoft = Color(0xFFE6EDF8),
            accent = Color(0xFFF97316), accentSoft = Color(0xFFFFF1E6),
        ),
    )
    override val feature = BusFeature
    override fun accountRepository() = FakeAccountRepository(BusData.seedBookings, BusData.offers)
}
