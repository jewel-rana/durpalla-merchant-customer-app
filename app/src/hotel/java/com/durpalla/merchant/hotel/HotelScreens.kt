package com.durpalla.merchant.hotel

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.durpalla.core.booking.taka
import com.durpalla.core.config.LocalMerchant
import com.durpalla.core.model.PayMethod
import com.durpalla.core.payment.PaymentMethodList
import com.durpalla.core.theme.Brand
import com.durpalla.core.theme.N
import com.durpalla.core.ui.*
import com.durpalla.merchant.R

@Composable
private fun Rating(r: Double) = Text("★ $r", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = N.Ink,
    modifier = Modifier.clip(RoundedCornerShape(50)).background(Color.White).padding(horizontal = 8.dp, vertical = 3.dp))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotelHomeScreen(s: HotelState, rooms: List<Room>, onShift: (Long) -> Unit, onNights: (Int) -> Unit, onGuests: (Int, Int, Int) -> Unit, onCheck: () -> Unit, onRoom: (Room) -> Unit, onNotifications: () -> Unit) {
    val m = LocalMerchant.current
    var guests by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().background(N.Bg).verticalScroll(rememberScrollState())) {
        Box(Modifier.fillMaxWidth().height(230.dp)) {
            Photo(R.drawable.img_resort, Modifier.fillMaxSize(), 0.dp)
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.15f), m.colors.primaryDark.copy(alpha = 0.85f)))))
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.foundation.Image(androidx.compose.ui.res.painterResource(m.logoRes), null, Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)))
                Column(Modifier.weight(1f).padding(start = 8.dp)) { Text("Welcome back,", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp); Text("Jewel", color = Color.White, fontWeight = FontWeight.Bold) }
                IconButton(onNotifications, Modifier.clip(CircleShape).background(Color.White.copy(alpha = 0.2f))) { Icon(Icons.Filled.NotificationsNone, null, tint = Color.White) }
            }
            Text("Your beachfront escape\nin Cox's Bazar", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold, lineHeight = 28.sp, modifier = Modifier.align(Alignment.BottomStart).padding(16.dp, 0.dp, 16.dp, 40.dp))
        }
        Column(Modifier.padding(horizontal = 16.dp).offset(y = (-28).dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MCard {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DateBox("CHECK-IN", s.fmt(s.checkIn), Modifier.weight(1f)) { onShift(1) }
                    DateBox("CHECK-OUT", s.fmt(s.checkOut), Modifier.weight(1f)) { onNights(s.nights + 1) }
                }
                Spacer(Modifier.height(8.dp))
                DateBox("GUESTS & ROOMS", s.guestsLabel, Modifier.fillMaxWidth()) { guests = true }
                Spacer(Modifier.height(12.dp))
                AccentButton("Check Availability", onCheck)
            }
            SectionTitle("Featured rooms", "See all", onCheck)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rooms.take(3).forEach { r ->
                    MCard(Modifier.width(180.dp), pad = 8.dp, onClick = { onRoom(r) }) {
                        Photo(r.photos.first(), Modifier.fillMaxWidth().height(110.dp), 10.dp)
                        Text(r.name, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 6.dp), maxLines = 1)
                        Row { Text("${taka(r.fromRate)}/night", color = m.colors.primary, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f)); Text("★ ${r.rating}", fontSize = 12.sp, color = m.colors.accent) }
                    }
                }
            }
            SectionTitle("Amenities")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                IconTile(Icons.Filled.Pool, "Pool"); IconTile(Icons.Filled.Spa, "Spa"); IconTile(Icons.Filled.Restaurant, "Dining"); IconTile(Icons.Filled.AirportShuttle, "Shuttle")
            }
        }
    }
    if (guests) ModalBottomSheet({ guests = false }, containerColor = Color.White) {
        Column(Modifier.padding(24.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Guests & rooms", style = MaterialTheme.typography.titleLarge)
            Stepper("Adults", s.adults) { onGuests(it, s.children, s.rooms) }
            Stepper("Children", s.children) { onGuests(s.adults, it, s.rooms) }
            Stepper("Rooms", s.rooms) { onGuests(s.adults, s.children, it) }
            Stepper("Nights", s.nights, onNights)
            AccentButton("Apply", { guests = false })
        }
    }
}

