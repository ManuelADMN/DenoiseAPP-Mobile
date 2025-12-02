# Denoise APP

Este documento justifica, con ejemplos de código concretos, cómo el proyecto **DenoiseAPP Mobile + Backend Spring Boot** cumple cada uno de los criterios solicitados.

---

## 1. Diseño visual estructurado y navegación coherente

La app móvil utiliza **Jetpack Compose**, un **tema unificado** y un **grafo de navegación** centralizado.

### Tema visual unificado

**Archivo:** `app/src/main/java/com/denoise/denoiseapp/core/ui/theme/Theme.kt`

```kotlin
@Composable
fun DenoiseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = DenoiseTypography,
        content = content
    )
}
```

Esto garantiza una paleta coherente (dark/light) en toda la interfaz.

### Navegación centralizada con rutas claras

**Archivo:** `app/src/main/java/com/denoise/denoiseapp/ui/navigation/AppNavGraph.kt`

```kotlin
object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val DASHBOARD = "dashboard"
    const val LIST = "reports/list"
    const val MAPA = "weather/map"
    const val FORM = "reports/form"
    const val DETAIL = "reports/detail/{id}"
    const val SETTINGS = "settings"
    const val ADMIN_USERS = "admin/users"
}

@Composable
fun AppNavGraph(startRoute: String = Routes.LOGIN) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavBar(navController = navController)
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startRoute,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.LOGIN) { LoginScreen(navController) }
            composable(Routes.DASHBOARD) { DashboardScreen(navController) }
            composable(Routes.LIST) { ReportListScreen(navController) }
            composable(Routes.FORM) { ReportFormScreen(navController) }
            // ... demás destinos
        }
    }
}
```

La navegación es coherente: cada pantalla tiene una ruta bien definida y se integra en una sola estructura.

---

## 2. Validación de formularios con retroalimentación por campo

Se combinan **reglas de dominio** y **validación en la UI** para dar feedback inmediato por campo.

### Reglas de negocio en el dominio

**Archivo:** `app/src/main/java/com/denoise/denoiseapp/domain/model/Salmon.kt`

```kotlin
data class Salmon(
    val id: String,
    val porcentajeInfeccion: Double,
    val estado: EstadoSalmon
) {
    init {
        require(porcentajeInfeccion in 0.0..100.0) {
            "El porcentaje de infección debe estar entre 0 y 100"
        }
    }

    companion object {
        fun desdeProbabilidad(prob: Double, umbral: Double = 0.5): Salmon {
            val porcentaje = prob * 100.0
            val estado = if (prob >= umbral) EstadoSalmon.INFECTADO else EstadoSalmon.SANO
            return Salmon(id = "auto", porcentajeInfeccion = porcentaje, estado = estado)
        }
    }
}
```

### Validación y error visual en el formulario

**Archivo:** `app/src/main/java/com/denoise/denoiseapp/ui/report/form/ReportFormScreen.kt`

```kotlin
OutlinedTextField(
    value = state.titulo,
    onValueChange = viewModel::onTituloChange,
    label = { Text("Título *") },
    isError = state.error != null && state.titulo.isBlank(),
    modifier = Modifier.fillMaxWidth()
)

LaunchedEffect(state.error) {
    state.error?.let { msg ->
        snackbarHostState.showSnackbar(message = msg)
        viewModel.errorMostrado()
    }
}
```

**Archivo:** `app/src/main/java/com/denoise/denoiseapp/presentation/report/FormViewModel.kt`

```kotlin
data class FormUiState(
    val titulo: String = "",
    val plantaNombre: String = "",
    val porcentajeInfectados: String = "",
    val error: String? = null
) {
    val esValido: Boolean
        get() = titulo.isNotBlank() && plantaNombre.isNotBlank()
}

fun guardar() {
    val state = ui.value
    if (!state.esValido) {
        ui.value = state.copy(error = "Por favor completa los campos obligatorios")
        return
    }
    // Llamada a caso de uso para persistir el reporte
}
```

De esta forma, la app valida primero los datos y luego entrega mensajes de error claros por campo (y a nivel global con Snackbars).

---

## 3. Gestión de estado y separación de lógica e interfaz

Se utiliza una arquitectura por capas (dominio, data, presentación, UI) con **ViewModels** y **repositorios**.

### Ejemplo de ViewModel + UseCase + Repository

**Archivo:** `app/src/main/java/com/denoise/denoiseapp/presentation/report/ListViewModel.kt`

```kotlin
class ListViewModel(
    application: Application,
    private val getReports: GetReports
) : AndroidViewModel(application) {

    private val _ui = MutableStateFlow(ReportListUiState())
    val ui: StateFlow<ReportListUiState> = _ui

    init {
        cargarReportes()
    }

    fun cargarReportes() {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(cargando = true)
            val reports = getReports()
            _ui.value = _ui.value.copy(cargando = false, reportes = reports)
        }
    }
}
```

