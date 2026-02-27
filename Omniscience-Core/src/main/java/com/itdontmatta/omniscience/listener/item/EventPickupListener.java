package com.itdontmatta.omniscience.listener.item;

import com.google.common.collect.ImmutableList;
import com.itdontmatta.omniscience.api.entry.OEntry;
import com.itdontmatta.omniscience.listener.OmniListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityPickupItemEvent;

public class EventPickupListener extends OmniListener {

    public EventPickupListener() {
        super(ImmutableList.of("pickup"));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        OEntry.create().source(event.getEntity()).pickup(event.getItem()).save();
    }
}
