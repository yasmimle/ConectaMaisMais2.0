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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.conecta.mobile.ui.theme.CardBackground
import com.conecta.mobile.ui.theme.ConectaMobileTheme
import com.conecta.mobile.ui.theme.DarkGreenPrimary
import com.conecta.mobile.ui.theme.LaranjaDestaque
import kotlinx.coroutines.launch

// CPF de teste do ajudante (fake). Só vale com usarApiFake = true.
val cpfAjudanteFake = "52998224725"

// Confere os dígitos do CPF (a API faz a mesma conta).
fun cpfValido(cpf: String): Boolean {
    if (cpf.length != 11 || cpf.any { !it.isDigit() }) return false
    if (cpf.all { it == cpf[0] }) return false

    for (t in 9..10) {
        var soma = 0
        for (i in 0 until t) {
            soma += (cpf[i] - '0') * (t + 1 - i)
        }
        var dv = (soma * 10) % 11
        if (dv == 10) dv = 0
        if (dv != cpf[t] - '0') return false
    }
    return true
}

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

// Máscara do CPF aplicada só na exibição: o estado guarda os dígitos crus, então
// reescrever o texto a cada tecla não reposiciona o cursor (era o que embaralhava
// os dígitos quando se digitava rápido).
class CpfVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digitos = text.text
        val saida = StringBuilder()
        for (i in digitos.indices) {
            saida.append(digitos[i])
            if (i == 2 || i == 5) saida.append('.')
            if (i == 8) saida.append('-')
        }

        val deslocamento: (Int) -> Int = { o ->
            (if (o >= 3) 1 else 0) + (if (o >= 6) 1 else 0) + (if (o >= 9) 1 else 0)
        }

        val map = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = offset + deslocamento(offset)
            override fun transformedToOriginal(offset: Int): Int {
                var o = offset
                if (offset > 3) o--
                if (offset > 7) o--
                if (offset > 11) o--
                return o.coerceIn(0, digitos.length)
            }
        }
        return TransformedText(AnnotatedString(saida.toString()), map)
    }
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
                        cpf = it.filter { c -> c.isDigit() }.take(11)
                        erro = false
                        mensagemErro = ""
                    },
                    visualTransformation = CpfVisualTransformation(),
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
                        text = "Digite um CPF válido",
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

                        if (!cpfValido(numeros)) {
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
                                        usuarioIdAtual = corpo.get("id")?.asInt ?: 0
                                        val tipo = corpo.get("tipo")?.asString
                                        if (tipo == "ajudante") onLoginAjudante() else onLogin()
                                    } else if (resposta.code() == 401) {
                                        mensagemErro = "CPF não encontrado. Procure a ONG."
                                    } else {
                                        mensagemErro = mensagemDoErro(resposta, "Não foi possível entrar. Tente de novo.")
                                    }
                                } catch (e: Exception) {
                                    // Diagnóstico: registra a causa real no logcat (tag ConectaLogin).
                                    android.util.Log.e("ConectaLogin", "falha no login por CPF: " + e.toString(), e)
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