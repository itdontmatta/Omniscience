package com.itdontmatta.omniscience.listener.player;

import com.google.common.collect.ImmutableList;
import com.itdontmatta.omniscience.api.entry.OEntry;
import com.itdontmatta.omniscience.listener.OmniListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerTeleportEvent;

public class EventTeleportListener extends OmniListener {

	public EventTeleportListener() {
		super(ImmutableList.of("teleport"));
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void teleport(PlayerTeleportEvent event) {
		if (event.getCause() != PlayerTeleportEvent.TeleportCause.UNKNOWN && isEnabled("teleport")) { // Thrown out unknowns
			OEntry.create().player(event.getPlayer()).teleported(event.getFrom(), event.getTo(), event.getCause()).save();
		}
	}
}