**Archivo:** `app/src/main/java/com/denoise/denoiseapp/domain/usecase/GetReports.kt`

```kotlin
class GetReports(private val repository: ReportRepository) {
    suspend operator fun invoke(): List<Reporte> = repository.getAll()
}
```

**Archivo:** `app/src/main/java/com/denoise/denoiseapp/data/repository/ReportRepositoryImpl.kt`

```kotlin
class ReportRepositoryImpl(
    private val api: DenoiseApiService,
    private val dao: ReportDao
) : ReportRepository {

    override suspend fun getAll(): List<Reporte> {
        val local = dao.getAll()
        return local.map { it.toDomain() }
    }
}
```

**UI:** `ReportListScreen.kt` solo observa el estado:

```kotlin
@Composable
fun ReportListScreen(navController: NavController, viewModel: ListViewModel = koinViewModel()) {
    val state by viewModel.ui.collectAsState()

    if (state.cargando) {
        CircularProgressIndicator()
    } else {
        LazyColumn {
            items(state.reportes) { reporte ->
                ReportCard(reporte = reporte) {
                    navController.navigate("reports/detail/${reporte.id}")
                }
            }
        }
    }
}
```

Esto muestra claramente la separación entre lógica (ViewModel/UseCase/Repository) y vista (Composables).

---

## 4. Animaciones funcionales y respuestas dinámicas

Se usan APIs de animación de Compose para mejorar la experiencia y responder al estado de la app.

### Transiciones animadas entre pantallas

**Archivo:** `app/src/main/java/com/denoise/denoiseapp/ui/navigation/AppNavGraph.kt`

```kotlin
composable(
    route = Routes.DASHBOARD,
    enterTransition = {
        slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300))
    },
    exitTransition = {
        slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300))
    }
) {
    DashboardScreen(navController)
}
```

### Banner de conectividad dinámica

**Archivo:** `app/src/main/java/com/denoise/denoiseapp/ui/components/ConnectivityBanner.kt`

```kotlin
@Composable
fun ConnectivityBanner(isOffline: Boolean) {
    AnimatedVisibility(visible = isOffline) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Red)
                .padding(8.dp)
        ) {
            Text("Sin conexión", color = Color.White)
        }
    }
}
```

Integrado con un monitor de red:

```kotlin
val isOffline by rememberConnectivityState()
ConnectivityBanner(isOffline = isOffline)
```

La UI responde de forma animada al cambio de estado de red.

---

## 5. Consumo de APIs externas e integración con recursos nativos

### Consumo de API externa de clima (Open-Meteo)

**Archivo:** `app/src/main/java/com/denoise/denoiseapp/data/remote/api/ExternalApiService.kt`

```kotlin
interface ExternalApiService {
    @GET("v1/forecast")
    suspend fun getWeather(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current_weather") current: Boolean = true,
        @Query("daily") daily: String = "temperature_2m_max,temperature_2m_min,weathercode",
        @Query("timezone") timezone: String = "auto"
    ): WeatherResponse
}
```

**Archivo:** `RetrofitClient.kt`

```kotlin
private const val BASE_URL_EXTERNAL = "https://api.open-meteo.com/"

val externalApi: ExternalApiService by lazy {
    Retrofit.Builder()
        .baseUrl(BASE_URL_EXTERNAL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ExternalApiService::class.java)
}
```

### Integración con cámara nativa

**Archivo:** `ui/report/form/ReportFormScreen.kt`

```kotlin
val context = LocalContext.current
val fotoUri = remember { mutableStateOf<Uri?>(null) }

val takePictureLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicture()
) { ok ->
    if (ok && fotoUri.value != null) {
        viewModel.agregarFoto(fotoUri.value!!)
    }
}

IconButton(onClick = {
    val file = crearArchivoTemporal(context)
    val uri = FileProvider.getUriForFile(
        context,
        "com.denoise.denoiseapp.fileprovider",
        file
    )
    fotoUri.value = uri
    takePictureLauncher.launch(uri)
}) {
    Icon(Icons.Default.CameraAlt, contentDescription = "Tomar foto")
}
```

### Integración con ubicación (GPS) y mapa

**Archivo:** `ui/weather/WeatherScreen.kt`

```kotlin
val context = LocalContext.current
val fusedLocationClient = remember {
    LocationServices.getFusedLocationProviderClient(context)
}

LaunchedEffect(Unit) {
    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        if (location != null) {
            viewModel.onLocationAvailable(location.latitude, location.longitude)
        }
    }
}
```

La combinación de cámara, GPS y mapas muestra el uso de recursos nativos del dispositivo.

---

## 6. Conexión con microservicios desarrollados en Spring Boot

### Cliente Android (Retrofit hacia Spring Boot)

**Archivo:** `app/src/main/java/com/denoise/denoiseapp/data/remote/api/DenoiseApiService.kt`

