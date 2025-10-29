package com.denoise.denoiseapp.ui.navigation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.denoise.denoiseapp.presentation.report.DetailViewModel
import com.denoise.denoiseapp.presentation.report.FormViewModel
import com.denoise.denoiseapp.presentation.report.ListViewModel
import com.denoise.denoiseapp.ui.report.detail.ReportDetailScreen
import com.denoise.denoiseapp.ui.report.form.ReportFormScreen
import com.denoise.denoiseapp.ui.report.list.ReportListScreen
import com.denoise.denoiseapp.ui.settings.SettingsScreen
import com.denoise.denoiseapp.core.ui.theme.DenoiseTheme
import androidx.compose.runtime.saveable.rememberSaveable

object Routes {
    const val LIST = "list"
    const val FORM = "form"
    const val DETAIL = "detail/{id}"
    const val SETTINGS = "settings"
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavGraph(modifier: Modifier = Modifier) {
    val nav = rememberNavController()

    // Estado de tema oscuro, guardado entre recomposiciones y rotación
    val darkThemeState = rememberSaveable { mutableStateOf(false) }

    // Envolvemos las pantallas con el tema aquí para que el toggle funcione de inmediato
    DenoiseTheme(darkTheme = darkThemeState.value) {
        NavHost(
            navController = nav,
            startDestination = Routes.LIST,
            modifier = modifier,
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
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

                // ReportFormScreen ahora usa onSaved: () -> Unit
                ReportFormScreen(
                    vm = vm,
                    onSaved = {
                        // Luego de guardar, vuelve a la lista
                        nav.popBackStack(Routes.LIST, inclusive = false)
                    }
                )
            }
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
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    isDarkTheme = darkThemeState.value,
                    onToggleTheme = { dark -> darkThemeState.value = dark }
                )
            }
        }
    }
}
