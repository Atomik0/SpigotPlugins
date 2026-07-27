package com.technical.buildcam;

import com.technical.buildcam.command.BuildCamCommand;
import com.technical.buildcam.listener.PlayerQuitListener;
import com.technical.buildcam.service.CameraService;
import com.technical.buildcam.storage.PathStorage;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class BuildCamPlugin extends JavaPlugin {
    private CameraService cameraService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        PathStorage storage = new PathStorage(this);

        this.cameraService = new CameraService(this, storage);

        BuildCamCommand commandHandler = new BuildCamCommand(cameraService);
        PluginCommand cmd = getCommand("buildcam");
        if (cmd != null) {
            cmd.setExecutor(commandHandler);
            cmd.setTabCompleter(commandHandler);
        }

        getServer().getPluginManager().registerEvents(new PlayerQuitListener(cameraService), this);

        getLogger().info("BuildCam plugin has been successfully enabled.");
    }

    @Override
    public void onDisable() {
        if (cameraService != null) {
            cameraService.stopAllSessions();
        }
        getLogger().info("BuildCam plugin has been disabled and all active sessions restored.");
    }

    public CameraService getCameraService() {
        return cameraService;
    }
}
