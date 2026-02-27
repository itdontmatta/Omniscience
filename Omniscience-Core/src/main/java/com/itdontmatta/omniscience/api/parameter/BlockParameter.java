package com.itdontmatta.omniscience.api.parameter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.itdontmatta.omniscience.api.data.DataKeys;
import com.itdontmatta.omniscience.api.query.FieldCondition;
import com.itdontmatta.omniscience.api.query.MatchRule;
import com.itdontmatta.omniscience.api.query.Query;
import com.itdontmatta.omniscience.api.query.QuerySession;
import com.itdontmatta.omniscience.api.util.DataHelper;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BlockParameter extends BaseParameterHandler {
    private final Pattern pattern = Pattern.compile("[\\w,:-\\\\*]+");

    public BlockParameter() {
        super(ImmutableList.of("b"));
    }

    @Override
    public boolean canRun(CommandSender sender) {
        return true;
    }

    @Override
    public boolean acceptsValue(String value) {
        return pattern.matcher(value).matches() && areBlockValues(value);
    }

    @Override
    public Optional<CompletableFuture<?>> buildForQuery(QuerySession session, String parameter, String value, Query query) {
        if (value.contains(",")) {
            convertStringToIncludes(DataKeys.TARGET, value.toUpperCase(), query);
        } else {
            query.addCondition(FieldCondition.of(DataKeys.TARGET, MatchRule.EQUALS, DataHelper.compileUserInput(value.toUpperCase())));
        }

        return Optional.empty();
    }

    @Override
    public Optional<List<String>> suggestTabCompletion(String partial) {
        return Optional.of(generateDefaultsBasedOnPartial(Lists.newArrayList(Material.values())
                .stream().filter(Material::isBlock).map(mat -> mat.name().toLowerCase()).collect(Collectors.toList()), partial != null ? partial.toLowerCase() : partial));
    }

    private boolean areBlockValues(String value) {
        for (String s : getInputAsList(value)) {
            Material mat = Material.getMaterial(s.toUpperCase());
            if (mat == null || !mat.isBlock()) {
                return false;
            }
        }
        return true;
    }
}
