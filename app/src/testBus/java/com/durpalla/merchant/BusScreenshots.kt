package com.durpalla.merchant

import androidx.compose.runtime.Composable
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.durpalla.core.account.*
import com.durpalla.core.booking.Deck
import com.durpalla.core.booking.Gender
import com.durpalla.core.model.PayMethod
import com.durpalla.core.theme.MerchantTheme
import com.durpalla.merchant.bus.*
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w390dp-h844dp-xxhdpi")
class BusScreenshots {
    private val repo = FakeBusRepository()
    private fun shot(name: String, c: @Composable () -> Unit) = captureRoboImage("build/shots/mapp_bus_$name.png") { MerchantTheme(Flavor.config) { c() } }
    private fun state(): BusState {
        val vm = BusViewModel(repo, Flavor.accountRepository())
        vm.search(); vm.selectTrip(repo.search("", "", "")[0])
        vm.toggleSeat(vm.state.value.map!!.seats.first { it.state == com.durpalla.core.booking.SeatState.AVAILABLE }.id)
        vm.setBoarding(repo.boarding("")[0]); vm.setDropping(repo.dropping("")[0]); vm.preparePassengers(); vm.setPay(PayMethod.BKASH); vm.pay()
        return vm.state.value
    }

    @Test fun home() = shot("home") { BusHomeScreen(BusState(), repo.cities, repo.popular, {}, {}, {}, {}, {}, {}, {}, {}, {}) }
    @Test fun results() = shot("results") { BusHomeStub { ResultsScreen(state(), {}, {}, {}) } }
    @Test fun seats() = shot("seats") { SeatSelectionScreen(state(), {}, {}, {}, {}, {}, {}, {}) }
    @Test fun sleeper() = shot("seats_sleeper") {
        val vm = BusViewModel(repo, Flavor.accountRepository()); vm.search(); vm.selectTrip(repo.search("", "", "")[2]); vm.setDeck(Deck.UPPER); vm.setGender(Gender.FEMALE)
        SeatSelectionScreen(vm.state.value, {}, {}, {}, {}, {}, {}, {})
    }
    @Test fun payment() = shot("payment") { BusPaymentScreen(state(), {}, {}, {}) }
    @Test fun ticket() = shot("ticket") { ETicketScreen(state(), {}, {}) }
    @Test fun dashboard() = shot("account_dashboard") { val r = Flavor.accountRepository(); DashboardScreen(r.user.value, r.bookings.value, {}, {}, {}, {}, {}) }
    @Test fun invoice() = shot("invoice") { val r = Flavor.accountRepository(); InvoiceScreen(r.bookings.value[0], r.user.value, {}, {}, {}) }
    @Test fun splash() = shot("splash") { com.durpalla.core.auth.SplashScreen {} }
}

@Composable private fun BusHomeStub(c: @Composable () -> Unit) = c()
