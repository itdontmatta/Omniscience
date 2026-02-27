package com.itdontmatta.omniscience.listener.player;

import com.google.common.collect.ImmutableList;
import com.itdontmatta.omniscience.api.entry.OEntry;
import com.itdontmatta.omniscience.listener.OmniListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;

public class EventQuitListener extends OmniListener {

    public EventQuitListener() {
        super(ImmutableList.of("quit"));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent e) {
        OEntry.create().player(e.getPlayer()).quit().save();
    }
}
