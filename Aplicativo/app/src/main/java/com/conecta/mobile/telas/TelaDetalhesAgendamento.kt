package com.conecta.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.conecta.mobile.ui.theme.CardBackground
import com.conecta.mobile.ui.theme.ConectaMobileTheme
import com.conecta.mobile.ui.theme.DarkGreenPrimary
import com.conecta.mobile.ui.theme.LightBackground
import com.conecta.mobile.ui.theme.TextPrimary

// TelaDetalhesAgendamento: mostra as informações do agendamento que foi tocado no Histórico.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaDetalhesAgendamento(onBack: () -> Unit) {

    // Dados que vêm do card tocado (se estiver vazio, usa um exemplo, por causa do preview).
    val status = if (agendamentoAtual.first.isEmpty()) "Aceito" else agendamentoAtual.first
    val data = if (agendamentoAtual.second.isEmpty()) "15/09/2026" else agendamentoAtual.second
    val tipoAjuda = if (agendamentoAtual.third.isEmpty()) "Consulta médica" else agendamentoAtual.third

    // Ainda de exemplo: a API vai mandar estes campos quando tiver o detalhe do agendamento.
    val destino = when {
        tipoAjuda.contains("médica", ignoreCase = true) -> "Hospital São Lucas"
        tipoAjuda.contains("mercado", ignoreCase = true) -> "Mercado Extra"
        else -> "Farmácia Popular"
    }
    val duracao = when {
        tipoAjuda.contains("médica", ignoreCase = true) -> "3h00"
        tipoAjuda.contains("mercado", ignoreCase = true) -> "2h00"
        else -> "1h30"
    }
    val idoso = "José Pereira"
    val ajudante = "Maria dos Santos"
    val hora = "14:00"
    val enderecoOng = "ONG Conecta++ - Rua A, 100"
    val casaIdoso = "Rua das Flores, 123 - Osasco/SP"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {
        TopAppBar(
            title = { Text("Detalhes do Agendamento", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 19.sp) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkGreenPrimary)
        )

        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    LinhaDetalhe(Icons.Default.Person, "Idoso:", idoso)
                    Divider(modifier = Modifier.padding(vertical = 14.dp), color = Color.LightGray.copy(alpha = 0.4f))
                    LinhaDetalhe(Icons.Default.Person, "Ajudante:", ajudante)
                    Divider(modifier = Modifier.padding(vertical = 14.dp), color = Color.LightGray.copy(alpha = 0.4f))
                    LinhaDetalhe(Icons.Default.DateRange, "Data:", "$data às $hora")
                    Divider(modifier = Modifier.padding(vertical = 14.dp), color = Color.LightGray.copy(alpha = 0.4f))
                    LinhaDetalhe(Icons.Default.Place, "Duração prevista:", duracao)
                    Divider(modifier = Modifier.padding(vertical = 14.dp), color = Color.LightGray.copy(alpha = 0.4f))
                    LinhaDetalhe(Icons.Default.Phone, "Tipo de ajuda:", tipoAjuda)
                    Divider(modifier = Modifier.padding(vertical = 14.dp), color = Color.LightGray.copy(alpha = 0.4f))
                    LinhaDetalhe(Icons.Default.Person, "Status:", status)
                }
            }

            Card(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Text(
                        text = "TRAJETO DO AJUDANTE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = DarkGreenPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LinhaDetalhe(Icons.Default.Place, "1. Sai da ONG:", enderecoOng)
                    Divider(modifier = Modifier.padding(vertical = 14.dp), color = Color.LightGray.copy(alpha = 0.4f))
                    LinhaDetalhe(Icons.Default.Home, "2. Busca o idoso em casa:", casaIdoso)
                    Divider(modifier = Modifier.padding(vertical = 14.dp), color = Color.LightGray.copy(alpha = 0.4f))
                    LinhaDetalhe(Icons.Default.LocalHospital, "3. Acompanha até o destino:", destino)
                    Divider(modifier = Modifier.padding(vertical = 14.dp), color = Color.LightGray.copy(alpha = 0.4f))
                    LinhaDetalhe(Icons.Default.Home, "4. Leva o idoso de volta para casa:", casaIdoso)
                    Divider(modifier = Modifier.padding(vertical = 14.dp), color = Color.LightGray.copy(alpha = 0.4f))
                    LinhaDetalhe(Icons.Default.Place, "5. Retorna para a ONG:", enderecoOng)
                }
            }
        }
    }
}

@Composable
fun LinhaDetalhe(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = DarkGreenPrimary, modifier = Modifier.size(32.dp))
        Spacer(modifier = Modifier.width(18.dp))
        Column {
            Text(label, color = Color.Gray, fontSize = 15.sp)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 19.sp, color = TextPrimary)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TelaDetalhesAgendamentoPreview() {
    ConectaMobileTheme {
        TelaDetalhesAgendamento(onBack = {})
    }
}