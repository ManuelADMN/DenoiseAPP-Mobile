# README — DenoiseAPP Mobile (Compose + Material 3)

**Cumplimiento de la rúbrica EP2/EP3 (DSY1105) · Persistencia local (Room) — sin Supabase**

Este proyecto implementa una app móvil de *Servicios técnicos / atención al cliente* enfocada en **registro y seguimiento de órdenes (reportes)** para inspección sanitaria (Denoise SH). La app está construida con **Jetpack Compose + Material 3**, **Room/SQLite** para almacenamiento **local**, **navegación con argumentos**, **ViewModels + Flow** para gestión de estado, **recursos nativos** (photo picker y vibración/haptics) y **animaciones** en navegación y UI.

> **Importante**: Se **reemplazó Supabase** por **almacenamiento local** (**Room**). No hay dependencias ni llamadas a Supabase.

---

## Cómo ejecutar

1. Abrir el proyecto en **Android Studio** (Giraffe/Koala+).
2. Sincronizar Gradle y compilar.
3. Ejecutar en emulador o dispositivo físico (minSdk 24, compileSdk 36).
4. Primer arranque: se genera **semilla de datos** para la demo.

---

## Stack técnico

* **Kotlin 2.0.21** · **Compose BOM 2024.09** · **Material 3**
* **Room 2.6.1** (entities/DAO/DB con `Callback` para seed)
* **Navigation Compose** (NavHost con argumentos y transiciones)
* **ViewModel + StateFlow** (estado reactivo por pantalla)
* **Photo Picker** (AndroidX Activity) · **HapticFeedback** (Compose)
* **Coil** (vista previa de imágenes, si aplica)

---

## Estructura principal

```
app/src/main/java/com/denoise/denoiseapp/
├─ core/
│  ├─ di/ServiceLocator.kt
│  ├─ ui/theme/{Theme.kt, Color.kt, Type.kt}
│  └─ util/{TimeUtils.kt, connectivity/ConnectivityMonitor.kt}
├─ data/
│  ├─ local/{db/AppDatabase.kt, dao/ReportDao.kt, entity/ReportEntity.kt}
│  ├─ repository/{ReportRepository.kt, ReportRepositoryImpl.kt, ReportMappers.kt}
│  └─ seed/DemoSeed.kt (seed principal se ejecuta en AppDatabase)
├─ domain/
│  ├─ model/{Report.kt, ReportStatus.kt, Planta.kt, Salmon.kt}
│  └─ usecase/{GetReports.kt, GetReportById.kt, CreateOrUpdateReport.kt, DeleteReport.kt, SearchReports.kt}
├─ presentation/report/{ListViewModel.kt, FormViewModel.kt, DetailViewModel.kt}
├─ ui/
│  ├─ navigation/AppNavGraph.kt
│  ├─ components/ConnectivityBanner.kt
│  ├─ dashboard/DashboardScreen.kt
│  ├─ settings/SettingsScreen.kt
│  └─ report/
│     ├─ list/{ReportListScreen.kt, FilterList.kt}
│     ├─ form/ReportFormScreen.kt
│     └─ detail/ReportDetailScreen.kt
└─ MainActivity.kt
```

---

## Mapa de cumplimiento — “¿Qué debe incluir?”

| Ítem                                | Dónde se ve en la app                                                                                                                      | Código fuente (rutas clave)                                                                                                                                                                                                                                                                                               |
| ----------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **✔️ Diseño visual con Material 3** | Temas claro/oscuro, TopAppBar, Cards, Buttons, TextFields en todas las pantallas.                                                          | `core/ui/theme/{Theme.kt, Color.kt, Type.kt}` · uso de **MaterialTheme** en `MainActivity.kt`, `AppNavGraph.kt`, `DashboardScreen.kt`, `Report*Screen.kt`.                                                                                                                                                                |
| **✔️ Formularios validados**        | Formulario de Reporte con campos obligatorios (*Título*, *Planta*), mensajes de error y control de guardado.                               | `ui/report/form/ReportFormScreen.kt` (propiedades `isError`, `supportingText`, validación en onClick). `presentation/report/FormViewModel.kt` (alta/edición).                                                                                                                                                             |
| **✔️ Navegación funcional**         | Flujo **List → Form → List → Detail(id) → back** con argumentos y *BottomBar* (Reportes/Dashboard/Ajustes).                                | `ui/navigation/AppNavGraph.kt` (NavHost, rutas, `detail/{id}` y `form?id={id}`, back-stack correcto).                                                                                                                                                                                                                     |
| **✔️ Gestión de estado**            | Estado por pantalla con **ViewModel + StateFlow**; la UI reacciona sin “pull-to-refresh”.                                                  | `presentation/report/{ListViewModel.kt, FormViewModel.kt, DetailViewModel.kt}` · `data/repository/ReportRepository(Impl).kt` (Flow).                                                                                                                                                                                      |
| **✔️ Almacenamiento local**         | Persistencia **Room/SQLite**: CRUD completo + **semilla** inicial para demo.                                                               | `data/local/{db/AppDatabase.kt, dao/ReportDao.kt, entity/ReportEntity.kt}` · seed en `AppDatabase.Callback`.                                                                                                                                                                                                              |
| **✔️ Uso de recursos nativos**      | **Photo Picker** (galería) para evidencias y **HapticFeedback** (vibración) en acciones clave; **banner “Sin conexión”** (monitor de red). | Picker en `ui/report/form/ReportFormScreen.kt` (`rememberLauncherForActivityResult(PickMultipleVisualMedia)`); haptics en `ReportDetailScreen.kt` (eliminar) y `SettingsScreen.kt` (botón “Probar vibración”); **Conectividad**: `core/util/connectivity/ConnectivityMonitor.kt` + `ui/components/ConnectivityBanner.kt`. |
| **✔️ Animaciones**                  | **Transiciones** laterales entre pantallas; **AnimatedVisibility** para filtros; **AnimatedContent/animateContentSize** en tarjetas.       | `ui/navigation/AppNavGraph.kt` (slide in/out con `tween`) · `ui/report/list/ReportListScreen.kt` (AnimatedVisibility/animateContentSize) · `ui/report/detail/ReportDetailScreen.kt` (AnimatedContent).                                                                                                                    |

