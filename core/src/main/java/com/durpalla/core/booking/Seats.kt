package com.durpalla.core.booking

enum class SeatState { AVAILABLE, BOOKED, LADIES, BLOCKED }
enum class Deck { LOWER, UPPER }

/** [col] is the visual column including the aisle gap (e.g. 2+1 uses cols 0,1,3). */
data class Seat(val id: String, val row: Int, val col: Int, val state: SeatState, val deck: Deck = Deck.LOWER, val fare: Int)

enum class SeatLayoutType(val label: String, val columns: List<Int>, val totalCols: Int, val decks: Int) {
    BUSINESS_2_1("Business AC (2+1)", listOf(0, 1, 3), 4, 1),
    ECONOMY_2_2("Economy (2+2)", listOf(0, 1, 3, 4), 5, 1),
    SLEEPER_1_1("Sleeper (1+1)", listOf(0, 2), 3, 2),
}

data class SeatMap(val type: SeatLayoutType, val seats: List<Seat>) {
    fun seat(id: String) = seats.firstOrNull { it.id == id }
    fun deck(d: Deck) = seats.filter { it.deck == d }
}

enum class Gender { MALE, FEMALE }

sealed interface SeatToggle {
    data class Ok(val selected: List<String>) : SeatToggle
    data class Rejected(val reason: String) : SeatToggle
}

/** Seat selection rules (shared by all bus merchants; the server re-validates on hold). */
object SeatRules {
    const val MAX_SEATS = 4

    fun toggle(map: SeatMap, selected: List<String>, seatId: String, gender: Gender = Gender.MALE, max: Int = MAX_SEATS): SeatToggle {
        val seat = map.seat(seatId) ?: return SeatToggle.Rejected("Unknown seat")
        if (seatId in selected) return SeatToggle.Ok(selected - seatId)
        return when {
            seat.state == SeatState.BOOKED -> SeatToggle.Rejected("Seat ${seat.id} is already booked")
            seat.state == SeatState.BLOCKED -> SeatToggle.Rejected("Seat ${seat.id} is not for sale")
            seat.state == SeatState.LADIES && gender != Gender.FEMALE -> SeatToggle.Rejected("Seat ${seat.id} is reserved for female passengers")
            selected.size >= max -> SeatToggle.Rejected("You can book up to $max seats at a time")
            else -> SeatToggle.Ok(selected + seatId)
        }
    }

    fun total(map: SeatMap, selected: List<String>): Int = selected.sumOf { map.seat(it)?.fare ?: 0 }

    /** Build a demo layout: rows A.., with a deterministic pattern of booked/ladies/blocked seats. */
    fun demo(type: SeatLayoutType, fare: Int, rows: Int = if (type == SeatLayoutType.SLEEPER_1_1) 6 else 10): SeatMap {
        val seats = mutableListOf<Seat>()
        for (deck in Deck.entries.take(type.decks)) for (r in 0 until rows) type.columns.forEachIndexed { ci, col ->
            val letter = 'A' + r
            val id = if (type.decks > 1) "${if (deck == Deck.LOWER) "L" else "U"}$letter${ci + 1}" else "$letter${ci + 1}"
            val k = (r * 7 + ci * 3 + deck.ordinal * 5) % 11
            val state = when {
                r == rows - 1 && ci == 0 -> SeatState.BLOCKED
                k == 0 || k == 4 || k == 7 -> SeatState.BOOKED
                k == 9 && ci == 0 -> SeatState.LADIES
                else -> SeatState.AVAILABLE
            }
            seats += Seat(id, r, col, state, deck, fare + if (deck == Deck.UPPER) -200 else 0)
        }
        return SeatMap(type, seats)
    }
}
