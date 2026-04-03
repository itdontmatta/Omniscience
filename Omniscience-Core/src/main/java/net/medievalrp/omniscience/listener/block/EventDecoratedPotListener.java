package net.medievalrp.omniscience.listener.block;

import com.google.common.collect.ImmutableList;
import net.medievalrp.omniscience.api.data.DataWrapper;
import net.medievalrp.omniscience.api.entry.OEntry;
import net.medievalrp.omniscience.listener.OmniListener;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.DecoratedPot;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import static net.medievalrp.omniscience.api.data.DataKeys.*;

/**
 * Listener for Decorated Pot interactions (1.20+)
 * Tracks when players insert or remove items from decorated pots
 */
public class EventDecoratedPotListener extends OmniListener {

    public EventDecoratedPotListener() {
        super(ImmutableList.of("pot-insert", "pot-remove"));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPotInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Block block = e.getClickedBlock();
        if (block == null || block.getType() != Material.DECORATED_POT) {
            return;
        }

        if (!(block.getState() instanceof DecoratedPot)) {
            return;
        }

        Player player = e.getPlayer();
        DecoratedPot pot = (DecoratedPot) block.getState();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        ItemStack itemInPot = pot.getInventory().getItem();

        // If player has item in hand and pot is empty/has space, they're inserting
        if (itemInHand.getType() != Material.AIR) {
            if (isEnabled("pot-insert")) {
                DataWrapper wrapper = DataWrapper.createNew();
                wrapper.set(TARGET, itemInHand.getType().name() + " into DECORATED_POT");
                wrapper.set(ITEMSTACK, itemInHand);
                wrapper.set(QUANTITY, 1);
                wrapper.set(DISPLAY_METHOD, "item");
                OEntry.create().source(player).customWithLocation("pot-insert", wrapper, block.getLocation()).save();
            }
        } else if (itemInPot != null && itemInPot.getType() != Material.AIR) {
            // Player has empty hand and pot has item, they're removing
            if (isEnabled("pot-remove")) {
                DataWrapper wrapper = DataWrapper.createNew();
                wrapper.set(TARGET, itemInPot.getType().name() + " from DECORATED_POT");
                wrapper.set(ITEMSTACK, itemInPot);
                wrapper.set(QUANTITY, itemInPot.getAmount());
                wrapper.set(DISPLAY_METHOD, "item");
                OEntry.create().source(player).customWithLocation("pot-remove", wrapper, block.getLocation()).save();
            }
        }
    }
}
