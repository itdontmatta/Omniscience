package com.itdontmatta.omniscience.listener;

import com.itdontmatta.omniscience.Omniscience;
import org.bukkit.command.Command;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;

public class PermissionListener implements Listener {

    private final Omniscience omniscience;
    private final Command pluginCommand;

    public PermissionListener(Omniscience omniscience, Command pluginCommand) {
        this.omniscience = omniscience;
        this.pluginCommand = pluginCommand;
    }

    @EventHandler
    public void onPlayerCommandSend(PlayerCommandSendEvent e) {

    }
}
