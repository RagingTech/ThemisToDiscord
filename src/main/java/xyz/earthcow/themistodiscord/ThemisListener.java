package xyz.earthcow.themistodiscord;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import com.gmail.olexorus.themis.api.CheckType;
import com.gmail.olexorus.themis.api.ThemisApi;
import com.gmail.olexorus.themis.api.ViolationEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAccessor;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class ThemisListener implements Listener {
    private final HashMap<UUID, HashMap<CheckType, Long>> lastSentTimesPerPlayer = new HashMap<>();
    private final HashMap<UUID, HashMap<CheckType, Integer>> repetitionCountersPerPlayer = new HashMap<>();
    private boolean pingSupportedVersion = true;

    @EventHandler
    public void onViolationEvent(ViolationEvent event) {
        if (ThemisToDiscord.client == null || ThemisToDiscord.client.isShutdown()) {
            ThemisToDiscord.instance.getLogger().severe("Themis message was not sent to discord because there is no active client. Use /ttd url <url> to specify the webhook url.");
            return;
        }

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

        LocalDateTime currentDateTime = LocalDateTime.now();
        TemporalAccessor currentTimestamp = currentDateTime.atZone(ZoneId.systemDefault());

        String ping = "NA";
        String tps = "NA";
        if (pingSupportedVersion) {
            try {
                ping = "" + Objects.requireNonNullElse(ThemisApi.getPing(player), 0);
                tps = "" + ThemisApi.getTps();
            } catch (NoSuchMethodError err) {
                ThemisToDiscord.instance.getLogger().warning("Please update Themis to 0.15.3 or higher for player ping and server tps!");
                pingSupportedVersion = false;
            }
        }

        String checkTypeStr = checkType.getDescription();

        WebhookEmbed embed = new WebhookEmbedBuilder()
                .setColor(config.categoryColors.getOrDefault(checkTypeStr, Color.GRAY).getRGB())
                .setTitle(new WebhookEmbed.EmbedTitle(checkTypeStr, null))
                .setDescription("Themis flagged " + player.getName() + " for " + checkTypeStr + " hacks!")
                .setAuthor(new WebhookEmbed.EmbedAuthor(player.getName(), null, null))
                .addField(new WebhookEmbed.EmbedField(true, "Score", "" + score))
                .addField(new WebhookEmbed.EmbedField(true, "Ping", ping))
                .addField(new WebhookEmbed.EmbedField(true, "TPS", tps))
                .setTimestamp(currentTimestamp)
                .build();

        ThemisToDiscord.client.send(embed);
        lastSentTimesForPlayer.put(checkType, System.currentTimeMillis());
        lastSentTimesPerPlayer.put(playerUUID, lastSentTimesForPlayer);
    }
}