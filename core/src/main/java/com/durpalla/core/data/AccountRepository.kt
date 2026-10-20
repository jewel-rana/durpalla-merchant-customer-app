package com.durpalla.core.data

import com.durpalla.core.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

interface AuthRepository {
    suspend fun requestOtp(phone: String): Result<Unit>
    suspend fun verify(phone: String, otp: String): Result<User>
}

/** Account data shared by every merchant app. Bookings are seeded/added by the vertical feature. */
interface AccountRepository {
    val user: StateFlow<User>
    val bookings: StateFlow<List<Booking>>
    val travellers: StateFlow<List<Traveller>>
    val payments: StateFlow<List<SavedPayment>>
    val offers: List<Offer>
    val notices: List<Notice>
    fun addBooking(b: Booking)
    suspend fun cancel(id: String): Result<Unit>
    fun updateProfile(name: String, email: String)
    fun addTraveller(t: Traveller)
}

class FakeAuthRepository : AuthRepository {
    override suspend fun requestOtp(phone: String) = run { delay(300); Result.success(Unit) }
    override suspend fun verify(phone: String, otp: String) =
        if (otp.length == 6) Result.success(User("Jewel Rana", "+88 $phone", "jewel@example.com", "2025", 1240)) else Result.failure(IllegalArgumentException("Invalid code"))
}

class FakeAccountRepository(seed: List<Booking>, override val offers: List<Offer>) : AccountRepository {
    private val _user = MutableStateFlow(User("Jewel Rana", "+880 1712-345678", "jewel@example.com", "2025", 1240))
    override val user: StateFlow<User> = _user
    private val _bookings = MutableStateFlow(seed)
    override val bookings: StateFlow<List<Booking>> = _bookings
    private val _travellers = MutableStateFlow(listOf(Traveller("t1", "Jewel Rana", "Male", 31, "01712345678"), Traveller("t2", "Nusrat Jahan", "Female", 29, "01812345678")))
    override val travellers: StateFlow<List<Traveller>> = _travellers
    override val payments: StateFlow<List<SavedPayment>> = MutableStateFlow(listOf(SavedPayment("p1", PayMethod.BKASH, "017•••678", true), SavedPayment("p2", PayMethod.CARD, "Visa •••• 4242", false)))
    override val notices = listOf(
        Notice("Booking confirmed", "Your booking is confirmed. E-ticket sent by SMS.", "2 h ago", true),
        Notice("15% OFF with bKash", "Use code BKASH15 on your next booking.", "Yesterday", true),
        Notice("Trip reminder", "Your trip starts tomorrow. Reach 30 min early.", "2 days ago", false),
    )
    override fun addBooking(b: Booking) = _bookings.update { listOf(b) + it }
    override suspend fun cancel(id: String): Result<Unit> { delay(300); _bookings.update { l -> l.map { if (it.id == id) it.copy(status = BookingStatus.CANCELLED) else it } }; return Result.success(Unit) }
    override fun updateProfile(name: String, email: String) = _user.update { it.copy(name = name, email = email) }
    override fun addTraveller(t: Traveller) = _travellers.update { it + t }
}
