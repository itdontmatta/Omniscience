package net.medievalrp.omniscience.api.parameter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.medievalrp.omniscience.api.data.DataKeys;
import net.medievalrp.omniscience.api.query.FieldCondition;
import net.medievalrp.omniscience.api.query.MatchRule;
import net.medievalrp.omniscience.api.query.Query;
import net.medievalrp.omniscience.api.query.QuerySession;
import net.medievalrp.omniscience.api.util.DataHelper;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class EntityParameter extends BaseParameterHandler {
    private final Pattern pattern = Pattern.compile("[\\w,:-\\\\*]+");

    public EntityParameter() {
        super(ImmutableList.of("e"));
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
            convertStringToIncludes(DataKeys.ENTITY_TYPE, value.toUpperCase(), query);
        } else {
            query.addCondition(FieldCondition.of(DataKeys.ENTITY_TYPE, MatchRule.EQUALS, DataHelper.compileUserInput(value.toUpperCase())));
        }

        return Optional.empty();
    }

    @Override
    public Optional<List<String>> suggestTabCompletion(String partial) {
        return Optional.of(generateDefaultsBasedOnPartial(Lists.newArrayList(EntityType.values()).stream()
                .map(ent -> ent.name().toLowerCase())
                .collect(Collectors.toList()), partial != null ? partial.toLowerCase() : partial));
    }
}
