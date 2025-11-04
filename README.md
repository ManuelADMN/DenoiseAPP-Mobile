
---

#DenoiseAPP (Compose + Material 3)

**EP2/EP3 (DSY1105) · Persistencia local (Room) · Enfoque en Dashboard**

App móvil para **registro y seguimiento de órdenes** (inspección sanitaria Denoise SH) construida con **Jetpack Compose + Material 3**, **Room/SQLite** (sin Supabase), **Navigation** con argumentos, **ViewModel + Flow** y **recursos nativos** (Photo Picker, Haptics).
Esta versión prioriza el **Dashboard analítico**: KPIs accionables, tendencias por estado, filtros persistentes y atajos a vistas operativas.

> **Importante**: Se **eliminó Supabase**. Toda la persistencia es **local con Room**.

---

## Qué cambió (en 1 minuto)

* **Dashboard v2** con 3 zonas:

  * **KPIs accionables** (toques abren listas filtradas).
  * **Tendencias** (creados vs finalizados por semana; distribución por estado).
  * **Backlog & calidad** (pendientes por antigüedad; media de evidencias por reporte).
* **Filtros globales** de **fecha, planta y estado** que afectan Dashboard y Lista.
* **Atajos rápidos**: *Nuevo reporte*, *Pendientes hoy*, *QA en curso*.
* **Conectividad visible**: banner si el dispositivo está offline.

---

## Cómo ejecutar

1. Abrir en **Android Studio** (Giraffe/Koala+).
2. Sincronizar Gradle y compilar.
3. Ejecutar (minSdk **24**, compileSdk **36**).
4. Primer arranque: se genera **semilla de datos** para demo (Room).

---

## Stack técnico

