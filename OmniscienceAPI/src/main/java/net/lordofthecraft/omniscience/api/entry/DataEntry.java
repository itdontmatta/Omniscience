package net.lordofthecraft.omniscience.api.entry;

import net.lordofthecraft.omniscience.api.OmniApi;
import net.lordofthecraft.omniscience.api.data.DataWrapper;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

import static net.lordofthecraft.omniscience.api.data.DataKeys.*;

public abstract class DataEntry {

    public DataWrapper data;

    public static DataEntry from(String eventName, boolean isAggregate) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        final DataEntry entry;
        if (isAggregate) {
            entry = new DataAggregateEntry();
        } else {
            entry = OmniApi.getEventClass(eventName)
                    .orElse(DataEntryComplete.class).getConstructor().newInstance();
        }

        return entry;
    }

    public String getVerbPastTense() {
        return translateToPastTense(getEventName());
    }

    public String getEventName() {
        return data.getString(EVENT_NAME).orElse("unknown");
    }

    public String getSourceName() {
        return data.getString(CAUSE).orElse("unknown");
    }

    public String getTargetName() {
        return data.getString(TARGET).orElse("");
    }

    private String translateToPastTense(String word) {
        return OmniApi.getEventPastTense(word);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataEntry)) return false;
        DataEntry dataEntry = (DataEntry) o;
        return Objects.equals(data, dataEntry.data);
    }

    @Override
    public String toString() {
        return "DataEntry{" +
                "data=" + data +
                '}';
    }
}
