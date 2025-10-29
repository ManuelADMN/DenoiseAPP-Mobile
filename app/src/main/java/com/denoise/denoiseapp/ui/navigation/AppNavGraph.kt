package com.denoise.denoiseapp.ui.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding

object Routes {
    const val DASHBOARD = "dashboard"
    const val LIST = "list"
    const val FORM = "form"
    const val DETAIL = "detail/{id}"
    const val SETTINGS = "settings"
}

private data class BottomItem(val route: String, val label: String, val emoji: String)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavGraph(modifier: Modifier = Modifier) {
    val nav = rememberNavController()
    val darkThemeState = rememberSaveable { mutableStateOf(false) }

    // Ítems de la bottom bar (izq: Reportes, centro: Dashboard, der: Ajustes)
    val bottomItems = remember {
        listOf(
            BottomItem(Routes.LIST, "Reportes", "📋"),
            BottomItem(Routes.DASHBOARD, "Dashboard", "📊"),
            BottomItem(Routes.SETTINGS, "Ajustes", "⚙️")
        )
    }

    DenoiseTheme(darkTheme = darkThemeState.value) {
        Scaffold(
            bottomBar = {
                val currentRoute = nav.currentBackStackEntryAsState().value
                    ?.destination?.route.normalize()

                NavigationBar {
                    bottomItems.forEach { item ->
                        val selected = when (item.route) {
                            Routes.DASHBOARD -> currentRoute == Routes.DASHBOARD
                            Routes.SETTINGS  -> currentRoute == Routes.SETTINGS
                            Routes.LIST      -> currentRoute == Routes.LIST ||
                                    currentRoute.startsWith("detail") ||
                                    currentRoute.startsWith("form")
                            else -> false
                        }
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                nav.navigate(item.route) {
                                    popUpTo(nav.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Text(item.emoji) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(Modifier.padding(innerPadding)) {
                NavHost(
                    navController = nav,
                    startDestination = Routes.DASHBOARD,
                    modifier = modifier,
                    // --- Transiciones laterales usando slideIn/slideOut horizontales ---
                    enterTransition = {
                        val from = initialState.destination.route.normalize()
                        val to = targetState.destination.route.normalize()
                        if (routeIndex(to) > routeIndex(from)) {
                            // Navegación "hacia la derecha" (List -> Dashboard -> Settings)
                            slideInHorizontally(animationSpec = tween(250)) { fullWidth -> fullWidth }
                        } else {
                            // Navegación "hacia la izquierda" (Settings -> Dashboard -> List)
                            slideInHorizontally(animationSpec = tween(250)) { fullWidth -> -fullWidth }
                        }
                    },
                    exitTransition = {
                        val from = initialState.destination.route.normalize()
                        val to = targetState.destination.route.normalize()
                        if (routeIndex(to) > routeIndex(from)) {
                            // Sale hacia la izquierda
                            slideOutHorizontally(animationSpec = tween(250)) { fullWidth -> -fullWidth }
                        } else {
                            // Sale hacia la derecha
                            slideOutHorizontally(animationSpec = tween(250)) { fullWidth -> fullWidth }
                        }
                    },
                    popEnterTransition = {
                        val from = initialState.destination.route.normalize()
                        val to = targetState.destination.route.normalize()
                        if (routeIndex(to) < routeIndex(from)) {
                            // Al volver, entra desde la izquierda
                            slideInHorizontally(animationSpec = tween(250)) { fullWidth -> -fullWidth }
                        } else {
                            // Al volver "hacia la derecha", entra desde la derecha
                            slideInHorizontally(animationSpec = tween(250)) { fullWidth -> fullWidth }
                        }
                    },
                    popExitTransition = {
                        val from = initialState.destination.route.normalize()
                        val to = targetState.destination.route.normalize()
                        if (routeIndex(to) < routeIndex(from)) {
                            // Al volver, sale hacia la derecha
                            slideOutHorizontally(animationSpec = tween(250)) { fullWidth -> fullWidth }
                        } else {
                            // Al volver "hacia la derecha", sale hacia la izquierda
                            slideOutHorizontally(animationSpec = tween(250)) { fullWidth -> -fullWidth }
                        }
                    }
                ) {
                    // ---- Dashboard ----
                    composable(Routes.DASHBOARD) {
                        DashboardScreen(
                            onIrALista = {
                                nav.navigate(Routes.LIST) {
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onOpenSettings = {
                                nav.navigate(Routes.SETTINGS) {
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            onOpenDashboard = { /* ya estás en dashboard */ }
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

                    // ---- Form ----
                    composable(
                        route = "${Routes.FORM}?id={id}",
                        arguments = listOf(
                            navArgument("id") {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            }
                        )
                    ) {
                        val vm: FormViewModel = viewModel()
                        val id = it.arguments?.getString("id")
                        if (id != null) vm.cargarParaEditar(id)

                        ReportFormScreen(
                            vm = vm,
                            onSaved = {
                                nav.popBackStack(Routes.LIST, inclusive = false)
                            }
                        )
                    }

                    // ---- Detail ----
                    composable(
                        Routes.DETAIL,
                        arguments = listOf(navArgument("id") { type = NavType.StringType })
                    ) {
                        val vm: DetailViewModel = viewModel()
                        val id = it.arguments?.getString("id")!!
                        vm.cargar(id)
                        ReportDetailScreen(
                            vm = vm,
                            onEdit = { nav.navigate("form?id=$id") },
                            onBack = { nav.popBackStack() }
                        )
                    }

                    // ---- Settings ----
                    composable(Routes.SETTINGS) {
                        SettingsScreen(
                            isDarkTheme = darkThemeState.value,
                            onToggleTheme = { dark -> darkThemeState.value = dark }
                        )
                    }
                }
            }
        }
    }
}

// --- Helpers ---

// Normaliza rutas (agrupa detail/form como LIST)
private fun String?.normalize(): String {
    val raw = this ?: return Routes.DASHBOARD
    val base = raw.substringBefore("?")
    return when {
        base.startsWith("detail") || base.startsWith("form") -> Routes.LIST
        else -> base
    }
}

// Índice posicional para dirección: List=0, Dashboard=1, Settings=2
private fun routeIndex(route: String): Int = when {
    route == Routes.LIST      -> 0
    route == Routes.DASHBOARD -> 1
    route == Routes.SETTINGS  -> 2
    route.startsWith("detail") || route.startsWith("form") -> 0
    else -> 1
}
