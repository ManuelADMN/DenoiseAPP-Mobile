package com.denoise.denoiseapp.ui.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.Map // <--- Asegúrate de importar esto
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
import com.denoise.denoiseapp.ui.weather.WeatherScreen // <--- Importamos la nueva pantalla

object Routes {
    const val DASHBOARD = "dashboard"
    const val LIST = "list"
    const val MAPA = "mapa" // <--- Nueva Ruta declarada
    const val FORM = "form"
    const val DETAIL = "detail/{id}"
    const val SETTINGS = "settings"
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavGraph(modifier: Modifier = Modifier) {
    val nav = rememberNavController()

    // Control del tema oscuro/claro
    var darkTheme by rememberSaveable { mutableStateOf(false) }

    DenoiseTheme(darkTheme = darkTheme) {

        val currentRoute = nav.currentBackStackEntryAsState().value
            ?.destination?.route.normalize()

        // Definimos en qué pantallas se muestra la barra inferior
        // AHORA INCLUYE Routes.MAPA
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
                            Triple(Routes.LIST, "Reportes", Icons.Outlined.ListAlt),
                            Triple(Routes.DASHBOARD, "Dash", Icons.Outlined.Assessment),
                            Triple(Routes.MAPA, "Mapa", Icons.Outlined.Map), // <--- BOTÓN NUEVO AQUÍ
                            Triple(Routes.SETTINGS, "Ajustes", Icons.Outlined.Settings)
                        )
                        items.forEach { (route, label, icon) ->
                            val selected = backRoute == route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    if (!selected) {
                                        nav.navigate(route) {
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
                    // ---- Dashboard ----
                    composable(Routes.DASHBOARD) {
                        DashboardScreen(
                            onIrALista = { nav.navigate(Routes.LIST) },
                            onOpenSettings = { nav.navigate(Routes.SETTINGS) }
                        )
                    }

                    // ---- Lista ----
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

                    // ---- NUEVA PANTALLA: MAPA / CLIMA ----
                    composable(Routes.MAPA) {
                        WeatherScreen()
                    }

                    // ---- Formulario ----
                    composable(
                        route = "${Routes.FORM}?id={id}",
                        arguments = listOf(navArgument("id") { type = NavType.StringType; nullable = true })
                    ) { backStackEntry ->
                        val vm: FormViewModel = viewModel()
                        val id = backStackEntry.arguments?.getString("id")
                        if (id != null) vm.cargarParaEditar(id)
                        ReportFormScreen(vm = vm, onSaved = { nav.popBackStack(Routes.LIST, inclusive = false) })
                    }

                    // ---- Detalle ----
                    composable(
                        Routes.DETAIL,
                        arguments = listOf(navArgument("id") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val vm: DetailViewModel = viewModel()
                        val id = backStackEntry.arguments?.getString("id")!!
                        vm.cargar(id)
                        ReportDetailScreen(vm = vm, onEdit = { nav.navigate("form?id=$id") }, onBack = { nav.popBackStack() })
                    }

                    // ---- Ajustes ----
                    composable(Routes.SETTINGS) {
                        SettingsScreen(isDarkTheme = darkTheme, onToggleTheme = { darkTheme = it })
                    }
                }
            }
        }
    }
}

// Función auxiliar para limpiar rutas con parámetros
private fun String?.normalize(): String {
    val raw = this ?: return Routes.DASHBOARD
    val base = raw.substringBefore("?")
    return when {
        base.startsWith("detail") || base.startsWith("form") -> Routes.LIST
        else -> base
    }
}