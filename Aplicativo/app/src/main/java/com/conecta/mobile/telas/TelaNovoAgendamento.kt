package com.conecta.mobile

import com.conecta.mobile.ui.theme.LaranjaDestaque
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.conecta.mobile.ui.theme.CardBackground
import com.conecta.mobile.ui.theme.ConectaMobileTheme
import com.conecta.mobile.ui.theme.DarkGreenPrimary
import com.conecta.mobile.ui.theme.LightBackground
import com.conecta.mobile.ui.theme.StatusGreenBg
import com.conecta.mobile.ui.theme.TextPrimary
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaNovoAgendamento(onBack: () -> Unit, onSalvar: () -> Unit) {

    var tipoSelecionado by remember { mutableStateOf("Hospital") }
    var origem by remember { mutableStateOf("Rua das Flores, 123 - Osasco/SP") }
    var destino by remember { mutableStateOf("") }
    var dataTexto by remember { mutableStateOf("") }
    var horaTexto by remember { mutableStateOf("") }

    var mostrarDatePicker by remember { mutableStateOf(false) }

    var erroOrigem by remember { mutableStateOf(false) }
    var erroDestino by remember { mutableStateOf(false) }
    var erroData by remember { mutableStateOf(false) }
    var erroHora by remember { mutableStateOf(false) }
    var textoErroData by remember { mutableStateOf("Escolha uma data") }
    var textoErroHora by remember { mutableStateOf("Escolha um horário") }

    // Envio para a API
    var carregando by remember { mutableStateOf(false) }
    var mensagemApi by remember { mutableStateOf("") }
    var agendado by remember { mutableStateOf(false) }
    val escopo = rememberCoroutineScope()
    val duracaoMin = duracaoPorTipo(tipoSelecionado)

    // Horários livres que a API devolve para a data e o tipo de ajuda escolhidos
    var horariosLivres by remember { mutableStateOf<List<String>>(emptyList()) }
    var carregandoHorarios by remember { mutableStateOf(false) }
    var avisoHorarios by remember { mutableStateOf("Escolha a data para ver os horários livres.") }

    // Sempre que a data ou o tipo mudam, busca de novo os horários livres.
    LaunchedEffect(dataTexto, tipoSelecionado) {
        horaTexto = ""
        horariosLivres = emptyList()
        val dataConsulta = dataBrParaIso(dataTexto)

        if (dataConsulta.isEmpty()) {
            avisoHorarios = "Escolha a data para ver os horários livres."
        } else if (dataConsulta < hojeIso()) {
            avisoHorarios = "Escolha uma data de hoje em diante."
        } else if (usarApiFake) {
            horariosLivres = listOf("08:00", "09:00", "10:00", "14:00", "15:00")
        } else {
            carregandoHorarios = true
            avisoHorarios = ""
            try {
                val resposta = api.horariosLivres(dataConsulta, tipoSelecionado, autorizacao())
                val corpo = resposta.body()
                if (resposta.isSuccessful && corpo != null) {
                    val livres = mutableListOf<String>()
                    for (item in corpo) {
                        livres.add(item.asString)
                    }
                    horariosLivres = livres
                    if (livres.isEmpty()) {
                        avisoHorarios = "Nenhum horário livre nesse dia para esse tipo de ajuda. Escolha outra data."
                    }
                } else {
                    avisoHorarios = mensagemDoErro(resposta, "Não foi possível ver os horários livres.")
                }
            } catch (e: Exception) {
                avisoHorarios = "Sem conexão com o servidor. Tente de novo."
            }
            carregandoHorarios = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {
        TopAppBar(
            title = { Text("Novo Agendamento", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkGreenPrimary)
        )

        Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
            Text("TIPO DE AJUDA", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)

            Spacer(modifier = Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                CategoriaCard("Hospital", Icons.Default.LocalHospital, tipoSelecionado == "Hospital") {
                    tipoSelecionado = "Hospital"
                }
                CategoriaCard("Mercado", Icons.Default.ShoppingCart, tipoSelecionado == "Mercado") {
                    tipoSelecionado = "Mercado"
                }
                CategoriaCard("Farmácia", Icons.Default.LocalPharmacy, tipoSelecionado == "Farmácia") {
                    tipoSelecionado = "Farmácia"
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Origem", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = origem,
                        onValueChange = {
                            origem = it
                            erroOrigem = false
                        },
                        placeholder = { Text("De onde você vai sair", fontSize = 16.sp) },
                        textStyle = TextStyle(fontSize = 18.sp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 60.dp),
                        leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, tint = DarkGreenPrimary, modifier = Modifier.size(26.dp)) },
                        shape = RoundedCornerShape(10.dp),
                        isError = erroOrigem
                    )
                    if (erroOrigem) {
                        Text("Preencha a origem", color = MaterialTheme.colorScheme.error, fontSize = 15.sp)
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text("Destino", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = destino,
                        onValueChange = {
                            destino = it
                            erroDestino = false
                        },
                        placeholder = { Text("Para onde você vai", fontSize = 16.sp) },
                        textStyle = TextStyle(fontSize = 18.sp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 60.dp),
                        leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, tint = DarkGreenPrimary, modifier = Modifier.size(26.dp)) },
                        shape = RoundedCornerShape(10.dp),
                        isError = erroDestino
                    )
                    if (erroDestino) {
                        Text("Preencha o destino", color = MaterialTheme.colorScheme.error, fontSize = 15.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        color = DarkGreenPrimary.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "🔄 O ajudante sai da ONG, busca o idoso em casa, acompanha até o Destino, leva de volta para casa e depois retorna à ONG.",
                            modifier = Modifier.padding(12.dp),
                            fontSize = 15.sp,
                            color = DarkGreenPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        color = LaranjaDestaque.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "⏱ Tempo previsto do atendimento: ${formatarDuracao(duracaoMin)}",
                            modifier = Modifier.padding(12.dp),
                            fontSize = 15.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text("Data", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = dataTexto,
                        onValueChange = { },
                        placeholder = { Text("Toque para escolher a data", fontSize = 16.sp) },
                        textStyle = TextStyle(fontSize = 18.sp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 60.dp)
                            .clickable {
                                mostrarDatePicker = true
                                erroData = false
                            },
                        leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, tint = DarkGreenPrimary, modifier = Modifier.size(26.dp)) },
                        shape = RoundedCornerShape(10.dp),
                        enabled = false,
                        isError = erroData,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = TextPrimary,
                            disabledBorderColor = if (erroData) MaterialTheme.colorScheme.error else Color.Gray,
                            disabledLeadingIconColor = DarkGreenPrimary,
                            disabledPlaceholderColor = Color.Gray
                        )
                    )
                    if (erroData) {
                        Text(textoErroData, color = MaterialTheme.colorScheme.error, fontSize = 15.sp)
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text("Horários livres", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (carregandoHorarios) {
                        Text("Procurando horários livres...", fontSize = 16.sp, color = Color.Gray)
                    } else if (horariosLivres.isEmpty()) {
                        Text(avisoHorarios, fontSize = 16.sp, color = Color.Gray)
                    } else {
                        // Os horários aparecem em linhas de 4. Toque em um para escolher.
                        horariosLivres.chunked(4).forEach { linha ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                linha.forEach { h ->
                                    Surface(
                                        color = if (horaTexto == h) DarkGreenPrimary else StatusGreenBg,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                horaTexto = h
                                                erroHora = false
                                            }
                                    ) {
                                        Text(
                                            text = h,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 12.dp),
                                            textAlign = TextAlign.Center,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (horaTexto == h) Color.White else DarkGreenPrimary
                                        )
                                    }
                                }
                                repeat(4 - linha.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                    if (erroHora) {
                        Text(textoErroHora, color = MaterialTheme.colorScheme.error, fontSize = 15.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (mensagemApi.isNotEmpty()) {
                Text(
                    text = mensagemApi,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Button(
                onClick = {
                    mensagemApi = ""
                    val dataIso = dataBrParaIso(dataTexto)

                    erroOrigem = origem.isBlank()
                    erroDestino = destino.isBlank()

                    erroData = false
                    if (dataTexto.isBlank()) {
                        erroData = true
                        textoErroData = "Escolha uma data"
                    } else if (dataIso < hojeIso()) {
                        erroData = true
                        textoErroData = "Escolha uma data de hoje em diante"
                    }

                    erroHora = false
                    if (horaTexto.isBlank()) {
                        erroHora = true
                        textoErroHora = "Escolha um horário"
                    } else {
                        // O atendimento inteiro precisa terminar até 20:00.
                        val minutosInicio = horaTexto.substring(0, 2).toInt() * 60 + horaTexto.substring(3, 5).toInt()
                        if (minutosInicio + duracaoMin > 20 * 60) {
                            erroHora = true
                            textoErroHora = "Esse atendimento dura ${formatarDuracao(duracaoMin)} e terminaria depois das 20:00"
                        }
                    }

                    if (!erroOrigem && !erroDestino && !erroData && !erroHora) {
                        if (usarApiFake) {
                            agendado = true
                        } else {
                            carregando = true
                            escopo.launch {
                                try {
                                    val corpo = mapOf<String, Any>(
                                        "idUsuario" to usuarioIdAtual,
                                        "data" to dataIso,
                                        "hora" to horaTexto,
                                        "tipo" to tipoSelecionado,
                                        "origem" to origem.trim(),
                                        "destino" to destino.trim()
                                    )
                                    val resposta = api.criarAgendamento(corpo, autorizacao())

                                    if (resposta.isSuccessful) {
                                        agendado = true
                                    } else {
                                        mensagemApi = mensagemDoErro(resposta, "Não foi possível agendar. Tente de novo.")
                                    }
                                } catch (e: Exception) {
                                    mensagemApi = "Sem conexão com o servidor. Tente de novo."
                                }
                                carregando = false
                            }
                        }
                    }
                },
                enabled = !carregando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkGreenPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (carregando) "AGENDANDO..." else "CONFIRMAR AGENDAMENTO",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }
        }
    }

    // Aviso de que deu certo
    if (agendado) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Agendamento confirmado!", fontSize = 22.sp, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Seu atendimento de $tipoSelecionado ficou marcado para $dataTexto às $horaTexto. " +
                        "Um ajudante vai buscar você em casa.",
                    fontSize = 18.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    agendado = false
                    onSalvar()
                }) {
                    Text("OK", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (mostrarDatePicker) {
        val estadoData = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = estadoData.selectedDateMillis
                    if (millis != null) {
                        val formato = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
                        formato.timeZone = TimeZone.getTimeZone("UTC")
                        dataTexto = formato.format(millis)
                    }
                    mostrarDatePicker = false
                }) {
                    Text("OK", fontSize = 16.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePicker = false }) {
                    Text("Cancelar", fontSize = 16.sp)
                }
            }
        ) {
            DatePicker(state = estadoData)
        }
    }
}

@Composable
fun CategoriaCard(label: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .size(width = 110.dp, height = 110.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) DarkGreenPrimary.copy(alpha = 0.1f) else CardBackground
        ),
        border = if (isSelected) BorderStroke(2.dp, DarkGreenPrimary) else BorderStroke(1.dp, Color.LightGray)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = DarkGreenPrimary,
                modifier = Modifier.size(42.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DarkGreenPrimary)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TelaNovoAgendamentoPreview() {
    ConectaMobileTheme {
        TelaNovoAgendamento(onBack = {}, onSalvar = {})
    }
}