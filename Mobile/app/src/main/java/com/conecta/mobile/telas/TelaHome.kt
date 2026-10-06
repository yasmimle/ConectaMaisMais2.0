package com.conecta.mobile

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.conecta.mobile.ui.theme.CardBackground
import com.conecta.mobile.ui.theme.ConectaMobileTheme
import com.conecta.mobile.ui.theme.DarkGreenPrimary
import com.conecta.mobile.ui.theme.LightBackground
import com.conecta.mobile.ui.theme.StatusGreen
import com.conecta.mobile.ui.theme.TextPrimary

// Telefone da ONG (o mesmo número que está no site). Confira se é o número certo.
val telefoneOng = "11915160209"

// TelaHome: tela principal, mostrada depois do login.
@Composable
fun TelaHome(
    onNovoAgendamento: () -> Unit,
    onVerHistorico: () -> Unit,
    onVerPerfil: () -> Unit
) {
    val context = LocalContext.current
    var mostrarContato by remember { mutableStateOf(false) }

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
            Image(
                painter = painterResource(id = R.drawable.img_home_perfil),
                contentDescription = "Foto do perfil",
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.White, CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Bem-vindo(a),", color = Color.White.copy(alpha = 0.85f), fontSize = 16.sp)
                Text("Olá!", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            HomeMenuCardFoto(
                titulo = "AGENDAR AJUDA",
                imagem = R.drawable.img_agendar_calendario,
                onClick = onNovoAgendamento
            )
            Spacer(modifier = Modifier.height(16.dp))
            HomeMenuCardFoto(
                titulo = "MEU HISTÓRICO",
                imagem = R.drawable.img_home_historico,
                onClick = onVerHistorico
            )
            Spacer(modifier = Modifier.height(16.dp))
            HomeMenuCardFoto(
                titulo = "MEU PERFIL",
                imagem = R.drawable.img_home_perfil,
                onClick = onVerPerfil
            )
            Spacer(modifier = Modifier.height(16.dp))
            HomeMenuCardFoto(
                titulo = "FALAR COM A ONG",
                imagem = R.drawable.img_home_ong,
                onClick = { mostrarContato = true }
            )
        }
    }

    // Janelinha com as duas formas de falar com a ONG
    if (mostrarContato) {
        AlertDialog(
            onDismissRequest = { mostrarContato = false },
            title = {
                Text("Falar com a ONG", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Como você prefere falar com a gente?", fontSize = 18.sp)
            },
            confirmButton = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$telefoneOng"))
                            context.startActivity(intent)
                            mostrarContato = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkGreenPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("LIGAR", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/55$telefoneOng"))
                            context.startActivity(intent)
                            mostrarContato = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("WHATSAPP", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarContato = false }) {
                    Text("Cancelar", fontSize = 17.sp)
                }
            }
        )
    }
}

// Card grande do menu: imagem à esquerda, título em letra grande e seta à direita.
@Composable
fun HomeMenuCardFoto(titulo: String, imagem: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = imagem),
                contentDescription = titulo,
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(18.dp))
            Text(
                text = titulo,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = DarkGreenPrimary,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TelaHomePreview() {
    ConectaMobileTheme {
        TelaHome(
            onNovoAgendamento = {},
            onVerHistorico = {},
            onVerPerfil = {}
        )
    }
}