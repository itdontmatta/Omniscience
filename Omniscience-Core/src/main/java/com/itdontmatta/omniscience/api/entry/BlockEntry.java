package com.itdontmatta.omniscience.api.entry;

import com.itdontmatta.omniscience.api.OmniApi;
import com.itdontmatta.omniscience.api.data.DataKey;
import com.itdontmatta.omniscience.api.data.DataWrapper;
import com.itdontmatta.omniscience.api.data.Transaction;
import com.itdontmatta.omniscience.api.util.DataHelper;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.Set;

import static com.itdontmatta.omniscience.api.data.DataKeys.*;

public class BlockEntry extends DataEntryComplete implements Actionable {

    public BlockEntry() {
    }

    @Override
    public ActionResult rollback() throws Exception {
        DataWrapper original = data.getWrapper(ORIGINAL_BLOCK)
                .orElseThrow(() -> skipped(SkipReason.INVALID));

        BlockData originalData = DataHelper.getBlockDataFromWrapper(original)
                .orElseThrow(() -> skipped(SkipReason.INVALID));
        Location location = DataHelper.getLocationFromDataWrapper(data)
                .orElseThrow(() -> skipped(SkipReason.INVALID_LOCATION));

        BlockState beforeState = location.getBlock().getState();

        location.getBlock().setType(originalData.getMaterial());

        BlockState editState = location.getBlock().getState();
        editState.setBlockData(originalData);

        handleTileEntity(editState, ORIGINAL_BLOCK);

        editState.update(false, false);

        return ActionResult.success(new Transaction<>(beforeState, location.getBlock().getState()));
    }

    @Override
    public ActionResult restore() throws Exception {
        Location location = DataHelper.getLocationFromDataWrapper(data)
                .orElseThrow(() -> skipped(SkipReason.INVALID_LOCATION));
        Optional<DataWrapper> oFinalState = data.getWrapper(NEW_BLOCK);
        BlockState beforeState = location.getBlock().getState();
        BlockState editState = location.getBlock().getState();
        if (!oFinalState.isPresent()) {
            location.getBlock().setBlockData(Material.AIR.createBlockData());
            return ActionResult.success(new Transaction<>(beforeState, location.getBlock().getState()));
        }
        DataWrapper finalState = oFinalState.get();

        BlockData finalData = DataHelper.getBlockDataFromWrapper(finalState)
                .orElseThrow(() -> skipped(SkipReason.INVALID));

        editState.setBlockData(finalData);

        handleTileEntity(editState, NEW_BLOCK);

        editState.update(true, false);

        return ActionResult.success(new Transaction<>(beforeState, location.getBlock().getState()));
    }

    private void handleTileEntity(BlockState state, DataKey parent) {
        if (state instanceof Container) {
            Container container = (Container) state;

            // Use getSnapshotInventory() for BlockState snapshots - this ensures
            // inventory changes are persisted when update() is called.
            // getBlockInventory()/getInventory() return the live inventory which
            // bypasses the snapshot mechanism.
            Inventory inventory = container.getSnapshotInventory();

            int inventorySize = inventory.getSize();

            data.getWrapper(parent.then(INVENTORY)).ifPresent(wrapper -> {
                Set<DataKey> keys = wrapper.getKeys(false);
                int restoredCount = 0;
                int failedCount = 0;

                for (DataKey key : keys) {
                    int slot;
                    try {
                        slot = Integer.parseInt(key.toString());
                    } catch (NumberFormatException e) {
                        OmniApi.warning("[Rollback] Invalid slot key: " + key);
                        continue;
                    }

                    if (slot >= inventorySize) {
                        OmniApi.warning("[Rollback] Slot " + slot + " exceeds inventory size " + inventorySize + " - skipping");
                        failedCount++;
                        continue;
                    }

                    Optional<ItemStack> itemOpt = wrapper.getConfigSerializable(key);
                    if (itemOpt.isPresent()) {
                        inventory.setItem(slot, itemOpt.get());
                        restoredCount++;
                    } else {
                        // Item failed to deserialize - already logged in DataHelper
                        failedCount++;
                    }
                }

                if (failedCount > 0) {
                    OmniApi.warning("[Rollback] Container rollback: " + restoredCount + " items restored, " + failedCount + " failed");
                }
            });
        } else if (state instanceof Sign) {
            Sign sign = (Sign) state;
            data.getStringList(parent.then(SIGN_TEXT)).ifPresent(signText -> {
                for (int i = 0; i < 4; i++) {
                    if (signText.size() >= i + 1) {
                        sign.setLine(i, signText.get(i));
                    }
                }
            });
        } else if (state instanceof Banner) {
            Banner banner = (Banner) state;
            data.getSerializableList(parent.then(BANNER_PATTERNS), Pattern.class).ifPresent(banner::setPatterns);
        } else if (state instanceof Jukebox) {
            Jukebox jukebox = (Jukebox) state;
            data.getConfigSerializable(parent.then(RECORD)).ifPresent(config ->  {
                if (config instanceof ItemStack) {
                    jukebox.setRecord((ItemStack) config);
                }
            });
        }
    }
}
