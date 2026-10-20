package com.durpalla.merchant.bus

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.durpalla.core.booking.*
import com.durpalla.core.config.LocalMerchant
import com.durpalla.core.model.PayMethod
import com.durpalla.core.payment.PaymentMethodList
import com.durpalla.core.theme.Brand
import com.durpalla.core.theme.N
import com.durpalla.core.ui.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusHomeScreen(
    s: BusState, cities: List<String>, popular: List<PopularRoute>,
    onFrom: (String) -> Unit, onTo: (String) -> Unit, onSwap: () -> Unit, onDate: (Int) -> Unit, onPassengers: (Int) -> Unit,
    onRoute: (PopularRoute) -> Unit, onSearch: () -> Unit, onNotifications: () -> Unit, onOffer: () -> Unit,
) {
    val m = LocalMerchant.current
    var sheet by remember { mutableStateOf(false) }
    var cityPick by remember { mutableStateOf<Boolean?>(null) } // true=from, false=to
    Screen {
        Row(verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.foundation.Image(androidx.compose.ui.res.painterResource(m.logoRes), null, Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).border(1.dp, N.Line, RoundedCornerShape(10.dp)))
            Column(Modifier.weight(1f).padding(start = 10.dp)) { Text("Good evening,", color = N.Muted, fontSize = 12.sp); Text("Jewel 👋", style = MaterialTheme.typography.titleMedium) }
            IconButton(onNotifications) { Icon(Icons.Filled.NotificationsNone, "Notifications") }
        }
        MCard(border = m.colors.accent) {
            Box {
                Column {
                    Field("FROM", s.from) { cityPick = true }
                    Spacer(Modifier.height(8.dp))
                    Field("TO", s.to) { cityPick = false }
                }
                FilledIconButton(onSwap, Modifier.align(Alignment.CenterEnd).padding(end = 8.dp), colors = IconButtonDefaults.filledIconButtonColors(containerColor = m.colors.primary)) { Icon(Icons.Filled.SwapVert, "Swap") }
            }
            Spacer(Modifier.height(10.dp))
            ChoiceChips(listOf("Today", "Tomorrow", "📅 Pick date"), s.dateIndex, onDate)
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { sheet = true }.background(N.Bg).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Person, null, tint = m.colors.primary); Text("  ${s.passengers} Passenger${if (s.passengers > 1) "s" else ""}", Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                Text(s.dateLabel, color = N.Muted, fontSize = 12.sp)
            }
            Spacer(Modifier.height(12.dp))
            AccentButton("Search Buses", onSearch)
        }
        SectionTitle("Popular routes", "See all")
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            popular.forEach { r ->
                MCard(Modifier.width(150.dp), pad = 8.dp, onClick = { onRoute(r) }) {
                    Photo(r.img, Modifier.fillMaxWidth().height(90.dp), 10.dp)
                    Text("${r.from} → ${r.to}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp), maxLines = 1)
                    Text("from ${taka(r.fromFare)}", color = m.colors.accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(m.colors.accent).clickable(onClick = onOffer).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text("15% OFF with bKash", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp); Text("Code BKASH15 · till 31 Oct", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp) }
            Text("Claim", color = m.colors.accent, fontWeight = FontWeight.Bold, modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(Color.White).padding(horizontal = 14.dp, vertical = 8.dp))
        }
    }
    if (sheet) ModalBottomSheet({ sheet = false }, containerColor = Color.White) {
        Column(Modifier.padding(24.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Passengers", style = MaterialTheme.typography.titleLarge)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Seats (max ${SeatRules.MAX_SEATS})", Modifier.weight(1f))
                OutlinedIconButton({ onPassengers(s.passengers - 1) }) { Icon(Icons.Filled.Remove, null) }
                Text("${s.passengers}", Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.titleLarge)
                OutlinedIconButton({ onPassengers(s.passengers + 1) }) { Icon(Icons.Filled.Add, null) }
            }
            AccentButton("Done", { sheet = false })
        }
    }
    cityPick?.let { isFrom ->
        ModalBottomSheet({ cityPick = null }, containerColor = Color.White) {
            Column(Modifier.padding(horizontal = 24.dp).navigationBarsPadding()) {
                Text(if (isFrom) "Leaving from" else "Going to", style = MaterialTheme.typography.titleLarge)
                cities.forEach { c -> Text(c, Modifier.fillMaxWidth().clickable { if (isFrom) onFrom(c) else onTo(c); cityPick = null }.padding(vertical = 14.dp)) }
            }
        }
    }
}

