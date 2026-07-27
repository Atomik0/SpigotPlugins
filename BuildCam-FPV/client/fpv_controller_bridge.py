#!/usr/bin/env python3
"""
BuildCam-FPV Controller Bridge
Multiplatform (Windows & Ubuntu Linux) FPV Radio Controller UDP Bridge for Minecraft
"""

import sys
import time
import socket
import os

try:
    import pygame
except ImportError:
    print("Error: 'pygame' library is not installed.")
    print("Please install it using: pip install pygame")
    sys.exit(1)

UDP_PORT = 8888

def clear_screen():
    os.system('cls' if os.name == 'nt' else 'clear')

def main():
    print("=== BuildCam-FPV Controller Bridge (Windows / Ubuntu) ===")
    print("Initializing SDL/Pygame Joystick System...")
    
    pygame.init()
    pygame.joystick.init()

    joystick_count = pygame.joystick.get_count()
    if joystick_count == 0:
        print("\n[!] No USB FPV radio or joystick detected!")
        print("Please connect your FPV Radio Controller via USB (in Joystick/Simulator Mode) and restart this program.")
        sys.exit(1)

    print(f"\nDetected {joystick_count} joystick(s):")
    for i in range(joystick_count):
        js = pygame.joystick.Joystick(i)
        js.init()
        print(f"  [{i}] {js.get_name()} (Axes: {js.get_numaxes()}, Buttons: {js.get_numbuttons()})")

    selected_index = 0
    if joystick_count > 1:
        try:
            selected_index = int(input("\nSelect joystick index [0]: ") or "0")
        except ValueError:
            selected_index = 0

    controller = pygame.joystick.Joystick(selected_index)
    controller.init()

    print(f"\nUsing controller: {controller.get_name()}")
    num_axes = controller.get_numaxes()

    if num_axes < 4:
        print(f"[!] Warning: Controller only has {num_axes} axes, 4 axes are expected for Roll, Pitch, Yaw, Throttle.")

    server_ip = input("\nEnter Minecraft Server IP Address [127.0.0.1]: ").strip() or "127.0.0.1"
    session_token = input("Enter your FPV Session Token (from /fpv start in Minecraft): ").strip().upper()

    if not session_token:
        print("[!] Error: Session Token cannot be empty!")
        sys.exit(1)

    # Default axis mapping (standard FPV transmitter in Mode 2)
    # Axis index assignment: Roll, Pitch, Yaw, Throttle
    axis_roll = 0
    axis_pitch = 1
    axis_yaw = 2
    axis_throttle = 3

    print("\n--- Standard Axis Mapping ---")
    print(f"Roll Axis: {axis_roll} | Pitch Axis: {axis_pitch} | Yaw Axis: {axis_yaw} | Throttle Axis: {axis_throttle}")
    print("Sending UDP packets to {}:{} at 60 Hz...".format(server_ip, UDP_PORT))
    print("Press Ctrl+C to stop.\n")

    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    clock = pygame.time.Clock()

    try:
        while True:
            pygame.event.pump()

            # Read axis values (-1.0 to 1.0)
            roll = controller.get_axis(axis_roll) if num_axes > axis_roll else 0.0
            pitch = controller.get_axis(axis_pitch) if num_axes > axis_pitch else 0.0
            yaw = controller.get_axis(axis_yaw) if num_axes > axis_yaw else 0.0
            raw_throttle = controller.get_axis(axis_throttle) if num_axes > axis_throttle else -1.0

            # Convert throttle from [-1.0, 1.0] joystick range to [0.0, 1.0]
            throttle = (1.0 - raw_throttle) / 2.0 if raw_throttle <= 1.0 else 0.0
            throttle = max(0.0, min(1.0, throttle))

            # Payload format: token,roll,pitch,yaw,throttle
            payload = f"{session_token},{roll:.4f},{pitch:.4f},{yaw:.4f},{throttle:.4f}"
            sock.sendto(payload.encode('utf-8'), (server_ip, UDP_PORT))

            sys.stdout.write(f"\r[TX -> {server_ip}] Token: {session_token} | Roll: {roll:+.2f} | Pitch: {pitch:+.2f} | Yaw: {yaw:+.2f} | Throttle: {throttle:.2f}   ")
            sys.stdout.flush()

            clock.tick(60) # 60 Hz UDP transmission

    except KeyboardInterrupt:
        print("\n\nFPV Controller Bridge stopped.")
    finally:
        sock.close()
        pygame.quit()

if __name__ == "__main__":
    main()
