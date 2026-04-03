package net.medievalrp.omniscience;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.medievalrp.omniscience.api.util.PastTenseWithEnabled;
import net.medievalrp.omniscience.listener.OmniListener;
import net.medievalrp.omniscience.listener.block.*;
import net.medievalrp.omniscience.listener.chat.EventCommandListener;
import net.medievalrp.omniscience.listener.chat.EventSayListener;
import net.medievalrp.omniscience.listener.entity.EventDeathListener;
import net.medievalrp.omniscience.listener.entity.EventHitListener;
import net.medievalrp.omniscience.listener.entity.EventInteractAtEntity;
import net.medievalrp.omniscience.listener.entity.EventMountListener;
import net.medievalrp.omniscience.listener.item.*;
import net.medievalrp.omniscience.listener.player.EventJoinListener;
import net.medievalrp.omniscience.listener.player.EventQuitListener;
import net.medievalrp.omniscience.listener.player.EventTeleportListener;
import org.bukkit.plugin.PluginManager;

import java.util.*;

public enum OmniEventRegistrar {
    INSTANCE;

    private final Map<String, PastTenseWithEnabled> eventMapping = Maps.newHashMap();
    private List<OmniListener> listeners = Lists.newArrayList();

    OmniEventRegistrar() {
        //Block
        listeners.add(new EventBreakListener());
        listeners.add(new EventDecayListener());
        listeners.add(new EventFormListener());
        listeners.add(new EventIgniteListener());
        listeners.add(new EventPlaceListener());
        listeners.add(new EventUseListener());
        listeners.add(new EventGrowListener());

        //Block - 1.19+ Events
        listeners.add(new EventSculkListener());

        //Block - 1.20+ Events
        listeners.add(new EventBookshelfListener());
        listeners.add(new EventBrushListener());
        listeners.add(new EventDecoratedPotListener());

        //Block - 1.21+ Events
        listeners.add(new EventCrafterListener());
        listeners.add(new EventVaultListener());

        //Chat
        listeners.add(new EventCommandListener());
        listeners.add(new EventSayListener());

        //Entity
        listeners.add(new EventDeathListener());
        listeners.add(new EventHitListener());
        listeners.add(new EventInteractAtEntity());
        listeners.add(new EventMountListener());

        //Item
        listeners.add(new EventContainerListener());
        listeners.add(new EventDropListener());
        listeners.add(new EventInventoryListener());
        listeners.add(new EventPickupListener());
        listeners.add(new EventEntityItemListener());

        //Item - 1.21+ (Bundles and enhanced Shulker tracking)
        listeners.add(new EventBundleListener());
        listeners.add(new EventShulkerListener());

        //Player
        listeners.add(new EventJoinListener());
        listeners.add(new EventQuitListener());
        listeners.add(new EventTeleportListener());
    }

    public Set<String> getEventNames() {
        return eventMapping.keySet();
    }

    public boolean isEventRegistered(String event) {
        return eventMapping.containsKey(event);
    }

    public boolean isEventEnabled(String event) {
        if (!eventMapping.containsKey(event)) {
            return false;
        }
        return eventMapping.get(event).isEnabled();
    }

    public String getPastTense(String event) {
        if (!eventMapping.containsKey(event)) {
            return event;
        }
        return eventMapping.get(event).getPastTense();
    }

    public Map<String, PastTenseWithEnabled> getEventMapping() {
        try {
            return ImmutableMap.copyOf(eventMapping);
        } catch (Exception e) {
            return new Map<String, PastTenseWithEnabled>() {
                @Override
                public int size() {
                    return 0;
                }

                @Override
                public boolean isEmpty() {
                    return false;
                }

                @Override
                public boolean containsKey(Object key) {
                    return false;
                }

                @Override
                public boolean containsValue(Object value) {
                    return false;
                }

                @Override
                public PastTenseWithEnabled get(Object key) {
                    return null;
                }

                @Override
                public PastTenseWithEnabled put(String key, PastTenseWithEnabled value) {
                    return null;
                }

                @Override
                public PastTenseWithEnabled remove(Object key) {
                    return null;
                }

                @Override
                public void putAll(Map<? extends String, ? extends PastTenseWithEnabled> m) {

                }

                @Override
                public void clear() {

                }

                @Override
                public Set<String> keySet() {
                    return null;
                }

                @Override
                public Collection<PastTenseWithEnabled> values() {
                    return null;
                }

                @Override
                public Set<Entry<String, PastTenseWithEnabled>> entrySet() {
                    return null;
                }
            };
        }
    }

    void addEvent(String name, String pastTense, boolean enabled) {
        eventMapping.put(name, new PastTenseWithEnabled(enabled, pastTense));
    }

    void enableEvents(PluginManager manager, Omniscience omniscience) {
        eventMapping.forEach((key, value) -> {
            Optional<OmniListener> listener = listeners.stream().filter(l -> l.handles(key)).findFirst();
            if (listener.isPresent() && value.isEnabled()) {
                OmniListener list = listener.get();
                if (!list.isEnabled()) {
                    manager.registerEvents(list, omniscience);
                    list.setEnabled(true);
                }
            }
        });
    }
}
