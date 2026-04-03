package net.medievalrp.omniscience.io;

import net.medievalrp.omniscience.api.data.DataWrapper;
import net.medievalrp.omniscience.api.entry.DataEntry;
import net.medievalrp.omniscience.api.query.QuerySession;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface RecordHandler {

    void write(List<DataWrapper> wrappers);

    CompletableFuture<List<DataEntry>> query(QuerySession session) throws Exception;

}
