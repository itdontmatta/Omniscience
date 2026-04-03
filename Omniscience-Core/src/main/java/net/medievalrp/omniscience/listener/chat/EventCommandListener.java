package net.medievalrp.omniscience.listener.chat;

import com.google.common.collect.ImmutableList;
import net.medievalrp.omniscience.api.entry.OEntry;
import net.medievalrp.omniscience.listener.OmniListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

public class EventCommandListener extends OmniListener {

    public EventCommandListener() {
        super(ImmutableList.of("command"));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onServerCommand(ServerCommandEvent event) {
        OEntry.create().source(event.getSender()).ranCommand(event.getCommand()).save();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        OEntry.create().source(event.getPlayer()).ranCommand(event.getMessage()).save();
    }
}