@Composable
private fun Stepper(label: String, v: Int, on: (Int) -> Unit) = Row(verticalAlignment = Alignment.CenterVertically) {
    Text(label, Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
    OutlinedIconButton({ on(v - 1) }) { Icon(Icons.Filled.Remove, null) }; Text("$v", Modifier.width(40.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center, style = MaterialTheme.typography.titleMedium)
    OutlinedIconButton({ on(v + 1) }) { Icon(Icons.Filled.Add, null) }
}

@Composable
private fun DateBox(label: String, value: String, modifier: Modifier, onClick: () -> Unit) =
    Column(modifier.clip(RoundedCornerShape(12.dp)).background(N.Bg).clickable(onClick = onClick).padding(12.dp)) {
        Text(label, fontSize = 10.sp, color = N.Muted, fontWeight = FontWeight.Bold); Text(value, style = MaterialTheme.typography.titleSmall)
    }

@Composable
fun RoomListScreen(s: HotelState, rooms: List<Room>, filters: List<String>, onBack: () -> Unit, onFilter: (Int) -> Unit, onRoom: (Room) -> Unit) {
    Screen(title = "Available rooms", onBack = onBack, actions = { IconButton({}) { Icon(Icons.Filled.Tune, null) } }) {
        Text("${s.datesLabel} · ${s.guestsLabel}", fontSize = 12.sp, color = N.Muted, modifier = Modifier.padding(start = 8.dp))
        ChoiceChips(filters, s.filter, onFilter)
        rooms.forEach { r ->
            MCard(pad = 0.dp, onClick = { onRoom(r) }) {
                Box { Photo(r.photos.first(), Modifier.fillMaxWidth().height(160.dp), 16.dp); Box(Modifier.align(Alignment.TopEnd).padding(10.dp)) { Rating(r.rating) } }
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) { Text(r.name, style = MaterialTheme.typography.titleMedium); Text("${r.size} · ${r.bed} · ${r.view}", fontSize = 12.sp, color = N.Muted) }
                    Column(horizontalAlignment = Alignment.End) { Text(taka(r.fromRate), color = Brand.colors.primary, fontWeight = FontWeight.Bold, fontSize = 18.sp); Text("/night", fontSize = 11.sp, color = N.Muted) }
                }
            }
        }
        if (rooms.isEmpty()) EmptyState("No rooms match")
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun RoomDetailScreen(s: HotelState, onBack: () -> Unit, onPlan: (RatePlan) -> Unit, onBook: () -> Unit) {
    val r = s.room ?: return; val f = s.fare
    Column(Modifier.fillMaxSize().background(N.Bg)) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            val pager = rememberPagerState { r.photos.size }
            Box(Modifier.fillMaxWidth().height(260.dp)) {
                HorizontalPager(pager) { Photo(r.photos[it], Modifier.fillMaxSize(), 0.dp) }
                IconButton(onBack, Modifier.padding(12.dp).clip(CircleShape).background(Color.White)) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                Text("${pager.currentPage + 1} / ${r.photos.size}", color = Color.White, fontSize = 12.sp, modifier = Modifier.align(Alignment.TopEnd).padding(16.dp).clip(RoundedCornerShape(50)).background(Color.Black.copy(alpha = 0.4f)).padding(horizontal = 10.dp, vertical = 4.dp))
            }
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) { Text(r.name, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f)); Text("★ ${r.rating}", color = Brand.colors.accent, fontWeight = FontWeight.Bold) }
                Text("${r.size} · ${r.bed} · ${r.view} · Max ${r.maxGuests} guests", color = N.Muted, fontSize = 13.sp)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { r.amenities.forEach { Pill(it, Tone.PRIMARY) } }
                SectionTitle("About this room"); Text(r.about, color = N.Muted)
                SectionTitle("Rate plans")
                r.plans.forEach { p ->
                    MCard(onClick = { onPlan(p) }, border = if (p == s.plan) Brand.colors.primary else N.Line, pad = 12.dp) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) { Text(p.name, style = MaterialTheme.typography.titleSmall); Text(p.perks, fontSize = 12.sp, color = N.Muted); if (!p.refundable) Text("Non-refundable", fontSize = 11.sp, color = N.Danger) }
                            Text(taka(p.rate), fontWeight = FontWeight.Bold, color = Brand.colors.primary); RadioButton(p == s.plan, { onPlan(p) })
                        }
                    }
                }
            }
        }
        Surface(color = Color.White, shadowElevation = 10.dp, shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)) {
            Box(Modifier.padding(16.dp)) { TotalBar("${s.nights} nights · ${taka(f?.roomTotal ?: 0)}", "${taka(s.plan?.rate ?: 0)} /night", "Book Now", s.plan != null, onBook) }
        }
    }
}

