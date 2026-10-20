package com.durpalla.merchant

import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.durpalla.core.config.MerchantConfig
import com.durpalla.core.data.AccountRepository

/** Contract every vertical (bus, hotel, ...) implements. The flavor's `Flavor` object picks one. */
interface VerticalFeature {
    /** Must register a destination with route [HOME_ROUTE]. */
    fun NavGraphBuilder.register(nav: NavController, owner: ViewModelStoreOwner, account: AccountRepository, snack: SnackbarHostState)

    companion object { const val HOME_ROUTE = "home" }
}

/** What each flavor must provide (implemented as `object Flavor` in src/<flavor>/). */
interface FlavorSetup {
    val config: MerchantConfig
    val feature: VerticalFeature
    fun accountRepository(): AccountRepository
}
