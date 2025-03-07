package xyz.earthcow.themistodiscord;

import com.gmail.olexorus.themis.api.CheckType;
import com.gmail.olexorus.themis.api.ThemisApi;
import com.gmail.olexorus.themis.api.ViolationEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import xyz.earthcow.discordwebhook.DiscordWebhook;

import java.awt.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class ThemisListener implements Listener {
    private final HashMap<UUID, HashMap<CheckType, Long>> lastSentTimesPerPlayer = new HashMap<>();
    private final HashMap<UUID, HashMap<CheckType, Integer>> repetitionCountersPerPlayer = new HashMap<>();
    private boolean pingSupportedVersion = true;

    @EventHandler
    public void onViolationEvent(ViolationEvent event) {
        Configuration config = ThemisToDiscord.config;
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        HashMap<CheckType, Long> lastSentTimesForPlayer = lastSentTimesPerPlayer.getOrDefault(playerUUID, new HashMap<>());
        HashMap<CheckType, Integer> repetitionCountersForPlayer = repetitionCountersPerPlayer.getOrDefault(playerUUID, new HashMap<>());

        CheckType checkType = event.getType();

        double score = Math.round(ThemisApi.getViolationScore(player, checkType) * 100.0) / 100.0;

        if (config.executionThreshold > score
                || config.repetitionDelay > ((System.currentTimeMillis() - lastSentTimesForPlayer.getOrDefault(checkType, 0L)) / 1000.0)) return;

        int repetitionCounterForCheckType = repetitionCountersForPlayer.getOrDefault(checkType, -2) + 1;
        repetitionCountersForPlayer.put(checkType, repetitionCounterForCheckType);
        repetitionCountersPerPlayer.put(playerUUID, repetitionCountersForPlayer);

        if (repetitionCounterForCheckType == config.repetitionThreshold) {
            repetitionCountersForPlayer.put(checkType, -1);
            repetitionCountersPerPlayer.put(playerUUID, repetitionCountersForPlayer);
        }

        if (repetitionCountersForPlayer.get(checkType) != -1) return;

        String ping = "NA";
        String tps = "NA";
        if (pingSupportedVersion) {
            try {
                ping = "" + Objects.requireNonNullElse(ThemisApi.getPing(player), 0);
                tps = "" + ThemisApi.getTps();
            } catch (NoSuchMethodError err) {
                ThemisToDiscord.log(LogLevel.WARN, "Please update Themis to 0.15.3 or higher for player ping and server tps!");
                pingSupportedVersion = false;
            }
        }

        String checkTypeStr = checkType.getDescription();

        DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject();

        embed
                .setColor(config.categoryColors.getOrDefault(checkTypeStr, Color.GRAY))
                .setTitle(checkTypeStr)
                .setDescription("Themis flagged " + player.getName() + " for " + checkTypeStr + " hacks!")
                .setAuthor(player.getName(), null, null)
                .addField("Score", "" + score, true)
                .addField("Ping", ping, true)
                .addField("TPS", tps, true)
                .setTimestamp((new Date()).toInstant());

        ThemisToDiscord.executeWebhook(embed, null);

        lastSentTimesForPlayer.put(checkType, System.currentTimeMillis());
        lastSentTimesPerPlayer.put(playerUUID, lastSentTimesForPlayer);
    }
}