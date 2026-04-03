package net.lordofthecraft.omniscience.listener.block;

import com.google.common.collect.ImmutableList;
import net.lordofthecraft.omniscience.api.data.DataWrapper;
import net.lordofthecraft.omniscience.api.entry.OEntry;
import net.lordofthecraft.omniscience.listener.OmniListener;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import static net.lordofthecraft.omniscience.api.data.DataKeys.*;

/**
 * Listener for Chiseled Bookshelf interactions (1.20+)
 * Tracks when players insert or remove books from chiseled bookshelves
 */
public class EventBookshelfListener extends OmniListener {

    public EventBookshelfListener() {
        super(ImmutableList.of("bookshelf-insert", "bookshelf-remove"));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBookshelfInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Block block = e.getClickedBlock();
        if (block == null || block.getType() != Material.CHISELED_BOOKSHELF) {
            return;
        }

        Player player = e.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        // Check if player is holding a book (inserting) or not (removing)
        if (isBook(itemInHand.getType())) {
            if (isEnabled("bookshelf-insert")) {
                DataWrapper wrapper = DataWrapper.createNew();
                wrapper.set(TARGET, itemInHand.getType().name());
                wrapper.set(ITEMSTACK, itemInHand);
                wrapper.set(DISPLAY_METHOD, "item");
                OEntry.create().source(player).customWithLocation("bookshelf-insert", wrapper, block.getLocation()).save();
            }
        } else {
            if (isEnabled("bookshelf-remove")) {
                DataWrapper wrapper = DataWrapper.createNew();
                wrapper.set(TARGET, "BOOK from " + block.getType().name());
                wrapper.set(DISPLAY_METHOD, "item");
                OEntry.create().source(player).customWithLocation("bookshelf-remove", wrapper, block.getLocation()).save();
            }
        }
    }

    private boolean isBook(Material material) {
        return material == Material.BOOK
                || material == Material.WRITABLE_BOOK
                || material == Material.WRITTEN_BOOK
                || material == Material.ENCHANTED_BOOK;
    }
}
