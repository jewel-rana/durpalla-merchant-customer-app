package com.durpalla.merchant.hotel

import com.durpalla.core.config.Vertical
import com.durpalla.core.model.*
import com.durpalla.merchant.R

data class RatePlan(val id: String, val name: String, val perks: String, val rate: Int, val refundable: Boolean)
data class Room(
    val id: String, val name: String, val size: String, val bed: String, val view: String, val maxGuests: Int, val rating: Double,
    val photos: List<Int>, val amenities: List<String>, val about: String, val plans: List<RatePlan>, val tags: Set<String>,
) { val fromRate get() = plans.minOf { it.rate } }

interface HotelRepository { val rooms: List<Room>; fun room(id: String): Room? }

object HotelData {
    val seedBookings = listOf(
        Booking("SP-2610-77310", Vertical.HOTEL, "Deluxe Sea View", "Sea Pearl Resort · 2 Adults · 1 Room", "24–27 Oct · 3 nights",
            listOf("Check-in" to "Fri, 24 Oct · 2:00 PM", "Check-out" to "Mon, 27 Oct · 12:00 PM", "Rate plan" to "Room + Breakfast"), 29325, "bKash", BookingStatus.UPCOMING, R.drawable.img_room1),
        Booking("SP-2609-61201", Vertical.HOTEL, "Family Villa", "Sea Pearl Resort · 4 Guests", "20–22 Sep · 2 nights",
            listOf("Check-in" to "20 Sep", "Check-out" to "22 Sep"), 55660, "Card", BookingStatus.COMPLETED, R.drawable.img_resort2),
        Booking("SP-2608-50017", Vertical.HOTEL, "Premier Suite", "Sea Pearl Resort · 2 Adults", "10–12 Aug · 2 nights",
            listOf("Check-in" to "10 Aug", "Check-out" to "12 Aug"), 35420, "Nagad", BookingStatus.CANCELLED, R.drawable.img_suite),
    )
    val offers = listOf(
        Offer("STAY3PAY2", "Stay 3, pay 2", "Deluxe Sea View, Sun–Thu", "30 Nov"),
        Offer("BKASH15", "15% OFF with bKash", "Up to ৳2,000 off", "31 Oct"),
        Offer("SPAFREE", "Free spa session", "On Premier Suite bookings", "31 Dec"),
    )
}

class FakeHotelRepository : HotelRepository {
    override val rooms = listOf(
        Room("deluxe", "Deluxe Sea View", "32 m²", "King bed", "Private balcony", 3, 4.8, listOf(R.drawable.img_room1, R.drawable.img_sea, R.drawable.img_beach),
            listOf("Free Wi-Fi", "AC", "Pool access", "Breakfast", "Smart TV"), "Wake up to panoramic views of the Bay of Bengal. Floor-to-ceiling windows, rain shower and a private balcony steps from the world's longest sea beach.",
            listOf(RatePlan("ro", "Room only", "Free cancellation till 48h", 7800, true), RatePlan("bb", "Room + Breakfast", "Buffet breakfast for 2", 8500, true), RatePlan("nr", "Non-refundable saver", "Breakfast · best price", 7200, false)),
            setOf("Sea view", "Breakfast", "Free cancel")),
        Room("suite", "Premier Suite", "55 m²", "King bed", "Living area · Bathtub", 3, 4.9, listOf(R.drawable.img_suite, R.drawable.img_pool, R.drawable.img_rest),
            listOf("Free Wi-Fi", "AC", "Bathtub", "Lounge access", "Breakfast"), "A spacious suite with a separate living room, soaking tub and lounge access.",
            listOf(RatePlan("bb", "Room + Breakfast", "Lounge access", 14000, true), RatePlan("hb", "Half board", "Breakfast + dinner", 16500, true)), setOf("Sea view", "Breakfast")),
        Room("villa", "Family Villa", "90 m²", "2 bedrooms", "Garden · Plunge pool", 6, 4.8, listOf(R.drawable.img_resort2, R.drawable.img_pool, R.drawable.img_room3),
            listOf("Free Wi-Fi", "AC", "Private pool", "Kitchenette", "Breakfast"), "Two-bedroom villa with a private plunge pool, ideal for families.",
            listOf(RatePlan("bb", "Villa + Breakfast", "Up to 6 guests", 22000, true)), setOf("Breakfast", "Free cancel")),
        Room("garden", "Garden Twin", "28 m²", "Twin beds", "Garden view", 2, 4.5, listOf(R.drawable.img_room3, R.drawable.img_resort),
            listOf("Free Wi-Fi", "AC"), "Cosy twin room overlooking the tropical gardens.", listOf(RatePlan("ro", "Room only", "Free cancellation", 5500, true)), setOf("Free cancel")),
    )
    override fun room(id: String) = rooms.firstOrNull { it.id == id }
}
