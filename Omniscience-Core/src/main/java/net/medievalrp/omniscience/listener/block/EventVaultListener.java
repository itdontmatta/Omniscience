package net.medievalrp.omniscience.listener.block;

import com.google.common.collect.ImmutableList;
import net.medievalrp.omniscience.Omniscience;
import net.medievalrp.omniscience.api.entry.OEntry;
import net.medievalrp.omniscience.listener.OmniListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listener for Vault block interactions (1.21+)
 * Tracks when players attempt to unlock vault blocks using trial keys.
 *
 * Uses delayed check approach to verify if the vault was actually unlocked
 * by checking if the player's trial key was consumed.
 */
public class EventVaultListener extends OmniListener {

    // Track recent vault interactions to avoid duplicates
    // Key: "world:x:y:z:playerUUID", Value: timestamp
    private final Map<String, Long> recentVaultEvents = new ConcurrentHashMap<>();

    private static final long DUPLICATE_WINDOW_MS = 3000;

    public EventVaultListener() {
        super(ImmutableList.of("vault"));
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.MONITOR)
    public void onVaultInteract(PlayerInteractEvent e) {
        if (!isEnabled("vault")) {
            return;
        }

        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (e.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Block block = e.getClickedBlock();
        if (block == null || block.getType() != Material.VAULT) {
            return;
        }

        Player player = e.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        // Check if player is using a trial key
        Material keyType = itemInHand.getType();
        if (keyType != Material.TRIAL_KEY && keyType != Material.OMINOUS_TRIAL_KEY) {
            return;
        }

        Location blockLocation = block.getLocation();
        String eventKey = blockLocation.getWorld().getName() + ":" +
                         blockLocation.getBlockX() + ":" +
                         blockLocation.getBlockY() + ":" +
                         blockLocation.getBlockZ() + ":" +
                         player.getUniqueId();

        // Check for duplicate within window
        Long lastLog = recentVaultEvents.get(eventKey);
        if (lastLog != null && System.currentTimeMillis() - lastLog < DUPLICATE_WINDOW_MS) {
            return;
        }

        int keyCountBefore = itemInHand.getAmount();

        // Schedule a check after the interaction processes to verify key was consumed
        Bukkit.getScheduler().runTaskLater(Omniscience.getPluginInstance(), () -> {
            // Check if the key was consumed (vault was unlocked)
            ItemStack currentItem = player.getInventory().getItemInMainHand();
            boolean keyConsumed = false;

            if (currentItem.getType() != keyType) {
                // Player no longer holding the key type - likely consumed
                keyConsumed = true;
            } else if (currentItem.getAmount() < keyCountBefore) {
                // Key count decreased
                keyConsumed = true;
            }

            if (keyConsumed) {
                // Mark as logged
                recentVaultEvents.put(eventKey, System.currentTimeMillis());

                // Clean up old entries
                cleanupOldEntries();

                // Log the vault unlock
                OEntry.create().source(player).use(block).save();
            }
        }, 1L); // Check after 1 tick
    }

    private void cleanupOldEntries() {
        long now = System.currentTimeMillis();
        recentVaultEvents.entrySet().removeIf(entry ->
            now - entry.getValue() > DUPLICATE_WINDOW_MS * 2);
    }
}
