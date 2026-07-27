package com.technical.buildcamfpv.net;

import com.technical.buildcamfpv.service.FpvService;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public class UdpServer {
    private final JavaPlugin plugin;
    private final FpvService fpvService;
    private final int port;
    private DatagramSocket socket;
    private volatile boolean running;
    private Thread listenThread;

    public UdpServer(JavaPlugin plugin, FpvService fpvService, int port) {
        this.plugin = plugin;
        this.fpvService = fpvService;
        this.port = port;
    }

    public synchronized void start() {
        if (running) return;

        try {
            this.socket = new DatagramSocket(port);
            this.running = true;

            this.listenThread = new Thread(this::listenLoop, "BuildCamFPV-UdpServer");
            this.listenThread.setDaemon(true);
            this.listenThread.start();

            plugin.getLogger().info("UDP Controller listener started on port " + port);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to start UDP Server on port " + port, e);
        }
    }

    private void listenLoop() {
        byte[] buffer = new byte[512];

        while (running && socket != null && !socket.isClosed()) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                String payload = new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8).trim();
                parseAndProcessPacket(payload);
            } catch (Exception e) {
                if (running) {
                    plugin.getLogger().log(Level.WARNING, "Error receiving UDP packet", e);
                }
            }
        }
    }

    private void parseAndProcessPacket(String payload) {
        // Expected payload format: "token,roll,pitch,yaw,throttle"
        // Example: "12345,0.1,-0.5,0.0,0.8"
        try {
            String[] parts = payload.split(",");
            if (parts.length < 5) return;

            String sessionToken = parts[0].trim();
            double roll = Double.parseDouble(parts[1].trim());
            double pitch = Double.parseDouble(parts[2].trim());
            double yaw = Double.parseDouble(parts[3].trim());
            double throttle = Double.parseDouble(parts[4].trim());

            fpvService.updateInputs(sessionToken, roll, pitch, yaw, throttle);
        } catch (NumberFormatException e) {
            // Ignore malformed numeric inputs silently to keep UDP loop responsive
        }
    }

    public synchronized void stop() {
        this.running = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        if (listenThread != null) {
            listenThread.interrupt();
        }
        plugin.getLogger().info("UDP Controller listener stopped.");
    }
}
