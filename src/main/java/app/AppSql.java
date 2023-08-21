package app;

import mapper.Tracker;
import model.TrackerAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AppSql {
    final List<Tracker> findCurrentTrackers() {
        return new ArrayList<>();
    }

    final List<Tracker> findUpdatingTrackers() {
        return new ArrayList<>();
    }

    String exportSql() {
        Map<String, TrackerAction> actions = new HashMap<>();

        for (Tracker updating : findUpdatingTrackers()) {
            actions.computeIfAbsent(updating.getId(), k -> TrackerAction.fromTracker(updating)).capture(updating);
        }
        for (Tracker current : findCurrentTrackers()) {
            TrackerAction action = actions.get(current.getId());
            if (action != null) {
                action.isLastExisted(current);
            }
        }
        return actions.values().stream().map(TrackerAction::exportSql)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining("\n"));
    }
}
