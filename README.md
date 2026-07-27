# SpigotPlugins Suite

Este repositorio contiene una suite de dos plugins independientes desarrollados para servidores Paper y Spigot de Minecraft (1.20.4+): **BuildCam** (para tomas cinematograficas precalculadas) y **BuildCam-FPV** (para simulacion de dron FPV en tiempo real mediante control USB).

---

## 1. Plugin: BuildCam

Plugin orientado a la grabacion cinematografica y recorridos de camara fluidos para construcciones y proyectos de Minecraft.

### Caracteristicas
- **Interpolacion Spline (Catmull-Rom)**: Transiciones continuas y curvas entre waypoints evitando giros o movimientos bruscos.
- **Gestion Manual de Rutas**: Permite registrar posiciones (X, Y, Z) y angulos de mirada (Yaw, Pitch) directamente dentro del juego.
- **Generador de Plantillas**: Generacion automatica de recorridos alrededor de estructuras.

### Comandos de BuildCam
- **Permiso**: `buildcam.use`
- **Alias**: `/buildcam`, `/bcam`, `/cam`

#### Gestion Manual
```text
/buildcam path create <nombre>           - Crear una nueva ruta de camara
/buildcam path delete <nombre>           - Eliminar una ruta existente
/buildcam path list                      - Listar todas las rutas registradas
/buildcam point add <ruta>               - Agregar posicion y vista actual a la ruta
/buildcam point list <ruta>              - Mostrar waypoints registrados
/buildcam point remove <ruta> <indice>   - Eliminar un waypoint especifico
```

#### Generacion con Plantillas
```text
/buildcam template generate orbit <nombre> [radio] [altura_offset] [pasos]
/buildcam template generate spiral <nombre> [radio] [altura_inicio] [altura_fin] [vueltas] [pasos]
/buildcam template generate flyby <nombre> [longitud] [distancia_lateral] [altura] [pasos]
/buildcam template generate topdown <nombre> [tamaño_escaneo] [altura] [pasos]
```

#### Reproduccion y Control
```text
/buildcam play <nombre> [duracion_segundos] - Iniciar recorrido en modo espectador
/buildcam stop                              - Detener reproduccion activa
```

---

## 2. Plugin: BuildCam-FPV

Plugin de simulacion de dron FPV en modo Acro (Rate Mode) que permite controlar la camara en tiempo real utilizando una emisora de radio FPV USB (RadioMaster, FrSky, TBS, Jumper, etc.) conectada a la PC.

### Caracteristicas
- **Fisica Acro (Rate Mode)**: Control de velocidad angular (Roll, Pitch, Yaw), vector de empuje (Throttle), gravedad e inercia mediante cuaterniones 3D.
- **Servidor UDP**: Listener integrado en el puerto 8888 para recepcion de datos a 60 Hz.
- **Sincronizacion de Espectadores**: Permite que otros jugadores en el servidor sigan la vista del dron en vivo.
- **Cliente Multiplataforma**: Incluye cliente Python compatible con Windows y Ubuntu Linux.

### Comandos de BuildCam-FPV
- **Permiso**: `buildcamfpv.use`
- **Alias**: `/fpv`, `/drone`, `/fpvdrone`

```text
/fpv start                       - Iniciar sesion FPV y obtener Token de conexion UDP
/fpv stop                        - Finalizar sesion FPV
/fpv status                      - Ver estado de conexion e inputs de la radio
/fpv tilt <grados>               - Ajustar angulo de inclinacion de la camara
/fpv rates <roll> <pitch> <yaw>  - Ajustar tasas de rotacion angular (deg/s)
```

### Ejecucion del Cliente FPV (Windows y Ubuntu)

El programa puente se encuentra en la carpeta `BuildCam-FPV/client/`.

1. Instalar dependencias:
   ```bash
   pip install -r BuildCam-FPV/client/requirements.txt
   ```
2. Ejecutar el script:
   - **En Ubuntu / Linux**:
     ```bash
     python3 BuildCam-FPV/client/fpv_controller_bridge.py
     ```
   - **En Windows**:
     ```powershell
     python BuildCam-FPV/client/fpv_controller_bridge.py
     ```
3. Ingresar la IP del servidor y el Token de 6 caracteres generado por `/fpv start`.

---

## Compilacion y Lanzamiento de Releases

### Compilacion Local
Para compilar ambos plugins localmente usando el wrapper de Gradle:

- **En Ubuntu / Linux**:
  ```bash
  ./gradlew build
  ```
- **En Windows**:
  ```cmd
  gradlew.bat build
  ```

Los archivos compilados `.jar` resultantes se generan en:
- `BuildCam/build/libs/BuildCam-1.0.x.jar`
- `BuildCam-FPV/build/libs/BuildCam-FPV-1.0.x.jar`

### Script de Publicacion de Releases (Automático)
Para generar una nueva version y publicarla automaticamente en la seccion Releases de GitHub:

- **En Ubuntu / Linux**:
  ```bash
  ./create_release.sh v1.0.3
  ```
- **En Windows**:
  ```cmd
  create_release.bat v1.0.3
  ```
