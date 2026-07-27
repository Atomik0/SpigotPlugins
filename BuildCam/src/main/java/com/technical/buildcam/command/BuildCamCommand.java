package com.technical.buildcam.command;

import com.technical.buildcam.model.CameraPath;
import com.technical.buildcam.model.CameraPoint;
import com.technical.buildcam.service.CameraService;
import com.technical.buildcam.template.TemplateGenerator;
import com.technical.buildcam.template.TemplateType;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class BuildCamCommand implements CommandExecutor, TabCompleter {
    private final CameraService cameraService;

    public BuildCamCommand(CameraService cameraService) {
        this.cameraService = Objects.requireNonNull(cameraService, "CameraService cannot be null");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Este comando solo puede ser ejecutado por un jugador.");
            return true;
        }

        if (!player.hasPermission("buildcam.use")) {
            player.sendMessage(ChatColor.RED + "No tienes permiso para utilizar BuildCam.");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelpMessage(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "path":
                handlePathCommand(player, args);
                break;
            case "point":
                handlePointCommand(player, args);
                break;
            case "template":
                handleTemplateCommand(player, args);
                break;
            case "play":
                handlePlayCommand(player, args);
                break;
            case "stop":
                handleStopCommand(player);
                break;
            default:
                sendHelpMessage(player);
                break;
        }

        return true;
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Comandos BuildCam ===");
        player.sendMessage(ChatColor.YELLOW + "/buildcam path create <nombre>" + ChatColor.WHITE + " - Crear una nueva ruta de camara.");
        player.sendMessage(ChatColor.YELLOW + "/buildcam path delete <nombre>" + ChatColor.WHITE + " - Eliminar una ruta de camara.");
        player.sendMessage(ChatColor.YELLOW + "/buildcam path list" + ChatColor.WHITE + " - Listar todas las rutas registradas.");
        player.sendMessage(ChatColor.YELLOW + "/buildcam point add <ruta>" + ChatColor.WHITE + " - Agregar punto actual a la ruta.");
        player.sendMessage(ChatColor.YELLOW + "/buildcam point remove <ruta> <indice>" + ChatColor.WHITE + " - Eliminar punto por indice.");
        player.sendMessage(ChatColor.YELLOW + "/buildcam point list <ruta>" + ChatColor.WHITE + " - Listar puntos de una ruta.");
        player.sendMessage(ChatColor.YELLOW + "/buildcam template generate <tipo> <ruta> [parametros]" + ChatColor.WHITE + " - Generar plantilla de ruta.");
        player.sendMessage(ChatColor.YELLOW + "/buildcam play <ruta> [duracion_segundos]" + ChatColor.WHITE + " - Reproducir ruta de camara.");
        player.sendMessage(ChatColor.YELLOW + "/buildcam stop" + ChatColor.WHITE + " - Detener reproduccion activa.");
    }

    private void handlePathCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Uso: /buildcam path <create|delete|list> [nombre]");
            return;
        }

        String action = args[1].toLowerCase();
        if (action.equals("list")) {
            Set<String> names = cameraService.getPathNames();
            if (names.isEmpty()) {
                player.sendMessage(ChatColor.YELLOW + "No hay rutas de camara registradas.");
            } else {
                player.sendMessage(ChatColor.GREEN + "Rutas disponibles: " + ChatColor.WHITE + String.join(", ", names));
            }
            return;
        }

        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Especifica un nombre de ruta.");
            return;
        }

        String name = args[2].toLowerCase();
        if (action.equals("create")) {
            if (cameraService.getPath(name) != null) {
                player.sendMessage(ChatColor.RED + "Ya existe una ruta con el nombre '" + name + "'.");
                return;
            }
            cameraService.savePath(new CameraPath(name));
            player.sendMessage(ChatColor.GREEN + "Ruta de camara '" + name + "' creada exitosamente.");
        } else if (action.equals("delete")) {
            if (cameraService.deletePath(name)) {
                player.sendMessage(ChatColor.GREEN + "Ruta de camara '" + name + "' eliminada.");
            } else {
                player.sendMessage(ChatColor.RED + "No se encontro la ruta '" + name + "'.");
            }
        }
    }

    private void handlePointCommand(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Uso: /buildcam point <add|remove|list> <ruta> [indice]");
            return;
        }

        String action = args[1].toLowerCase();
        String pathName = args[2].toLowerCase();
        CameraPath path = cameraService.getPath(pathName);

        if (path == null) {
            player.sendMessage(ChatColor.RED + "La ruta '" + pathName + "' no existe. Creala con /buildcam path create " + pathName);
            return;
        }

        if (action.equals("add")) {
            CameraPoint point = CameraPoint.fromLocation(player.getLocation());
            path.addPoint(point);
            cameraService.savePath(path);
            player.sendMessage(ChatColor.GREEN + "Punto #" + path.size() + " agregado a la ruta '" + pathName + "'.");
        } else if (action.equals("remove")) {
            if (args.length < 4) {
                player.sendMessage(ChatColor.RED + "Especifica el indice del punto a eliminar.");
                return;
            }
            try {
                int index = Integer.parseInt(args[3]) - 1;
                if (path.removePoint(index)) {
                    cameraService.savePath(path);
                    player.sendMessage(ChatColor.GREEN + "Punto eliminado de la ruta '" + pathName + "'.");
                } else {
                    player.sendMessage(ChatColor.RED + "Indice fuera de rango.");
                }
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "El indice debe ser un numero entero valido.");
            }
        } else if (action.equals("list")) {
            if (path.isEmpty()) {
                player.sendMessage(ChatColor.YELLOW + "La ruta '" + pathName + "' no contiene puntos.");
                return;
            }
            player.sendMessage(ChatColor.GOLD + "=== Puntos de ruta: " + pathName + " ===");
            for (int i = 0; i < path.getPoints().size(); i++) {
                CameraPoint pt = path.getPoints().get(i);
                player.sendMessage(String.format(ChatColor.YELLOW + "#%d: " + ChatColor.WHITE + "X: %.1f Y: %.1f Z: %.1f (Yaw: %.0f, Pitch: %.0f)",
                        (i + 1), pt.getX(), pt.getY(), pt.getZ(), pt.getYaw(), pt.getPitch()));
            }
        }
    }

    private void handleTemplateCommand(Player player, String[] args) {
        if (args.length < 4 || !args[1].equalsIgnoreCase("generate")) {
            player.sendMessage(ChatColor.RED + "Uso: /buildcam template generate <tipo> <nombre_ruta> [parametros...]");
            player.sendMessage(ChatColor.GRAY + "Tipos de plantilla: orbit, spiral, flyby, topdown");
            return;
        }

        TemplateType type = TemplateType.fromString(args[2]);
        if (type == null) {
            player.sendMessage(ChatColor.RED + "Tipo de plantilla no valido. Tipos disponibles: orbit, spiral, flyby, topdown.");
            return;
        }

        String pathName = args[3].toLowerCase();
        Location target = player.getLocation();
        CameraPath generatedPath = null;

        try {
            switch (type) {
                case ORBIT:
                    double radius = args.length > 4 ? Double.parseDouble(args[4]) : 10.0;
                    double heightOffset = args.length > 5 ? Double.parseDouble(args[5]) : 5.0;
                    int orbitSteps = args.length > 6 ? Integer.parseInt(args[6]) : 16;
                    generatedPath = TemplateGenerator.generateOrbit(pathName, target, radius, heightOffset, orbitSteps);
                    break;
                case SPIRAL:
                    double spiralRadius = args.length > 4 ? Double.parseDouble(args[4]) : 12.0;
                    double startHeight = args.length > 5 ? Double.parseDouble(args[5]) : 0.0;
                    double endHeight = args.length > 6 ? Double.parseDouble(args[6]) : 15.0;
                    double turns = args.length > 7 ? Double.parseDouble(args[7]) : 2.0;
                    int spiralSteps = args.length > 8 ? Integer.parseInt(args[8]) : 24;
                    generatedPath = TemplateGenerator.generateSpiral(pathName, target, spiralRadius, startHeight, endHeight, turns, spiralSteps);
                    break;
                case FLYBY:
                    double length = args.length > 4 ? Double.parseDouble(args[4]) : 30.0;
                    double sideOffset = args.length > 5 ? Double.parseDouble(args[5]) : 15.0;
                    double flybyHeight = args.length > 6 ? Double.parseDouble(args[6]) : 5.0;
                    int flybySteps = args.length > 7 ? Integer.parseInt(args[7]) : 16;
                    generatedPath = TemplateGenerator.generateFlyby(pathName, target, length, sideOffset, flybyHeight, flybySteps);
                    break;
                case TOPDOWN:
                    double scanSize = args.length > 4 ? Double.parseDouble(args[4]) : 40.0;
                    double topHeight = args.length > 5 ? Double.parseDouble(args[5]) : 30.0;
                    int topSteps = args.length > 6 ? Integer.parseInt(args[6]) : 16;
                    generatedPath = TemplateGenerator.generateTopdown(pathName, target, scanSize, topHeight, topSteps);
                    break;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Error en los parametros numericos. Revisa la sintaxis.");
            return;
        }

        if (generatedPath != null) {
            cameraService.savePath(generatedPath);
            player.sendMessage(ChatColor.GREEN + "Plantilla '" + type.getKey() + "' generada exitosamente en la ruta '" + pathName + "' (" + generatedPath.size() + " puntos).");
        }
    }

    private void handlePlayCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Uso: /buildcam play <nombre_ruta> [duracion_segundos]");
            return;
        }

        String pathName = args[1].toLowerCase();
        CameraPath path = cameraService.getPath(pathName);
        if (path == null) {
            player.sendMessage(ChatColor.RED + "La ruta '" + pathName + "' no existe.");
            return;
        }

        double duration = 10.0;
        if (args.length >= 3) {
            try {
                duration = Double.parseDouble(args[2]);
            } catch (NumberFormatException e) {
                player.sendMessage(ChatColor.RED + "La duracion debe ser un numero valido.");
                return;
            }
        }

        if (cameraService.startPlayback(player, path, duration)) {
            player.sendMessage(ChatColor.GREEN + "Reproduciendo trayectoria '" + pathName + "' (" + duration + "s)... Usa /buildcam stop para cancelar.");
        } else {
            player.sendMessage(ChatColor.RED + "La ruta necesita al menos 2 puntos para reproducirse.");
        }
    }

    private void handleStopCommand(Player player) {
        if (cameraService.isPlaying(player)) {
            cameraService.stopPlayback(player, true);
            player.sendMessage(ChatColor.YELLOW + "Reproduccion de camara detenida.");
        } else {
            player.sendMessage(ChatColor.RED + "No tienes ninguna reproduccion activa.");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filterPrefix(List.of("help", "path", "point", "template", "play", "stop"), args[0]);
        }

        String sub = args[0].toLowerCase();
        if (sub.equals("path") && args.length == 2) {
            return filterPrefix(List.of("create", "delete", "list"), args[1]);
        }

        if (sub.equals("path") && args.length == 3 && args[1].equalsIgnoreCase("delete")) {
            return filterPrefix(new ArrayList<>(cameraService.getPathNames()), args[2]);
        }

        if (sub.equals("point") && args.length == 2) {
            return filterPrefix(List.of("add", "remove", "list"), args[1]);
        }

        if (sub.equals("point") && args.length == 3) {
            return filterPrefix(new ArrayList<>(cameraService.getPathNames()), args[2]);
        }

        if (sub.equals("template") && args.length == 2) {
            return filterPrefix(List.of("generate"), args[1]);
        }

        if (sub.equals("template") && args.length == 3) {
            return filterPrefix(List.of("orbit", "spiral", "flyby", "topdown"), args[2]);
        }

        if (sub.equals("play") && args.length == 2) {
            return filterPrefix(new ArrayList<>(cameraService.getPathNames()), args[1]);
        }

        return Collections.emptyList();
    }

    private List<String> filterPrefix(List<String> list, String prefix) {
        return list.stream()
                .filter(s -> s.toLowerCase().startsWith(prefix.toLowerCase()))
                .collect(Collectors.toList());
    }
}
