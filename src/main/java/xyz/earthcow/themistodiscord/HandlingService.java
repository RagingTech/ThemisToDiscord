package xyz.earthcow.themistodiscord;

import com.gmail.olexorus.themis.api.CheckType;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class HandlingService {
    private record CheckKey(UUID uuid, CheckType checkType) {}
    private record CheckData(long lastSent, double repetitionCount) {}

    private final Cache<CheckKey, CheckData> playerCheckData;

    private final double executionThreshold;
    private final double repetitionDelay;
    private final double repetitionThreshold;

    private static final long DEFAULT_LAST_SENT = 0L;
    private static final double DEFAULT_REPETITION_COUNT = 0.0;

    public HandlingService(@NotNull Section handlingSection, @NotNull String msgName, @NotNull ThemisToDiscord ttd) {
        double localExecutionThreshold = handlingSection.getDouble("execution-threshold");
        double localRepetitionDelay = handlingSection.getDouble("repetition-delay");
        double localRepetitionThreshold = handlingSection.getDouble("repetition-threshold");
        if (localExecutionThreshold < 1) {
            ttd.log(LogLevel.WARN, "Execution threshold must be positive! Message: " + msgName + " will use the default of 10.0");
            localExecutionThreshold = 10.0;
        }
        if (localRepetitionDelay < 1 || localRepetitionDelay >= 86400) {
            ttd.log(LogLevel.WARN, "Repetition delay must be positive and less than 24 hours! Message: " + msgName + " will use the default of 10.0");
            localRepetitionDelay = 10.0;
        }
        if (localRepetitionThreshold < 1) {
            ttd.log(LogLevel.WARN, "Repetition threshold must be positive! Message: " + msgName + " will use the default of 5.0");
            localRepetitionThreshold = 5.0;
        }
        this.executionThreshold = localExecutionThreshold;
        this.repetitionDelay = localRepetitionDelay;
        this.repetitionThreshold = localRepetitionThreshold;

        this.playerCheckData = CacheBuilder.newBuilder()
                .expireAfterAccess(24, TimeUnit.HOURS)
                .build();
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
        CheckData checkData = playerCheckData.getIfPresent(new CheckKey(player.getUniqueId(), checkType));
        if (checkData == null) return DEFAULT_LAST_SENT;
        return checkData.lastSent;
    }

    public void updateLastSentTimeForPlayer(Player player, CheckType checkType) {
        CheckKey playerCheckDataKey = new CheckKey(player.getUniqueId(), checkType);
        CheckData checkData = playerCheckData.getIfPresent(playerCheckDataKey);
        if (checkData == null) {
            checkData = new CheckData(System.currentTimeMillis(), DEFAULT_REPETITION_COUNT);
        } else {
            checkData = new CheckData(System.currentTimeMillis(), checkData.repetitionCount);
        }
        playerCheckData.put(playerCheckDataKey, checkData);
    }

    public double getRepetitionCountForPlayer(Player player, CheckType checkType) {
        CheckData checkData = playerCheckData.getIfPresent(new CheckKey(player.getUniqueId(), checkType));
        if (checkData == null) return DEFAULT_REPETITION_COUNT;
        return checkData.repetitionCount;
    }

    public void putRepetitionCountForPlayer(Player player, CheckType checkType, double repetitionCount) {
        CheckKey playerCheckDataKey = new CheckKey(player.getUniqueId(), checkType);
        CheckData checkData = playerCheckData.getIfPresent(playerCheckDataKey);
        if (checkData == null) {
            checkData = new CheckData(DEFAULT_LAST_SENT, repetitionCount);
        } else {
            checkData = new CheckData(checkData.lastSent, repetitionCount);
        }
        playerCheckData.put(playerCheckDataKey, checkData);
    }
}