```kotlin
interface DenoiseApiService {
    @GET("/api/v1/reports")
    suspend fun getAllReports(): List<ReportEntity>

    @GET("/api/v1/reports/{id}")
    suspend fun getReportById(@Path("id") id: String): ReportEntity

    @POST("/api/v1/reports")
    suspend fun createReport(@Body report: ReportEntity): ReportEntity

    @PUT("/api/v1/reports/{id}")
    suspend fun updateReport(
        @Path("id") id: String,
        @Body report: ReportEntity
    ): ReportEntity

    @DELETE("/api/v1/reports/{id}")
    suspend fun deleteReport(
        @Path("id") id: String,
        @Query("user") user: String
    ): Response<Unit>
}
```

**Archivo:** `RetrofitClient.kt`

```kotlin
private const val BASE_URL_MICROSERVICE = "http://10.0.2.2:8080/"

val denoiseApi: DenoiseApiService by lazy {
    Retrofit.Builder()
        .baseUrl(BASE_URL_MICROSERVICE)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(DenoiseApiService::class.java)
}
```

### Microservicio Spring Boot (backend)

**Archivo (representativo):** `backend/src/main/java/com/denoise/backend/ReportController.java`

```java
@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public List<ReportDto> getAll() {
        return reportService.getAll();
    }

    @PostMapping
    public ReportDto create(@RequestBody ReportDto dto) {
        return reportService.create(dto);
    }
}
```

La app móvil consume estos endpoints REST del microservicio Spring Boot a través de Retrofit, cumpliendo el criterio de integración.

---

## 7. Principios de programación orientada a objetos

Se aplican principios OO tanto en la app (Kotlin) como en el backend (Java).

### Data class con lógica e invariantes

**Archivo:** `domain/model/Report.kt`

```kotlin
data class Reporte(
    val id: String,
    val titulo: String,
    val planta: Planta,
    val porcentajeInfectados: Double,
    val estado: ReporteEstado
) {
    init {
        require(titulo.isNotBlank()) { "El título no puede estar vacío" }
        require(porcentajeInfectados in 0.0..100.0) {
            "El porcentaje de infectados debe estar entre 0 y 100"
        }
    }

    fun conEstado(nuevoEstado: ReporteEstado): Reporte =
        copy(estado = nuevoEstado)
}
```

### Interfaces y polimorfismo en repositorios

**Archivo:** `data/repository/ReportRepository.kt`

```kotlin
interface ReportRepository {
    suspend fun getAll(): List<Reporte>
    suspend fun getById(id: String): Reporte?
    suspend fun save(reporte: Reporte)
}
```

**Archivo:** `data/repository/ReportRepositoryImpl.kt`

```kotlin
class ReportRepositoryImpl(
    private val api: DenoiseApiService,
    private val dao: ReportDao
) : ReportRepository {

    override suspend fun getAll(): List<Reporte> {
        return dao.getAll().map { it.toDomain() }
    }

    override suspend fun save(reporte: Reporte) {
        dao.insert(reporte.toEntity())
    }
}
```

Este diseño permite intercambiar implementaciones (por ejemplo, solo local vs local+remoto) sin cambiar la capa de dominio.

---

## 8. Ejecución de pruebas unitarias y validación de resultados

### Pruebas unitarias en la app (JUnit)

**Archivo:** `app/src/test/java/com/denoise/denoiseapp/ExampleUnitTest.kt`

```kotlin
class ExampleUnitTest {

    @Test
    fun t01_salmon_probabilidad_0_0_retorna_estado_SANO() {
        val salmon = Salmon.desdeProbabilidad(0.0)
        assertEquals(EstadoSalmon.SANO, salmon.estado)
        assertEquals(0.0, salmon.porcentajeInfeccion, 0.001)
    }

    @Test(expected = IllegalArgumentException::class)
    fun t02_salmon_porcentaje_negativo_lanza_excepcion() {
        Salmon(id = "1", porcentajeInfeccion = -5.0, estado = EstadoSalmon.SANO)
    }
}
```

Se validan los casos normales y los casos de error, asegurando la correcta implementación de las reglas de negocio.

### Pruebas en el backend (Spring Boot)

**Archivo (estándar):** `backend/src/test/java/com/denoise/backend/BackendApplicationTests.java`

```java
@SpringBootTest
class BackendApplicationTests {

    @Test
    void contextLoads() {
        // Verifica que el contexto de Spring se levanta correctamente
    }
}
```

Esto garantiza que la configuración del microservicio es válida y que los componentes principales (controladores, servicios, repositorios) se inicializan sin errores.

---

Con estos ejemplos de código y rutas, se evidencia el cumplimiento de todos los criterios de la rúbrica: diseño visual, validación de formularios, gestión de estado, animaciones, consumo de APIs externas, conexión con microservicios, principios OO y pruebas unitarias.
