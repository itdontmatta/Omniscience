package com.itdontmatta.omniscience.worldedit;

import com.itdontmatta.omniscience.Omniscience;
import com.itdontmatta.omniscience.api.data.DataWrapper;
import com.itdontmatta.omniscience.api.entry.ActionResult;
import com.itdontmatta.omniscience.api.entry.DataEntry;
import com.itdontmatta.omniscience.api.entry.SkipReason;
import com.itdontmatta.omniscience.api.util.DataHelper;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockState;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.itdontmatta.omniscience.api.data.DataKeys.*;

/**
 * Handles batch rollback/restore operations using FastAsyncWorldEdit for better performance.
 */
public class FAWERollbackHandler {

    private static boolean faweAvailable = false;

    static {
        try {
            Class.forName("com.sk89q.worldedit.WorldEdit");
            faweAvailable = true;
            Omniscience.getPluginInstance().getLogger().info("FAWE/WorldEdit detected - batch rollback enabled");
        } catch (ClassNotFoundException e) {
            faweAvailable = false;
        }
    }

    public static boolean isFaweAvailable() {
        return faweAvailable;
    }

    /**
     * Performs a batch rollback of block entries using FAWE
     * @param entries List of data entries to rollback
     * @param sender The command sender (for actor context)
     * @return List of ActionResults
     */
    public static List<ActionResult> batchRollback(List<DataEntry> entries, CommandSender sender) {
        return batchProcess(entries, sender, true);
    }

    /**
     * Performs a batch restore of block entries using FAWE
     * @param entries List of data entries to restore
     * @param sender The command sender (for actor context)
     * @return List of ActionResults
     */
    public static List<ActionResult> batchRestore(List<DataEntry> entries, CommandSender sender) {
        return batchProcess(entries, sender, false);
    }

    private static List<ActionResult> batchProcess(List<DataEntry> entries, CommandSender sender, boolean isRollback) {
        List<ActionResult> results = new ArrayList<>();

        // Group entries by world
        Map<World, List<BlockChange>> changesByWorld = new HashMap<>();

        Omniscience.getPluginInstance().getLogger().info("[FAWE] Processing " + entries.size() + " entries, isRollback=" + isRollback);

        for (DataEntry entry : entries) {
            // Only process block entries
            if (!isBlockEntry(entry)) {
                Omniscience.getPluginInstance().getLogger().info("[FAWE] Entry is not a block entry: " + entry.getEventName() + ", data keys: " + entry.data.getKeys(false));
                results.add(ActionResult.skipped(SkipReason.INVALID));
                continue;
            }

            try {
                Optional<Location> optLocation = DataHelper.getLocationFromDataWrapper(entry.data);
                if (!optLocation.isPresent()) {
                    Omniscience.getPluginInstance().getLogger().info("[FAWE] No location for entry: " + entry.getEventName());
                    results.add(ActionResult.skipped(SkipReason.INVALID_LOCATION));
                    continue;
                }

                Location location = optLocation.get();
                World world = location.getWorld();
                if (world == null) {
                    Omniscience.getPluginInstance().getLogger().info("[FAWE] World is null for entry: " + entry.getEventName());
                    results.add(ActionResult.skipped(SkipReason.INVALID_LOCATION));
                    continue;
                }

                // Get the block data to apply
                Optional<BlockData> blockData;
                if (isRollback) {
                    blockData = getOriginalBlockData(entry);
                } else {
                    blockData = getNewBlockData(entry);
                }

                if (!blockData.isPresent()) {
                    Omniscience.getPluginInstance().getLogger().info("[FAWE] No block data for entry: " + entry.getEventName());
                    results.add(ActionResult.skipped(SkipReason.INVALID));
                    continue;
                }

                Omniscience.getPluginInstance().getLogger().info("[FAWE] Adding block change at " + location + " -> " + blockData.get().getMaterial());
                changesByWorld.computeIfAbsent(world, k -> new ArrayList<>())
                    .add(new BlockChange(location, blockData.get()));

            } catch (Exception e) {
                Omniscience.getPluginInstance().getLogger().warning("[FAWE] Exception processing entry: " + e.getMessage());
                results.add(ActionResult.skipped(SkipReason.INVALID));
            }
        }

        // Apply changes per world using FAWE
        for (Map.Entry<World, List<BlockChange>> worldEntry : changesByWorld.entrySet()) {
            World world = worldEntry.getKey();
            List<BlockChange> changes = worldEntry.getValue();

            Omniscience.getPluginInstance().getLogger().info("[FAWE] Applying " + changes.size() + " block changes in world " + world.getName());

            try {
                int applied = applyChangesWithFAWE(world, changes, sender);
                Omniscience.getPluginInstance().getLogger().info("[FAWE] Successfully applied " + applied + " block changes");
                // Add success results for each applied change
                for (int i = 0; i < applied; i++) {
                    results.add(ActionResult.success(null));
                }
            } catch (Exception e) {
                Omniscience.getPluginInstance().getLogger().warning("[FAWE] Batch operation failed: " + e.getMessage());
                e.printStackTrace();
                // Fall back - mark all as skipped
                for (int i = 0; i < changes.size(); i++) {
                    results.add(ActionResult.skipped(SkipReason.INVALID));
                }
            }
        }

        return results;
    }

    private static int applyChangesWithFAWE(World bukkitWorld, List<BlockChange> changes, CommandSender sender) throws Exception {
        com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(bukkitWorld);

        // Create edit session using try-with-resources to ensure proper cleanup
        try (EditSession editSession = WorldEdit.getInstance().newEditSession(weWorld)) {
            int count = 0;
            for (BlockChange change : changes) {
                BlockVector3 pos = BlockVector3.at(
                    change.location.getBlockX(),
                    change.location.getBlockY(),
                    change.location.getBlockZ()
                );

                // Convert Bukkit BlockData to WorldEdit BlockState
                BlockState weBlockState = BukkitAdapter.adapt(change.blockData);

                // Use setBlock which returns true if the block was changed
                if (editSession.setBlock(pos, weBlockState)) {
                    count++;
                }
            }

            // Commit changes - close() will be called automatically by try-with-resources
            // which should flush all pending changes
            Omniscience.getPluginInstance().getLogger().info("[FAWE] Committed " + count + " block changes, closing session...");
            return count;
        }
        // EditSession.close() is called here automatically which should flush changes
    }

    public static boolean isBlockEntry(DataEntry entry) {
        // Check if this entry has block data (ORIGINAL_BLOCK or NEW_BLOCK)
        return entry.data.getWrapper(ORIGINAL_BLOCK).isPresent() ||
               entry.data.getWrapper(NEW_BLOCK).isPresent();
    }

    private static Optional<BlockData> getOriginalBlockData(DataEntry entry) {
        Optional<DataWrapper> original = entry.data.getWrapper(ORIGINAL_BLOCK);
        if (!original.isPresent()) {
            return Optional.empty();
        }
        return DataHelper.getBlockDataFromWrapper(original.get());
    }

    private static Optional<BlockData> getNewBlockData(DataEntry entry) {
        Optional<DataWrapper> newBlock = entry.data.getWrapper(NEW_BLOCK);
        if (!newBlock.isPresent()) {
            // If no new block, it means the block was removed (air)
            return Optional.of(org.bukkit.Material.AIR.createBlockData());
        }
        return DataHelper.getBlockDataFromWrapper(newBlock.get());
    }

    private static class BlockChange {
        final Location location;
        final BlockData blockData;

        BlockChange(Location location, BlockData blockData) {
            this.location = location;
            this.blockData = blockData;
        }
    }
}
