package com.denoise.denoiseapp.ui.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ListAlt // Icono corregido (espejado)
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Map // Icono para el mapa
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.denoise.denoiseapp.core.ui.theme.DenoiseTheme
import com.denoise.denoiseapp.presentation.report.DetailViewModel
import com.denoise.denoiseapp.presentation.report.FormViewModel
import com.denoise.denoiseapp.presentation.report.ListViewModel
import com.denoise.denoiseapp.ui.dashboard.DashboardScreen
import com.denoise.denoiseapp.ui.report.detail.ReportDetailScreen
import com.denoise.denoiseapp.ui.report.form.ReportFormScreen
import com.denoise.denoiseapp.ui.report.list.ReportListScreen
import com.denoise.denoiseapp.ui.settings.SettingsScreen
import com.denoise.denoiseapp.ui.weather.WeatherScreen

// Definición de todas las rutas de navegación
object Routes {
    const val DASHBOARD = "dashboard"
    const val LIST = "list"
    const val MAPA = "mapa" // Nueva ruta para el mapa/clima
    const val FORM = "form"
    const val DETAIL = "detail/{id}"
    const val SETTINGS = "settings"
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavGraph(modifier: Modifier = Modifier) {
    val nav = rememberNavController()
    var darkTheme by rememberSaveable { mutableStateOf(false) } // Estado simple para el tema

    DenoiseTheme(darkTheme = darkTheme) {

        // Obtenemos la ruta actual para saber qué botón de la barra inferior resaltar
        val currentRoute = nav.currentBackStackEntryAsState().value
            ?.destination?.route.normalize()

        // Pantallas que deben mostrar la barra de navegación inferior
        val showBottomBar = currentRoute in setOf(Routes.LIST, Routes.DASHBOARD, Routes.MAPA, Routes.SETTINGS)

        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    val backRoute = nav.currentBackStackEntryAsState().value
                        ?.destination?.route.normalize()

                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ) {
                        val items = listOf(
                            // Usamos Icons.AutoMirrored.Outlined.ListAlt para evitar el warning de deprecación
                            Triple(Routes.LIST, "Reportes", Icons.AutoMirrored.Outlined.ListAlt),
                            Triple(Routes.DASHBOARD, "Dash", Icons.Outlined.Assessment),
                            Triple(Routes.MAPA, "Mapa", Icons.Outlined.Map), // Nuevo botón de Mapa
                            Triple(Routes.SETTINGS, "Ajustes", Icons.Outlined.Settings)
                        )
                        items.forEach { (route, label, icon) ->
                            val selected = backRoute == route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    if (!selected) {
                                        nav.navigate(route) {
                                            // Evita crear múltiples copias de la misma pantalla en el stack
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
                    startDestination = Routes.DASHBOARD,
                    modifier = modifier
                ) {
                    // 1. PANTALLA DASHBOARD
                    composable(Routes.DASHBOARD) {
                        // Llamada limpia sin argumentos extraños
                        DashboardScreen(
                            onOpenSettings = { nav.navigate(Routes.SETTINGS) }
                        )
                    }

                    // 2. PANTALLA LISTA DE REPORTES
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

                    // 3. PANTALLA MAPA / CLIMA (NUEVA)
                    composable(Routes.MAPA) {
                        WeatherScreen()
                    }

                    // 4. PANTALLA FORMULARIO (Crear/Editar)
                    composable(
                        route = "${Routes.FORM}?id={id}",
                        arguments = listOf(navArgument("id") { type = NavType.StringType; nullable = true })
                    ) { backStackEntry ->
                        val vm: FormViewModel = viewModel()
                        val id = backStackEntry.arguments?.getString("id")
                        if (id != null) vm.cargarParaEditar(id)
                        ReportFormScreen(vm = vm, onSaved = { nav.popBackStack(Routes.LIST, inclusive = false) })
                    }

                    // 5. PANTALLA DETALLE DE REPORTE
                    composable(
                        Routes.DETAIL,
                        arguments = listOf(navArgument("id") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val vm: DetailViewModel = viewModel()
                        val id = backStackEntry.arguments?.getString("id")!!
                        vm.cargar(id)
                        ReportDetailScreen(vm = vm, onEdit = { nav.navigate("form?id=$id") }, onBack = { nav.popBackStack() })
                    }

                    // 6. PANTALLA AJUSTES
                    composable(Routes.SETTINGS) {
                        SettingsScreen(isDarkTheme = darkTheme, onToggleTheme = { darkTheme = it })
                    }
                }
            }
        }
    }
}

// Función auxiliar para limpiar la ruta y saber en qué pantalla "base" estamos
private fun String?.normalize(): String {
    val raw = this ?: return Routes.DASHBOARD
    val base = raw.substringBefore("?")
    return when {
        base.startsWith("detail") || base.startsWith("form") -> Routes.LIST
        else -> base
    }
}