package net.lordofthecraft.omniscience.listener.player;

import com.google.common.collect.ImmutableList;
import net.lordofthecraft.omniscience.api.entry.OEntry;
import net.lordofthecraft.omniscience.listener.OmniListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;

public class EventJoinListener extends OmniListener {

    public EventJoinListener() {
        super(ImmutableList.of("join"));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        String host = player.getAddress() != null ? player.getAddress().getHostString() : "unknown";
        OEntry.create().player(player).joined(host).save();
    }
}
