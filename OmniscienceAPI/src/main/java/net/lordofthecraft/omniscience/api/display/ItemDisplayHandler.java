package net.lordofthecraft.omniscience.api.display;

import net.lordofthecraft.omniscience.api.data.DataKeys;
import net.lordofthecraft.omniscience.api.entry.DataEntry;
import net.lordofthecraft.omniscience.api.query.QuerySession;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
        return Optional.empty();
    }

    @Override
    public Optional<TextComponent> buildTargetSpecificHoverData(DataEntry entry, String target, QuerySession session) {
        try {
            Optional<ItemStack> oItemStack = entry.data.getConfigSerializable(DataKeys.ITEMSTACK);
            if (oItemStack.isPresent()) {
                ItemStack is = oItemStack.get();
                TextComponent component = new TextComponent(target);
                try {
                    String itemId = is.getType().getKey().toString();
                    int count = is.getAmount();
                    Item itemContent = new Item(itemId, count, null);
                    component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, itemContent));
                } catch (Exception e) {
                    // Fallback: just show the item type as text hover
                    TextComponent hoverText = new TextComponent(is.getType().name() + " x" + is.getAmount());
                    component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{hoverText}));
                }
                return Optional.of(component);
            }
        } catch (Exception e) {
            // Legacy item data from pre-1.20.5 can't be deserialized - just skip hover
        }
        return Optional.empty();
    }
}
