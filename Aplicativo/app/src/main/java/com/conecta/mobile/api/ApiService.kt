package com.conecta.mobile

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.Calendar
import java.util.concurrent.TimeUnit

// true = o app usa dados de exemplo (sem API). false = usa a API de verdade.
val usarApiFake = false

// Endereço da API. Vem do build: no debug aponta para 127.0.0.1 (túnel do adb,
// com `adb reverse tcp:5000 tcp:5000`) e no release para o 10.0.2.2/host.
// Em celular de verdade fora do túnel, troque no build.gradle.kts pelo IP do
// computador (ex.: http://192.168.0.10:5000/).
val URL_BASE_API: String = BuildConfig.API_BASE_URL

// Dados do login (ficam na memória enquanto o app estiver aberto).
var tokenAtual: String? = null
// Id de quem entrou: do idoso (USUARIO) ou do ajudante (AJUDANTE), conforme o tipo do login.
var usuarioIdAtual: Int = 0
// Agendamento que a pessoa tocou no Histórico (os campos que a API devolveu:
// status, data, hora, tipo, origem, destino, descricao).
var agendamentoAtual: JsonObject = JsonObject()

// Endereço da ONG (o mesmo que está no banco de dados).
const val ENDERECO_ONG = "Rua Gugu, São Paulo SP"

// Sair da conta: limpa o que ficou guardado do login em memória.
fun encerrarSessao() {
    tokenAtual = null
    usuarioIdAtual = 0
    agendamentoAtual = JsonObject()
}

// Corpo do POST /agendamentos. Precisa ser uma classe concreta: com
// Map<String, Any> o Retrofit recusa a assinatura em tempo de execução
// ("Parameter type must not include a type variable or wildcard: java.util.Map"),
// porque o Kotlin gera Map<String, ?> na assinatura do método. Era por isso que
// nenhum agendamento saía do app, mesmo com o servidor no ar.
data class NovoAgendamento(
    val idUsuario: Int,
    val data: String,
    val hora: String,
    val tipo: String,
    val origem: String,
    val destino: String
)

interface ApiService {

    @POST("auth/login-cpf")
    suspend fun loginCpf(@Body corpo: Map<String, String>): Response<JsonObject>

    @GET("ong/contato")
    suspend fun contatoOng(): Response<JsonObject>

    // ----- Idoso -----

    @GET("usuarios/{id}")
    suspend fun perfilUsuario(
        @Path("id") id: Int,
        @Header("Authorization") autorizacao: String
    ): Response<JsonObject>

    @POST("agendamentos")
    suspend fun criarAgendamento(
        @Body corpo: NovoAgendamento,
        @Header("Authorization") autorizacao: String
    ): Response<JsonObject>

    // Horários livres de um dia para o tipo de ajuda (de 30 em 30 minutos).
    @GET("agendamentos/horarios-livres")
    suspend fun horariosLivres(
        @Query("data") data: String,
        @Query("tipo") tipo: String,
        @Header("Authorization") autorizacao: String
    ): Response<JsonArray>

    @GET("agendamentos/usuario/{id}")
    suspend fun listarAgendamentos(
        @Path("id") id: Int,
        @Header("Authorization") autorizacao: String
    ): Response<JsonArray>

    // ----- Ajudante -----

    @GET("agendamentos/ajudante/{id}")
    suspend fun agendaAjudante(
        @Path("id") id: Int,
        @Query("data") data: String,
        @Header("Authorization") autorizacao: String
    ): Response<JsonArray>

    @PUT("agendamentos/{id}/concluir")
    suspend fun concluirAgendamento(
        @Path("id") id: Int,
        @Header("Authorization") autorizacao: String
    ): Response<JsonObject>

    @PUT("agendamentos/{id}/cancelar")
    suspend fun cancelarAgendamento(
        @Path("id") id: Int,
        @Header("Authorization") autorizacao: String
    ): Response<JsonObject>

    @GET("ajudantes/{id}/disponibilidade")
    suspend fun listarDisponibilidade(
        @Path("id") id: Int,
        @Header("Authorization") autorizacao: String
    ): Response<JsonArray>

    @POST("ajudantes/{id}/disponibilidade")
    suspend fun adicionarDisponibilidade(
        @Path("id") id: Int,
        @Body corpo: Map<String, String>,
        @Header("Authorization") autorizacao: String
    ): Response<JsonObject>

    @DELETE("ajudantes/disponibilidade/{idDisponibilidade}")
    suspend fun removerDisponibilidade(
        @Path("idDisponibilidade") idDisponibilidade: Int,
        @Header("Authorization") autorizacao: String
    ): Response<Unit>
}

// Endereços possíveis do servidor, na ordem em que o app tenta:
//  - o que veio do build (BuildConfig.API_BASE_URL);
//  - 10.0.2.2, que é o computador visto de dentro do emulador do Android — funciona
//    em qualquer máquina, sem precisar de comando nenhum;
//  - 127.0.0.1, que só responde com o túnel do adb (adb reverse tcp:5000 tcp:5000),
//    usado quando o app roda em celular ligado por USB.
fun candidatosDeServidor(): List<String> {
    val lista = mutableListOf<String>()
    for (endereco in listOf(BuildConfig.API_BASE_URL, "http://10.0.2.2:5000/", "http://127.0.0.1:5000/")) {
        val normalizado = if (endereco.endsWith("/")) endereco else "$endereco/"
        if (!lista.contains(normalizado)) lista.add(normalizado)
    }
    return lista
}

