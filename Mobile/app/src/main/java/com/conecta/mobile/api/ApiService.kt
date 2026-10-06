package com.conecta.mobile

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

// Enquanto a API não está pronta, deixe true (o app usa os dados de exemplo).
// Quando o backend estiver no ar, troque para false.
val usarApiFake = true

// Endereço da API (a porta quem diz é a pessoa do backend).
const val URL_BASE_API = "http://10.0.2.2:5000/"

// Dados do login (ficam na memória enquanto o app estiver aberto).
var tokenAtual: String? = null
var usuarioIdAtual: Int = 0
// Agendamento que a pessoa tocou no Histórico (status, data, descrição).
var agendamentoAtual: Triple<String, String, String> = Triple("", "", "")

interface ApiService {

    @POST("auth/login-cpf")
    suspend fun loginCpf(@Body corpo: Map<String, String>): Response<JsonObject>

    @GET("agendamentos/usuario/{id}")
    suspend fun listarAgendamentos(
        @Path("id") id: Int,
        @Header("Authorization") autorizacao: String
    ): Response<JsonArray>
}

val api: ApiService = Retrofit.Builder()
    .baseUrl(URL_BASE_API)
    .addConverterFactory(GsonConverterFactory.create())
    .build()
    .create(ApiService::class.java)