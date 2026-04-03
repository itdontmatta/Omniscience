package net.medievalrp.omniscience.listener.block;

import com.google.common.collect.ImmutableList;
import net.medievalrp.omniscience.api.data.DataWrapper;
import net.medievalrp.omniscience.api.entry.OEntry;
import net.medievalrp.omniscience.listener.OmniListener;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.CrafterCraftEvent;
import org.bukkit.inventory.ItemStack;

import static net.medievalrp.omniscience.api.data.DataKeys.*;

/**
 * Listener for Crafter block events (1.21+)
 * Tracks when crafter blocks automatically craft items
 */
public class EventCrafterListener extends OmniListener {

    public EventCrafterListener() {
        super(ImmutableList.of("craft"));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onCrafterCraft(CrafterCraftEvent e) {
        if (!isEnabled("craft")) {
            return;
        }

        Block block = e.getBlock();
        ItemStack result = e.getResult();

        DataWrapper wrapper = DataWrapper.createNew();
        wrapper.set(TARGET, result.getType().name());
        wrapper.set(ITEMSTACK, result);
        wrapper.set(QUANTITY, result.getAmount());
        wrapper.set(DISPLAY_METHOD, "item");

        OEntry.create().source(block).customWithLocation("craft", wrapper, block.getLocation()).save();
    }
}
