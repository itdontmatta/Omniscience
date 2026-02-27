package com.itdontmatta.omniscience.api.parameter;

import com.google.common.collect.ImmutableList;
import com.itdontmatta.omniscience.OmniEventRegistrar;
import com.itdontmatta.omniscience.api.OmniApi;
import com.itdontmatta.omniscience.api.data.DataKeys;
import com.itdontmatta.omniscience.api.query.FieldCondition;
import com.itdontmatta.omniscience.api.query.MatchRule;
import com.itdontmatta.omniscience.api.query.Query;
import com.itdontmatta.omniscience.api.query.QuerySession;
import com.itdontmatta.omniscience.api.util.DataHelper;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class EventParameter extends BaseParameterHandler {
    //Credit to Prism for this regex
    private final Pattern pattern = Pattern.compile("[!]?[\\w,-\\\\*]+");

    public EventParameter() {
        super(ImmutableList.of("a"));
    }

    @Override
    public boolean canRun(CommandSender sender) {
        return true;
    }

    @Override
    public boolean acceptsValue(String value) {
        return pattern.matcher(value).matches() && hasEventNames(value);
    }

    @Override
    public Optional<CompletableFuture<?>> buildForQuery(QuerySession session, String parameter, String value, Query query) {
        if (value.contains(",")) {
            convertStringToIncludes(DataKeys.EVENT_NAME, value, query);
        } else {
            query.addCondition(FieldCondition.of(DataKeys.EVENT_NAME, MatchRule.EQUALS, DataHelper.compileUserInput(value)));
        }

        return Optional.empty();
    }

    @Override
    public Optional<List<String>> suggestTabCompletion(String partial) {
        return Optional.of(generateDefaultsBasedOnPartial(OmniEventRegistrar.INSTANCE.getEventNames()
                .stream().map(String::toLowerCase).collect(Collectors.toList()), partial));
    }

    public boolean hasEventNames(String value) {
        value = value.toLowerCase();
        for (String s : getInputAsList(value)) {
            if (!OmniApi.getEnabledEvents().contains(s)) {
                return false;
            }
        }
        return true;
    }
}