@Composable
fun CheckoutScreen(s: HotelState, onBack: () -> Unit, onGuest: (String, String, String) -> Unit, onPay: (PayMethod) -> Unit, onConfirm: () -> Unit) {
    val r = s.room ?: return; val f = s.fare ?: return; val p = s.plan ?: return
    Screen(title = "Checkout", onBack = onBack, bottomBar = { AccentButton("Pay ${taka(f.total)}", onConfirm, enabled = s.guestName.isNotBlank() && s.guestPhone.length == 11) }) {
        MCard(pad = 12.dp) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Photo(r.photos.first(), Modifier.size(64.dp))
                Column(Modifier.padding(start = 12.dp)) { Text(r.name, style = MaterialTheme.typography.titleMedium); Text("${s.datesLabel} · ${s.nights} nights", fontSize = 12.sp, color = N.Muted); Text("${s.guestsLabel} · ${p.name}", fontSize = 12.sp, color = N.Muted) }
            }
        }
        MCard {
            KV("${s.nights} nights × ${s.rooms} room × ${taka(p.rate)}", taka(f.roomTotal))
            KV("Service charge (10%)", taka(f.serviceCharge)); KV("VAT (15%)", taka(f.vat))
            HorizontalDivider(Modifier.padding(vertical = 4.dp), color = N.Line); KV("Total", taka(f.total), Brand.colors.primary, true)
        }
        MCard {
            Text("Guest details", style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(s.guestName, { onGuest(it, s.guestPhone, s.request) }, Modifier.fillMaxWidth(), label = { Text("Full name") }, singleLine = true)
            OutlinedTextField(s.guestPhone, { onGuest(s.guestName, it, s.request) }, Modifier.fillMaxWidth(), label = { Text("Mobile") }, singleLine = true)
            OutlinedTextField(s.request, { onGuest(s.guestName, s.guestPhone, it) }, Modifier.fillMaxWidth(), label = { Text("Special request (optional)") })
        }
        PaymentMethodList(s.pay, onPay)
    }
}

@Composable
fun BookingConfirmedScreen(s: HotelState, onCalendar: () -> Unit, onBookings: () -> Unit) {
    val b = s.confirmed ?: return; val r = s.room ?: return; val m = LocalMerchant.current
    Screen(bottomBar = { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { PrimaryButton("Add to Calendar", onCalendar, Modifier.weight(1f)); AccentButton("View My Bookings", onBookings, Modifier.weight(1f)) } }) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            SuccessBadge(); Text("Booking Confirmed!", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 8.dp))
            Text("Confirmation ${b.id} sent via SMS & email", fontSize = 12.sp, color = N.Muted)
        }
        MCard(pad = 0.dp) {
            Photo(R.drawable.img_pool, Modifier.fillMaxWidth().height(130.dp), 16.dp)
            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(m.merchantName, style = MaterialTheme.typography.titleLarge)
                Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Column(Modifier.weight(1f)) { Text("Check-in", fontSize = 11.sp, color = N.Muted); Text("${s.fmt(s.checkIn)}, 2 PM", fontWeight = FontWeight.Bold) }
                    Column(horizontalAlignment = Alignment.End) { Text("Check-out", fontSize = 11.sp, color = N.Muted); Text("${s.fmt(s.checkOut)}, 12 PM", fontWeight = FontWeight.Bold) }
                }
                KV("${r.name} · ${s.guestsLabel}", "Paid ${taka(b.amount)}", Brand.colors.primary, true)
                Spacer(Modifier.height(8.dp)); QrCode(b.id, Modifier.size(140.dp))
                Text("Show this voucher at reception", fontSize = 12.sp, color = N.Muted)
            }
        }
    }
}
