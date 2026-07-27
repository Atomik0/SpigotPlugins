package com.technical.buildcamfpv;

import com.technical.buildcamfpv.command.FpvCommand;
import com.technical.buildcamfpv.net.UdpServer;
import com.technical.buildcamfpv.service.FpvService;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class BuildCamFPVPlugin extends JavaPlugin {
    private FpvService fpvService;
    private UdpServer udpServer;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.fpvService = new FpvService(this);

        int udpPort = getConfig().getInt("udp-port", 8888);
        this.udpServer = new UdpServer(this, fpvService, udpPort);
        this.udpServer.start();

        FpvCommand fpvCommandHandler = new FpvCommand(fpvService);
        PluginCommand cmd = getCommand("fpv");
        if (cmd != null) {
            cmd.setExecutor(fpvCommandHandler);
            cmd.setTabCompleter(fpvCommandHandler);
        }

        getLogger().info("BuildCam-FPV plugin has been successfully enabled on UDP port " + udpPort);
    }

    @Override
    public void onDisable() {
        if (udpServer != null) {
            udpServer.stop();
        }
        if (fpvService != null) {
            fpvService.stopAllSessions();
        }
        getLogger().info("BuildCam-FPV plugin has been disabled and all sessions stopped.");
    }

    public FpvService getFpvService() {
        return fpvService;
    }
}
