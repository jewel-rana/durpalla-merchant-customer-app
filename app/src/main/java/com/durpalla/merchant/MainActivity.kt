package com.durpalla.merchant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.durpalla.core.account.AccountRoutes
import com.durpalla.core.account.AccountViewModel
import com.durpalla.core.account.accountGraph
import com.durpalla.core.auth.*
import com.durpalla.core.data.FakeAuthRepository
import com.durpalla.core.theme.MerchantTheme
import com.durpalla.core.theme.N

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { MerchantTheme(Flavor.config) { MerchantApp(Flavor) } }
    }
}

inline fun <reified T : ViewModel> factory(crossinline make: () -> T) = object : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST") override fun <V : ViewModel> create(modelClass: Class<V>): V = make() as V
}

private data class Tab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

object AppRoutes { const val SPLASH = "splash"; const val LOGIN = "login"; const val OTP = "otp" }

/**
 * The ONE Scaffold in the app. It owns status-bar / navigation-bar insets and the bottom navigation,
 * and passes a single padding down; screens never add system insets themselves.
 */
@Composable
fun MerchantApp(setup: FlavorSetup, startDestination: String = AppRoutes.SPLASH) {
    val nav = rememberNavController()
    val owner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current)
    val accountRepo = remember { setup.accountRepository() }
    val accountVm: AccountViewModel = viewModel(owner, factory = factory { AccountViewModel(accountRepo) })
    val authVm: AuthViewModel = viewModel(owner, factory = factory { AuthViewModel(FakeAuthRepository()) })
    val snack = remember { SnackbarHostState() }
    val current = nav.currentBackStackEntryAsState().value?.destination?.route
    val tabs = listOf(
        Tab(VerticalFeature.HOME_ROUTE, "Home", Icons.Filled.Home), Tab(AccountRoutes.BOOKINGS, "Bookings", Icons.Filled.ConfirmationNumber),
        Tab(AccountRoutes.OFFERS, "Offers", Icons.Filled.LocalOffer), Tab(AccountRoutes.PROFILE, "Account", Icons.Filled.Person),
    )
    val showTabs = current in tabs.map { it.route }
    val fullBleed = current == AppRoutes.SPLASH

    Scaffold(
        containerColor = if (fullBleed) Color.Transparent else N.Bg,
        snackbarHost = { SnackbarHost(snack) },
        bottomBar = {
            if (showTabs) NavigationBar(containerColor = Color.White) {
                tabs.forEach { t ->
                    NavigationBarItem(current == t.route, onClick = {
                        nav.navigate(t.route) { popUpTo(VerticalFeature.HOME_ROUTE) { saveState = true }; launchSingleTop = true; restoreState = true }
                    }, icon = { Icon(t.icon, null) }, label = { Text(t.label) },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = MaterialTheme.colorScheme.primaryContainer))
                }
            }
        },
    ) { insets ->
        NavHost(nav, startDestination, Modifier.padding(if (fullBleed) androidx.compose.foundation.layout.PaddingValues() else insets)) {
            composable(AppRoutes.SPLASH) { SplashScreen { nav.navigate(AppRoutes.LOGIN) { popUpTo(AppRoutes.SPLASH) { inclusive = true } } } }
            composable(AppRoutes.LOGIN) { val s by authVm.state.collectAsState(); LoginScreen(s, authVm::phone) { authVm.send { nav.navigate(AppRoutes.OTP) } } }
            composable(AppRoutes.OTP) {
                val s by authVm.state.collectAsState()
                OtpScreen(s.phone, s.error, { nav.popBackStack() }) { authVm.verify(it) { nav.navigate(VerticalFeature.HOME_ROUTE) { popUpTo(0) { inclusive = true } } } }
            }
            with(setup.feature) { register(nav, owner, accountRepo, snack) }
            accountGraph(nav, { accountVm }, snack) { nav.navigate(AppRoutes.LOGIN) { popUpTo(0) { inclusive = true } } }
        }
    }
}
