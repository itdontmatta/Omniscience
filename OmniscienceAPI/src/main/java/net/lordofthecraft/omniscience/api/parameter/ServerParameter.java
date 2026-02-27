package net.lordofthecraft.omniscience.api.parameter;

import com.google.common.collect.ImmutableList;
import net.lordofthecraft.omniscience.api.data.DataKeys;
import net.lordofthecraft.omniscience.api.query.FieldCondition;
import net.lordofthecraft.omniscience.api.query.MatchRule;
import net.lordofthecraft.omniscience.api.query.Query;
import net.lordofthecraft.omniscience.api.query.QuerySession;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class ServerParameter extends BaseParameterHandler {

    private final Pattern pattern = Pattern.compile("[\\w-]+");

    public ServerParameter() {
        super(ImmutableList.of("server", "srv"));
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
        String serverId = resolveServerId(value);
        if (serverId != null) {
            query.addCondition(FieldCondition.of(DataKeys.SERVER, MatchRule.EQUALS, serverId));
        }
        return Optional.empty();
    }

    @Override
    public Optional<List<String>> suggestTabCompletion(String partial) {
        List<String> servers = getAvailableServers();
        if (servers.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(generateDefaultsBasedOnPartial(servers, partial != null ? partial.toLowerCase() : partial));
    }

    /**
     * Resolves a server input to its canonical ID.
     * Override this to provide custom server resolution logic.
     */
    protected String resolveServerId(String input) {
        // TODO: implement server resolution when multi-server support is added
        return input.toLowerCase();
    }

    /**
     * Returns a list of available servers for tab completion.
     * Override this to provide server discovery logic.
     */
    protected List<String> getAvailableServers() {
        // TODO: implement server discovery when multi-server support is added
        return List.of();
    }
}
