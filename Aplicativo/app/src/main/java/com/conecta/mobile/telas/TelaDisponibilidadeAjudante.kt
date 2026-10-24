package com.conecta.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.conecta.mobile.ui.theme.CardBackground
import com.conecta.mobile.ui.theme.ConectaMobileTheme
import com.conecta.mobile.ui.theme.DarkGreenPrimary
import com.conecta.mobile.ui.theme.LaranjaDestaque
import com.conecta.mobile.ui.theme.LightBackground
import com.conecta.mobile.ui.theme.StatusGreenBg
import com.conecta.mobile.ui.theme.StatusRed
import com.conecta.mobile.ui.theme.TextPrimary
import java.util.Calendar

// TelaDisponibilidadeAjudante (IBL 19 e 30): o ajudante escolhe a data e a
// hora de início e de fim em que pode atender. Horário do app: 07:00 às 20:00.
// Dados só na memória enquanto a API não existe (usarApiFake).
@Composable
fun TelaDisponibilidadeAjudante(onVoltar: () -> Unit) {

    // Próximos 7 dias (hoje + 6), como texto "yyyy-MM-dd" (formato que a API vai receber)
    val dias = remember {
        (0..6).map { n ->
            val c = Calendar.getInstance()
            c.add(Calendar.DAY_OF_YEAR, n)
            val ano = c.get(Calendar.YEAR)
            val mes = (c.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
            val dia = c.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
            "$ano-$mes-$dia"
        }
    }
    // Horas inteiras: início 07 a 19, fim 08 a 20
    val horasInicio = (7..19).toList()
    val horasFim = (8..20).toList()

    var diaEscolhido by remember { mutableStateOf(0) }
    var horaInicio by remember { mutableStateOf(8) }
    var horaFim by remember { mutableStateOf(12) }
    var mensagem by remember { mutableStateOf("") }
    var mensagemErro by remember { mutableStateOf(false) }

    // Cada item da lista é um texto "2026-10-26|08|12" (data, início, fim)
    val disponibilidades = remember { mutableStateListOf<String>() }

    fun hora(h: Int) = h.toString().padStart(2, '0') + ":00"
    // "2026-10-26" -> "26/10"
    fun diaMes(data: String) = data.substring(8, 10) + "/" + data.substring(5, 7)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {
        // Cabeçalho
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkGreenPrimary)
                .padding(horizontal = 12.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onVoltar() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text("Área do ajudante", color = Color.White.copy(alpha = 0.85f), fontSize = 16.sp)
                Text("Minha disponibilidade", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {

                        Text("1. Escolha o dia", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(10.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(dias.size) { i ->
                                OpcaoSelecionavel(
                                    texto = diaMes(dias[i]),
                                    selecionado = diaEscolhido == i,
                                    onClick = { diaEscolhido = i; mensagem = "" }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text("2. Hora de início", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(10.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(horasInicio.size) { i ->
                                val h = horasInicio[i]
                                OpcaoSelecionavel(
                                    texto = hora(h),
                                    selecionado = horaInicio == h,
                                    onClick = { horaInicio = h; mensagem = "" }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text("3. Hora de fim", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                        Spacer(modifier = Modifier.height(10.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(horasFim.size) { i ->
                                val h = horasFim[i]
                                OpcaoSelecionavel(
                                    texto = hora(h),
                                    selecionado = horaFim == h,
                                    onClick = { horaFim = h; mensagem = "" }
                                )
                            }
                        }

                        if (mensagem.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = mensagem,
                                color = if (mensagemErro) MaterialTheme.colorScheme.error else DarkGreenPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                val data = dias[diaEscolhido]
                                val chave = "$data|$horaInicio|$horaFim"
                                if (horaFim <= horaInicio) {
                                    mensagem = "A hora de fim precisa ser depois da hora de início."
                                    mensagemErro = true
                                } else if (disponibilidades.any { it.startsWith("$data|") && sobrepoe(it, horaInicio, horaFim) }) {
                                    mensagem = "Esse horário se mistura com outro já adicionado."
                                    mensagemErro = true
                                } else {
                                    disponibilidades.add(chave)
                                    disponibilidades.sort()
                                    mensagem = "Horário adicionado."
                                    mensagemErro = false
                                    // Quando a API existir: enviar a disponibilidade aqui
                                    // (data yyyy-MM-dd, início e fim HH:mm).
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LaranjaDestaque),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("ADICIONAR HORÁRIO", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "HORÁRIOS QUE VOCÊ PODE ATENDER",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (disponibilidades.isEmpty()) {
                    Text("Nenhum horário adicionado ainda.", fontSize = 16.sp, color = Color.Gray)
                }
            }

            items(disponibilidades.size) { i ->
                val partes = disponibilidades[i].split("|")
                val data = partes[0]
                val inicio = partes[1].toInt()
                val fim = partes[2].toInt()

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(color = StatusGreenBg, shape = RoundedCornerShape(8.dp)) {
                            Text(
                                text = diaMes(data),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkGreenPrimary
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = "${hora(inicio)} às ${hora(fim)}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { disponibilidades.removeAt(i) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Remover horário", tint = StatusRed)
                        }
                    }
                }
            }
        }
    }
}

// Verdadeiro se o novo intervalo [inicio, fim) se mistura com um item da lista.
private fun sobrepoe(item: String, inicio: Int, fim: Int): Boolean {
    val partes = item.split("|")
    val ini = partes[1].toInt()
    val f = partes[2].toInt()
    return inicio < f && fim > ini
}

@Composable
private fun OpcaoSelecionavel(texto: String, selecionado: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (selecionado) DarkGreenPrimary else StatusGreenBg,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = texto,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = if (selecionado) Color.White else DarkGreenPrimary
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TelaDisponibilidadeAjudantePreview() {
    ConectaMobileTheme {
        TelaDisponibilidadeAjudante(onVoltar = {})
    }
}