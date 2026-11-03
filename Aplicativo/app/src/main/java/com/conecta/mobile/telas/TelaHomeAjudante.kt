package com.conecta.mobile

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.conecta.mobile.ui.theme.CardBackground
import com.conecta.mobile.ui.theme.ConectaMobileTheme
import com.conecta.mobile.ui.theme.DarkGreenPrimary
import com.conecta.mobile.ui.theme.LaranjaDestaque
import com.conecta.mobile.ui.theme.LightBackground
import com.conecta.mobile.ui.theme.StatusGreen
import com.conecta.mobile.ui.theme.StatusGreenBg
import com.conecta.mobile.ui.theme.StatusRed
import com.conecta.mobile.ui.theme.TextPrimary
import com.google.gson.JsonObject
import kotlinx.coroutines.launch

// Atendimento de exemplo (fake), só para quando usarApiFake = true.
// Usa o mesmo formato de JSON que a API devolve.
fun atendimentoFake(
    id: Int, hora: String, nome: String, telefone: String, tipo: String,
    origem: String, destino: String, duracaoMin: Int
): JsonObject {
    val obj = JsonObject()
    obj.addProperty("id", id)
    obj.addProperty("hora", hora)
    obj.addProperty("nomeIdoso", nome)
    obj.addProperty("telefoneIdoso", telefone)
    obj.addProperty("tipo", tipo)
    obj.addProperty("origem", origem)
    obj.addProperty("destino", destino)
    obj.addProperty("duracaoMin", duracaoMin)
    obj.addProperty("status", "Agendado")
    return obj
}

// Texto para o campo "tipo de ajuda" na tela do ajudante.
fun textoTipoAjuda(tipo: String): String {
    return when (tipo) {
        "Hospital" -> "Consulta médica"
        "Mercado" -> "Compras no mercado"
        "Farmácia", "Farmacia" -> "Remédios na farmácia"
        else -> tipo
    }
}

