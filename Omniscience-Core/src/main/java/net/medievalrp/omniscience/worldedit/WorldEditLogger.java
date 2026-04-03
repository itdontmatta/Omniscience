package net.medievalrp.omniscience.worldedit;

import net.medievalrp.omniscience.Omniscience;
import net.medievalrp.omniscience.api.data.LocationTransaction;
import net.medievalrp.omniscience.api.entry.OEntry;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;

/**
 * Logs WorldEdit/FAWE block changes to Omniscience
 */
public class WorldEditLogger {

    private static boolean registered = false;

    public static void register() {
        if (registered) {
            return;
        }

        try {
            WorldEdit.getInstance().getEventBus().register(new WorldEditLogger());
            registered = true;
            Omniscience.getPluginInstance().getLogger().info("WorldEdit/FAWE logging enabled");
        } catch (Exception e) {
            Omniscience.getPluginInstance().getLogger().warning("Failed to register WorldEdit logger: " + e.getMessage());
        }
    }

    public static void unregister() {
        if (!registered) {
            return;
        }

        try {
            // WorldEdit doesn't have an easy unregister, but it's fine for reload
            registered = false;
        } catch (Exception e) {
            // Ignore
        }
    }

    @Subscribe
    public void onEditSession(EditSessionEvent event) {
        // Only log on BEFORE stage
        if (event.getStage() != EditSession.Stage.BEFORE_CHANGE) {
            return;
        }

        Actor actor = event.getActor();
        if (actor == null || !actor.isPlayer()) {
            return;
        }

        // Get the player
        Player player = Bukkit.getPlayer(actor.getUniqueId());
        if (player == null) {
            return;
        }

        // Wrap the extent with our logging extent
        event.setExtent(new LoggingExtent(event.getExtent(), event.getWorld(), player));
    }

    /**
     * Extent wrapper that logs block changes to Omniscience
     */
    private static class LoggingExtent extends AbstractDelegateExtent {
        private final com.sk89q.worldedit.world.World weWorld;
        private final Player player;

        public LoggingExtent(Extent extent, com.sk89q.worldedit.world.World world, Player player) {
            super(extent);
            this.weWorld = world;
            this.player = player;
        }

        @Override
        public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 position, T block) throws com.sk89q.worldedit.WorldEditException {
            // Get the Bukkit world
            World bukkitWorld = BukkitAdapter.adapt(weWorld);
            if (bukkitWorld == null) {
                return super.setBlock(position, block);
            }

            Location location = new Location(bukkitWorld, position.getX(), position.getY(), position.getZ());

            // Get original block state before the change
            BlockState originalState = location.getBlock().getState();

            // Apply the change first
            boolean result = super.setBlock(position, block);

            if (result) {
                // Get the new block state after the change
                BlockState newState = location.getBlock().getState();

                // Log the change
                try {
                    LocationTransaction<BlockState> transaction = new LocationTransaction<>(location, originalState, newState);
                    OEntry.create()
                        .source(player)
                        .brokeBlock(transaction)
                        .save();
                } catch (Exception e) {
                    // Don't let logging failures affect the edit
                    Omniscience.getPluginInstance().getLogger().fine("Failed to log WorldEdit change: " + e.getMessage());
                }
            }

            return result;
        }
    }
}
