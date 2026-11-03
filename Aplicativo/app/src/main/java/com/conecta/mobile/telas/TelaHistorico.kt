package com.conecta.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.JsonObject
import com.conecta.mobile.ui.theme.CardBackground
import com.conecta.mobile.ui.theme.ConectaMobileTheme
import com.conecta.mobile.ui.theme.DarkGreenPrimary
import com.conecta.mobile.ui.theme.LightBackground
import com.conecta.mobile.ui.theme.StatusGreen
import com.conecta.mobile.ui.theme.StatusGreenBg
import com.conecta.mobile.ui.theme.StatusRed
import com.conecta.mobile.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaHistorico(onBack: () -> Unit, onAgendamentoClick: () -> Unit) {

    // Dados de exemplo (fake), usados só quando usarApiFake é true.
    val agendamentosFake = listOf(
        agendamentoExemplo("Agendado", "20/09/2026", "Farmácia", "Farmácia Popular"),
        agendamentoExemplo("Concluído", "10/09/2026", "Hospital", "Hospital São Lucas"),
        agendamentoExemplo("Concluído", "05/09/2026", "Mercado", "Mercado Extra"),
        agendamentoExemplo("Cancelado", "01/09/2026", "Farmácia", "Farmácia Popular")
    )

    var lista by remember { mutableStateOf<List<JsonObject>>(if (usarApiFake) agendamentosFake else emptyList()) }
    var carregando by remember { mutableStateOf(!usarApiFake) }
    var erroCarga by remember { mutableStateOf("") }

    // Busca na API assim que a tela abre (só quando não está em modo de teste).
    LaunchedEffect(Unit) {
        if (!usarApiFake) {
            carregando = true
            erroCarga = ""
            try {
                val resposta = api.listarAgendamentos(usuarioIdAtual, "Bearer $tokenAtual")
                val corpo = resposta.body()

                if (resposta.isSuccessful && corpo != null) {
                    val nova = mutableListOf<JsonObject>()
                    for (item in corpo) {
                        nova.add(item.asJsonObject)
                    }
                    lista = nova
                } else {
                    erroCarga = "Não foi possível carregar seu histórico."
                }
            } catch (e: Exception) {
                erroCarga = "Sem conexão com o servidor. Tente de novo."
            }
            carregando = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {
        TopAppBar(
            title = { Text("Histórico", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkGreenPrimary)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
        ) {
            if (carregando) {
                Text("Carregando seu histórico...", fontSize = 19.sp, color = TextPrimary)
            } else if (erroCarga.isNotEmpty()) {
                Text(erroCarga, fontSize = 19.sp, color = MaterialTheme.colorScheme.error)
            } else if (lista.isEmpty()) {
                Text("Você ainda não tem agendamentos.", fontSize = 19.sp, color = TextPrimary)
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn {
                items(lista) { agendamento ->
                    CardHistorico(
                        status = textoDe(agendamento, "status"),
                        data = textoDe(agendamento, "data") + " às " + textoDe(agendamento, "hora"),
                        descricao = textoDe(agendamento, "descricao"),
                        onClick = {
                            agendamentoAtual = agendamento
                            onAgendamentoClick()
                        }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }
    }
}

// Monta um agendamento de exemplo (só para o modo de teste).
private fun agendamentoExemplo(status: String, data: String, tipo: String, destino: String): JsonObject {
    val obj = JsonObject()
    obj.addProperty("status", status)
    obj.addProperty("data", data)
    obj.addProperty("hora", "09:00")
    obj.addProperty("tipo", tipo)
    obj.addProperty("origem", "Rua das Flores, 123 - Osasco/SP")
    obj.addProperty("destino", destino)
    obj.addProperty("descricao", "$tipo - $destino")
    return obj
}

@Composable
fun CardHistorico(status: String, data: String, descricao: String, onClick: () -> Unit) {
    // Verde = concluído, vermelho = cancelado, verde-escuro = agendado
    val corStatus = when (status) {
        "Concluído" -> StatusGreen
        "Cancelado" -> StatusRed
        else -> DarkGreenPrimary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(corStatus, CircleShape)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(status, color = corStatus, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = StatusGreenBg,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = data,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkGreenPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(descricao, fontWeight = FontWeight.Bold, fontSize = 19.sp, color = TextPrimary)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TelaHistoricoPreview() {
    ConectaMobileTheme {
        TelaHistorico(onBack = {}, onAgendamentoClick = {})
    }
}