package com.durpalla.core.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

/*
 * Durpalla Limited booking/payment API. Every request carries the merchant id header so one
 * backend serves every white-label app. Not called by default (BuildConfig.USE_REMOTE_API = false).
 */
interface DurpallaApi {
    @POST("auth/otp") suspend fun requestOtp(@Body body: OtpRequest)
    @POST("auth/verify") suspend fun verifyOtp(@Body body: VerifyRequest): TokenDto

    // Bus
    @GET("bus/trips") suspend fun searchTrips(@Query("from") from: String, @Query("to") to: String, @Query("date") date: String, @Query("seats") seats: Int): List<BusTripDto>
    @GET("bus/trips/{id}/seats") suspend fun seatMap(@Path("id") tripId: String): SeatMapDto
    @POST("bus/trips/{id}/hold") suspend fun holdSeats(@Path("id") tripId: String, @Body body: HoldRequest): HoldDto

    // Hotel
    @GET("hotel/rooms") suspend fun rooms(@Query("checkIn") checkIn: String, @Query("checkOut") checkOut: String, @Query("adults") adults: Int, @Query("rooms") rooms: Int): List<RoomDto>

    // Shared
    @POST("payments/intent") suspend fun paymentIntent(@Body body: PaymentIntentRequest): PaymentIntentDto
    @GET("me/bookings") suspend fun bookings(@Query("status") status: String? = null): List<BookingDto>
    @POST("me/bookings/{id}/cancel") suspend fun cancel(@Path("id") id: String): BookingDto
    @GET("me/bookings/{id}/invoice") suspend fun invoice(@Path("id") id: String): InvoiceDto
}

data class OtpRequest(val phone: String)
data class VerifyRequest(val phone: String, val otp: String)
data class TokenDto(val accessToken: String, val refreshToken: String)
data class BusTripDto(val id: String, val operator: String, val coach: String, val type: String, val departure: String, val arrival: String, val fare: Int, val seatsLeft: Int, val amenities: List<String>)
data class SeatDto(val id: String, val row: Int, val col: Int, val state: String, val deck: String, val fare: Int)
data class SeatMapDto(val layout: String, val seats: List<SeatDto>)
data class HoldRequest(val seatIds: List<String>, val boarding: String, val dropping: String)
data class HoldDto(val holdId: String, val expiresAt: String, val amount: Int)
data class RoomDto(val id: String, val name: String, val sizeSqm: Int, val bed: String, val rate: Int, val photos: List<String>, val amenities: List<String>)
data class PaymentIntentRequest(val bookingRef: String, val method: String, val amount: Int)
data class PaymentIntentDto(val intentId: String, val redirectUrl: String?, val status: String)
data class BookingDto(val id: String, val vertical: String, val title: String, val status: String, val amount: Int, val date: String)
data class InvoiceDto(val number: String, val pdfUrl: String, val lines: List<Map<String, Any>>)

object ApiFactory {
    fun create(baseUrl: String, merchantId: String, token: () -> String? = { null }, debug: Boolean = false): DurpallaApi {
        val headers = Interceptor { chain ->
            val b = chain.request().newBuilder().header("X-Merchant-Id", merchantId)
            token()?.let { b.header("Authorization", "Bearer $it") }
            chain.proceed(b.build())
        }
        val client = OkHttpClient.Builder().addInterceptor(headers)
            .apply { if (debug) addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)) }.build()
        return Retrofit.Builder().baseUrl(baseUrl).client(client).addConverterFactory(GsonConverterFactory.create()).build().create(DurpallaApi::class.java)
    }
}
