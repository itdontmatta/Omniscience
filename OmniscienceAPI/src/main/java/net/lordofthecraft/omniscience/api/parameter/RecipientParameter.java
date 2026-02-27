package net.lordofthecraft.omniscience.api.parameter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.lordofthecraft.omniscience.api.data.DataKeys;
import net.lordofthecraft.omniscience.api.query.FieldCondition;
import net.lordofthecraft.omniscience.api.query.MatchRule;
import net.lordofthecraft.omniscience.api.query.Query;
import net.lordofthecraft.omniscience.api.query.QuerySession;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RecipientParameter extends BaseParameterHandler {

    private final Pattern pattern = Pattern.compile("[\\w,!:-]+");

    public RecipientParameter() {
        super(ImmutableList.of("rcp", "recipient"));
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
            List<String> include = Lists.newArrayList();
            List<String> exclude = Lists.newArrayList();

            for (String name : value.split(",")) {
                boolean excluded = name.startsWith("!");
                String cleanName = excluded ? name.substring(1) : name;
                OfflinePlayer player = Bukkit.getOfflinePlayer(cleanName);

                if (player != null) {
                    String uuid = player.getUniqueId().toString();
                    if (excluded) {
                        exclude.add(uuid);
                    } else {
                        include.add(uuid);
                    }
                }
            }

            if (!include.isEmpty()) {
                query.addCondition(FieldCondition.of(DataKeys.RECIPIENT, MatchRule.INCLUDES, include));
            }
            if (!exclude.isEmpty()) {
                query.addCondition(FieldCondition.of(DataKeys.RECIPIENT, MatchRule.EXCLUDES, exclude));
            }
        } else {
            OfflinePlayer player = Bukkit.getOfflinePlayer(value);
            if (player != null) {
                query.addCondition(FieldCondition.of(DataKeys.RECIPIENT, MatchRule.EQUALS, player.getUniqueId().toString()));
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<List<String>> suggestTabCompletion(String partial) {
        return Optional.of(generateDefaultsBasedOnPartial(
                Bukkit.getOnlinePlayers().stream()
                        .map(p -> p.getName().toLowerCase())
                        .collect(Collectors.toList()),
                partial != null ? partial.toLowerCase() : partial
        ));
    }
}
