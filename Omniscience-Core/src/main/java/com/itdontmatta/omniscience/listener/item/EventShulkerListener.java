package com.itdontmatta.omniscience.listener.item;

import com.google.common.collect.ImmutableList;
import com.itdontmatta.omniscience.api.data.DataWrapper;
import com.itdontmatta.omniscience.api.data.InventoryTransaction;
import com.itdontmatta.omniscience.api.entry.OEntry;
import com.itdontmatta.omniscience.api.util.InventoryUtil;
import com.itdontmatta.omniscience.listener.OmniListener;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static com.itdontmatta.omniscience.api.data.DataKeys.*;

/**
 * Enhanced Shulker Box listener
 * Tracks:
 * - Opening/closing shulker boxes (both placed and in-inventory via plugins)
 * - Depositing items into shulker boxes
 * - Withdrawing items from shulker boxes
 * - Shulker box color for better identification
 */
public class EventShulkerListener extends OmniListener {

    public EventShulkerListener() {
        super(ImmutableList.of("shulker-open", "shulker-close", "shulker-deposit", "shulker-withdraw"));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onShulkerOpen(InventoryOpenEvent e) {
        if (!isEnabled("shulker-open")) {
            return;
        }

        if (!(e.getPlayer() instanceof Player player)) {
            return;
        }

        Inventory inventory = e.getInventory();

        // Check if this is a shulker box inventory
        if (inventory.getType() != InventoryType.SHULKER_BOX) {
            return;
        }

        InventoryHolder holder = inventory.getHolder();
        Location location = null;
        String shulkerColor = "SHULKER_BOX";

        // Placed shulker box
        if (holder instanceof ShulkerBox shulkerBox) {
            location = shulkerBox.getLocation();
            shulkerColor = shulkerBox.getType().name();
        }

        // Count items in the shulker
        int stackCount = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                stackCount++;
            }
        }

        DataWrapper wrapper = DataWrapper.createNew();
        wrapper.set(TARGET, shulkerColor);
        wrapper.set(QUANTITY, stackCount);

        if (location != null) {
            OEntry.create().source(player).customWithLocation("shulker-open", wrapper, location).save();
        } else {
            // In-inventory shulker (opened via plugin)
            OEntry.create().source(player).custom("shulker-open", wrapper).save();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onShulkerClose(InventoryCloseEvent e) {
        if (!isEnabled("shulker-close")) {
            return;
        }

        if (!(e.getPlayer() instanceof Player player)) {
            return;
        }

        Inventory inventory = e.getInventory();

        // Check if this is a shulker box inventory
        if (inventory.getType() != InventoryType.SHULKER_BOX) {
            return;
        }

        InventoryHolder holder = inventory.getHolder();
        Location location = null;
        String shulkerColor = "SHULKER_BOX";

        // Placed shulker box
        if (holder instanceof ShulkerBox shulkerBox) {
            location = shulkerBox.getLocation();
            shulkerColor = shulkerBox.getType().name();
        }

        // Count items in the shulker after closing
        int stackCount = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                stackCount++;
            }
        }

        DataWrapper wrapper = DataWrapper.createNew();
        wrapper.set(TARGET, shulkerColor);
        wrapper.set(QUANTITY, stackCount);

        if (location != null) {
            OEntry.create().source(player).customWithLocation("shulker-close", wrapper, location).save();
        } else {
            // In-inventory shulker (opened via plugin)
            OEntry.create().source(player).custom("shulker-close", wrapper).save();
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onShulkerItemClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) {
            return;
        }

        Inventory inventory = e.getInventory();

        // Check if this is a shulker box inventory
        if (inventory.getType() != InventoryType.SHULKER_BOX) {
            return;
        }

        InventoryHolder holder = inventory.getHolder();
        Location location = null;
        String shulkerColor = "SHULKER_BOX";

        // Placed shulker box
        if (holder instanceof ShulkerBox shulkerBox) {
            location = shulkerBox.getLocation();
            shulkerColor = shulkerBox.getType().name();
        }

        // Use InventoryUtil to identify what transactions occurred
        List<InventoryTransaction<ItemStack>> transactions = InventoryUtil.identifyTransactions(e);

        for (InventoryTransaction<ItemStack> transaction : transactions) {
            switch (transaction.getType()) {
                case WITHDRAW:
                    if (isEnabled("shulker-withdraw")) {
                        logShulkerTransaction(player, transaction.getDiff(), shulkerColor, location, "shulker-withdraw");
                    }
                    break;
                case DEPOSIT:
                    if (isEnabled("shulker-deposit")) {
                        logShulkerTransaction(player, transaction.getDiff(), shulkerColor, location, "shulker-deposit");
                    }
                    break;
                case CLONE:
                    // Ignore clone actions
                    break;
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onShulkerItemDrag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) {
            return;
        }

        Inventory inventory = e.getInventory();

        // Check if this is a shulker box inventory
        if (inventory.getType() != InventoryType.SHULKER_BOX) {
            return;
        }

        if (!isEnabled("shulker-deposit")) {
            return;
        }

        InventoryHolder holder = inventory.getHolder();
        Location location = null;
        String shulkerColor = "SHULKER_BOX";

        // Placed shulker box
        if (holder instanceof ShulkerBox shulkerBox) {
            location = shulkerBox.getLocation();
            shulkerColor = shulkerBox.getType().name();
        }

        // Use InventoryUtil to identify what transactions occurred
        List<InventoryTransaction<ItemStack>> transactions = InventoryUtil.identifyTransactions(e);

        for (InventoryTransaction<ItemStack> transaction : transactions) {
            if (transaction.getType() == InventoryTransaction.ActionType.DEPOSIT) {
                logShulkerTransaction(player, transaction.getDiff(), shulkerColor, location, "shulker-deposit");
            }
        }
    }

    private void logShulkerTransaction(Player player, ItemStack item, String shulkerColor, Location location, String eventName) {
        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        DataWrapper wrapper = DataWrapper.createNew();
        // Format TARGET like existing deposit/withdraw: "ITEM_TYPE in/from CONTAINER_TYPE"
        boolean isWithdraw = eventName.contains("withdraw");
        String targetFormat = item.getType().name() + (isWithdraw ? " from " : " in ") + shulkerColor;
        wrapper.set(TARGET, targetFormat);
        wrapper.set(ITEMSTACK, item.clone());
        wrapper.set(QUANTITY, item.getAmount());
        wrapper.set(DISPLAY_METHOD, "item");

        if (location != null) {
            OEntry.create().source(player).customWithLocation(eventName, wrapper, location).save();
        } else {
            // In-inventory shulker (opened via plugin)
            OEntry.create().source(player).custom(eventName, wrapper).save();
        }
    }
}
