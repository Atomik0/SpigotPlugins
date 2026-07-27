package com.technical.buildcamfpv.command;

import com.technical.buildcamfpv.model.FpvSession;
import com.technical.buildcamfpv.service.FpvService;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class FpvCommand implements CommandExecutor, TabCompleter {
    private final FpvService fpvService;

    public FpvCommand(FpvService fpvService) {
        this.fpvService = Objects.requireNonNull(fpvService, "FpvService cannot be null");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Este comando solo puede ser ejecutado por un jugador.");
            return true;
        }

        if (!player.hasPermission("buildcamfpv.use")) {
            player.sendMessage(ChatColor.RED + "No tienes permiso para utilizar BuildCam-FPV.");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelpMessage(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "start":
                handleStartCommand(player);
                break;
            case "stop":
                handleStopCommand(player);
                break;
            case "status":
                handleStatusCommand(player);
                break;
            case "tilt":
                handleTiltCommand(player, args);
                break;
            case "rates":
                handleRatesCommand(player, args);
                break;
            default:
                sendHelpMessage(player);
                break;
        }

        return true;
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Comandos BuildCam-FPV ===");
        player.sendMessage(ChatColor.YELLOW + "/fpv start" + ChatColor.WHITE + " - Iniciar sesion de simulacion FPV y obtener Token UDP.");
        player.sendMessage(ChatColor.YELLOW + "/fpv stop" + ChatColor.WHITE + " - Detener simulacion FPV y restaurar modo de juego.");
        player.sendMessage(ChatColor.YELLOW + "/fpv status" + ChatColor.WHITE + " - Ver estado de conexion e inputs de la radio.");
        player.sendMessage(ChatColor.YELLOW + "/fpv tilt <grados>" + ChatColor.WHITE + " - Ajustar angulo de inclinacion de la camara.");
        player.sendMessage(ChatColor.YELLOW + "/fpv rates <roll> <pitch> <yaw>" + ChatColor.WHITE + " - Ajustar tasas de rotacion (deg/s).");
    }

    private void handleStartCommand(Player player) {
        FpvSession session = fpvService.startSession(player);
        player.sendMessage(ChatColor.GREEN + "=== Sesion de Dron FPV Iniciada ===");
        player.sendMessage(ChatColor.YELLOW + "Tu Token de Conexion UDP es: " + ChatColor.GOLD + ChatColor.BOLD + session.getSessionToken());
        player.sendMessage(ChatColor.WHITE + "Ingresa este token en el programa puente 'fpv_controller_bridge' en tu PC.");
        player.sendMessage(ChatColor.GRAY + "Usa /fpv stop para terminar la sesion.");
    }

    private void handleStopCommand(Player player) {
        if (fpvService.isSessionActive(player)) {
            fpvService.stopSession(player, true);
            player.sendMessage(ChatColor.YELLOW + "Sesion de simulacion FPV finalizada.");
        } else {
            player.sendMessage(ChatColor.RED + "No tienes ninguna sesion FPV activa.");
        }
    }

    private void handleStatusCommand(Player player) {
        FpvSession session = fpvService.getSession(player);
        if (session == null) {
            player.sendMessage(ChatColor.RED + "No tienes una sesion FPV activa.");
            return;
        }

        long timeSinceLastInput = System.currentTimeMillis() - session.getLastInputTimestamp();
        boolean connected = timeSinceLastInput < 2000;

        player.sendMessage(ChatColor.GOLD + "=== Estado de FPV Session ===");
        player.sendMessage(ChatColor.YELLOW + "Token UDP: " + ChatColor.WHITE + session.getSessionToken());
        player.sendMessage(ChatColor.YELLOW + "Estado Radio USB: " + (connected ? ChatColor.GREEN + "CONECTADO" : ChatColor.RED + "DESCONECTADO (Esperando datos UDP)"));
        player.sendMessage(String.format(ChatColor.YELLOW + "Inputs actual: " + ChatColor.WHITE + "Roll: %.2f | Pitch: %.2f | Yaw: %.2f | Throttle: %.2f",
                session.getCurrentRollInput(), session.getCurrentPitchInput(), session.getCurrentYawInput(), session.getCurrentThrottleInput()));
        player.sendMessage(ChatColor.YELLOW + "Camera Tilt: " + ChatColor.WHITE + session.getPhysicsEngine().getCameraTilt() + " deg");
    }

    private void handleTiltCommand(Player player, String[] args) {
        FpvSession session = fpvService.getSession(player);
        if (session == null) {
            player.sendMessage(ChatColor.RED + "Inicia una sesion FPV con /fpv start primero.");
            return;
        }

        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Uso: /fpv tilt <grados>");
            return;
        }

        try {
            double tilt = Double.parseDouble(args[1]);
            session.getPhysicsEngine().setCameraTilt(tilt);
            player.sendMessage(ChatColor.GREEN + "Camera tilt ajustado a: " + tilt + " grados.");
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "El valor de tilt debe ser un numero valido.");
        }
    }

    private void handleRatesCommand(Player player, String[] args) {
        FpvSession session = fpvService.getSession(player);
        if (session == null) {
            player.sendMessage(ChatColor.RED + "Inicia una sesion FPV con /fpv start primero.");
            return;
        }

        if (args.length < 4) {
            player.sendMessage(ChatColor.RED + "Uso: /fpv rates <roll_rate> <pitch_rate> <yaw_rate>");
            return;
        }

        try {
            double rollRate = Double.parseDouble(args[1]);
            double pitchRate = Double.parseDouble(args[2]);
            double yawRate = Double.parseDouble(args[3]);

            session.getPhysicsEngine().setMaxRollRate(rollRate);
            session.getPhysicsEngine().setMaxPitchRate(pitchRate);
            session.getPhysicsEngine().setMaxYawRate(yawRate);

            player.sendMessage(String.format(ChatColor.GREEN + "Rates actualizados a: Roll: %.0f deg/s | Pitch: %.0f deg/s | Yaw: %.0f deg/s",
                    rollRate, pitchRate, yawRate));
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Los valores de rates deben ser numeros validos.");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filterPrefix(List.of("help", "start", "stop", "status", "tilt", "rates"), args[0]);
        }
        return Collections.emptyList();
    }

    private List<String> filterPrefix(List<String> list, String prefix) {
        return list.stream()
                .filter(s -> s.toLowerCase().startsWith(prefix.toLowerCase()))
                .collect(Collectors.toList());
    }
}
