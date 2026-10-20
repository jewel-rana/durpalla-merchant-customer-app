package com.durpalla.merchant.bus

import androidx.compose.material3.SnackbarHostState
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

object BusRoutes { const val RESULTS = "bus/results"; const val SEATS = "bus/seats"; const val POINTS = "bus/points"; const val PAX = "bus/passengers"; const val PAY = "bus/pay"; const val TICKET = "bus/ticket" }

object BusFeature : VerticalFeature {
    override fun NavGraphBuilder.register(nav: NavController, owner: ViewModelStoreOwner, account: AccountRepository, snack: SnackbarHostState) {
        val vm: @androidx.compose.runtime.Composable () -> BusViewModel = { viewModel(owner, factory = factory { BusViewModel(FakeBusRepository(), account) }) }
        composable(VerticalFeature.HOME_ROUTE) {
            val v = vm(); val s by v.state.collectAsStateWithLifecycle()
            BusHomeScreen(s, v.cities, v.popular, v::setFrom, v::setTo, v::swap, v::setDate, v::setPassengers, v::pickRoute,
                { v.search(); nav.navigate(BusRoutes.RESULTS) }, { nav.navigate(AccountRoutes.NOTIFS) }, { nav.navigate(AccountRoutes.OFFERS) })
        }
        composable(BusRoutes.RESULTS) { val v = vm(); val s by v.state.collectAsStateWithLifecycle(); ResultsScreen(s, { nav.popBackStack() }, v::setFilter) { v.selectTrip(it); nav.navigate(BusRoutes.SEATS) } }
        composable(BusRoutes.SEATS) {
            val v = vm(); val s by v.state.collectAsStateWithLifecycle()
            SeatSelectionScreen(s, { nav.popBackStack() }, v::switchLayout, v::setDeck, v::setGender, v::toggleSeat, v::clearSeatError) { nav.navigate(BusRoutes.POINTS) }
        }
        composable(BusRoutes.POINTS) {
            val v = vm(); val s by v.state.collectAsStateWithLifecycle()
            BoardingScreen(s, v.boardingPoints(), v.droppingPoints(), { nav.popBackStack() }, v::setBoarding, v::setDropping) { v.preparePassengers(); nav.navigate(BusRoutes.PAX) }
        }
        composable(BusRoutes.PAX) { val v = vm(); val s by v.state.collectAsStateWithLifecycle(); PassengerDetailsScreen(s, { nav.popBackStack() }, v::updatePassenger) { nav.navigate(BusRoutes.PAY) } }
        composable(BusRoutes.PAY) {
            val v = vm(); val s by v.state.collectAsStateWithLifecycle()
            BusPaymentScreen(s, { nav.popBackStack() }, v::setPay) { v.pay(); nav.navigate(BusRoutes.TICKET) { popUpTo(VerticalFeature.HOME_ROUTE) } }
        }
        composable(BusRoutes.TICKET) {
            val v = vm(); val s by v.state.collectAsStateWithLifecycle(); val scope = rememberCoroutineScope()
            ETicketScreen(s, { scope.launch { snack.showSnackbar("E-ticket PDF saved (stub)") } }) { nav.navigate(AccountRoutes.BOOKINGS) { popUpTo(VerticalFeature.HOME_ROUTE) } }
        }
    }
}
