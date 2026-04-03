package net.lordofthecraft.omniscience.api.entry;

import net.lordofthecraft.omniscience.api.data.DataKey;
import net.lordofthecraft.omniscience.api.data.DataWrapper;
import net.lordofthecraft.omniscience.api.data.Transaction;
import net.lordofthecraft.omniscience.api.util.DataHelper;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

import static net.lordofthecraft.omniscience.api.data.DataKeys.*;

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

            data.getWrapper(parent.then(INVENTORY)).ifPresent(wrapper -> wrapper.getKeys(false)
                    .forEach(key -> wrapper.getConfigSerializable(key)
                            .ifPresent(config -> {
                                if (config instanceof ItemStack) {
                                    container.getInventory().setItem(Integer.valueOf(key.toString()), (ItemStack) config);
                                }
                            })));
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
