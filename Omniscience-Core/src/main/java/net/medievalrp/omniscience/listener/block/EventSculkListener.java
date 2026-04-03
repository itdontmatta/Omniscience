package net.medievalrp.omniscience.listener.block;

import com.google.common.collect.ImmutableList;
import net.medievalrp.omniscience.api.entry.OEntry;
import net.medievalrp.omniscience.listener.OmniListener;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockReceiveGameEvent;

/**
 * Listener for Sculk Sensor activation events (1.19+)
 * Tracks when sculk sensors detect vibrations from players
 */
public class EventSculkListener extends OmniListener {

    public EventSculkListener() {
        super(ImmutableList.of("sculk"));
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.MONITOR)
    public void onSculkActivation(BlockReceiveGameEvent e) {
        if (!isEnabled("sculk")) {
            return;
        }

        Block block = e.getBlock();
        String blockType = block.getType().name();

        // Only track sculk sensor related blocks
        if (!blockType.contains("SCULK_SENSOR") && !blockType.contains("SCULK_SHRIEKER")) {
            return;
        }

        Entity entity = e.getEntity();
        if (entity instanceof Player) {
            OEntry.create().source(entity).use(block).save();
        }
    }
}
