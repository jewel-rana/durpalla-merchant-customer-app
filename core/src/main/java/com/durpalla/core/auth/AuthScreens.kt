package com.durpalla.core.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.durpalla.core.config.LocalMerchant
import com.durpalla.core.data.AuthRepository
import com.durpalla.core.theme.N
import com.durpalla.core.ui.AccentButton
import com.durpalla.core.ui.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginState(val phone: String = "", val loading: Boolean = false, val error: String? = null)

class AuthViewModel(private val repo: AuthRepository) : ViewModel() {
    private val _s = MutableStateFlow(LoginState()); val state: StateFlow<LoginState> = _s
    fun phone(p: String) = _s.update { it.copy(phone = p.filter(Char::isDigit).take(11), error = null) }
    fun send(ok: () -> Unit) = viewModelScope.launch {
        if (!_s.value.phone.matches(Regex("01[3-9]\\d{8}"))) { _s.update { it.copy(error = "Enter a valid Bangladeshi mobile number") }; return@launch }
        _s.update { it.copy(loading = true) }; repo.requestOtp(_s.value.phone); _s.update { it.copy(loading = false) }; ok()
    }
    fun verify(code: String, ok: () -> Unit) = viewModelScope.launch {
        repo.verify(_s.value.phone, code).onSuccess { ok() }.onFailure { e -> _s.update { it.copy(error = e.message) } }
    }
}

@Composable
fun SplashScreen(onStart: () -> Unit) {
    val m = LocalMerchant.current
    Box(Modifier.fillMaxSize()) {
        Image(painterResource(m.splashImageRes), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(m.colors.primary.copy(alpha = 0.75f), m.colors.primaryDark.copy(alpha = 0.95f)))))
        Column(Modifier.align(Alignment.Center).statusBarsPadding(), horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painterResource(m.logoRes), m.merchantName, Modifier.size(96.dp).clip(RoundedCornerShape(24.dp)))
            Spacer(Modifier.height(16.dp))
            Text(m.merchantName, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text(m.tagline, color = Color.White.copy(alpha = 0.8f))
        }
        Column(Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            AccentButton("Get Started", onStart)
            Text("Powered by Durpalla Limited", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, modifier = Modifier.padding(top = 12.dp))
        }
    }
}

@Composable
fun LoginScreen(s: LoginState, onPhone: (String) -> Unit, onSend: () -> Unit) {
    val m = LocalMerchant.current
    Screen(bottomBar = { AccentButton(if (s.loading) "Sending…" else "Send OTP", onSend, enabled = !s.loading) }) {
        Spacer(Modifier.height(24.dp))
        Image(painterResource(m.logoRes), null, Modifier.size(64.dp).clip(RoundedCornerShape(16.dp)))
        Text("Log in to ${m.shortName}", style = MaterialTheme.typography.headlineSmall)
        Text("We'll send a 6-digit code to verify your number.", color = N.Muted)
        OutlinedTextField(s.phone, onPhone, Modifier.fillMaxWidth(), prefix = { Text("+88 ") }, placeholder = { Text("01XXXXXXXXX") }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), isError = s.error != null, supportingText = { s.error?.let { Text(it) } }, shape = RoundedCornerShape(14.dp))
        Text("By continuing you agree to the Terms & Privacy Policy. Bookings and payments are processed by Durpalla Limited.", fontSize = 12.sp, color = N.Muted)
    }
}

@Composable
fun OtpScreen(phone: String, error: String?, onBack: () -> Unit, onVerify: (String) -> Unit) {
    var code by remember { mutableStateOf("") }
    Screen(onBack = onBack, bottomBar = { AccentButton("Verify & continue", { onVerify(code) }, enabled = code.length == 6) }) {
        Text("Enter the code", style = MaterialTheme.typography.headlineSmall)
        Text("Sent to +88 $phone", color = N.Muted)
        OutlinedTextField(code, { code = it.filter(Char::isDigit).take(6) }, Modifier.fillMaxWidth(), singleLine = true,
            textStyle = LocalTextStyle.current.copy(fontSize = 28.sp, letterSpacing = 12.sp, textAlign = TextAlign.Center),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), isError = error != null, supportingText = { error?.let { Text(it) } }, shape = RoundedCornerShape(14.dp))
        Text("Resend code", color = LocalMerchant.current.colors.primary, fontWeight = FontWeight.SemiBold, modifier = Modifier.clickable { })
    }
}
