package com.durpalla.core.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.durpalla.core.model.PayMethod
import com.durpalla.core.theme.Brand
import com.durpalla.core.theme.N
import com.durpalla.core.ui.MCard

@Composable
fun PaymentMethodList(selected: PayMethod, onSelect: (PayMethod) -> Unit) {
    Text("Pay with", style = MaterialTheme.typography.titleMedium)
    PayMethod.entries.forEach { m ->
        MCard(onClick = { onSelect(m) }, border = if (m == selected) Brand.colors.primary else N.Line, pad = 12.dp) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Color(m.color)), contentAlignment = Alignment.Center) {
                    Text(m.badge, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Text(m.label, Modifier.weight(1f).padding(horizontal = 12.dp), style = MaterialTheme.typography.titleSmall)
                RadioButton(m == selected, { onSelect(m) })
            }
        }
    }
    SecureNote()
}

@Composable
fun SecureNote() = Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(N.SuccessSoft).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
    Icon(Icons.Filled.VerifiedUser, null, tint = N.Success, modifier = Modifier.size(18.dp))
    Text("  Secure payment powered by Durpalla Limited", color = N.Success, fontSize = 13.sp, fontWeight = FontWeight.Medium)
}
