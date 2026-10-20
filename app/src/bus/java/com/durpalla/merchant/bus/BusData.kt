package com.durpalla.merchant.bus

import com.durpalla.core.booking.SeatLayoutType
import com.durpalla.core.config.Vertical
import com.durpalla.core.model.*
import com.durpalla.merchant.R

data class PopularRoute(val from: String, val to: String, val fromFare: Int, val img: Int)
data class BusTrip(
    val id: String, val name: String, val coach: String, val layout: SeatLayoutType, val ac: Boolean,
    val dep: String, val arr: String, val duration: String, val fare: Int, val seatsLeft: Int, val amenities: List<String>, val img: Int,
)
data class Point(val name: String, val time: String, val address: String)

/** Repository contract for bus search. Swap [FakeBusRepository] for an API-backed one (see README). */
interface BusRepository {
    val cities: List<String>
    val popular: List<PopularRoute>
    fun search(from: String, to: String, date: String): List<BusTrip>
    fun boarding(tripId: String): List<Point>
    fun dropping(tripId: String): List<Point>
}

object BusData {
    val seedBookings = listOf(
        Booking("GL-2610-46313", Vertical.BUS, "Dhaka → Cox's Bazar", "Green Line Business AC · Coach GL-1206", "Fri, 24 Oct · 07:30",
            listOf("Seats" to "C2", "Boarding" to "Kalabagan, 07:15", "Dropping" to "Kolatoli", "Passenger" to "Jewel Rana"), 1050, "bKash", BookingStatus.UPCOMING, R.drawable.img_bus),
        Booking("GL-2609-41120", Vertical.BUS, "Dhaka → Chattogram", "Green Line Economy · Coach GL-2304", "Mon, 29 Sep · 10:00",
            listOf("Seats" to "D3, D4", "Boarding" to "Sayedabad", "Dropping" to "GEC Circle"), 1660, "Nagad", BookingStatus.COMPLETED, R.drawable.img_ctg),
        Booking("GL-2608-38002", Vertical.BUS, "Dhaka → Sylhet", "Green Line Sleeper · Coach GL-408", "Sat, 16 Aug · 22:30",
            listOf("Seats" to "LA1"), 2530, "Card", BookingStatus.CANCELLED, R.drawable.img_tea),
    )
    val offers = listOf(
        Offer("BKASH15", "15% OFF with bKash", "Up to ৳150 off on any route", "31 Oct"),
        Offer("GLNEW", "৳100 off your first trip", "New Green Line app users", "31 Dec"),
        Offer("COXS200", "Cox's Bazar weekend deal", "৳200 off Business AC", "30 Nov"),
    )
}

class FakeBusRepository : BusRepository {
    override val cities = listOf("Dhaka", "Cox's Bazar", "Chattogram", "Sylhet", "Rajshahi", "Khulna", "Barishal")
    override val popular = listOf(
        PopularRoute("Dhaka", "Cox's Bazar", 1200, R.drawable.img_beach),
        PopularRoute("Dhaka", "Chattogram", 800, R.drawable.img_ctg),
        PopularRoute("Dhaka", "Sylhet", 900, R.drawable.img_tea),
    )
    override fun search(from: String, to: String, date: String) = listOf(
        BusTrip("t1", "Green Line Business AC", "Coach GL-1206", SeatLayoutType.BUSINESS_2_1, true, "07:30", "17:30", "10h 0m", 1200, 12, listOf("AC", "Wi-Fi", "USB"), R.drawable.img_biz),
        BusTrip("t2", "Green Line Economy", "Coach GL-2304", SeatLayoutType.ECONOMY_2_2, false, "10:00", "20:00", "10h 0m", 800, 23, listOf("Water", "Blanket"), R.drawable.img_eco),
        BusTrip("t3", "Green Line Sleeper", "Coach GL-408", SeatLayoutType.SLEEPER_1_1, true, "22:30", "08:00", "9h 30m", 2500, 4, listOf("AC", "Wi-Fi", "USB", "Blanket"), R.drawable.img_sleep),
        BusTrip("t4", "Green Line Business AC", "Coach GL-1210", SeatLayoutType.BUSINESS_2_1, true, "23:00", "08:30", "9h 30m", 1300, 7, listOf("AC", "USB"), R.drawable.img_bus2),
    )
    override fun boarding(tripId: String) = listOf(
        Point("Kalabagan", "07:15", "Green Line counter, Mirpur Rd"), Point("Arambagh", "07:30", "Motijheel"), Point("Sayedabad", "07:50", "Bus terminal"), Point("Jatrabari", "08:05", "Flyover gate"),
    )
    override fun dropping(tripId: String) = listOf(Point("Kolatoli", "17:30", "Hotel Motel Zone"), Point("Dolphin More", "17:20", "Kolatoli Rd"), Point("Bus Terminal", "17:00", "Cox's Bazar"))
}
