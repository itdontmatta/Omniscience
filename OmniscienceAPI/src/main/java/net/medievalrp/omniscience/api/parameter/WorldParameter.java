package net.medievalrp.omniscience.api.parameter;

import com.google.common.collect.ImmutableList;
import net.medievalrp.omniscience.api.query.Query;
import net.medievalrp.omniscience.api.query.QuerySession;
import net.medievalrp.omniscience.api.query.SearchConditionGroup;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class WorldParameter extends BaseParameterHandler {

    public WorldParameter() {
        super(ImmutableList.of("w", "world"));
    }

    @Override
    public boolean canRun(CommandSender sender) {
        return true;
    }

    @Override
    public boolean acceptsValue(String value) {
        return Bukkit.getWorld(value) != null;
    }

    @Override
    public Optional<CompletableFuture<?>> buildForQuery(QuerySession session, String parameter, String value, Query query) {
        World world = Bukkit.getWorld(value);
        if (world == null) {
            return Optional.empty();
        }

        // ignore default radius when searching by world
        session.addIgnoredDefault("r");

        query.addCondition(SearchConditionGroup.from(world));
        return Optional.empty();
    }

    @Override
    public Optional<List<String>> suggestTabCompletion(String partial) {
        return Optional.of(generateDefaultsBasedOnPartial(
                Bukkit.getWorlds().stream()
                        .map(w -> w.getName().toLowerCase())
                        .collect(Collectors.toList()),
                partial != null ? partial.toLowerCase() : partial
        ));
    }
}
