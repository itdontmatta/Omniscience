package net.medievalrp.omniscience.api.parameter;

import com.google.common.collect.ImmutableList;
import net.medievalrp.omniscience.api.data.DataKeys;
import net.medievalrp.omniscience.api.query.FieldCondition;
import net.medievalrp.omniscience.api.query.MatchRule;
import net.medievalrp.omniscience.api.query.Query;
import net.medievalrp.omniscience.api.query.QuerySession;
import net.medievalrp.omniscience.api.util.DataHelper;
import org.bukkit.command.CommandSender;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class CauseParameter extends BaseParameterHandler {
    private final Pattern pattern = Pattern.compile("[\\w,:-\\\\*]+");

    public CauseParameter() {
        super(ImmutableList.of("c"));
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
            convertStringToIncludes(DataKeys.CAUSE, value, query);
        } else {
            query.addCondition(FieldCondition.of(DataKeys.CAUSE, MatchRule.EQUALS, DataHelper.compileUserInput(value)));
        }

        return Optional.empty();
    }
}
