package com.itdontmatta.omniscience.api.display;

import com.google.common.collect.Lists;
import com.itdontmatta.omniscience.api.data.DataKeys;
import com.itdontmatta.omniscience.api.entry.DataEntry;
import com.itdontmatta.omniscience.api.query.QuerySession;
import org.bukkit.ChatColor;

import java.util.List;
import java.util.Optional;

public class TeleportDisplayHandler extends SimpleDisplayHandler {

	public TeleportDisplayHandler() {
		super("teleport");
	}

	@Override
	public Optional<String> buildTargetMessage(DataEntry entry, String target, QuerySession session) {
		return Optional.empty();
	}

	@Override
	public Optional<List<String>> buildAdditionalHoverData(DataEntry entry, QuerySession session) {
		List<String> hoverData = Lists.newArrayList();
		entry.data.getString(DataKeys.TELEPORT_CAUSE).ifPresent(data -> hoverData.add(ChatColor.DARK_GRAY + "Teleport Cause: " + ChatColor.RESET + data));
		return Optional.of(hoverData);
	}
}
