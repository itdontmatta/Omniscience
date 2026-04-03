package net.lordofthecraft.omniscience.command.util;

import net.lordofthecraft.omniscience.OmniConfig;
import net.lordofthecraft.omniscience.Omniscience;
import net.lordofthecraft.omniscience.api.entry.DataEntry;
import net.lordofthecraft.omniscience.api.query.QuerySession;
import net.lordofthecraft.omniscience.api.util.Formatter;
import net.lordofthecraft.omniscience.command.async.AsyncCallback;
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
