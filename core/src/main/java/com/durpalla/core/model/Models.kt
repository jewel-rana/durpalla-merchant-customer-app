package com.durpalla.core.model

import com.durpalla.core.config.Vertical

enum class BookingStatus(val label: String) { UPCOMING("Confirmed"), COMPLETED("Completed"), CANCELLED("Cancelled") }

data class Booking(
    val id: String, val vertical: Vertical, val title: String, val subtitle: String, val dateLabel: String,
    val detailLines: List<Pair<String, String>>, val amount: Int, val paidVia: String, val status: BookingStatus, val imageRes: Int? = null,
)

data class User(val name: String, val phone: String, val email: String, val memberSince: String, val points: Int)
data class Traveller(val id: String, val name: String, val gender: String, val age: Int, val phone: String)
data class SavedPayment(val id: String, val method: PayMethod, val masked: String, val default: Boolean)
data class Offer(val code: String, val title: String, val subtitle: String, val validTill: String)
data class Notice(val title: String, val body: String, val time: String, val unread: Boolean)

enum class PayMethod(val label: String, val badge: String, val color: Long) {
    BKASH("bKash", "bKash", 0xFFE2136E), NAGAD("Nagad", "Nagad", 0xFFF6921E), ROCKET("Rocket", "Rocket", 0xFF8C3494), CARD("Visa / Mastercard / Amex", "Card", 0xFF1A1F71)
}