@Composable
private fun Field(label: String, value: String, onClick: () -> Unit) =
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(N.Bg).clickable(onClick = onClick).padding(12.dp)) {
        Text(label, fontSize = 10.sp, color = N.Muted, fontWeight = FontWeight.Bold); Text(value, style = MaterialTheme.typography.titleMedium)
    }

@Composable
fun ResultsScreen(s: BusState, onBack: () -> Unit, onFilter: (Int) -> Unit, onTrip: (BusTrip) -> Unit) {
    Screen(title = "${s.from} → ${s.to}", onBack = onBack, actions = { IconButton({}) { Icon(Icons.Filled.Tune, "Filters") } }) {
        Text("${s.dateLabel} · ${s.passengers} passenger", color = N.Muted, fontSize = 12.sp, modifier = Modifier.padding(start = 8.dp))
        ChoiceChips(listOf("All ${s.results.size}", "AC", "Non-AC", "Sleeper", "Night"), s.filter, onFilter)
        s.filtered.forEachIndexed { i, t ->
            MCard(onClick = { onTrip(t) }, border = if (i == 0) Brand.colors.primary else N.Line) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Photo(t.img, Modifier.size(40.dp), 10.dp)
                    Column(Modifier.weight(1f).padding(horizontal = 10.dp)) { Text(t.name, style = MaterialTheme.typography.titleSmall); Text(t.coach, color = N.Muted, fontSize = 12.sp) }
                    Text(taka(t.fare), color = Brand.colors.accent, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column { Text(t.dep, style = MaterialTheme.typography.titleLarge, maxLines = 1); Text(s.from, fontSize = 11.sp, color = N.Muted) }
                    Column(Modifier.weight(1f).padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) { Text(t.duration, fontSize = 11.sp, color = N.Muted); HorizontalDivider(color = N.Line) }
                    Column(horizontalAlignment = Alignment.End) { Text(t.arr, style = MaterialTheme.typography.titleLarge, maxLines = 1); Text(s.to, fontSize = 11.sp, color = N.Muted) }
                }
                Spacer(Modifier.height(8.dp))
                Row { Text("${t.seatsLeft} seats left", color = if (t.seatsLeft < 6) N.Danger else N.Success, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f)); Text(t.amenities.joinToString(" · "), fontSize = 11.sp, color = N.Muted) }
            }
        }
        if (s.filtered.isEmpty()) EmptyState("No buses match this filter")
    }
}

@Composable
fun SeatSelectionScreen(s: BusState, onBack: () -> Unit, onLayout: (SeatLayoutType) -> Unit, onDeck: (Deck) -> Unit, onGender: (Gender) -> Unit, onSeat: (String) -> Unit, onDismissError: () -> Unit, onContinue: () -> Unit) {
    val map = s.map ?: return
    LaunchedEffect(s.seatError) { if (s.seatError != null) { kotlinx.coroutines.delay(2500); onDismissError() } }
    Screen(title = "Select seats", onBack = onBack, actions = { Text("${s.dateLabel} · ${s.trip?.dep}", fontSize = 12.sp, color = N.Muted, modifier = Modifier.padding(end = 8.dp)) },
        bottomBar = {
            TotalBar(if (s.selected.isEmpty()) "0 seats selected" else "${s.selected.size} seat${if (s.selected.size > 1) "s" else ""} · ${s.selected.joinToString()}", taka(s.fare.seatFare), "Continue", s.selected.isNotEmpty(), onContinue)
        }) {
        ChoiceChips(SeatLayoutType.entries.map { it.label.substringBefore(" (") }, SeatLayoutType.entries.indexOf(map.type), { onLayout(SeatLayoutType.entries[it]) })
        Legend()
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Travelling as  ", fontSize = 12.sp, color = N.Muted)
            ChoiceChips(listOf("Male", "Female"), s.travellerGender.ordinal, { onGender(Gender.entries[it]) })
        }
        if (map.type.decks > 1) ChoiceChips(listOf("Lower deck", "Upper deck"), s.deck.ordinal, { onDeck(Deck.entries[it]) })
        s.seatError?.let { Text(it, color = N.Danger, fontSize = 13.sp, modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(N.DangerSoft).padding(10.dp)) }
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { SeatGrid(map, s.deck, s.selected, onSeat) }
    }
}

@Composable
private fun Legend() = Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
    listOf("Available" to Color.White, "Selected" to Brand.colors.accent, "Booked" to N.Booked, "Ladies" to N.Ladies, "Blocked" to N.Blocked).forEach { (l, c) ->
        Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(14.dp).clip(RoundedCornerShape(4.dp)).background(c).border(1.dp, N.Line, RoundedCornerShape(4.dp))); Text(" $l", fontSize = 12.sp) }
    }
}

