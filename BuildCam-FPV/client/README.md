# BuildCam-FPV Client (Windows & Ubuntu Linux)

Programa puente multiplataforma para conectar controles de radio FPV USB (RadioMaster, FrSky, TBS Tango, Jumper, FlySky, BetaFPV) con el plugin BuildCam-FPV en Minecraft.

---

## Requisitos de Sistema

- Python 3.8 o superior.
- Control FPV conectado por cable USB a la PC en modo **Joystick / Simulator**.

---

## Instalación de Dependencias

### En Ubuntu / Debian Linux:
```bash
sudo apt update
sudo apt install python3 python3-pip
pip3 install -r requirements.txt
```

### En Windows (PowerShell / Command Prompt):
```powershell
pip install -r requirements.txt
```

---

## Modo de Uso Paso a Paso

1. Conecta tu emisora FPV por puerto USB a tu PC y selecciona el modo **USB Joystick / Simulator**.
2. Entra al servidor de Minecraft e inicia una sesión con el comando:
   ```text
   /fpv start
   ```
   El servidor te entregara un **Token de Conexión UDP** de 6 caracteres (por ejemplo: `X7A9K2`).

3. Ejecuta el programa puente en tu PC:
   ```bash
   python3 fpv_controller_bridge.py
   ```
4. Ingresa la IP del servidor de Minecraft (usa `127.0.0.1` si juegas localmente) y el **Token UDP** generado.
5. ¡Listo! Las lecturas de los sticks de tu control se transmitiran inmediatamente al servidor y podras volar en modo Acro FPV dentro de Minecraft.
