package com.durpalla.merchant

import androidx.compose.ui.graphics.Color
import com.durpalla.core.config.BrandColors
import com.durpalla.core.config.MerchantConfig
import com.durpalla.core.config.Vertical
import com.durpalla.core.data.FakeAccountRepository
import com.durpalla.merchant.hotel.HotelData
import com.durpalla.merchant.hotel.HotelFeature

/** Sea Pearl Resort (hotel merchant). */
object Flavor : FlavorSetup {
    override val config = MerchantConfig(
        vertical = Vertical.HOTEL,
        merchantId = BuildConfig.MERCHANT_ID,
        merchantName = "Sea Pearl Resort",
        shortName = "Sea Pearl",
        tagline = "Beachfront luxury in Cox's Bazar",
        apiBaseUrl = BuildConfig.DURPALLA_API_BASE_URL,
        supportPhone = "09xxx",
        supportEmail = "reservations@seapearl.example",
        logoRes = R.drawable.merchant_logo,
        splashImageRes = R.drawable.img_resort,
        colors = BrandColors(
            primary = Color(0xFF0E6B63), primaryDark = Color(0xFF094A45), primarySoft = Color(0xFFE3F2F0),
            accent = Color(0xFFC9A227), accentSoft = Color(0xFFFBF5E1),
        ),
    )
    override val feature = HotelFeature
    override fun accountRepository() = FakeAccountRepository(HotelData.seedBookings, HotelData.offers)
}
