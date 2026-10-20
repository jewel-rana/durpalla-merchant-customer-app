package com.durpalla.merchant.bus

import androidx.lifecycle.ViewModel
import com.durpalla.core.booking.*
import com.durpalla.core.config.Vertical
import com.durpalla.core.data.AccountRepository
import com.durpalla.core.model.*
import com.durpalla.merchant.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class PassengerForm(val name: String = "", val phone: String = "", val gender: Gender = Gender.MALE, val age: String = "")

data class BusState(
    val from: String = "Dhaka", val to: String = "Cox's Bazar", val dateIndex: Int = 0, val passengers: Int = 1,
    val results: List<BusTrip> = emptyList(), val filter: Int = 0,
    val trip: BusTrip? = null, val map: SeatMap? = null, val selected: List<String> = emptyList(), val deck: Deck = Deck.LOWER,
    val travellerGender: Gender = Gender.MALE, val seatError: String? = null,
    val boarding: Point? = null, val dropping: Point? = null,
    val passengersForm: List<PassengerForm> = emptyList(), val pay: PayMethod = PayMethod.BKASH, val promo: Int = 0,
    val ticket: Booking? = null,
) {
    val dateLabel get() = listOf("Today", "Tomorrow", "Fri, 24 Oct")[dateIndex]
    val filtered get() = when (filter) { 1 -> results.filter { it.ac }; 2 -> results.filter { !it.ac }; 3 -> results.filter { it.layout == SeatLayoutType.SLEEPER_1_1 }; else -> results }
    val fare get() = FareCalculator.bus(selected.mapNotNull { map?.seat(it)?.fare }, promo)
}

class BusViewModel(private val repo: BusRepository, private val account: AccountRepository) : ViewModel() {
    private val _s = MutableStateFlow(BusState()); val state: StateFlow<BusState> = _s
    val popular get() = repo.popular
    val cities get() = repo.cities
    fun boardingPoints() = repo.boarding(_s.value.trip?.id ?: "")
    fun droppingPoints() = repo.dropping(_s.value.trip?.id ?: "")

    fun setFrom(c: String) = _s.update { it.copy(from = c) }
    fun setTo(c: String) = _s.update { it.copy(to = c) }
    fun swap() = _s.update { it.copy(from = it.to, to = it.from) }
    fun setDate(i: Int) = _s.update { it.copy(dateIndex = i) }
    fun setPassengers(n: Int) = _s.update { it.copy(passengers = n.coerceIn(1, SeatRules.MAX_SEATS)) }
    fun pickRoute(r: PopularRoute) = _s.update { it.copy(from = r.from, to = r.to) }
    fun search() = _s.update { it.copy(results = repo.search(it.from, it.to, it.dateLabel), filter = 0) }
    fun setFilter(i: Int) = _s.update { it.copy(filter = i) }

    fun selectTrip(t: BusTrip) = _s.update { it.copy(trip = t, map = SeatRules.demo(t.layout, t.fare), selected = emptyList(), deck = Deck.LOWER, seatError = null) }
    fun switchLayout(type: SeatLayoutType) = _s.update { s ->
        val t = s.results.firstOrNull { it.layout == type } ?: s.trip ?: return@update s
        s.copy(trip = t, map = SeatRules.demo(type, t.fare), selected = emptyList(), deck = Deck.LOWER, seatError = null)
    }
    fun setDeck(d: Deck) = _s.update { it.copy(deck = d) }
    fun setGender(g: Gender) = _s.update { it.copy(travellerGender = g, selected = if (g == Gender.MALE) it.selected.filter { id -> it.map?.seat(id)?.state != SeatState.LADIES } else it.selected) }
    fun toggleSeat(id: String) = _s.update { s ->
        val map = s.map ?: return@update s
        when (val r = SeatRules.toggle(map, s.selected, id, s.travellerGender, SeatRules.MAX_SEATS)) {
            is SeatToggle.Ok -> s.copy(selected = r.selected, seatError = null)
            is SeatToggle.Rejected -> s.copy(seatError = r.reason)
        }
    }
    fun clearSeatError() = _s.update { it.copy(seatError = null) }
    fun setBoarding(p: Point) = _s.update { it.copy(boarding = p) }
    fun setDropping(p: Point) = _s.update { it.copy(dropping = p) }
    fun preparePassengers() = _s.update { s ->
        val list = s.selected.mapIndexed { i, _ -> s.passengersForm.getOrNull(i) ?: if (i == 0) PassengerForm("Jewel Rana", "01712345678", s.travellerGender, "31") else PassengerForm(gender = s.travellerGender) }
        s.copy(passengersForm = list)
    }
    fun updatePassenger(i: Int, p: PassengerForm) = _s.update { s -> s.copy(passengersForm = s.passengersForm.toMutableList().also { it[i] = p }) }
    fun setPay(m: PayMethod) = _s.update { it.copy(pay = m, promo = if (m == PayMethod.BKASH) minOf(150, (it.fare.seatFare * 0.15).toInt()) else 0) }

    fun pay() {
        val s = _s.value; val t = s.trip ?: return
        val b = Booking(
            "GL-${(100000..999999).random()}", Vertical.BUS, "${s.from} → ${s.to}", "${t.name} · ${t.coach}", "${s.dateLabel} · ${t.dep}",
            listOf("Seats" to s.selected.joinToString(), "Boarding" to "${s.boarding?.name}, ${s.boarding?.time}", "Dropping" to (s.dropping?.name ?: "-"),
                "Passenger" to (s.passengersForm.firstOrNull()?.name ?: "-"), "Fare" to taka(s.fare.seatFare), "Service fee" to taka(s.fare.serviceFee), "Discount" to taka(-s.fare.discount)),
            s.fare.total, s.pay.label, BookingStatus.UPCOMING, t.img,
        )
        account.addBooking(b)
        _s.update { it.copy(ticket = b) }
    }
}
