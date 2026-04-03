package net.lordofthecraft.omniscience.api.display;

import net.lordofthecraft.omniscience.api.entry.DataEntry;
import net.lordofthecraft.omniscience.api.flag.Flag;
import net.lordofthecraft.omniscience.api.query.QuerySession;

import java.util.List;
import java.util.Optional;

import static net.lordofthecraft.omniscience.api.data.DataKeys.MESSAGE;

public class MessageDisplayHandler extends SimpleDisplayHandler {

    public MessageDisplayHandler() {
        super("message");
    }

    @Override
    public Optional<String> buildTargetMessage(DataEntry entry, String target, QuerySession session) {
        if (!session.hasFlag(Flag.NO_GROUP) || !entry.data.getKeys(false).contains(MESSAGE)) {
            return Optional.empty();
        }
        return entry.data.getString(MESSAGE);
    }

    @Override
    public Optional<List<String>> buildAdditionalHoverData(DataEntry entry, QuerySession session) {
        return Optional.empty();
    }
}
