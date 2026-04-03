package net.lordofthecraft.omniscience.command.async;

import net.lordofthecraft.omniscience.api.entry.DataEntry;

import java.util.List;

public interface AsyncCallback {

    void success(List<DataEntry> results);

    void empty();

    void error(Exception e);
}
