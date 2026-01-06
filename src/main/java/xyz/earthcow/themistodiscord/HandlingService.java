package xyz.earthcow.themistodiscord;

import com.gmail.olexorus.themis.api.CheckType;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HandlingService {
    private record CheckKey(UUID uuid, CheckType checkType) {}
    private record CheckData(long lastSent, int repetitionCount) {}

    private final Map<CheckKey, CheckData> playerCheckData = new HashMap<>();

    private final double executionThreshold;
    private final double repetitionDelay;
    private final double repetitionThreshold;

    private static final long DEFAULT_LAST_SENT = 0;
    private static final int DEFAULT_REPETITION_COUNT = -2;

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
        CheckData checkData = playerCheckData.get(new CheckKey(player.getUniqueId(), checkType));
        if (checkData == null) return DEFAULT_LAST_SENT;
        return checkData.lastSent;
    }

    public void updateLastSentTimeForPlayer(Player player, CheckType checkType) {
        CheckKey playerCheckDataKey = new CheckKey(player.getUniqueId(), checkType);
        CheckData checkData = playerCheckData.get(playerCheckDataKey);
        if (checkData == null) {
            checkData = new CheckData(System.currentTimeMillis(), DEFAULT_REPETITION_COUNT);
        } else {
            checkData = new CheckData(System.currentTimeMillis(), checkData.repetitionCount);
        }
        playerCheckData.put(playerCheckDataKey, checkData);
    }

    public int getRepetitionCountForPlayer(Player player, CheckType checkType) {
        CheckData checkData = playerCheckData.get(new CheckKey(player.getUniqueId(), checkType));
        if (checkData == null) return DEFAULT_REPETITION_COUNT;
        return checkData.repetitionCount;
    }

    public void putRepetitionCountForPlayer(Player player, CheckType checkType, int repetitionCount) {
        CheckKey playerCheckDataKey = new CheckKey(player.getUniqueId(), checkType);
        CheckData checkData = playerCheckData.get(playerCheckDataKey);
        if (checkData == null) {
            checkData = new CheckData(DEFAULT_LAST_SENT, repetitionCount);
        } else {
            checkData = new CheckData(checkData.lastSent, repetitionCount);
        }
        playerCheckData.put(playerCheckDataKey, checkData);
    }
}
