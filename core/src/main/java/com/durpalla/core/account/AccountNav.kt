package com.durpalla.core.account

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.coroutines.launch

object AccountRoutes {
    const val DASHBOARD = "account/dashboard"; const val BOOKINGS = "bookings"; const val OFFERS = "offers"; const val PROFILE = "account"
    const val DETAILS = "booking/{id}"; const val INVOICE = "booking/{id}/invoice"
    const val NOTIFS = "notifications"; const val EDIT = "account/edit"; const val TRAVELLERS = "account/travellers"
    const val PAYMENTS = "account/payments"; const val LANGUAGE = "account/language"; const val HELP = "help"
    fun details(id: String) = "booking/$id"; fun invoice(id: String) = "booking/$id/invoice"
}

/** Registers every shared account screen. [onLogout] lets the host app reset to login. */
fun NavGraphBuilder.accountGraph(nav: NavController, vm: () -> AccountViewModel, snack: SnackbarHostState, onLogout: () -> Unit) {
    composable(AccountRoutes.DASHBOARD) {
        val v = vm(); val u by v.user.collectAsStateWithLifecycle(); val b by v.bookings.collectAsStateWithLifecycle()
        DashboardScreen(u, b, { nav.navigate(AccountRoutes.BOOKINGS) }, { nav.navigate(AccountRoutes.details(it.id)) }, { nav.navigate(AccountRoutes.OFFERS) }, { nav.navigate(AccountRoutes.NOTIFS) }, { nav.navigate(AccountRoutes.EDIT) })
    }
    composable(AccountRoutes.BOOKINGS) { val b by vm().bookings.collectAsStateWithLifecycle(); BookingsScreen(b) { nav.navigate(AccountRoutes.details(it.id)) } }
    composable(AccountRoutes.DETAILS) { e ->
        val v = vm(); val list by v.bookings.collectAsStateWithLifecycle(); val scope = rememberCoroutineScope()
        val b = list.firstOrNull { it.id == e.arguments?.getString("id") } ?: return@composable
        BookingDetailsScreen(b, { nav.popBackStack() }, { nav.navigate(AccountRoutes.invoice(b.id)) },
            { v.cancel(b.id) { scope.launch { snack.showSnackbar("Booking cancelled. Refund initiated.") } } }, { nav.navigate(AccountRoutes.HELP) })
    }
    composable(AccountRoutes.INVOICE) { e ->
        val v = vm(); val list by v.bookings.collectAsStateWithLifecycle(); val u by v.user.collectAsStateWithLifecycle(); val scope = rememberCoroutineScope()
        val b = list.firstOrNull { it.id == e.arguments?.getString("id") } ?: return@composable
        InvoiceScreen(b, u, { nav.popBackStack() }, { scope.launch { snack.showSnackbar("Share sheet (stub)") } }, { scope.launch { snack.showSnackbar("Invoice PDF saved (stub)") } })
    }
    composable(AccountRoutes.OFFERS) { val scope = rememberCoroutineScope(); OffersScreen(vm().repo.offers) { c -> scope.launch { snack.showSnackbar("Code $c copied") } } }
    composable(AccountRoutes.NOTIFS) { NotificationsScreen(vm().repo.notices) { nav.popBackStack() } }
    composable(AccountRoutes.PROFILE) {
        val u by vm().user.collectAsStateWithLifecycle()
        ProfileScreen(u, { nav.navigate(AccountRoutes.EDIT) }, { nav.navigate(AccountRoutes.TRAVELLERS) }, { nav.navigate(AccountRoutes.PAYMENTS) },
            { nav.navigate(AccountRoutes.LANGUAGE) }, { nav.navigate(AccountRoutes.HELP) }, { nav.navigate(AccountRoutes.NOTIFS) }, { nav.navigate(AccountRoutes.DASHBOARD) }, onLogout)
    }
    composable(AccountRoutes.EDIT) { val v = vm(); val u by v.user.collectAsStateWithLifecycle(); EditProfileScreen(u, { nav.popBackStack() }) { n, e -> v.save(n, e); nav.popBackStack() } }
    composable(AccountRoutes.TRAVELLERS) { val v = vm(); val t by v.travellers.collectAsStateWithLifecycle(); SavedTravellersScreen(t, { nav.popBackStack() }, v::addTraveller) }
    composable(AccountRoutes.PAYMENTS) { val p by vm().payments.collectAsStateWithLifecycle(); PaymentMethodsScreen(p) { nav.popBackStack() } }
    composable(AccountRoutes.LANGUAGE) { LanguageScreen { nav.popBackStack() } }
    composable(AccountRoutes.HELP) { HelpScreen { nav.popBackStack() } }
}
