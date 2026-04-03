package net.medievalrp.omniscience.io;

import net.medievalrp.omniscience.Omniscience;

public interface StorageHandler {

    boolean connect(Omniscience omniscience) throws Exception;

    RecordHandler records();

    void close();
}
