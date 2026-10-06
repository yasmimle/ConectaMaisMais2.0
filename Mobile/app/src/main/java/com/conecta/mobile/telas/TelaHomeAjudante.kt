package com.conecta.mobile

import com.conecta.mobile.ui.theme.LaranjaDestaque
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
import androidx.compose.runtime.Composable
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
import com.conecta.mobile.ui.theme.LightBackground
import com.conecta.mobile.ui.theme.StatusGreenBg
import com.conecta.mobile.ui.theme.StatusRed
import com.conecta.mobile.ui.theme.TextPrimary

// TelaHomeAjudante: painel do ajudante, com os atendimentos do dia.
@Composable
fun TelaHomeAjudante(onSair: () -> Unit) {

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
                text = "3 atendimentos agendados",
                fontSize = 16.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Dados de exemplo (fake), enquanto a API não está pronta.
            LazyColumn(modifier = Modifier.weight(1f)) {
                item {
                    CardAtendimento(
                        hora = "08:00",
                        idoso = "José Pereira",
                        telefone = "(11) 91234-5678",
                        tipoAjuda = "Consulta médica",
                        casaIdoso = "Rua das Flores, 123 - Osasco/SP",
                        destino = "Hospital São Lucas",
                        duracao = "3h00"
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }
                item {
                    CardAtendimento(
                        hora = "13:30",
                        idoso = "Maria Aparecida",
                        telefone = "(11) 98765-4321",
                        tipoAjuda = "Compras no mercado",
                        casaIdoso = "Av. Brasil, 450 - Osasco/SP",
                        destino = "Mercado Extra",
                        duracao = "2h00"
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }
                item {
                    CardAtendimento(
                        hora = "16:30",
                        idoso = "Antônio Lima",
                        telefone = "(11) 97654-3210",
                        tipoAjuda = "Remédios na farmácia",
                        casaIdoso = "Rua do Sol, 78 - Osasco/SP",
                        destino = "Farmácia Popular",
                        duracao = "1h30"
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }
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
}

@Composable
fun CardAtendimento(
    hora: String,
    idoso: String,
    telefone: String,
    tipoAjuda: String,
    casaIdoso: String,
    destino: String,
    duracao: String
) {
    val context = LocalContext.current

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

                Column {
                    Text(idoso, fontWeight = FontWeight.Bold, fontSize = 21.sp, color = TextPrimary)
                    Text(tipoAjuda, fontSize = 16.sp, color = Color.Gray)
                }
            }

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
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TelaHomeAjudantePreview() {
    ConectaMobileTheme {
        TelaHomeAjudante(onSair = {})
    }
}