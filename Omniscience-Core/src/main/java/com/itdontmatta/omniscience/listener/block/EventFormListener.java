package com.itdontmatta.omniscience.listener.block;

import com.google.common.collect.ImmutableList;
import com.itdontmatta.omniscience.api.data.LocationTransaction;
import com.itdontmatta.omniscience.api.entry.OEntry;
import com.itdontmatta.omniscience.listener.OmniListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockFormEvent;

public class EventFormListener extends OmniListener {

    public EventFormListener() {
        super(ImmutableList.of("form"));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockForm(BlockFormEvent event) {
        OEntry.create().environment().formedBlock(new LocationTransaction<>(event.getBlock().getLocation(), event.getBlock().getState(), event.getNewState())).save();
    }
}
