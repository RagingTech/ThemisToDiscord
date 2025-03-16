package xyz.earthcow.themistodiscord;

import com.gmail.olexorus.themis.api.CheckType;
import com.gmail.olexorus.themis.api.ThemisApi;
import com.gmail.olexorus.themis.api.ViolationEvent;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ThemisListener implements Listener {
    private boolean pingSupportedVersion = true;

    @EventHandler
    public void onViolationEvent(ViolationEvent event) {
        Player player = event.getPlayer();
        CheckType checkType = event.getType();

        double ping = 0, tps = 0;
        if (pingSupportedVersion) {
            try {
                ping = (ThemisApi.getPing(player) != null) ? ThemisApi.getPing(player) : 0;
                tps = ThemisApi.getTps();
            } catch (NoSuchMethodError e) {
                ThemisToDiscord.log(LogLevel.WARN, "Please update Themis to 0.15.3 or higher for player ping and server tps!");
                pingSupportedVersion = false;
            }
        }

        double score = Math.round(ThemisApi.getViolationScore(player, checkType) * 100.0) / 100.0;

        for (Message message : ThemisToDiscord.config.getMessages()) {
            Section handling = message.getHandling();

            if (handling == null || !handling.getBoolean("Enabled", false)) {
                continue;
            }

            if (handling.getDouble("execution-threshold") > score
                || handling.getDouble("repetition-delay") > ((System.currentTimeMillis() - message.getLastSentTimeForPlayer(player, checkType)) / 1000.0))
                continue;

            int repetitionCounterForCheckType = message.getRepetitionCountForPlayer(player, checkType) + 1;

            if (repetitionCounterForCheckType == handling.getDouble("repetition-threshold")) {
                message.putRepetitionCountForPlayer(player, checkType, -1);
                repetitionCounterForCheckType = -1;
            } else {
                message.putRepetitionCountForPlayer(player, checkType, repetitionCounterForCheckType);
            }

            if (repetitionCounterForCheckType != -1) continue;

            message.execute(player, checkType.getDescription(), score, ping, tps, null);
            message.updateLastSentTimeForPlayer(player, checkType);
        }
    }
}