package com.itdontmatta.omniscience.listener.block;

import com.google.common.collect.ImmutableList;
import com.itdontmatta.omniscience.Omniscience;
import com.itdontmatta.omniscience.api.data.DataWrapper;
import com.itdontmatta.omniscience.api.entry.OEntry;
import com.itdontmatta.omniscience.listener.OmniListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BrushableBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.itdontmatta.omniscience.api.data.DataKeys.*;

/**
 * Listener for Archaeology/Brushing events (1.20+)
 * Tracks when players brush suspicious blocks and find items
 *
 * Uses delayed task approach since there is no dedicated brush completion event.
 * When a player right-clicks a suspicious block with a brush, we schedule a check
 * after brushing completes (100 ticks = 5 seconds) to see if the block transformed.
 */
public class EventBrushListener extends OmniListener {

    // Track recent brush events to avoid duplicate logging
    // Key: location string, Value: timestamp
    private final Map<String, Long> recentBrushEvents = new ConcurrentHashMap<>();

    // Brushing takes approximately 96 ticks (4.8 seconds), we use 100 for safety
    private static final int BRUSH_COMPLETION_DELAY = 100;

    // Don't log the same location within 5 seconds
    private static final long DUPLICATE_WINDOW_MS = 5000;

    public EventBrushListener() {
        super(ImmutableList.of("brush"));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerBrush(PlayerInteractEvent e) {
        if (!isEnabled("brush")) {
            return;
        }

        // Only handle right-click on blocks
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // Only handle main hand to avoid double events
        if (e.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Block block = e.getClickedBlock();
        if (block == null) {
            return;
        }

        Material blockType = block.getType();

        // Check if this is a suspicious block
        if (blockType != Material.SUSPICIOUS_SAND && blockType != Material.SUSPICIOUS_GRAVEL) {
            return;
        }

        Player player = e.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        // Check if player is holding a brush
        if (itemInHand.getType() != Material.BRUSH) {
            return;
        }

        // Get the loot table item before brushing completes (if possible)
        ItemStack lootItem = null;
        if (block.getState() instanceof BrushableBlock brushable) {
            lootItem = brushable.getItem();
        }

        Location blockLocation = block.getLocation();
        String locationKey = blockLocation.getWorld().getName() + ":" +
                            blockLocation.getBlockX() + ":" +
                            blockLocation.getBlockY() + ":" +
                            blockLocation.getBlockZ();
        Material originalType = blockType;
        ItemStack capturedLoot = lootItem;

        // Schedule a check after brushing should complete
        Bukkit.getScheduler().runTaskLater(Omniscience.getPluginInstance(), () -> {
            // Check if we already logged this location recently
            Long lastLog = recentBrushEvents.get(locationKey);
            if (lastLog != null && System.currentTimeMillis() - lastLog < DUPLICATE_WINDOW_MS) {
                return;
            }

            // Check if block has transformed (suspicious -> sand/gravel)
            Block currentBlock = blockLocation.getBlock();
            Material newType = currentBlock.getType();

            // Verify the block actually changed from suspicious to normal
            if (originalType == newType) {
                return; // Block hasn't changed yet, brushing incomplete
            }

            // Check if it changed to the expected result
            boolean validTransform = (originalType == Material.SUSPICIOUS_SAND && newType == Material.SAND) ||
                                    (originalType == Material.SUSPICIOUS_GRAVEL && newType == Material.GRAVEL);

            if (!validTransform) {
                return;
            }

            // Mark this location as logged
            recentBrushEvents.put(locationKey, System.currentTimeMillis());

            // Clean up old entries periodically
            cleanupOldEntries();

            // Log the brush event
            DataWrapper wrapper = DataWrapper.createNew();

            if (capturedLoot != null && capturedLoot.getType() != Material.AIR) {
                wrapper.set(TARGET, capturedLoot.getType().name() + " from " + originalType.name());
                wrapper.set(ITEMSTACK, capturedLoot);
                wrapper.set(QUANTITY, capturedLoot.getAmount());
                wrapper.set(DISPLAY_METHOD, "item");
            } else {
                wrapper.set(TARGET, originalType.name());
            }

            OEntry.create().source(player).customWithLocation("brush", wrapper, blockLocation).save();

        }, BRUSH_COMPLETION_DELAY);
    }

    private void cleanupOldEntries() {
        long now = System.currentTimeMillis();
        recentBrushEvents.entrySet().removeIf(entry ->
            now - entry.getValue() > DUPLICATE_WINDOW_MS * 2);
    }
}