* **Kotlin 2.0.21** · **Compose BOM 2024.09** · **Material 3**
* **Room 2.6.1** (entities/DAO/DB con `Callback` para seed)
* **Navigation Compose** (rutas con argumentos + transiciones)
* **ViewModel + StateFlow** (estado reactivo por pantalla)
* **Photo Picker** (AndroidX Activity) · **HapticFeedback** (Compose)
* **Canvas/Compose** para gráficos (sin dependencias externas)

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
│  └─ seed/DemoSeed.kt
├─ domain/
│  ├─ model/{Report.kt, ReportStatus.kt, Planta.kt, Salmon.kt}
│  └─ usecase/{GetReports.kt, GetReportById.kt, CreateOrUpdateReport.kt,
│             DeleteReport.kt, SearchReports.kt}
├─ presentation/
│  ├─ report/{ListViewModel.kt, FormViewModel.kt, DetailViewModel.kt}
│  └─ dashboard/DashboardViewModel.kt   ← **(nuevo/actualizado)**
├─ ui/
│  ├─ navigation/AppNavGraph.kt
│  ├─ components/ConnectivityBanner.kt
│  ├─ dashboard/DashboardScreen.kt      ← **(nuevo/actualizado)**
│  ├─ settings/SettingsScreen.kt
│  └─ report/
│     ├─ list/{ReportListScreen.kt, FilterList.kt}
│     ├─ form/ReportFormScreen.kt
│     └─ detail/ReportDetailScreen.kt
└─ MainActivity.kt
```

---

## Dashboard — contenido y fórmulas

**Zonas y widgets**

1. **KPIs (Cards)**

* **Total**: `count(reports)`
* **Activos**: `count(status != FINALIZADO)`
* **% Finalizados**: `count(FINALIZADO)/count(all) * 100`
* **SLA prom.**: `avg(finalizedAt - createdAt)` (solo finalizados)
* **Evidencias prom.**: `avg(evidencesCount)`

2. **Tendencias**

* **Creación vs cierre (semanal)**: series por semana
  `created_week[w] = count(createdAt in w)`
  `closed_week[w]  = count(finalizedAt in w)`
* **Distribución por estado (actual)**: `groupBy(status).count()`

3. **Backlog & calidad**

* **Aging pendientes**: buckets por días desde `createdAt`
  `0–2 | 3–7 | 8–14 | >14`
* **Top plantas con pendientes**: `groupBy(planta).count(pendientes)` **desc**

**Interacciones**

* Tocar un **KPI** o **barra/pastel** → abre **Lista** con filtros aplicados.
* Filtro de **rango de fechas** en la barra superior del Dashboard (sticky).
* **Chips** de estado/planta afectan todo el tablero (y se recuerdan en la sesión).
* **Acciones rápidas**: *Nuevo*, *Pendientes hoy*, *QA en curso*.

**Rendimiento y UX**

* Cálculos en **ViewModel** (Flows combinados) con mínima recomposición.
* Gráficos con **Canvas** y `animate*` para entradas/salidas suaves.
* Soporte claro/oscuro (Material 3), accesibilidad (tipos y contrastes).

---

## Mapa de cumplimiento (EP2/EP3) centrado en Dashboard

| Ítem rúbrica                       | Evidencia en la app                                                                                       | Código clave                                                                      |
| ---------------------------------- | --------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------- |
| **Visualización de datos/KPIs**    | KPIs (Total, Activos, % Finalizados, SLA prom., Evidencias prom.)                                         | `ui/dashboard/DashboardScreen.kt`, `presentation/dashboard/DashboardViewModel.kt` |
| **Historial y seguimiento**        | Tendencias creados vs finalizados (semanal), distribución por estado                                      | `DashboardViewModel.kt` (deriva Flows de Room)                                    |
| **Filtros y navegación funcional** | Filtros globales (fecha/estado/planta), tap en KPI → Lista filtrada, transiciones animadas                | `AppNavGraph.kt`, `DashboardScreen.kt`, `ReportListScreen.kt`                     |
| **Persistencia local**             | Room (CRUD, seed, consultas agregadas simples desde DAO/Repo)                                             | `data/local/*`, `ReportRepositoryImpl.kt`                                         |
| **Formularios validados**          | Crear/Editar con errores visibles; Photo Picker                                                           | `ReportFormScreen.kt`, `FormViewModel.kt`                                         |
| **Recursos nativos (2+)**          | Photo Picker, Haptics en acciones; banner **Sin conexión**                                                | `ReportFormScreen.kt`, `SettingsScreen.kt`, `ConnectivityBanner.kt`               |
| **Animaciones en UI/Navegación**   | Animaciones en KPIs/gráficos (Canvas + `animate*`), transiciones `slideIn/slideOut`, `AnimatedVisibility` | `DashboardScreen.kt`, `AppNavGraph.kt`, `ReportListScreen.kt`                     |

---

## Flujo: del Dashboard a la operación

1. Ver **KPIs** y **Tendencias** → identificar cuello de botella (p.ej., *QA bajo*).
2. Tocar el KPI **Activos** o la barra de **Pendientes** → **Lista** filtrada.
3. Abrir **Detalle** → **Editar estado** o **Añadir evidencias**.
4. Volver al **Dashboard** → métricas se actualizan en vivo (Flow/Room).

---

## Validaciones de formulario (resumen)

* **Obligatorios**: *Título* y *Planta* (`isError`, `supportingText`).
* **Evidencias**: `PickMultipleVisualMedia` (hasta 5).
* **Guardado** condicionado; **Haptics** al confirmar/eliminar.

---

## Conectividad

* **Monitor**: `ConnectivityMonitor` (Flow).
* **Banner**: `ConnectivityBanner` visible si no hay red (no bloquea la app).

---

## Permisos y accesibilidad

* **Permisos**: `INTERNET`, `ACCESS_NETWORK_STATE` (banner/monitor).
* **A11y**: tamaños tocables M3, tipografías legibles, estados de error claros.

---

## Notas de arquitectura

* UI → **ViewModel** → **UseCases** → **Repository** → **Room** (DAO).
* **StateFlow** recolectado en Compose (`collectAsState`).
* **ServiceLocator** simple (sin Hilt) para EP2/EP3.
* Cálculos del Dashboard **en ViewModel** (no en Composables).

---

## Checklist de demo (rápida)

1. **Dashboard**: revisar KPIs, mover el **rango de fechas** y observar cambios.
2. Tocar **% Finalizados** → ver **Lista** filtrada y gráfica estable.
3. Crear **Nuevo reporte** (errores si faltan campos) + **Photo Picker**.
4. Volver al **Dashboard** → KPIs y Tendencias se actualizan.
5. **Detalle**: cambiar estado a **QA** o **Finalizado** (Haptics en eliminar).
6. **Ajustes**: alternar **modo oscuro** y probar vibración.
7. Apagar red → **banner Sin conexión** (la app sigue operativa).

---

## Conclusión

La app cumple la rúbrica con foco en **Dashboard**: **KPIs accionables**, **tendencias temporales**, **filtros globales**, **navegación funcional** y **persistencia local**; además de **formularios validados**, **recursos nativos** y **animaciones**. El tablero guía el trabajo diario y reduce el tiempo para encontrar y cerrar pendientes.

---
