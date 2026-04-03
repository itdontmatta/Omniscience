package net.lordofthecraft.omniscience.io;

import net.lordofthecraft.omniscience.Omniscience;

public interface StorageHandler {

    boolean connect(Omniscience omniscience) throws Exception;

    RecordHandler records();

    void close();
}
