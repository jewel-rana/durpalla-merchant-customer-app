package com.durpalla.core.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.durpalla.core.data.AccountRepository
import com.durpalla.core.model.Traveller
import kotlinx.coroutines.launch

class AccountViewModel(val repo: AccountRepository) : ViewModel() {
    val user = repo.user
    val bookings = repo.bookings
    val travellers = repo.travellers
    val payments = repo.payments
    fun cancel(id: String, done: () -> Unit) = viewModelScope.launch { repo.cancel(id); done() }
    fun save(name: String, email: String) = repo.updateProfile(name, email)
    fun addTraveller(name: String, gender: String, age: Int) = repo.addTraveller(Traveller("t${System.nanoTime()}", name, gender, age, ""))
}
