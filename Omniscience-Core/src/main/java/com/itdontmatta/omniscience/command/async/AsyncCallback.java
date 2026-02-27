package com.itdontmatta.omniscience.command.async;

import com.itdontmatta.omniscience.api.entry.DataEntry;

import java.util.List;

public interface AsyncCallback {

    void success(List<DataEntry> results);

    void empty();

    void error(Exception e);
}
