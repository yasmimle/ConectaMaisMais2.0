package com.conecta.mobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.conecta.mobile.ui.theme.CardBackground
import com.conecta.mobile.ui.theme.ConectaMobileTheme
import com.conecta.mobile.ui.theme.DarkGreenPrimary
import com.conecta.mobile.ui.theme.LaranjaDestaque
import kotlinx.coroutines.launch

// CPF de teste do ajudante (fake). Só vale com usarApiFake = true.
val cpfAjudanteFake = "11111111111"

// Formata o texto digitado no padrão 000.000.000-00, só com números.
fun formatarCpf(input: String): String {
    val numeros = input.filter { it.isDigit() }.take(11)
    val builder = StringBuilder()

    for (i in numeros.indices) {
        builder.append(numeros[i])
        if (i == 2 || i == 5) builder.append(".")
        if (i == 8) builder.append("-")
    }

    return builder.toString()
}

// TelaLogin: todo mundo entra só com CPF. O app decide para qual tela mandar.
@Composable
fun TelaLogin(onLogin: () -> Unit, onLoginAjudante: () -> Unit) {

    var cpf by remember { mutableStateOf("") }
    var erro by remember { mutableStateOf(false) }
    var carregando by remember { mutableStateOf(false) }
    var mensagemErro by remember { mutableStateOf("") }
    val escopo = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkGreenPrimary)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = "Logo Conecta++",
            tint = Color.White,
            modifier = Modifier.size(88.dp)
        )

        Text(
            text = "Conecta++",
            color = Color.White,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 40.dp)
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "CPF", fontWeight = FontWeight.Bold, fontSize = 18.sp)

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = cpf,
                    onValueChange = {
                        cpf = formatarCpf(it)
                        erro = false
                        mensagemErro = ""
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = DarkGreenPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    },
                    placeholder = { Text("123.456.789-00", fontSize = 18.sp) },
                    textStyle = TextStyle(fontSize = 20.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    isError = erro,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                if (erro) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Digite um CPF válido (11 números)",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 16.sp
                    )
                }

                if (mensagemErro.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = mensagemErro,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val numeros = cpf.filter { it.isDigit() }

                        if (numeros.length != 11) {
                            erro = true
                        } else if (usarApiFake) {
                            // Modo de teste: sem API
                            if (numeros == cpfAjudanteFake) onLoginAjudante() else onLogin()
                        } else {
                            // Modo real: pergunta para a API
                            carregando = true
                            mensagemErro = ""
                            escopo.launch {
                                try {
                                    val resposta = api.loginCpf(mapOf("cpf" to numeros))
                                    val corpo = resposta.body()

                                    if (resposta.isSuccessful && corpo != null) {
                                        tokenAtual = corpo.get("token")?.asString
                                        tokenAtual = corpo.get("token")?.asString
                                        usuarioIdAtual = corpo.get("id")?.asInt ?: 0
                                        val tipo = corpo.get("tipo")?.asString
                                        if (tipo == "ajudante") onLoginAjudante() else onLogin()
                                    } else {
                                        mensagemErro = "CPF não encontrado. Procure a ONG."
                                    }
                                } catch (e: Exception) {
                                    mensagemErro = "Sem conexão com o servidor. Tente de novo."
                                }
                                carregando = false
                            }
                        }
                    },
                    enabled = !carregando,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LaranjaDestaque),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (carregando) "ENTRANDO..." else "ACESSAR MINHA CONTA",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TelaLoginPreview() {
    ConectaMobileTheme {
        TelaLogin(onLogin = {}, onLoginAjudante = {})
    }
}