package com.itdontmatta.omniscience.api.flag;

import com.google.common.collect.ImmutableList;
import com.itdontmatta.omniscience.api.query.Query;
import com.itdontmatta.omniscience.api.query.QuerySession;
import org.bukkit.command.CommandSender;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class FlagExtended extends BaseFlagHandler {

    public FlagExtended() {
        super(ImmutableList.of("ex"));
    }

    @Override
    public boolean acceptsSource(CommandSender sender) {
        return true;
    }

    @Override
    public boolean acceptsValue(String value) {
        return true;
    }

    @Override
    public Optional<CompletableFuture<?>> process(QuerySession session, String flag, String value, Query query) {
        session.addFlag(Flag.EXTENDED);
        return Optional.empty();
    }
}
