package com.conecta.mobile

import com.conecta.mobile.ui.theme.LaranjaDestaque
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.conecta.mobile.ui.theme.CardBackground
import com.conecta.mobile.ui.theme.ConectaMobileTheme
import com.conecta.mobile.ui.theme.DarkGreenPrimary
import com.conecta.mobile.ui.theme.LightBackground
import com.conecta.mobile.ui.theme.StatusRed
import com.conecta.mobile.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaPerfil(onBack: () -> Unit, onSair: () -> Unit) {

    val nome = "Maria da Silva"
    val cpf = "123.456.789-00"
    val telefone = "(11) 91234-5678"
    val endereco = "Rua das Flores, 123 - Osasco/SP"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {
        TopAppBar(
            title = { Text("Perfil do Usuário", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkGreenPrimary)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 20.dp)
                    .size(130.dp)
                    .clip(CircleShape)
                    .background(DarkGreenPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Foto de Perfil",
                    tint = Color.White,
                    modifier = Modifier.size(76.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    ItemPerfil("Nome Completo:", nome)
                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.4f))
                    ItemPerfil("CPF:", cpf)
                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.4f))
                    ItemPerfil("Telefone:", telefone)
                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.4f))
                    ItemPerfil("Endereço:", endereco)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            OutlinedButton(
                onClick = { onSair() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(2.dp, StatusRed)
            ) {
                Text("SAIR DA CONTA", color = StatusRed, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        }
    }
}

@Composable
fun ItemPerfil(label: String, value: String) {
    Column {
        Text(label, color = Color.Gray, fontSize = 15.sp)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 19.sp, color = TextPrimary)
    }
}

@Preview(showBackground = true)
@Composable
fun TelaPerfilPreview() {
    ConectaMobileTheme {
        TelaPerfil(onBack = {}, onSair = {})
    }
}