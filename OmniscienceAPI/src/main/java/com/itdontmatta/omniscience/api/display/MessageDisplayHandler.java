package com.itdontmatta.omniscience.api.display;

import com.itdontmatta.omniscience.api.entry.DataEntry;
import com.itdontmatta.omniscience.api.flag.Flag;
import com.itdontmatta.omniscience.api.query.QuerySession;

import java.util.List;
import java.util.Optional;

import static com.itdontmatta.omniscience.api.data.DataKeys.MESSAGE;

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
