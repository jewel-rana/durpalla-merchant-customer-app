package com.durpalla.merchant

import androidx.compose.runtime.Composable
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.durpalla.core.account.*
import com.durpalla.core.theme.MerchantTheme
import com.durpalla.merchant.hotel.*
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w390dp-h844dp-xxhdpi")
class HotelScreenshots {
    private val repo = FakeHotelRepository()
    private fun shot(name: String, c: @Composable () -> Unit) = captureRoboImage("build/shots/mapp_hotel_$name.png") { MerchantTheme(Flavor.config) { c() } }
    private fun vm() = HotelViewModel(repo, Flavor.accountRepository()).apply { openRoom("deluxe"); setPlan(repo.rooms[0].plans[1]) }

    @Test fun home() = shot("home") { HotelHomeScreen(vm().state.value, repo.rooms, {}, {}, { _, _, _ -> }, {}, {}, {}) }
    @Test fun rooms() = shot("rooms") { val v = vm(); RoomListScreen(v.state.value, repo.rooms, v.filters, {}, {}, {}) }
    @Test fun detail() = shot("room_detail") { RoomDetailScreen(vm().state.value, {}, {}, {}) }
    @Test fun checkout() = shot("checkout") { CheckoutScreen(vm().state.value, {}, { _, _, _ -> }, {}, {}) }
    @Test fun confirmed() = shot("confirmed") { val v = vm(); v.pay(); BookingConfirmedScreen(v.state.value, {}, {}) }
    @Test fun bookings() = shot("bookings") { BookingsScreen(Flavor.accountRepository().bookings.value) {} }
    @Test fun profile() = shot("account") { ProfileScreen(Flavor.accountRepository().user.value, {}, {}, {}, {}, {}, {}, {}, {}) }
}
