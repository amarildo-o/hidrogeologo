# HidroCampo

Aplicación Android para hidrogeólogos, orientada a registrar en campo pruebas y datos de pozos de agua y manantiales.

## Funcionalidad

Para cada punto de agua (pozo, noria o manantial) se captura:

- **Datos administrativos y ubicación**: sitio, folio, propietario, comunidad, municipio, estado, técnico responsable, fecha de visita y coordenadas GPS (capturadas con el GPS del dispositivo o editables manualmente).
- **Estado de la infraestructura**: condición del brocal, cercado, losa sanitaria, ademe de protección, candado, caseta de bombeo, camino de acceso, radio de protección sanitaria y condición general.
- **Parámetros hidráulicos y del pozo**: profundidad total, diámetro, niveles estático y dinámico, caudal y duración de prueba de bombeo, capacidad específica, tipo de acuífero, material de ademe, intervalos de rejilla y uso del agua.
- **Componentes electromecánicos**: tipo/marca/modelo de bomba, potencia del motor, voltaje, fase, fuente de energía, panel de control, generador de respaldo y profundidad de instalación.
- **Calidad fisicoquímica del agua**: pH, conductividad eléctrica, sólidos disueltos totales, temperatura, turbidez, oxígeno disuelto, cloro libre residual, olor, color y aspecto.
- **Observaciones y hallazgos**: notas libres sobre riesgos, hallazgos y recomendaciones.
- **Fotografías**: captura de fotos con la cámara del dispositivo, asociadas a cada registro.
- **Exportación**:
  - **CSV** (desde el listado, ícono de descarga en la barra superior): exporta *todos* los registros en una sola tabla (una fila por pozo/manantial, todas las secciones aplanadas en columnas), lista para abrir en Excel/Google Sheets o importar a un SIG.
  - **PDF** (desde el detalle de un registro, ícono de PDF): genera un reporte de una página por registro con todas las secciones y las fotografías incrustadas.
  - Ambos formatos se generan en `getExternalFilesDir("exports")` y se comparten con el selector de Android (`Intent.ACTION_SEND`) hacia cualquier app instalada (correo, Drive, WhatsApp, etc.), sin requerir conexión a internet para generarlos.

Los registros se almacenan localmente (Room/SQLite), por lo que la app funciona sin conexión a internet, ideal para trabajo de campo.

## Arquitectura

- **Kotlin + Jetpack Compose (Material 3)** para la interfaz.
- **Room** para persistencia local (`WellRecord` + `RecordPhoto`, con fotos referenciadas por archivo en `getExternalFilesDir("fotos")`).
- **CameraX / ActivityResultContracts.TakePicture** + `FileProvider` para la captura de fotografías.
- **FusedLocationProviderClient** (Google Play Services) para la geolocalización del punto de agua.
- **`android.graphics.pdf.PdfDocument`** (API de plataforma, sin librerías externas) para generar los reportes PDF, y un exportador CSV propio (`CsvExporter`) para el volcado tabular. Ambos se comparten vía `FileProvider` + `Intent.ACTION_SEND`.
- **Navigation Compose** con tres pantallas: listado de registros, formulario multi-sección (pestañas deslizables) y detalle de un registro.
- Sin inyección de dependencias externa: un `Application` (`HidroCampoApp`) expone el repositorio (`FieldRecordRepository`) como singleton, y los `ViewModel` se crean con `viewModelFactory { initializer { ... } }`.

## Estructura del proyecto

```
app/src/main/java/com/hidrogeologo/campo/
├── data/            Entidades Room, DAOs, base de datos y repositorio
├── ui/
│   ├── theme/       Tema Material 3
│   ├── navigation/  Rutas y NavHost
│   ├── list/        Listado de registros
│   ├── form/        Formulario multi-sección (una pestaña por categoría de datos)
│   ├── detail/       Vista de detalle de un registro
│   └── components/  Campos de formulario reutilizables y galería de fotos
└── util/            Ubicación, archivos de foto, fechas y exportación (CSV/PDF) + compartir
```

## Cómo compilar

Este proyecto se generó y revisó en un entorno sin Android SDK ni acceso a los repositorios de Google (`dl.google.com`), por lo que **no pudo compilarse ni ejecutarse dentro de esta sesión**. Para compilarlo:

1. Abrir la carpeta del proyecto en Android Studio (Koala o superior).
2. Dejar que Android Studio descargue el Android SDK (compileSdk/targetSdk 34) y sincronice Gradle.
3. Ejecutar en un emulador o dispositivo físico con Android 8.0 (API 26) o superior.

También puede compilarse por línea de comandos una vez configurado `ANDROID_HOME`:

```
./gradlew assembleDebug
```

## Limitaciones conocidas / próximos pasos sugeridos

- Las fotos tomadas antes de guardar el registro se conservan en memoria (`pendingPhotos`) y se asocian a la base de datos hasta presionar "Guardar"; si se sale del formulario sin guardar, los archivos de foto ya escritos en disco no se eliminan automáticamente.
- No incluye sincronización en la nube; el almacenamiento (y los archivos exportados) es 100% local en el dispositivo hasta que el usuario los comparte manualmente.
- El CSV exporta siempre *todos* los registros (no hay selección/filtrado); el PDF es por registro individual.
- No incluye pruebas automatizadas (unit/UI tests).
- Los catálogos (condición, tipo de acuífero, etc.) son campos de texto libre o listas simples embebidas en el código; se pueden mover a un catálogo configurable si se requiere estandarización entre usuarios.
