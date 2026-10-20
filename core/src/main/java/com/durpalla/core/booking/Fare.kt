package com.durpalla.core.booking

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

fun taka(v: Int): String = (if (v < 0) "-৳" else "৳") + NumberFormat.getIntegerInstance(Locale.US).format(kotlin.math.abs(v))

data class BusFare(val seatFare: Int, val serviceFee: Int, val discount: Int) { val total get() = seatFare + serviceFee - discount }

data class HotelFare(val nights: Int, val rooms: Int, val rate: Int, val roomTotal: Int, val serviceCharge: Int, val vat: Int) {
    val total get() = roomTotal + serviceCharge + vat
}

object FareCalculator {
    const val BUS_SERVICE_FEE_PER_SEAT = 30
    const val HOTEL_SERVICE_RATE = 0.10
    const val HOTEL_VAT_RATE = 0.15

    fun bus(seatFares: List<Int>, discount: Int = 0): BusFare {
        val sub = seatFares.sum()
        val fee = BUS_SERVICE_FEE_PER_SEAT * seatFares.size
        return BusFare(sub, fee, discount.coerceIn(0, sub))
    }

    /** Bangladesh hotel billing: 10% service charge on room total, 15% VAT on (room total + service charge). */
    fun hotel(rate: Int, nights: Int, rooms: Int = 1): HotelFare {
        require(nights >= 1 && rooms >= 1)
        val roomTotal = rate * nights * rooms
        val service = (roomTotal * HOTEL_SERVICE_RATE).roundToInt()
        val vat = ((roomTotal + service) * HOTEL_VAT_RATE).roundToInt()
        return HotelFare(nights, rooms, rate, roomTotal, service, vat)
    }
}
