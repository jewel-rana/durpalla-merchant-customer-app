package com.durpalla.merchant.hotel

import androidx.lifecycle.ViewModel
import com.durpalla.core.booking.FareCalculator
import com.durpalla.core.booking.HotelFare
import com.durpalla.core.booking.taka
import com.durpalla.core.config.Vertical
import com.durpalla.core.data.AccountRepository
import com.durpalla.core.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class HotelState(
    val checkIn: LocalDate = LocalDate.now().plusDays(4), val nights: Int = 3, val adults: Int = 2, val children: Int = 0, val rooms: Int = 1,
    val filter: Int = 0, val room: Room? = null, val plan: RatePlan? = null, val pay: PayMethod = PayMethod.BKASH,
    val guestName: String = "Jewel Rana", val guestPhone: String = "01712345678", val request: String = "", val confirmed: Booking? = null,
) {
    val checkOut: LocalDate get() = checkIn.plusDays(nights.toLong())
    val fare: HotelFare? get() = plan?.let { FareCalculator.hotel(it.rate, nights, rooms) }
    fun fmt(d: LocalDate): String = d.format(DateTimeFormatter.ofPattern("EEE, dd MMM", Locale.US))
    val datesLabel get() = "${fmt(checkIn)} – ${fmt(checkOut)}"
    val guestsLabel get() = "$adults Adult${if (adults > 1) "s" else ""}${if (children > 0) " · $children Child" else ""} · $rooms Room${if (rooms > 1) "s" else ""}"
}

class HotelViewModel(private val repo: HotelRepository, private val account: AccountRepository) : ViewModel() {
    private val _s = MutableStateFlow(HotelState()); val state: StateFlow<HotelState> = _s
    val rooms get() = repo.rooms
    val filters = listOf("All", "Sea view", "Breakfast", "Free cancel")
    fun filtered(s: HotelState) = if (s.filter == 0) repo.rooms else repo.rooms.filter { filters[s.filter] in it.tags }

    fun shiftCheckIn(days: Long) = _s.update { it.copy(checkIn = maxOf(LocalDate.now(), it.checkIn.plusDays(days))) }
    fun setNights(n: Int) = _s.update { it.copy(nights = n.coerceIn(1, 30)) }
    fun setGuests(adults: Int, children: Int, rooms: Int) = _s.update { it.copy(adults = adults.coerceIn(1, 12), children = children.coerceIn(0, 6), rooms = rooms.coerceIn(1, 5)) }
    fun setFilter(i: Int) = _s.update { it.copy(filter = i) }
    fun openRoom(id: String) = _s.update { s -> val r = repo.room(id); s.copy(room = r, plan = r?.plans?.first()) }
    fun setPlan(p: RatePlan) = _s.update { it.copy(plan = p) }
    fun setPay(m: PayMethod) = _s.update { it.copy(pay = m) }
    fun setGuest(name: String, phone: String, req: String) = _s.update { it.copy(guestName = name, guestPhone = phone.filter(Char::isDigit).take(11), request = req) }

    fun pay() {
        val s = _s.value; val r = s.room ?: return; val f = s.fare ?: return; val p = s.plan ?: return
        val b = Booking("SP-${(100000..999999).random()}", Vertical.HOTEL, r.name, "Sea Pearl Resort · ${s.guestsLabel}", "${s.datesLabel} · ${s.nights} nights",
            listOf("Check-in" to "${s.fmt(s.checkIn)} · 2:00 PM", "Check-out" to "${s.fmt(s.checkOut)} · 12:00 PM", "Rate plan" to p.name, "Guest" to s.guestName,
                "${s.nights} nights × ${s.rooms} room × ${taka(p.rate)}" to taka(f.roomTotal), "Service charge (10%)" to taka(f.serviceCharge), "VAT (15%)" to taka(f.vat)),
            f.total, s.pay.label, BookingStatus.UPCOMING, r.photos.first())
        account.addBooking(b); _s.update { it.copy(confirmed = b) }
    }
}
