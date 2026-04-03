package net.lordofthecraft.omniscience.api.entry;

public interface Actionable {

    ActionResult rollback() throws Exception;

    ActionResult restore() throws Exception;

    default ActionableException skipped(SkipReason reason) {
        return new ActionableException(ActionResult.skipped(reason));
    }
}
