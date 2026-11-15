package com.conecta.mobile

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.OkHttpClient
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

// Endereço da API. No emulador do Android Studio, 10.0.2.2 é o computador.
// A porta 5000 é a do launchSettings.json da API.
// Em celular de verdade, troque pelo IP do computador (ex.: http://192.168.0.10:5000/).
const val URL_BASE_API = "http://10.0.2.2:5000/"

// Dados do login (ficam na memória enquanto o app estiver aberto).
var tokenAtual: String? = null
// Id de quem entrou: do idoso (USUARIO) ou do ajudante (AJUDANTE), conforme o tipo do login.
var usuarioIdAtual: Int = 0
// Agendamento que a pessoa tocou no Histórico (os campos que a API devolveu:
// status, data, hora, tipo, origem, destino, descricao).
var agendamentoAtual: JsonObject = JsonObject()

// Endereço da ONG (o mesmo que está no banco de dados).
const val ENDERECO_ONG = "Rua Gugu, São Paulo SP"

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
        @Body corpo: Map<String, Any>,
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

// A API pode demorar um pouco ao calcular a distância pelos endereços.
private val clienteHttp = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(40, TimeUnit.SECONDS)
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
