package com.technical.buildcam.listener;

import com.technical.buildcam.service.CameraService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Objects;

public class PlayerQuitListener implements Listener {
    private final CameraService cameraService;

    public PlayerQuitListener(CameraService cameraService) {
        this.cameraService = Objects.requireNonNull(cameraService, "CameraService cannot be null");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (cameraService.isPlaying(event.getPlayer())) {
            cameraService.stopPlayback(event.getPlayer(), true);
        }
    }
}
