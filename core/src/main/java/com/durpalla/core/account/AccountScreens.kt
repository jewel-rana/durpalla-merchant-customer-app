package com.durpalla.core.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.durpalla.core.booking.taka
import com.durpalla.core.config.LocalMerchant
import com.durpalla.core.model.*
import com.durpalla.core.theme.Brand
import com.durpalla.core.theme.N
import com.durpalla.core.ui.*

fun BookingStatus.tone() = when (this) { BookingStatus.UPCOMING -> Tone.SUCCESS; BookingStatus.COMPLETED -> Tone.NEUTRAL; BookingStatus.CANCELLED -> Tone.DANGER }

@Composable
fun BookingRow(b: Booking, onClick: () -> Unit) = MCard(onClick = onClick, pad = 12.dp) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        b.imageRes?.let { Photo(it, Modifier.size(64.dp)) }
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(b.title, style = MaterialTheme.typography.titleSmall, maxLines = 1)
            Text(b.dateLabel, color = N.Muted, fontSize = 12.sp)
            Text("View details", color = Brand.colors.primary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Pill(b.status.label.uppercase(), b.status.tone())
    }
}

@Composable
fun DashboardScreen(user: User, bookings: List<Booking>, onBookings: () -> Unit, onBooking: (Booking) -> Unit, onOffers: () -> Unit, onNotifications: () -> Unit, onProfile: () -> Unit) {
    val m = LocalMerchant.current
    Screen(title = "My account", actions = { IconButton(onClick = onNotifications) { BadgedBox({ Badge(containerColor = m.colors.accent) }) { Icon(Icons.Filled.Notifications, "Notifications") } } }) {
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Brush.linearGradient(listOf(m.colors.primary, m.colors.primaryDark))).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(user.name, 52.dp)
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text(user.name, color = Color.White, style = MaterialTheme.typography.titleLarge); Text(user.phone, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                }
                TextButton(onProfile) { Text("Edit", color = Color.White) }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Upcoming" to bookings.count { it.status == BookingStatus.UPCOMING }, "Completed" to bookings.count { it.status == BookingStatus.COMPLETED }, "Points" to user.points).forEach { (k, v) ->
                    Column(Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.14f)).padding(10.dp)) {
                        Text("$v", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp); Text(k, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    }
                }
            }
        }
        SectionTitle("Next booking", "All bookings", onBookings)
        bookings.firstOrNull { it.status == BookingStatus.UPCOMING }?.let { BookingRow(it) { onBooking(it) } } ?: EmptyState("No upcoming bookings")
        MCard(color = m.colors.accentSoft, border = null, onClick = onOffers) {
            Text("Offers for you", fontWeight = FontWeight.Bold, color = m.colors.accent); Text("15% OFF with bKash · code BKASH15", fontSize = 13.sp)
        }
    }
}

@Composable
fun BookingsScreen(bookings: List<Booking>, onBooking: (Booking) -> Unit) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    Screen(title = "My bookings") {
        ChoiceChips(listOf("Upcoming", "Completed", "Cancelled"), tab, { tab = it })
        val shown = bookings.filter { it.status == BookingStatus.entries[tab] }
        if (shown.isEmpty()) EmptyState("Nothing here yet")
        shown.forEach { BookingRow(it) { onBooking(it) } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailsScreen(b: Booking, onBack: () -> Unit, onInvoice: () -> Unit, onCancel: () -> Unit, onSupport: () -> Unit) {
    var confirm by remember { mutableStateOf(false) }
    Screen(title = "Booking details", onBack = onBack, bottomBar = {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlineButton("Invoice", onInvoice, Modifier.weight(1f))
            if (b.status == BookingStatus.UPCOMING) OutlineButton("Cancel", { confirm = true }, Modifier.weight(1f), N.Danger)
        }
    }) {
        b.imageRes?.let { Photo(it, Modifier.fillMaxWidth().height(150.dp)) }
        MCard {
            Row { Text(b.title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f)); Pill(b.status.label, b.status.tone()) }
            Text(b.subtitle, color = N.Muted, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp)); KV("Booking ID", b.id); b.detailLines.forEach { (k, v) -> KV(k, v) }
            HorizontalDivider(color = N.Line); KV("Paid via ${b.paidVia}", taka(b.amount), bold = true)
        }
        if (b.status == BookingStatus.UPCOMING) MCard(Modifier, pad = 12.dp) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                QrCode(b.id, Modifier.size(110.dp)); Text("Show this QR at check-in / boarding", Modifier.padding(start = 12.dp), color = N.Muted)
            }
        }
        MCard(onClick = onSupport) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Filled.SupportAgent, null, tint = Brand.colors.primary); Text("  Contact support", fontWeight = FontWeight.SemiBold) } }
    }
    if (confirm) ModalBottomSheet(onDismissRequest = { confirm = false }, containerColor = Color.White) {
        Column(Modifier.padding(24.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Cancel this booking?", style = MaterialTheme.typography.titleLarge)
            Text("Refund of ${taka((b.amount * 0.8).toInt())} (after 20% cancellation fee) goes back to ${b.paidVia} within 3–5 working days.", color = N.Muted)
            Button({ confirm = false; onCancel() }, Modifier.fillMaxWidth().height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = N.Danger), shape = RoundedCornerShape(14.dp)) { Text("Yes, cancel booking") }
            OutlineButton("Keep booking", { confirm = false })
        }
    }
}

