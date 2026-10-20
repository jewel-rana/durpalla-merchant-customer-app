package com.durpalla.merchant.hotel

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.durpalla.core.account.AccountRoutes
import com.durpalla.core.data.AccountRepository
import com.durpalla.merchant.VerticalFeature
import com.durpalla.merchant.factory
import kotlinx.coroutines.launch

object HotelRoutes { const val ROOMS = "hotel/rooms"; const val ROOM = "hotel/room"; const val CHECKOUT = "hotel/checkout"; const val CONFIRMED = "hotel/confirmed" }

object HotelFeature : VerticalFeature {
    override fun NavGraphBuilder.register(nav: NavController, owner: ViewModelStoreOwner, account: AccountRepository, snack: SnackbarHostState) {
        val vm: @Composable () -> HotelViewModel = { viewModel(owner, factory = factory { HotelViewModel(FakeHotelRepository(), account) }) }
        composable(VerticalFeature.HOME_ROUTE) {
            val v = vm(); val s by v.state.collectAsStateWithLifecycle()
            HotelHomeScreen(s, v.rooms, v::shiftCheckIn, v::setNights, v::setGuests, { nav.navigate(HotelRoutes.ROOMS) }, { v.openRoom(it.id); nav.navigate(HotelRoutes.ROOM) }, { nav.navigate(AccountRoutes.NOTIFS) })
        }
        composable(HotelRoutes.ROOMS) { val v = vm(); val s by v.state.collectAsStateWithLifecycle(); RoomListScreen(s, v.filtered(s), v.filters, { nav.popBackStack() }, v::setFilter) { v.openRoom(it.id); nav.navigate(HotelRoutes.ROOM) } }
        composable(HotelRoutes.ROOM) { val v = vm(); val s by v.state.collectAsStateWithLifecycle(); RoomDetailScreen(s, { nav.popBackStack() }, v::setPlan) { nav.navigate(HotelRoutes.CHECKOUT) } }
        composable(HotelRoutes.CHECKOUT) {
            val v = vm(); val s by v.state.collectAsStateWithLifecycle()
            CheckoutScreen(s, { nav.popBackStack() }, v::setGuest, v::setPay) { v.pay(); nav.navigate(HotelRoutes.CONFIRMED) { popUpTo(VerticalFeature.HOME_ROUTE) } }
        }
        composable(HotelRoutes.CONFIRMED) {
            val v = vm(); val s by v.state.collectAsStateWithLifecycle(); val scope = rememberCoroutineScope()
            BookingConfirmedScreen(s, { scope.launch { snack.showSnackbar("Added to calendar (stub)") } }) { nav.navigate(AccountRoutes.BOOKINGS) { popUpTo(VerticalFeature.HOME_ROUTE) } }
        }
    }
}
