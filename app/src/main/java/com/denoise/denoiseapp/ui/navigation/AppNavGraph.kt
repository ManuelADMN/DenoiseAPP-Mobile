package com.denoise.denoiseapp.ui.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ListAlt
import androidx.compose.material.icons.filled.SupervisedUserCircle
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.denoise.denoiseapp.core.ui.theme.DenoiseTheme
import com.denoise.denoiseapp.core.util.SessionManager
import com.denoise.denoiseapp.presentation.report.DetailViewModel
import com.denoise.denoiseapp.presentation.report.FormViewModel
import com.denoise.denoiseapp.presentation.report.ListViewModel
import com.denoise.denoiseapp.ui.admin.AdminUsersScreen
import com.denoise.denoiseapp.ui.auth.LoginScreen
import com.denoise.denoiseapp.ui.auth.RegisterScreen
import com.denoise.denoiseapp.ui.dashboard.DashboardScreen
import com.denoise.denoiseapp.ui.report.detail.ReportDetailScreen
import com.denoise.denoiseapp.ui.report.form.ReportFormScreen
import com.denoise.denoiseapp.ui.report.list.ReportListScreen
import com.denoise.denoiseapp.ui.settings.SettingsScreen
import com.denoise.denoiseapp.ui.weather.WeatherScreen

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val DASHBOARD = "dashboard"
    const val LIST = "list"
    const val MAPA = "mapa"
    const val FORM = "form"
    const val DETAIL = "detail/{id}"
    const val SETTINGS = "settings"
    const val ADMIN_USERS = "admin_users"
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavGraph(modifier: Modifier = Modifier) {
    val nav = rememberNavController()
    var darkTheme by rememberSaveable { mutableStateOf(false) }

    // --- CONTROL DE SESIÓN ---
    val context = LocalContext.current
    // Recordamos el SessionManager para no recrearlo en cada recomposición
    val session = remember { SessionManager(context) }

    // Decidimos la pantalla de inicio: Dashboard si ya está logueado, sino Login
    val startRoute = if (session.isLoggedIn()) Routes.DASHBOARD else Routes.LOGIN

    DenoiseTheme(darkTheme = darkTheme) {

        // Observamos la ruta actual. Esto fuerza la recomposición cuando navegamos.
        val navBackStackEntry by nav.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route.normalize()

        // Calculamos isAdmin AQUÍ, dependiente de currentRoute.
        // Así, cada vez que navegamos (ej. Login -> Dashboard), se verifica el rol de nuevo.
        val isAdmin = remember(currentRoute) { session.isAdmin() }

        // Definimos en qué pantallas se debe mostrar la barra de navegación inferior
        val showBottomBar = currentRoute in setOf(
            Routes.LIST,
            Routes.DASHBOARD,
            Routes.MAPA,
            Routes.SETTINGS,
            Routes.ADMIN_USERS
        )

        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ) {
                        val items = mutableListOf(
                            Triple(Routes.LIST, "Reportes", Icons.AutoMirrored.Outlined.ListAlt),
                            Triple(Routes.DASHBOARD, "Dash", Icons.Outlined.Assessment),
                            Triple(Routes.MAPA, "Mapa", Icons.Outlined.Map)
                        )

                        // Botón Extra solo para Admin
                        if (isAdmin) {
                            items.add(Triple(Routes.ADMIN_USERS, "Usuarios", Icons.Default.SupervisedUserCircle))
                        }

                        // Ajustes siempre visible
                        items.add(Triple(Routes.SETTINGS, "Ajustes", Icons.Outlined.Settings))

                        items.forEach { (route, label, icon) ->
                            // CORRECCIÓN: Usamos directamente 'currentRoute' que declaramos arriba
                            // y eliminamos la referencia a 'backRoute' que daba error
                            val isSelected = currentRoute == route

                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    if (!isSelected) {
                                        nav.navigate(route) {
                                            // PopUp hasta el inicio del grafo para no acumular pantallas
                                            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = { Icon(icon, contentDescription = label) },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(Modifier.padding(innerPadding)) {
                NavHost(
                    navController = nav,
                    startDestination = startRoute,
                    modifier = modifier
                ) {
                    // --- AUTENTICACIÓN ---
                    composable(Routes.LOGIN) {
                        LoginScreen(
                            onLoginSuccess = {
                                nav.navigate(Routes.DASHBOARD) {
                                    popUpTo(Routes.LOGIN) { inclusive = true } // Borra login del historial
                                }
                            },
                            onNavigateToRegister = { nav.navigate(Routes.REGISTER) }
                        )
                    }
                    composable(Routes.REGISTER) {
                        RegisterScreen(
                            onRegisterSuccess = {
                                nav.navigate(Routes.DASHBOARD) {
                                    popUpTo(Routes.LOGIN) { inclusive = true }
                                }
                            },
                            onBack = { nav.popBackStack() }
                        )
                    }

                    // --- PANTALLAS PRINCIPALES ---

                    composable(Routes.DASHBOARD) {
                        DashboardScreen(onOpenSettings = { nav.navigate(Routes.SETTINGS) })
                    }

                    composable(Routes.LIST) {
                        val vm: ListViewModel = viewModel()
                        ReportListScreen(
                            vm = vm,
                            onAdd = { nav.navigate(Routes.FORM) },
                            onOpen = { id -> nav.navigate("detail/$id") },
                            onEdit = { id -> nav.navigate("form?id=$id") },
                            onOpenSettings = { nav.navigate(Routes.SETTINGS) }
                        )
                    }

                    composable(Routes.MAPA) {
                        WeatherScreen(isAdmin = isAdmin) // Pasamos permiso de Admin al Mapa
                    }

                    // --- FORMULARIO DE REPORTE ---
                    composable(
                        route = "${Routes.FORM}?id={id}",
                        arguments = listOf(navArgument("id") { type = NavType.StringType; nullable = true })
                    ) { backStackEntry ->
                        val vm: FormViewModel = viewModel()
                        val id = backStackEntry.arguments?.getString("id")
                        if (id != null) vm.cargarParaEditar(id)

                        ReportFormScreen(
                            vm = vm,
                            onSaved = {
                                // Navegación segura al guardar
                                nav.popBackStack()
                            }
                        )
                    }

                    // --- DETALLE DE REPORTE ---
                    composable(
                        Routes.DETAIL,
                        arguments = listOf(navArgument("id") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val vm: DetailViewModel = viewModel()
                        val id = backStackEntry.arguments?.getString("id")!!
                        vm.cargar(id)

                        ReportDetailScreen(
                            vm = vm,
                            onEdit = { nav.navigate("form?id=$id") },
                            onBack = { nav.popBackStack() },
                            isAdmin = isAdmin // Pasamos el rol para activar funciones de admin
                        )
                    }

                    // --- AJUSTES (Con Logout) ---
                    composable(Routes.SETTINGS) {
                        SettingsScreen(
                            isDarkTheme = darkTheme,
                            onToggleTheme = { darkTheme = it },
                            onLogout = {
                                session.logout()
                                // Redirige al login y borra todo el stack anterior
                                nav.navigate(Routes.LOGIN) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }

                    // --- ADMINISTRACIÓN DE USUARIOS (Solo Admin) ---
                    composable(Routes.ADMIN_USERS) {
                        if (!isAdmin) {
                            // Protección: Si entra por URL y no es admin, lo mandamos al dashboard
                            LaunchedEffect(Unit) { nav.navigate(Routes.DASHBOARD) }
                        } else {
                            AdminUsersScreen()
                        }
                    }
                }
            }
        }
    }
}

// Helper para normalizar rutas y determinar qué botón resaltar en la barra inferior
private fun String?.normalize(): String {
    val raw = this ?: return Routes.LOGIN
    val base = raw.substringBefore("?")
    return when {
        base.startsWith("detail") || base.startsWith("form") -> Routes.LIST
        else -> base
    }
}