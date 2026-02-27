package com.itdontmatta.omniscience.api.parameter;

import com.google.common.collect.ImmutableList;
import com.itdontmatta.omniscience.api.data.DataKeys;
import com.itdontmatta.omniscience.api.query.FieldCondition;
import com.itdontmatta.omniscience.api.query.MatchRule;
import com.itdontmatta.omniscience.api.query.Query;
import com.itdontmatta.omniscience.api.query.QuerySession;
import com.itdontmatta.omniscience.api.util.DataHelper;
import org.bukkit.command.CommandSender;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class MessageParameter extends BaseParameterHandler {
    private final Pattern pattern = Pattern.compile("[\\w!,:-\\\\*]+");

    public MessageParameter() {
        super(ImmutableList.of("m"));
    }

    @Override
    public boolean canRun(CommandSender sender) {
        return true;
    }

    @Override
    public boolean acceptsValue(String value) {
        return pattern.matcher(value).matches();
    }

    @Override
    public Optional<CompletableFuture<?>> buildForQuery(QuerySession session, String parameter, String value, Query query) {
        if (value.contains(",")) {
            query.addCondition(FieldCondition.of(DataKeys.MESSAGE, MatchRule.EQUALS, compileMessageSearch(value.split(","))));
        } else {
            query.addCondition(FieldCondition.of(DataKeys.MESSAGE, MatchRule.EQUALS, DataHelper.compileUserInput(value)));
        }

        return Optional.empty();
    }
}
