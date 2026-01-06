package xyz.earthcow.themistodiscord;

import com.gmail.olexorus.themis.api.CheckType;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class HandlingService {
    private final HashMap<UUID, HashMap<CheckType, Long>> lastSentTimesPerPlayer = new HashMap<>();
    private final HashMap<UUID, HashMap<CheckType, Integer>> repetitionCountersPerPlayer = new HashMap<>();

    private final double executionThreshold;
    private final double repetitionDelay;
    private final double repetitionThreshold;

    public HandlingService(Section handlingSection) {
        this.executionThreshold = handlingSection.getDouble("execution-threshold");
        this.repetitionDelay = handlingSection.getDouble("repetition-delay");
        this.repetitionThreshold = handlingSection.getDouble("repetition-threshold");
    }

    public double getExecutionThreshold() {
        return executionThreshold;
    }

    public double getRepetitionDelay() {
        return repetitionDelay;
    }

    public double getRepetitionThreshold() {
        return repetitionThreshold;
    }

    public long getLastSentTimeForPlayer(Player player, CheckType checkType) {
        HashMap<CheckType, Long> times = lastSentTimesPerPlayer.get(player.getUniqueId());
        if (times == null) {
            return 0;
        }
        return times.getOrDefault(checkType, 0L);
    }

    public void updateLastSentTimeForPlayer(Player player, CheckType checkType) {
        HashMap<CheckType, Long> times = lastSentTimesPerPlayer.getOrDefault(player.getUniqueId(), new HashMap<>());
        times.put(checkType, System.currentTimeMillis());
        lastSentTimesPerPlayer.put(player.getUniqueId(), times);
    }

    public int getRepetitionCountForPlayer(Player player, CheckType checkType) {
        HashMap<CheckType, Integer> repetitionCounts = repetitionCountersPerPlayer.get(player.getUniqueId());
        if (repetitionCounts == null) {
            return -2;
        }
        return repetitionCounts.getOrDefault(checkType, -2);
    }

    public void putRepetitionCountForPlayer(Player player, CheckType checkType, int repetitionCount) {
        HashMap<CheckType, Integer> repetitionCounts = repetitionCountersPerPlayer.getOrDefault(player.getUniqueId(), new HashMap<>());
        repetitionCounts.put(checkType, repetitionCount);
        repetitionCountersPerPlayer.put(player.getUniqueId(), repetitionCounts);
    }
}
