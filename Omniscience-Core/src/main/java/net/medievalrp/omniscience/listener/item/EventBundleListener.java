package net.medievalrp.omniscience.listener.item;

import com.google.common.collect.ImmutableList;
import net.medievalrp.omniscience.api.data.DataWrapper;
import net.medievalrp.omniscience.api.entry.OEntry;
import net.medievalrp.omniscience.listener.OmniListener;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;

import static net.medievalrp.omniscience.api.data.DataKeys.*;

/**
 * Listener for Bundle interactions (1.21+)
 * Tracks when players insert items into or extract items from bundles.
 *
 * Bundle mechanics:
 * - Right-click an item onto a bundle -> inserts item into bundle
 * - Right-click a bundle with empty cursor -> extracts top item from bundle
 */
public class EventBundleListener extends OmniListener {

    public EventBundleListener() {
        super(ImmutableList.of("bundle-insert", "bundle-extract"));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBundleInteract(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) {
            return;
        }

        // Bundles use right-click interactions
        if (e.getClick() != ClickType.RIGHT) {
            return;
        }

        ItemStack clickedItem = e.getCurrentItem();
        ItemStack cursorItem = e.getCursor();

        // Case 1: Inserting item into bundle (cursor has item, clicking on bundle)
        if (clickedItem != null && clickedItem.getType() == Material.BUNDLE &&
            cursorItem != null && cursorItem.getType() != Material.AIR) {

            if (!isEnabled("bundle-insert")) {
                return;
            }

            // Check if bundle has room (bundles have limited capacity)
            if (clickedItem.getItemMeta() instanceof BundleMeta bundleMeta) {
                // Log the deposit into bundle
                DataWrapper wrapper = DataWrapper.createNew();
                // Format TARGET like existing deposit/withdraw: "ITEM_TYPE in BUNDLE"
                wrapper.set(TARGET, cursorItem.getType().name() + " in BUNDLE");
                wrapper.set(ITEMSTACK, cursorItem.clone());
                wrapper.set(QUANTITY, cursorItem.getAmount());
                wrapper.set(DISPLAY_METHOD, "item");

                OEntry.create().source(player).custom("bundle-insert", wrapper).save();
            }
        }
        // Case 2: Extracting item from bundle (empty cursor, clicking on bundle with items)
        else if (clickedItem != null && clickedItem.getType() == Material.BUNDLE &&
                 (cursorItem == null || cursorItem.getType() == Material.AIR)) {

            if (!isEnabled("bundle-extract")) {
                return;
            }

            if (clickedItem.getItemMeta() instanceof BundleMeta bundleMeta) {
                if (!bundleMeta.hasItems()) {
                    return; // Empty bundle, nothing to extract
                }

                // Get the item that will be extracted (last item in bundle)
                var items = bundleMeta.getItems();
                if (!items.isEmpty()) {
                    ItemStack extractedItem = items.get(items.size() - 1);

                    DataWrapper wrapper = DataWrapper.createNew();
                    // Format TARGET like existing deposit/withdraw: "ITEM_TYPE from BUNDLE"
                    wrapper.set(TARGET, extractedItem.getType().name() + " from BUNDLE");
                    wrapper.set(ITEMSTACK, extractedItem.clone());
                    wrapper.set(QUANTITY, extractedItem.getAmount());
                    wrapper.set(DISPLAY_METHOD, "item");

                    OEntry.create().source(player).custom("bundle-extract", wrapper).save();
                }
            }
        }
    }
}