// Endereço que respondeu por último. Fica guardado para não repetir a descoberta
// a cada chamada; se a rede cair, ele é zerado e a próxima chamada descobre de novo.
@Volatile
private var baseConfirmada: String? = null

// Troca só esquema/host/porta da requisição, mantendo caminho, query e corpo.
private fun Request.comBase(base: String): Request {
    val outra = HttpUrl.parse(base) ?: return this
    val novo = url().newBuilder()
        .scheme(outra.scheme())
        .host(outra.host())
        .port(outra.port())
        .build()
    return newBuilder().url(novo).build()
}

// Descobre qual endereço do servidor responde e manda a chamada para ele.
// Sem isso o app dependia de alguém rodar `adb reverse` na mão para funcionar.
private class DescobrirServidorInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val original = chain.request()
        val confirmado = baseConfirmada

        if (confirmado != null) {
            try {
                return chain.proceed(original.comBase(confirmado))
            } catch (e: java.io.IOException) {
                // O endereço que funcionava parou de responder: esquece e redescobre
                // na próxima chamada (não insiste aqui para não duplicar o envio,
                // que pode ser um agendamento).
                baseConfirmada = null
                throw e
            }
        }

        // Sonda curta: uma chamada de leitura em cada endereço até achar quem responde.
        val sonda = OkHttpClient.Builder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(3, TimeUnit.SECONDS)
            .build()

        for (base in candidatosDeServidor()) {
            val raiz = HttpUrl.parse(base) ?: continue
            val pedido = Request.Builder()
                .url(raiz.newBuilder().addPathSegments("ong/contato").build())
                .build()
            try {
                sonda.newCall(pedido).execute().use { resposta ->
                    if (resposta.isSuccessful) {
                        baseConfirmada = base
                        android.util.Log.i("ConectaServidor", "servidor encontrado em $base")
                        return chain.proceed(original.comBase(base))
                    }
                }
            } catch (e: Exception) {
                android.util.Log.i("ConectaServidor", "sem resposta em $base: " + e.toString())
            }
        }

        // Nenhum respondeu: deixa a chamada original falhar com o erro de verdade.
        android.util.Log.e("ConectaServidor", "nenhum endereco respondeu: " + candidatosDeServidor())
        return chain.proceed(original)
    }
}

// A API pode demorar um pouco ao calcular a distância pelos endereços.
private val clienteHttp = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(40, TimeUnit.SECONDS)
    .addInterceptor(DescobrirServidorInterceptor())
    .build()

val api: ApiService = Retrofit.Builder()
    .baseUrl(URL_BASE_API)
    .client(clienteHttp)
    .addConverterFactory(GsonConverterFactory.create())
    .build()
    .create(ApiService::class.java)

// ---------------------------------------------------------------------
// Funções de apoio (sem class, como pede o professor)
// ---------------------------------------------------------------------

// Texto "Bearer ..." que a API pede nas rotas do app.
fun autorizacao(): String = "Bearer " + (tokenAtual ?: "")

// A API devolve erros assim: { "erro": "mensagem" }. Esta função pega a mensagem.
fun mensagemDoErro(resposta: Response<*>, padrao: String): String {
    return try {
        val texto = resposta.errorBody()?.string()
        if (texto.isNullOrBlank()) {
            padrao
        } else {
            JsonParser.parseString(texto).asJsonObject.get("erro")?.asString ?: padrao
        }
    } catch (e: Exception) {
        padrao
    }
}

// Lê um texto de um JsonObject (devolve "" se o campo não existir ou vier nulo).
fun textoDe(obj: JsonObject, campo: String): String {
    val valor = obj.get(campo)
    return if (valor == null || valor.isJsonNull) "" else valor.asString
}

// Data de hoje no formato da API: yyyy-MM-dd
fun hojeIso(): String {
    val c = Calendar.getInstance()
    val ano = c.get(Calendar.YEAR)
    val mes = (c.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
    val dia = c.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
    return "$ano-$mes-$dia"
}

// "26/10/2026" -> "2026-10-26" (devolve "" se o texto não tiver o tamanho certo)
fun dataBrParaIso(dataBr: String): String {
    if (dataBr.length != 10) return ""
    return dataBr.substring(6, 10) + "-" + dataBr.substring(3, 5) + "-" + dataBr.substring(0, 2)
}

// Duração por tipo de ajuda, em minutos (as mesmas regras da API).
fun duracaoPorTipo(tipo: String): Int {
    return when (tipo) {
        "Hospital" -> 180
        "Mercado" -> 120
        else -> 90
    }
}

// 180 -> "3h00", 90 -> "1h30"
fun formatarDuracao(minutos: Int): String {
    val h = minutos / 60
    val m = (minutos % 60).toString().padStart(2, '0')
    return "${h}h$m"
}
