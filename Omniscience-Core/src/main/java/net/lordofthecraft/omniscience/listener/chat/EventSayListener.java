package net.lordofthecraft.omniscience.listener.chat;

import com.google.common.collect.ImmutableList;
import net.lordofthecraft.omniscience.api.entry.OEntry;
import net.lordofthecraft.omniscience.listener.OmniListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class EventSayListener extends OmniListener {

    public EventSayListener() {
        super(ImmutableList.of("say"));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        OEntry.create().source(event.getPlayer()).said(event.getMessage()).save();
    }
}
