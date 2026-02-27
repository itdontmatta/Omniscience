package com.itdontmatta.omniscience.io;

import com.itdontmatta.omniscience.Omniscience;

public interface StorageHandler {

    boolean connect(Omniscience omniscience) throws Exception;

    RecordHandler records();

    void close();
}
