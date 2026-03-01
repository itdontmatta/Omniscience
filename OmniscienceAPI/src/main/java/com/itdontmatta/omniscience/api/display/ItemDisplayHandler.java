package com.itdontmatta.omniscience.api.display;

import com.itdontmatta.omniscience.api.OmniApi;
import com.itdontmatta.omniscience.api.data.DataKeys;
import com.itdontmatta.omniscience.api.entry.DataEntry;
import com.itdontmatta.omniscience.api.query.QuerySession;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.ItemTag;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Item;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

public class ItemDisplayHandler extends SimpleDisplayHandler {

    public ItemDisplayHandler() {
        super("item");
    }

    @Override
    public Optional<String> buildTargetMessage(DataEntry entry, String target, QuerySession session) {
        Optional<String> entity = entry.data.getString(DataKeys.ENTITY_TYPE);
        Optional<String> event = entry.data.getString(DataKeys.EVENT_NAME);
        boolean withdraw = event.isPresent() && event.get().contains("withdraw");
        return entity.map(s -> target + (withdraw ? " from " : " into ") + s);
    }

    @Override
    public Optional<List<String>> buildAdditionalHoverData(DataEntry entry, QuerySession session) {
        List<String> hoverLines = new java.util.ArrayList<>();

        // Add custom item name
        entry.data.getString(DataKeys.NAME).ifPresent(name ->
            hoverLines.add(net.md_5.bungee.api.ChatColor.DARK_GRAY + "Name: " + net.md_5.bungee.api.ChatColor.WHITE + name));

        // Add enchantments
        entry.data.getString(DataKeys.ITEM_ENCHANTS).ifPresent(enchants ->
            hoverLines.add(net.md_5.bungee.api.ChatColor.DARK_GRAY + "Enchants: " + net.md_5.bungee.api.ChatColor.LIGHT_PURPLE + enchants));

        // Add lore (each line separately)
        entry.data.getString(DataKeys.ITEM_LORE).ifPresent(lore -> {
            hoverLines.add(net.md_5.bungee.api.ChatColor.DARK_GRAY + "Lore:");
            for (String line : lore.split("\n")) {
                hoverLines.add(net.md_5.bungee.api.ChatColor.DARK_PURPLE + "  " + line);
            }
        });

        return hoverLines.isEmpty() ? Optional.empty() : Optional.of(hoverLines);
    }

    @Override
    public Optional<TextComponent> buildTargetSpecificHoverData(DataEntry entry, String target, QuerySession session) {
        // Use stored NAME field if present, otherwise use target
        String displayTarget = entry.data.getString(DataKeys.NAME).orElse(target);

        // Just return the custom name as a TextComponent - don't try to deserialize ItemStack
        // (ItemStack deserialization is broken in 1.20.5+ due to component format changes)
        if (!displayTarget.equals(target)) {
            return Optional.of(new TextComponent(displayTarget));
        }
        return Optional.empty();
    }
}
