package com.conecta.mobile.navigation

import com.conecta.mobile.ui.theme.LaranjaDestaque
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.conecta.mobile.TelaDetalhesAgendamento
import com.conecta.mobile.TelaHistorico
import com.conecta.mobile.TelaHome
import com.conecta.mobile.TelaHomeAjudante
import com.conecta.mobile.TelaLogin
import com.conecta.mobile.TelaNovoAgendamento
import com.conecta.mobile.TelaPerfil

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            TelaLogin(
                onLogin = {
                    navController.navigate("home")
                },
                onLoginAjudante = {
                    navController.navigate("homeAjudante")
                }
            )
        }

        composable("homeAjudante") {
            TelaHomeAjudante(
                onSair = {
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            TelaHome(
                onNovoAgendamento = {
                    navController.navigate("novoAgendamento")
                },
                onVerHistorico = {
                    navController.navigate("historico")
                },
                onVerPerfil = {
                    navController.navigate("perfil")
                }
            )
        }

        composable("historico") {
            TelaHistorico(
                onBack = { navController.popBackStack() },
                onAgendamentoClick = {
                    navController.navigate("detalhesAgendamento")
                }
            )
        }

        composable("novoAgendamento") {
            TelaNovoAgendamento(
                onBack = { navController.popBackStack() },
                onSalvar = {
                    navController.popBackStack()
                }
            )
        }

        composable("perfil") {
            TelaPerfil(
                onBack = { navController.popBackStack() },
                onSair = {
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("detalhesAgendamento") {
            TelaDetalhesAgendamento(
                onBack = { navController.popBackStack() }
            )
        }
    }
}