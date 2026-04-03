package net.lordofthecraft.omniscience.api.entry;

import net.lordofthecraft.omniscience.api.OmniApi;
import net.lordofthecraft.omniscience.api.data.DataKeys;
import net.lordofthecraft.omniscience.api.data.DataWrapper;

import java.util.concurrent.LinkedBlockingDeque;

public final class EntryQueue {

    private static final LinkedBlockingDeque<DataWrapper> queue = new LinkedBlockingDeque<>();

    private EntryQueue() {
    }

    public static void submit(final DataWrapper wrapper) {
        if (wrapper == null) {
            throw new IllegalArgumentException("A null wrapper was handed to save for the saving queue");
        }

        String eventName = wrapper.getString(DataKeys.EVENT_NAME)
                .orElseThrow(() -> new IllegalArgumentException("Event Name was not specified in passed in wrapper!"));

        if (!OmniApi.isEventRegistered(eventName)) {
            throw new IllegalArgumentException("The event " + eventName + " is not registered with Omniscience. This event cannot be saved!");
        }

        queue.add(wrapper);
    }

    public static LinkedBlockingDeque<DataWrapper> getQueue() {
        return queue;
    }
}