@Composable
fun SeatGrid(map: SeatMap, deck: Deck, selected: List<String>, onSeat: (String) -> Unit) {
    val seats = map.deck(deck)
    val sleeper = map.type == SeatLayoutType.SLEEPER_1_1
    val w = if (sleeper) 56.dp else 40.dp; val h = if (sleeper) 72.dp else 40.dp
    Column(Modifier.width(IntrinsicSize.Max).clip(RoundedCornerShape(22.dp)).border(1.5.dp, N.Line, RoundedCornerShape(22.dp)).background(Color.White).padding(14.dp)) {
        Row(Modifier.width(w * map.type.totalCols + 8.dp * (map.type.totalCols - 1)), horizontalArrangement = Arrangement.SpaceBetween) {
            IconLabel(Icons.Filled.SensorDoor, "Door"); IconLabel(if (deck == Deck.LOWER) Icons.Filled.AirlineSeatReclineNormal else Icons.Filled.Layers, if (deck == Deck.LOWER) "Driver" else "Upper deck")
        }
        HorizontalDivider(Modifier.padding(vertical = 8.dp), color = N.Line)
        seats.groupBy { it.row }.toSortedMap().values.forEach { row ->
            Row(Modifier.padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (0 until map.type.totalCols).forEach { c ->
                    val seat = row.firstOrNull { it.col == c }
                    if (seat == null) Spacer(Modifier.size(w, h)) else {
                        val sel = seat.id in selected
                        val (bg, fg) = when {
                            sel -> Brand.colors.accent to Color.White
                            seat.state == SeatState.BOOKED -> N.Booked to N.Muted
                            seat.state == SeatState.BLOCKED -> N.Blocked to Color.White
                            seat.state == SeatState.LADIES -> N.Ladies to N.LadiesInk
                            else -> Color.White to N.Ink
                        }
                        Box(Modifier.size(w, h).clip(RoundedCornerShape(9.dp)).background(bg).border(1.dp, if (seat.state == SeatState.AVAILABLE && !sel) N.Booked else bg, RoundedCornerShape(9.dp)).clickable { onSeat(seat.id) }, contentAlignment = Alignment.Center) {
                            Text(if (seat.state == SeatState.BLOCKED) "×" else seat.id, color = fg, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        HorizontalDivider(Modifier.padding(vertical = 8.dp), color = N.Line)
        IconLabel(Icons.Filled.Wc, "Toilet")
    }
}

@Composable
fun BoardingScreen(s: BusState, boarding: List<Point>, dropping: List<Point>, onBack: () -> Unit, onBoarding: (Point) -> Unit, onDropping: (Point) -> Unit, onContinue: () -> Unit) {
    var tab by remember { mutableIntStateOf(0) }
    Screen(title = "Boarding & dropping", onBack = onBack, bottomBar = {
        AccentButton(if (tab == 0 && s.dropping == null) "Next: dropping point" else "Continue", { if (tab == 0 && s.dropping == null) tab = 1 else onContinue() }, enabled = if (tab == 0) s.boarding != null else s.boarding != null && s.dropping != null)
    }) {
        ChoiceChips(listOf("Boarding · ${s.boarding?.name ?: "select"}", "Dropping · ${s.dropping?.name ?: "select"}"), tab, { tab = it })
        val pts = if (tab == 0) boarding else dropping; val sel = if (tab == 0) s.boarding else s.dropping
        pts.forEach { p ->
            MCard(onClick = { if (tab == 0) onBoarding(p) else onDropping(p) }, border = if (p == sel) Brand.colors.primary else N.Line, pad = 12.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(p.time, fontWeight = FontWeight.Bold, color = Brand.colors.primary, modifier = Modifier.width(56.dp))
                    Column(Modifier.weight(1f)) { Text(p.name, style = MaterialTheme.typography.titleSmall); Text(p.address, fontSize = 12.sp, color = N.Muted) }
                    RadioButton(p == sel, { if (tab == 0) onBoarding(p) else onDropping(p) })
                }
            }
        }
    }
}

@Composable
fun PassengerDetailsScreen(s: BusState, onBack: () -> Unit, onChange: (Int, PassengerForm) -> Unit, onContinue: () -> Unit) {
    val valid = s.passengersForm.isNotEmpty() && s.passengersForm.all { it.name.isNotBlank() } && s.passengersForm.first().phone.length == 11
    Screen(title = "Passenger details", onBack = onBack, bottomBar = { TotalBar("${s.selected.size} seat · ${s.selected.joinToString()}", taka(s.fare.seatFare), "Continue", valid, onContinue) }) {
        s.passengersForm.forEachIndexed { i, p ->
            MCard {
                Text("Seat ${s.selected.getOrNull(i)} · Passenger ${i + 1}", fontWeight = FontWeight.Bold, color = Brand.colors.primary)
                OutlinedTextField(p.name, { onChange(i, p.copy(name = it)) }, Modifier.fillMaxWidth(), label = { Text("Full name") }, singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(p.age, { onChange(i, p.copy(age = it.filter(Char::isDigit).take(3))) }, Modifier.width(90.dp), label = { Text("Age") }, singleLine = true)
                    ChoiceChips(listOf("Male", "Female"), p.gender.ordinal, { onChange(i, p.copy(gender = Gender.entries[it])) })
                }
                if (i == 0) OutlinedTextField(p.phone, { onChange(i, p.copy(phone = it.filter(Char::isDigit).take(11))) }, Modifier.fillMaxWidth(), label = { Text("Mobile (e-ticket by SMS)") }, singleLine = true)
            }
        }
        MCard(color = Brand.colors.primarySoft, border = null) {
            KV("Boarding", "${s.boarding?.name} · ${s.boarding?.time}"); KV("Dropping", s.dropping?.name ?: "-")
        }
    }
}

@Composable
fun BusPaymentScreen(s: BusState, onBack: () -> Unit, onMethod: (PayMethod) -> Unit, onPay: () -> Unit) {
    val f = s.fare; val t = s.trip ?: return
    Screen(title = "Payment", onBack = onBack, bottomBar = { AccentButton("Pay ${taka(f.total)}", onPay) }) {
        MCard {
            Row { Text("${s.from} → ${s.to}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f)); Text(s.dateLabel, fontSize = 12.sp, color = N.Muted) }
            Text("${t.name} · Seat ${s.selected.joinToString()} · ${s.passengersForm.firstOrNull()?.name}", fontSize = 12.sp, color = N.Muted)
            HorizontalDivider(Modifier.padding(vertical = 8.dp), color = N.Line)
            KV("Fare", taka(f.seatFare)); KV("Service fee", taka(f.serviceFee)); if (f.discount > 0) KV("bKash offer", taka(-f.discount), N.Success)
            HorizontalDivider(Modifier.padding(vertical = 4.dp), color = N.Line); KV("Total", taka(f.total), Brand.colors.primary, true)
        }
        PaymentMethodList(s.pay, onMethod)
    }
}

@Composable
fun ETicketScreen(s: BusState, onDownload: () -> Unit, onTickets: () -> Unit) {
    val b = s.ticket ?: return; val t = s.trip ?: return; val m = LocalMerchant.current
    Screen(bottomBar = { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { PrimaryButton("Download PDF", onDownload, Modifier.weight(1f)); AccentButton("Go to My Tickets", onTickets, Modifier.weight(1f)) } }) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            SuccessBadge(); Text("Booking Confirmed!", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 8.dp))
            Text("E-ticket sent to +88 ${s.passengersForm.firstOrNull()?.phone}", fontSize = 12.sp, color = N.Muted)
        }
        Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Row(Modifier.fillMaxWidth().background(m.colors.primary).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text(m.merchantName, color = Color.White, fontWeight = FontWeight.Bold); Text("PNR ${b.id}", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp) }
                Text("Seat ${s.selected.joinToString()}", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column { Text(t.dep, style = MaterialTheme.typography.headlineSmall); Text("${s.from} · ${s.boarding?.name}", fontSize = 11.sp, color = N.Muted) }
                    Text(s.dateLabel, Modifier.weight(1f), fontSize = 12.sp, color = N.Muted, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Column(horizontalAlignment = Alignment.End) { Text(t.arr, style = MaterialTheme.typography.headlineSmall); Text(s.to, fontSize = 11.sp, color = N.Muted) }
                }
                HorizontalDivider(Modifier.padding(vertical = 12.dp), color = N.Line)
                QrCode(b.id)
                Text("Show this QR at boarding", fontSize = 12.sp, color = N.Muted, modifier = Modifier.padding(top = 6.dp))
                Spacer(Modifier.height(8.dp)); KV("Coach", t.coach); KV("Paid", "${taka(b.amount)} · ${b.paidVia}", bold = true)
            }
        }
    }
}

@Composable
private fun IconLabel(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) = Row(verticalAlignment = Alignment.CenterVertically) {
    Icon(icon, null, tint = N.Muted, modifier = Modifier.size(16.dp)); Text(" $text", fontSize = 12.sp, color = N.Muted)
}
