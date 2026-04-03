package net.lordofthecraft.omniscience.listener.block;

import com.google.common.collect.ImmutableList;
import net.lordofthecraft.omniscience.api.data.LocationTransaction;
import net.lordofthecraft.omniscience.api.entry.OEntry;
import net.lordofthecraft.omniscience.listener.OmniListener;
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
