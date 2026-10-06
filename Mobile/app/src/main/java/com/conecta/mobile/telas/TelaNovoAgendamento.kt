package com.conecta.mobile

import com.conecta.mobile.ui.theme.LaranjaDestaque
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.conecta.mobile.ui.theme.CardBackground
import com.conecta.mobile.ui.theme.ConectaMobileTheme
import com.conecta.mobile.ui.theme.DarkGreenPrimary
import com.conecta.mobile.ui.theme.LightBackground
import com.conecta.mobile.ui.theme.TextPrimary
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelaNovoAgendamento(onBack: () -> Unit, onSalvar: () -> Unit) {

    var tipoSelecionado by remember { mutableStateOf("Hospital") }
    var origem by remember { mutableStateOf("Rua das Flores, 123 - Osasco/SP") }
    var destino by remember { mutableStateOf("") }
    var dataTexto by remember { mutableStateOf("") }
    var horaTexto by remember { mutableStateOf("") }

    var mostrarDatePicker by remember { mutableStateOf(false) }
    var mostrarTimePicker by remember { mutableStateOf(false) }

    var erroOrigem by remember { mutableStateOf(false) }
    var erroDestino by remember { mutableStateOf(false) }
    var erroData by remember { mutableStateOf(false) }
    var erroHora by remember { mutableStateOf(false) }

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

        Column(modifier = Modifier.padding(16.dp)) {
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
                        Text("Escolha uma data", color = MaterialTheme.colorScheme.error, fontSize = 15.sp)
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text("Hora", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = horaTexto,
                        onValueChange = { },
                        placeholder = { Text("Toque para escolher a hora", fontSize = 16.sp) },
                        textStyle = TextStyle(fontSize = 18.sp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 60.dp)
                            .clickable {
                                mostrarTimePicker = true
                                erroHora = false
                            },
                        leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null, tint = DarkGreenPrimary, modifier = Modifier.size(26.dp)) },
                        shape = RoundedCornerShape(10.dp),
                        enabled = false,
                        isError = erroHora,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = TextPrimary,
                            disabledBorderColor = if (erroHora) MaterialTheme.colorScheme.error else Color.Gray,
                            disabledLeadingIconColor = DarkGreenPrimary,
                            disabledPlaceholderColor = Color.Gray
                        )
                    )
                    if (erroHora) {
                        Text("Escolha um horário", color = MaterialTheme.colorScheme.error, fontSize = 15.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    erroOrigem = origem.isBlank()
                    erroDestino = destino.isBlank()
                    erroData = dataTexto.isBlank()
                    erroHora = horaTexto.isBlank()

                    if (!erroOrigem && !erroDestino && !erroData && !erroHora) {
                        onSalvar()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkGreenPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("CONFIRMAR AGENDAMENTO", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        }
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

    if (mostrarTimePicker) {
        val estadoHora = rememberTimePickerState(initialHour = 8, is24Hour = true)
        var mensagemInvalida by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { mostrarTimePicker = false }) {
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Horário de atendimento: 07:00 às 20:00",
                        fontSize = 15.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    TimePicker(state = estadoHora)

                    if (mensagemInvalida) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Escolha um horário entre 07:00 e 20:00",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row {
                        TextButton(onClick = { mostrarTimePicker = false }) {
                            Text("Cancelar", fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = {
                            val hora = estadoHora.hour
                            val minuto = estadoHora.minute

                            val dentroDoHorario = hora in 7..20 && !(hora == 20 && minuto > 0)

                            if (dentroDoHorario) {
                                val h = hora.toString().padStart(2, '0')
                                val m = minuto.toString().padStart(2, '0')
                                horaTexto = "$h:$m"
                                erroHora = false
                                mostrarTimePicker = false
                            } else {
                                mensagemInvalida = true
                            }
                        }) {
                            Text("OK", fontSize = 16.sp)
                        }
                    }
                }
            }
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