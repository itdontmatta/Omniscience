package com.itdontmatta.omniscience.io;

import com.itdontmatta.omniscience.api.data.DataWrapper;
import com.itdontmatta.omniscience.api.entry.DataEntry;
import com.itdontmatta.omniscience.api.query.QuerySession;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface RecordHandler {

    void write(List<DataWrapper> wrappers);

    CompletableFuture<List<DataEntry>> query(QuerySession session) throws Exception;

}