@Composable
fun InvoiceScreen(b: Booking, user: User, onBack: () -> Unit, onShare: () -> Unit, onDownload: () -> Unit) {
    val m = LocalMerchant.current
    Screen(title = "Invoice", onBack = onBack, bottomBar = {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { OutlineButton("Share", onShare, Modifier.weight(1f)); AccentButton("Download PDF", onDownload, Modifier.weight(1f)) }
    }) {
        MCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) { Text(m.merchantName, style = MaterialTheme.typography.titleLarge, color = m.colors.primary); Text("Tax invoice", color = N.Muted, fontSize = 12.sp) }
                Pill(if (b.status == BookingStatus.CANCELLED) "REFUNDED" else "PAID", if (b.status == BookingStatus.CANCELLED) Tone.DANGER else Tone.SUCCESS)
            }
            Spacer(Modifier.height(8.dp)); KV("Invoice no.", "INV-${b.id.takeLast(6)}"); KV("Billed to", user.name); KV("Phone", user.phone); KV("Date", b.dateLabel)
            HorizontalDivider(Modifier.padding(vertical = 8.dp), color = N.Line)
            Text(b.title, fontWeight = FontWeight.SemiBold); b.detailLines.forEach { (k, v) -> KV(k, v) }
            HorizontalDivider(Modifier.padding(vertical = 8.dp), color = N.Line)
            KV("Total paid", taka(b.amount), bold = true); KV("Payment method", b.paidVia)
            Spacer(Modifier.height(8.dp))
            Text("Payment processed by Durpalla Limited on behalf of ${m.merchantName}.", fontSize = 11.sp, color = N.Muted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun OffersScreen(offers: List<Offer>, onCopy: (String) -> Unit) = Screen(title = "Offers") {
    offers.forEach { o ->
        MCard(color = Brand.colors.accentSoft, border = null) {
            Text(o.title, style = MaterialTheme.typography.titleMedium); Text(o.subtitle, color = N.Muted, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(o.code, fontWeight = FontWeight.Bold, color = Brand.colors.accent, modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color.White).padding(horizontal = 10.dp, vertical = 6.dp))
                Text("  Valid till ${o.validTill}", fontSize = 12.sp, color = N.Muted, modifier = Modifier.weight(1f))
                TextButton({ onCopy(o.code) }) { Text("Copy") }
            }
        }
    }
}

@Composable
fun NotificationsScreen(list: List<Notice>, onBack: () -> Unit) = Screen(title = "Notifications", onBack = onBack) {
    list.forEach { n ->
        MCard(color = if (n.unread) Brand.colors.primarySoft else Color.White) {
            Row { Text(n.title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f)); Text(n.time, fontSize = 11.sp, color = N.Muted) }
            Text(n.body, fontSize = 13.sp, color = N.Muted)
        }
    }
}

