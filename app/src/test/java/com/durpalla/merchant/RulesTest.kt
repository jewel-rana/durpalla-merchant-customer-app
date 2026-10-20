package com.durpalla.merchant

import com.durpalla.core.booking.*
import org.junit.Assert.*
import org.junit.Test

class SeatRulesTest {
    private val map = SeatMap(SeatLayoutType.BUSINESS_2_1, listOf(
        Seat("A1", 0, 0, SeatState.AVAILABLE, fare = 1200), Seat("A2", 0, 1, SeatState.BOOKED, fare = 1200),
        Seat("A3", 0, 3, SeatState.LADIES, fare = 1200), Seat("B1", 1, 0, SeatState.BLOCKED, fare = 1200),
        Seat("B2", 1, 1, SeatState.AVAILABLE, fare = 1200), Seat("B3", 1, 3, SeatState.AVAILABLE, fare = 1200),
        Seat("C1", 2, 0, SeatState.AVAILABLE, fare = 1200), Seat("C2", 2, 1, SeatState.AVAILABLE, fare = 1000),
    ))

    @Test fun selectAndDeselect() {
        val r = SeatRules.toggle(map, emptyList(), "A1") as SeatToggle.Ok
        assertEquals(listOf("A1"), r.selected)
        assertEquals(emptyList<String>(), (SeatRules.toggle(map, r.selected, "A1") as SeatToggle.Ok).selected)
    }
    @Test fun bookedAndBlockedRejected() {
        assertTrue(SeatRules.toggle(map, emptyList(), "A2") is SeatToggle.Rejected)
        assertTrue(SeatRules.toggle(map, emptyList(), "B1") is SeatToggle.Rejected)
        assertTrue(SeatRules.toggle(map, emptyList(), "Z9") is SeatToggle.Rejected)
    }
    @Test fun ladiesSeatOnlyForFemale() {
        assertTrue(SeatRules.toggle(map, emptyList(), "A3", Gender.MALE) is SeatToggle.Rejected)
        assertTrue(SeatRules.toggle(map, emptyList(), "A3", Gender.FEMALE) is SeatToggle.Ok)
    }
    @Test fun maxSeatsEnforced() {
        val four = listOf("A1", "B2", "B3", "C1")
        val r = SeatRules.toggle(map, four, "C2")
        assertTrue(r is SeatToggle.Rejected)
        assertTrue((r as SeatToggle.Rejected).reason.contains("${SeatRules.MAX_SEATS}"))
    }
    @Test fun totalSumsSelectedFares() = assertEquals(2200, SeatRules.total(map, listOf("A1", "C2")))

    @Test fun demoLayoutsHaveExpectedShape() {
        val sleeper = SeatRules.demo(SeatLayoutType.SLEEPER_1_1, 2500)
        assertEquals(2, sleeper.seats.map { it.deck }.distinct().size)
        assertEquals(sleeper.seats.size, sleeper.seats.map { it.id }.distinct().size)
        val eco = SeatRules.demo(SeatLayoutType.ECONOMY_2_2, 800)
        assertEquals(40, eco.seats.size)
        assertTrue(eco.seats.any { it.state == SeatState.AVAILABLE } && eco.seats.any { it.state == SeatState.BOOKED })
    }
}

class FareCalculatorTest {
    @Test fun hotelServiceChargeAndVat() {
        val f = FareCalculator.hotel(rate = 8500, nights = 3)
        assertEquals(25500, f.roomTotal)
        assertEquals(2550, f.serviceCharge)              // 10%
        assertEquals(4208, f.vat)                        // 15% of 28,050 = 4,207.5 -> 4,208
        assertEquals(32258, f.total)
    }
    @Test fun hotelMultipleRooms() = assertEquals(2 * 2 * 5000, FareCalculator.hotel(5000, 2, 2).roomTotal)
    @Test(expected = IllegalArgumentException::class) fun hotelZeroNightsInvalid() { FareCalculator.hotel(5000, 0) }

    @Test fun busFareWithServiceFeeAndDiscount() {
        val f = FareCalculator.bus(listOf(1200, 1200), discount = 150)
        assertEquals(2400, f.seatFare); assertEquals(60, f.serviceFee); assertEquals(2310, f.total)
    }
    @Test fun busDiscountNeverExceedsFare() = assertEquals(30, FareCalculator.bus(listOf(100), discount = 500).total)
    @Test fun takaFormat() { assertEquals("৳29,325", taka(29325)); assertEquals("-৳180", taka(-180)) }
}