---

## Mapa contra “Estructura APP”

| Sección (rúbrica)       | ¿Qué muestra la app?                                                                         | Dónde (UI / lógica)                                                                                                                     |
| ----------------------- | -------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------- |
| **Registro/Monitoreo**  | Lista de reportes con planta, línea, lote, estado, evidencias (contador).                    | `ui/report/list/ReportListScreen.kt` · VM: `ListViewModel.kt` · modelos: `domain/model/Report*.kt`                                      |
| **Seguimiento estado**  | Estados **PENDIENTE/EN_PROCESO/QA/FINALIZADO** + filtros por estado/búsqueda.                | `domain/model/ReportStatus.kt` · `FilterList.kt` · lógica en `ListViewModel.kt` (filtro + query).                                       |
| **CRUD servicios**      | Crear/editar (Form), ver detalle (Detail), eliminar (Detail/List).                           | **Form**: `ReportFormScreen.kt` + `FormViewModel.kt` · **Detail**: `ReportDetailScreen.kt` + `DetailViewModel.kt` · **Repo/DAO**: Room. |
| **Historial**           | Lista ordenada por fecha con **búsqueda** y **filtros** (planta/estado/fecha concepto).      | `ReportListScreen.kt` + `ListViewModel.kt` · consulta en `ReportDao.listAllOrderByFecha()`.                                             |
| **Detalle servicio**    | Campos completos, acciones de **editar**/**eliminar**, **evidencias** (contador), KPIs base. | `ReportDetailScreen.kt` + `DetailViewModel.kt`.                                                                                         |
| **Formulario validado** | Campos obligatorios, errores visibles, picker de evidencias, notas.                          | `ReportFormScreen.kt` (validación UI) + `FormViewModel.kt` (guardar/actualizar).                                                        |

**Roles**

| Rol                          | Cómo se cubre                                                                      |
| ---------------------------- | ---------------------------------------------------------------------------------- |
| **Operario/Cliente interno** | Crea solicitudes desde **Form**, revisa sus órdenes en **List/Detail**.            |
| **Técnico/Administrador**    | Gestiona estados y edición desde **Detail/Form**; puede **eliminar** y actualizar. |

> La app incluye las acciones de ambos roles; si se requiere segmentación por permisos, es extensible desde `domain/model` + `presentation` (no requerido en EP2/EP3).

---

## Paso a paso (sin código) → **Dónde está en el repo**

| Paso                                                  | Carpeta/Archivo(s)                                                                                                                                               |
| ----------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **0) Pre-flight** (M3, tema claro/oscuro, compila)    | Tema: `core/ui/theme/*` · Compose/M3 activo en `build.gradle.kts` y aplicado en `MainActivity.kt`.                                                               |
| **1) Modelado dominio** (Report/Status/Salmon/Planta) | `domain/model/{Report.kt, ReportStatus.kt, Salmon.kt, Planta.kt}`.                                                                                               |
| **2) Almacenamiento local** (Room)                    | `data/local/{entity/ReportEntity.kt, dao/ReportDao.kt, db/AppDatabase.kt}`.                                                                                      |
| **3) Repositorio (Flow)**                             | `data/repository/{ReportRepository.kt, ReportRepositoryImpl.kt, ReportMappers.kt}`.                                                                              |
| **4) Casos de uso (CRUD + Get/Search)**               | `domain/usecase/{GetReports.kt, GetReportById.kt, CreateOrUpdateReport.kt, DeleteReport.kt, SearchReports.kt}`.                                                  |
| **5) Semilla demo (10–20)**                           | `AppDatabase.kt` (`RoomDatabase.Callback` genera datos en primer arranque).                                                                                      |
| **6) ViewModels por pantalla**                        | `presentation/report/{ListViewModel.kt, FormViewModel.kt, DetailViewModel.kt}`.                                                                                  |
| **7) UI — List/Form/Detail (+ Ajustes)**              | `ui/report/{list,form,detail}` · `ui/settings/SettingsScreen.kt` (tema, vibración).                                                                              |
| **8) Navegación (rutas/args)**                        | `ui/navigation/AppNavGraph.kt` (NavHost, `detail/{id}`, `form?id={id}`).                                                                                         |
| **9) Material 3 aplicado**                            | Uso de `MaterialTheme` y componentes M3 en todas las pantallas.                                                                                                  |
| **10) Validaciones**                                  | `ReportFormScreen.kt` (errores visibles; guardado condicionado).                                                                                                 |
| **11) Recursos nativos (2+)**                         | **Photo Picker** en `ReportFormScreen.kt`; **Haptics** en `ReportDetailScreen.kt` y `SettingsScreen.kt`; **Conectividad** banner y monitor.                      |
| **12) Animaciones (3+)**                              | Transiciones de navegación (`AppNavGraph.kt`), filtros (`AnimatedVisibility` en `ReportListScreen.kt`), detalles (`AnimatedContent` en `ReportDetailScreen.kt`). |
| **13) QA rápido**                                     | Estados en ViewModel (rotación conserva), CRUD end-to-end sin crash, accesibilidad básica (tipografías y contrastes M3).                                         |
| **14) Entrega**                                       | Este README + código; APK/Video/Screenshots se agregan en `release/` (cuando se generen).                                                                        |

---

## Persistencia local (sin Supabase)

* **Room**:

  * **Entidad**: `ReportEntity`
  * **DAO**: `ReportDao` (listar ordenado, por id, upsert, delete)
  * **DB**: `AppDatabase` (singleton + **seed** inicial)
  * **Repositorio**: `ReportRepositoryImpl` (**Flow** → UI reactiva)

* **Semilla de datos** para demo: se ejecuta **una sola vez** en la creación de la DB (ver `RoomDatabase.Callback` en `AppDatabase.kt`).

---

## Navegación y argumentos

* **Rutas**: `list`, `form?id={id}`, `detail/{id}`, `dashboard`, `settings`.
* **Transiciones**: deslizamientos laterales con `slideIn/slideOut` (`tween(250ms)`).

---

## Validaciones de formulario

* **Obligatorios**: *Título* y *Planta* (errores visibles con `isError` + `supportingText`).
* **Evidencias**: `rememberLauncherForActivityResult(PickMultipleVisualMedia)` (hasta 5).
* **Guardado**: sólo si no hay errores; retroalimentación háptica al confirmar.

---

## Recursos nativos

* **Photo Picker** (galería) — sin permisos de almacenamiento en Android 13+.
* **Vibración/Haptics** — `LocalHapticFeedback` (botón “Probar vibración” y al eliminar).
* **Conectividad** — monitor (`ConnectivityMonitor`) + **banner** “Sin conexión”.

---

## Animaciones

* **Navegación**: `slideInHorizontally/slideOutHorizontally` con `tween(250)`.
* **Filtros**: `AnimatedVisibility` al mostrar/ocultar.
* **Detalle**: `AnimatedContent` y `animateContentSize` en tarjetas/lista.

---

## Permisos y accesibilidad

* **Permisos**: `INTERNET`, `ACCESS_NETWORK_STATE` (para banner/monitor de red).
* **Accesibilidad básica**: tamaños tocables M3, tipografías legibles, estados de error claros.

---

## Notas de arquitectura

* **Capa de UI** no conversa con DAO: usa **Casos de Uso** → **Repositorio** → **Room**.
* **Estado** en **ViewModels** con **StateFlow**, recolectado en Compose (`collectAsState`).
* **DI simple** con `ServiceLocator` (sin Hilt para simplificar EP2/EP3).

---

## Checklist de demo (rápida)

1. Abrir app → ver **Dashboard** con KPIs.
2. Ir a **Reportes** → ver lista con **búsqueda** y **filtros** (animados).
3. **Crear** nuevo reporte (errores si faltan campos) + **Photo Picker**.
4. Volver a **Lista** → aparece el nuevo ítem sin refrescar manual.
5. Abrir **Detalle** → **Editar** o **Eliminar** (vibración al eliminar).
6. **Ajustes** → cambiar **Modo oscuro** + **Probar vibración**.
7. Desconectar red (simulado) → aparece **banner “Sin conexión”**.

---

### Conclusión

El proyecto **cumple** con los ítems marcados en la rúbrica: **Material 3**, **Formularios validados**, **Navegación funcional**, **Gestión de estado**, **Almacenamiento local** (Room), **Uso de recursos nativos** (Photo Picker + Haptics + Conectividad) y **Animaciones** (navegación + UI). La estructura y el código están mapeados en las tablas para verificación directa. Si quieres, puedo adjuntar un APK de demo y una minuta de video con marcas de tiempo para cada criterio.
