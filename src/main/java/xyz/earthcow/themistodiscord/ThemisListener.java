package xyz.earthcow.themistodiscord;

import com.gmail.olexorus.themis.api.CheckType;
import com.gmail.olexorus.themis.api.ThemisApi;
import com.gmail.olexorus.themis.api.ViolationEvent;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class ThemisListener implements Listener {
    @NotNull
    private final ThemisToDiscord ttd;
    @NotNull
    private final Configuration config;
    private boolean pingSupportedVersion = true;

    public ThemisListener(@NotNull ThemisToDiscord ttd, @NotNull Configuration config) {
        this.ttd = ttd;
        this.config = config;
    }

    @EventHandler
    public void onViolationEvent(@NotNull ViolationEvent event) {
        Player player = event.getPlayer();
        CheckType checkType = event.getType();

        double ping = 0, tps = 0;
        if (pingSupportedVersion) {
            try {
                ping = (ThemisApi.getPing(player) != null) ? ThemisApi.getPing(player) : 0;
                tps = ThemisApi.getTps();
            } catch (NoSuchMethodError e) {
                ttd.log(LogLevel.WARN, "Please update Themis to 0.15.3 or higher for player ping and server tps!");
                pingSupportedVersion = false;
            }
        }

        double score = Math.round(ThemisApi.getViolationScore(player, checkType) * 100.0) / 100.0;

        for (Message message : config.getMessages()) {
            HandlingService handling = message.getHandlingService();

            if (handling == null) {
                continue;
            }

            if (handling.getExecutionThreshold() > score
                || handling.getRepetitionDelay() > ((System.currentTimeMillis() - handling.getLastSentTimeForPlayer(player, checkType)) / 1000.0))
                continue;

            double currentRepCount = handling.getRepetitionCountForPlayer(player, checkType) + 1.0;

            if (currentRepCount >= handling.getRepetitionThreshold()) {
                message.execute(player, checkType.getDescription(), score, ping, tps, null);
                handling.updateLastSentTimeForPlayer(player, checkType);

                // Reset the counter
                handling.putRepetitionCountForPlayer(player, checkType, 0);
            } else {
                // Update the counter
                handling.putRepetitionCountForPlayer(player, checkType, currentRepCount);
            }
        }
    }
}