// TelaHomeAjudante: painel do ajudante, com os atendimentos do dia.
@Composable
fun TelaHomeAjudante(onSair: () -> Unit, onDisponibilidade: () -> Unit) {

    var lista by remember { mutableStateOf<List<JsonObject>>(emptyList()) }
    var carregando by remember { mutableStateOf(!usarApiFake) }
    var erroCarga by remember { mutableStateOf("") }
    var recarregar by remember { mutableStateOf(0) }

    // Ação escolhida (para o aviso "Tem certeza?"): id do atendimento e "concluir" ou "cancelar"
    var idAcao by remember { mutableStateOf(0) }
    var acao by remember { mutableStateOf("") }
    var mensagemAcao by remember { mutableStateOf("") }
    val escopo = rememberCoroutineScope()

    // Busca os atendimentos de hoje na API (e busca de novo quando "recarregar" muda).
    LaunchedEffect(recarregar) {
        if (usarApiFake) {
            if (lista.isEmpty()) {
                lista = listOf(
                    atendimentoFake(1, "08:00", "José Pereira", "(11) 91234-5678", "Hospital",
                        "Rua das Flores, 123 - Osasco/SP", "Hospital São Lucas", 180),
                    atendimentoFake(2, "13:30", "Maria Aparecida", "(11) 98765-4321", "Mercado",
                        "Av. Brasil, 450 - Osasco/SP", "Mercado Extra", 120),
                    atendimentoFake(3, "16:30", "Antônio Lima", "(11) 97654-3210", "Farmácia",
                        "Rua do Sol, 78 - Osasco/SP", "Farmácia Popular", 90)
                )
            }
        } else {
            carregando = true
            erroCarga = ""
            try {
                val resposta = api.agendaAjudante(usuarioIdAtual, hojeIso(), autorizacao())
                val corpo = resposta.body()

                if (resposta.isSuccessful && corpo != null) {
                    val nova = mutableListOf<JsonObject>()
                    for (item in corpo) {
                        nova.add(item.asJsonObject)
                    }
                    lista = nova
                } else {
                    erroCarga = mensagemDoErro(resposta, "Não foi possível carregar os atendimentos.")
                }
            } catch (e: Exception) {
                erroCarga = "Sem conexão com o servidor. Tente de novo."
            }
            carregando = false
        }
    }

    // Quantos ainda estão agendados
    val pendentes = lista.count { (it.get("status")?.asString ?: "") == "Agendado" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkGreenPrimary)
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Ajudante",
                tint = Color.White,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Área do ajudante", color = Color.White.copy(alpha = 0.85f), fontSize = 16.sp)
                Text("Olá!", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
        ) {
            Text(
                text = "ATENDIMENTOS DE HOJE",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (carregando) "Carregando..." else if (pendentes == 1) "1 atendimento agendado" else "$pendentes atendimentos agendados",
                fontSize = 16.sp,
                color = Color.Gray
            )

            if (erroCarga.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(erroCarga, fontSize = 16.sp, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }

            if (mensagemAcao.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(mensagemAcao, fontSize = 16.sp, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }

            if (!carregando && erroCarga.isEmpty() && lista.isEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Nenhum atendimento para hoje.", fontSize = 17.sp, color = TextPrimary)
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(lista.size) { i ->
                    val a = lista[i]
                    val duracaoMin = if (a.get("duracaoMin")?.isJsonNull == false) a.get("duracaoMin").asInt else 0

                    CardAtendimento(
                        hora = a.get("hora")?.asString ?: "",
                        idoso = a.get("nomeIdoso")?.asString ?: "",
                        telefone = a.get("telefoneIdoso")?.asString ?: "",
                        tipoAjuda = textoTipoAjuda(a.get("tipo")?.asString ?: ""),
                        casaIdoso = a.get("origem")?.asString ?: "",
                        destino = a.get("destino")?.asString ?: "",
                        duracao = if (duracaoMin > 0) formatarDuracao(duracaoMin) else "-",
                        status = a.get("status")?.asString ?: "Agendado",
                        onConcluir = {
                            mensagemAcao = ""
                            idAcao = a.get("id").asInt
                            acao = "concluir"
                        },
                        onCancelar = {
                            mensagemAcao = ""
                            idAcao = a.get("id").asInt
                            acao = "cancelar"
                        }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { onDisponibilidade() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkGreenPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("MINHA DISPONIBILIDADE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

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

    // "Tem certeza?" antes de concluir ou cancelar
    if (acao.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { acao = "" },
            title = {
                Text(
                    if (acao == "concluir") "Concluir atendimento?" else "Cancelar atendimento?",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    if (acao == "concluir") "O atendimento será marcado como Concluído."
                    else "O atendimento será marcado como Cancelado.",
                    fontSize = 18.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val id = idAcao
                    val fazer = acao
                    acao = ""

                    if (usarApiFake) {
                        // Modo de teste: muda o status só na tela.
                        val novoStatus = if (fazer == "concluir") "Concluído" else "Cancelado"
                        lista = lista.map { item ->
                            if (item.get("id").asInt == id) {
                                val copia = item.deepCopy()
                                copia.addProperty("status", novoStatus)
                                copia
                            } else item
                        }
                    } else {
                        escopo.launch {
                            try {
                                val resposta = if (fazer == "concluir")
                                    api.concluirAgendamento(id, autorizacao())
                                else
                                    api.cancelarAgendamento(id, autorizacao())

                                if (resposta.isSuccessful) {
                                    recarregar = recarregar + 1
                                } else {
                                    mensagemAcao = mensagemDoErro(resposta, "Não foi possível atualizar o atendimento.")
                                }
                            } catch (e: Exception) {
                                mensagemAcao = "Sem conexão com o servidor. Tente de novo."
                            }
                        }
                    }
                }) {
                    Text("SIM", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { acao = "" }) {
                    Text("VOLTAR", fontSize = 18.sp)
                }
            }
        )
    }
}

@Composable
fun CardAtendimento(
    hora: String,
    idoso: String,
    telefone: String,
    tipoAjuda: String,
    casaIdoso: String,
    destino: String,
    duracao: String,
    status: String,
    onConcluir: () -> Unit,
    onCancelar: () -> Unit
) {
    val context = LocalContext.current

    // Cor do status: verde = concluído, vermelho = cancelado
    val corStatus = when (status) {
        "Concluído" -> StatusGreen
        "Cancelado" -> StatusRed
        else -> DarkGreenPrimary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = StatusGreenBg,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = hora,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkGreenPrimary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(idoso, fontWeight = FontWeight.Bold, fontSize = 21.sp, color = TextPrimary)
                    Text(tipoAjuda, fontSize = 16.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(status, color = corStatus, fontWeight = FontWeight.Bold, fontSize = 17.sp)

            Divider(modifier = Modifier.padding(vertical = 14.dp), color = Color.LightGray.copy(alpha = 0.4f))

            LinhaDetalhe(Icons.Default.Phone, "Telefone do idoso:", telefone)
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.4f))
            LinhaDetalhe(Icons.Default.Home, "Casa do idoso:", casaIdoso)
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.4f))
            LinhaDetalhe(Icons.Default.LocationOn, "Destino:", destino)
            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.4f))
            LinhaDetalhe(Icons.Default.Schedule, "Duração prevista:", duracao)

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                color = DarkGreenPrimary.copy(alpha = 0.08f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("TRAJETO", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ONG → Casa do idoso → $destino → Casa do idoso → ONG",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkGreenPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val numero = telefone.filter { it.isDigit() }
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$numero"))
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkGreenPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Call, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(10.dp))
                Text("LIGAR PARA O IDOSO", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }

            // Só dá para concluir ou cancelar o que ainda está agendado.
            if (status == "Agendado") {
                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { onConcluir() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LaranjaDestaque),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("CONCLUIR ATENDIMENTO", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = { onCancelar() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(2.dp, StatusRed)
                ) {
                    Text("CANCELAR ATENDIMENTO", color = StatusRed, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TelaHomeAjudantePreview() {
    ConectaMobileTheme {
        TelaHomeAjudante(onSair = {}, onDisponibilidade = {})
    }
}
