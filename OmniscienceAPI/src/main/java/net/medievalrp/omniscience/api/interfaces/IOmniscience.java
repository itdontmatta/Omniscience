package net.medievalrp.omniscience.api.interfaces;

import net.medievalrp.omniscience.api.entry.DataEntry;
import net.medievalrp.omniscience.api.flag.FlagHandler;
import net.medievalrp.omniscience.api.parameter.ParameterHandler;
import net.medievalrp.omniscience.api.util.PastTenseWithEnabled;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

public interface IOmniscience {

    Optional<Class<? extends DataEntry>> getEventClass(String name);

    void info(String info);

    void warning(String warning);

    void severe(String error);

    void log(Level level, String message, Throwable ex);

    boolean areDefaultsEnabled();

    List<ParameterHandler> getParameters();

    Map<String, PastTenseWithEnabled> getEvents();

    void registerEvent(String event, String pastTense);

    Optional<FlagHandler> getFlagHandler(String flag);

    void registerFlagHandler(FlagHandler handler);

    Optional<ParameterHandler> getParameterHandler(String name);

    void registerParameterHandler(ParameterHandler handler);

    String getDefaultTime();

    int getDefaultRadius();

    int getMaxRadius();

    String getSimpleDateFormat();

    void registerWorldEditHandler(WorldEditHandler handler);

    String getDateFormat();
}
