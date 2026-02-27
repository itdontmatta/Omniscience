package com.itdontmatta.omniscience.command.util;

import com.itdontmatta.omniscience.OmniConfig;
import com.itdontmatta.omniscience.Omniscience;
import com.itdontmatta.omniscience.api.entry.DataEntry;
import com.itdontmatta.omniscience.api.query.QuerySession;
import com.itdontmatta.omniscience.api.util.Formatter;
import com.itdontmatta.omniscience.command.async.AsyncCallback;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class Async {

    public static void lookup(final QuerySession session, AsyncCallback callback) {
        session.getQuery().setSearchLimit(OmniConfig.INSTANCE.getLookupSizeLimit());
        Bukkit.getScheduler().runTaskAsynchronously(Omniscience.getProvidingPlugin(Omniscience.class), () -> {
            try {
                CompletableFuture<List<DataEntry>> future = Omniscience.getStorageHandler().records().query(session);
                future.thenAccept(results -> {
                    try {
                        if (results.isEmpty()) {
                            callback.empty();
                        } else {
                            callback.success(results);
                        }
                    } catch (Exception e) {
                        session.getSender().sendMessage(Formatter.error(e.getMessage()));
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                callback.error(e);
                e.printStackTrace();
            }
        });
    }
}
