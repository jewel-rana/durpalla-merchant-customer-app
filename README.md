# Durpalla Merchant App (white-label customer app)

One codebase, many branded apps. Bus operators and hotels/resorts publish the app under **their** brand;
search, booking, payments and the backend are **Durpalla Limited**'s.
Kotlin · Jetpack Compose · Material 3 · Navigation Compose · ViewModel + StateFlow · Coil · Retrofit-ready.

| Flavor | Merchant | applicationId | Vertical | Brand |
|---|---|---|---|---|
| `bus` | Green Line Paribahan | `com.durpalla.merchant.greenline` | BUS | blue `#0B3D91` / orange `#F97316` |
| `hotel` | Sea Pearl Resort | `com.durpalla.merchant.seapearl` | HOTEL | teal `#0E6B63` / gold `#C9A227` |

## Build
JDK 17 + Android SDK 35.
```
./gradlew assembleBusDebug assembleHotelDebug
./gradlew testBusDebugUnitTest testHotelDebugUnitTest          # rules + Robolectric screenshot tests
./gradlew testBusDebugUnitTest -Proborazzi.test.record=true      # also writes PNGs to app/build/shots/
```
Demo login: any valid BD mobile (`01[3-9]XXXXXXXX`) and any 6-digit OTP.

## Modules & structure
```
core/                       shared by every merchant (Android library)
  config/MerchantConfig.kt  vertical, merchantId, API base URL, brand colours, logo, support contacts
  theme/                    MerchantTheme(config) -> Material 3 scheme from brand colours
  ui/Components.kt          Screen shell, cards, buttons, TotalBar, chips, Photo (Coil), QR, ...
  booking/Seats.kt          seat model + SeatRules (booked/blocked/ladies/max 4 seats) + demo layouts
  booking/Fare.kt           FareCalculator: bus fare+fee-discount; hotel 10% service + 15% VAT
  auth/                     Splash ("Powered by Durpalla Limited"), phone login, OTP
  payment/                  bKash / Nagad / Rocket / Card list + "Secure payment powered by Durpalla Limited"
  account/                  Dashboard, Bookings, Booking details (cancel sheet, support), Invoice, Offers,
                            Notifications, Profile, Edit profile, Saved travellers, Payment methods, Language, Help
  data/                     AuthRepository / AccountRepository interfaces + fakes
  api/DurpallaApi.kt        Retrofit interface, DTOs, ApiFactory (adds X-Merchant-Id + bearer token)
app/
  src/main/                 MainActivity: the ONLY Scaffold (insets + bottom nav Home/Bookings/Offers/Account)
                            VerticalFeature + FlavorSetup contracts
  src/bus/                  Flavor.kt (config) + bus/ feature: Home, Results, Seats, Boarding/Dropping,
                            Passengers, Payment, E-ticket; brand res, photos, adaptive icon
  src/hotel/                Flavor.kt + hotel/ feature: Home, Room list, Room detail, Checkout, Confirmed
  src/test/                 SeatRulesTest, FareCalculatorTest (run in both flavors)
  src/testBus|testHotel/    Roborazzi screenshot tests of key screens
```
Insets: screens use `core.ui.Screen` (a Column, not a Scaffold). The single Scaffold in `MainActivity`
applies status/nav-bar insets once; only the full-bleed splash and modal sheets handle their own.

## Add a new merchant
1. In `app/build.gradle.kts` add a flavor:
   ```kotlin
   create("shyamoli") { dimension = "merchant"; applicationId = "com.durpalla.merchant.shyamoli"
       buildConfigField("String", "MERCHANT_ID", "\"mrc_shyamoli\"") }
   ```
2. Copy `app/src/bus` (or `hotel`) to `app/src/shyamoli`. Then edit:
   - `res/values/brand.xml`: app name, `brand_primary`, `ic_launcher_background`
   - `res/drawable-nodpi/merchant_logo.png`, `ic_launcher_fg.png` (432×432, artwork inside the centre 66%), `mipmap-*/ic_launcher*.png`
   - `Flavor.kt`: `MerchantConfig` (name, tagline, colours, support, splash image)
   - photos in `drawable-nodpi/img_*.jpg`, sample data in the feature's `*Data.kt`
3. A new vertical (e.g. launch/ferry) means a new feature package that implements `VerticalFeature` and registers route `home`.

## Hooking up the Durpalla API
- Set `USE_REMOTE_API` / `DURPALLA_API_BASE_URL` in `app/build.gradle.kts`.
- `ApiFactory.create(config.apiBaseUrl, config.merchantId, tokenProvider)` returns `DurpallaApi`.
- Implement `BusRepository` / `HotelRepository` / `AccountRepository` / `AuthRepository` against it, mapping DTOs to models,
  then swap them in `Flavor.accountRepository()` and the feature's `viewModel(factory = …)`.
- Seat holds: call `holdSeats` before payment. The server must re-validate `SeatRules`.
- Payments: `paymentIntent` returns a bKash/Nagad/Rocket/card redirect URL. Open it in a Custom Tab and confirm by webhook.

## Photo credits
Bundled photos are the free-licence Unsplash / WordPress Photo Directory images used in the merchant web projects
(see `/workspace/brand/photos/CREDITS.md` in the design workspace).