@Composable
fun ProfileScreen(user: User, onEdit: () -> Unit, onTravellers: () -> Unit, onPayments: () -> Unit, onLanguage: () -> Unit, onHelp: () -> Unit, onNotifications: () -> Unit, onDashboard: () -> Unit, onLogout: () -> Unit) {
    val m = LocalMerchant.current
    Screen(title = "Account") {
        MCard(onClick = onEdit) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(user.name, 56.dp)
                Column(Modifier.weight(1f).padding(start = 12.dp)) { Text(user.name, style = MaterialTheme.typography.titleLarge); Text("${user.phone} · ${user.email}", color = N.Muted, fontSize = 12.sp) }
                Icon(Icons.Filled.Edit, null, tint = Brand.colors.primary)
            }
        }
        MCard {
            MenuRow(Icons.Filled.Dashboard, "Dashboard", onClick = onDashboard)
            MenuRow(Icons.Filled.Group, "Saved travellers", onClick = onTravellers)
            MenuRow(Icons.Filled.CreditCard, "Payment methods", onClick = onPayments)
            MenuRow(Icons.Filled.Notifications, "Notifications", onClick = onNotifications)
            MenuRow(Icons.Filled.Language, "Language", "English", onClick = onLanguage)
            MenuRow(Icons.AutoMirrored.Filled.Help, "Help & support", onClick = onHelp)
            MenuRow(Icons.AutoMirrored.Filled.Logout, "Log out", onClick = onLogout)
        }
        Text("${m.merchantName} · Powered by Durpalla Limited", fontSize = 11.sp, color = N.Muted, modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
fun EditProfileScreen(user: User, onBack: () -> Unit, onSave: (String, String) -> Unit) {
    var name by remember { mutableStateOf(user.name) }; var email by remember { mutableStateOf(user.email) }
    Screen(title = "Edit profile", onBack = onBack, bottomBar = { AccentButton("Save changes", { onSave(name, email) }, enabled = name.isNotBlank()) }) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { Avatar(name, 88.dp) }
        OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Full name") }, singleLine = true)
        OutlinedTextField(user.phone, {}, Modifier.fillMaxWidth(), label = { Text("Mobile (verified)") }, enabled = false)
        OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth(), label = { Text("Email") }, singleLine = true)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedTravellersScreen(list: List<Traveller>, onBack: () -> Unit, onAdd: (String, String, Int) -> Unit) {
    var adding by remember { mutableStateOf(false) }
    Screen(title = "Saved travellers", onBack = onBack, bottomBar = { AccentButton("+ Add traveller", { adding = true }) }) {
        list.forEach { t -> MCard(pad = 12.dp) { Row(verticalAlignment = Alignment.CenterVertically) { Avatar(t.name); Column(Modifier.padding(start = 12.dp)) { Text(t.name, style = MaterialTheme.typography.titleSmall); Text("${t.gender} · ${t.age} yrs", color = N.Muted, fontSize = 12.sp) } } } }
    }
    if (adding) {
        var name by remember { mutableStateOf("") }; var age by remember { mutableStateOf("") }; var female by remember { mutableStateOf(false) }
        ModalBottomSheet({ adding = false }, containerColor = Color.White) {
            Column(Modifier.padding(24.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("New traveller", style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Name") })
                OutlinedTextField(age, { age = it.filter(Char::isDigit).take(3) }, Modifier.fillMaxWidth(), label = { Text("Age") })
                ChoiceChips(listOf("Male", "Female"), if (female) 1 else 0, { female = it == 1 })
                AccentButton("Save", { onAdd(name, if (female) "Female" else "Male", age.toIntOrNull() ?: 0); adding = false }, enabled = name.isNotBlank())
            }
        }
    }
}

@Composable
fun PaymentMethodsScreen(list: List<SavedPayment>, onBack: () -> Unit) = Screen(title = "Payment methods", onBack = onBack, bottomBar = { OutlineButton("+ Add payment method", {}) }) {
    list.forEach { p ->
        MCard(pad = 12.dp) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Color(p.method.color)), contentAlignment = Alignment.Center) { Text(p.method.badge, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) { Text(p.method.label, style = MaterialTheme.typography.titleSmall); Text(p.masked, color = N.Muted, fontSize = 12.sp) }
                if (p.default) Pill("Default", Tone.SUCCESS)
            }
        }
    }
    Text("Cards are tokenised by Durpalla Limited; the app never stores card numbers.", fontSize = 12.sp, color = N.Muted)
}

@Composable
fun LanguageScreen(onBack: () -> Unit) {
    var sel by rememberSaveable { mutableIntStateOf(0) }
    Screen(title = "Language", onBack = onBack) {
        listOf("English", "বাংলা (Bangla)").forEachIndexed { i, l ->
            MCard(onClick = { sel = i }, border = if (i == sel) Brand.colors.primary else N.Line, pad = 12.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) { Text(l, Modifier.weight(1f), fontWeight = FontWeight.SemiBold); RadioButton(i == sel, { sel = i }) }
            }
        }
    }
}

@Composable
fun HelpScreen(onBack: () -> Unit) {
    val m = LocalMerchant.current
    val faqs = listOf(
        "How do I cancel?" to "Open the booking > Cancel. Refunds go back to the original payment method in 3–5 working days.",
        "I paid but didn't get a ticket" to "Payments are confirmed by Durpalla Limited within 2 minutes. Check My bookings or call support.",
        "Can I change my date?" to "Date changes depend on ${m.merchantName}'s policy. Contact support with your booking ID.",
    )
    Screen(title = "Help & support", onBack = onBack) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { AccentButton("Call ${m.supportPhone}", {}, Modifier.weight(1f)); OutlineButton("Email us", {}, Modifier.weight(1f)) }
        faqs.forEach { (q, a) ->
            var open by remember { mutableStateOf(false) }
            MCard(onClick = { open = !open }) { Text(q, fontWeight = FontWeight.SemiBold); if (open) Text(a, color = N.Muted, modifier = Modifier.padding(top = 6.dp)) }
        }
    }
}
