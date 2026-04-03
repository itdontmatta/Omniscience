package net.medievalrp.omniscience.api.display;

import com.google.common.collect.Lists;
import net.medievalrp.omniscience.api.data.DataKeys;
import net.medievalrp.omniscience.api.entry.DataEntry;
import net.medievalrp.omniscience.api.query.QuerySession;
import org.bukkit.ChatColor;

import java.util.List;
import java.util.Optional;

public class DamageDisplayHandler extends SimpleDisplayHandler {

    public DamageDisplayHandler() {
        super("damage");
    }

    @Override
    public Optional<String> buildTargetMessage(DataEntry entry, String target, QuerySession session) {
        return Optional.empty();
    }

    @Override
    public Optional<List<String>> buildAdditionalHoverData(DataEntry entry, QuerySession session) {
        List<String> hoverData = Lists.newArrayList();
        entry.data.getString(DataKeys.DAMAGE_CAUSE).ifPresent(data -> hoverData.add(ChatColor.DARK_GRAY + "Damage Cause: " + ChatColor.RESET + data));
        entry.data.get(DataKeys.DAMAGE_AMOUNT).ifPresent(data -> hoverData.add(ChatColor.DARK_GRAY + "Damage Amount: " + ChatColor.RESET + data));
        return Optional.of(hoverData);
    }
}
