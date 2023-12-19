package xyz.earthcow.themistodiscord;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import com.gmail.olexorus.themis.api.NotificationEvent;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAccessor;
import java.util.regex.Matcher;

public class ThemisListener implements Listener {

    @EventHandler
    public void onNotificationEvent(NotificationEvent event) {
        if (ThemisToDiscord.client == null || ThemisToDiscord.client.isShutdown()) {
            ThemisToDiscord.instance.getLogger().severe("Themis message was not sent to discord because there is no active client. Use /ttd url <url> to specify the webhook url.");
            return;
        }

        Configuration config = ThemisToDiscord.config;
        String themisMsg = ChatColor.stripColor(event.getMessage());

        Matcher matcher = config.regexPatterns.get("playerName").matcher(themisMsg);
        String playerName = matcher.find() ? matcher.group(1) : null;

        matcher = config.regexPatterns.get("hackCategory").matcher(themisMsg);
        String category = matcher.find() ? matcher.group(1) : null;

        matcher = config.regexPatterns.get("score").matcher(themisMsg);
        String score = matcher.find() ? matcher.group(1) : null;

        matcher = config.regexPatterns.get("ping").matcher(themisMsg);
        String ping = matcher.find() ? matcher.group(1) : null;

        matcher = config.regexPatterns.get("tps").matcher(themisMsg);
        String tps = matcher.find() ? matcher.group(1) : null;

        if (playerName == null || category == null || score == null || ping == null || tps == null) {
            ThemisToDiscord.instance.getLogger().severe("Themis message was not sent to discord due to lack of regex matches");
            return;
        }

        LocalDateTime currentDateTime = LocalDateTime.now();
        TemporalAccessor currentTimestamp = currentDateTime.atZone(ZoneId.systemDefault());

        WebhookEmbed embed = new WebhookEmbedBuilder()
                .setColor(config.categoryColors.getOrDefault(category, Color.GRAY).getRGB())
                .setTitle(new WebhookEmbed.EmbedTitle(category, null))
                .setDescription("Themis flagged " + playerName + " for " + category + " hacks!")
                .setAuthor(new WebhookEmbed.EmbedAuthor(playerName, null, null))
                .addField(new WebhookEmbed.EmbedField(true, "Score", score))
                .addField(new WebhookEmbed.EmbedField(true, "Ping", ping))
                .addField(new WebhookEmbed.EmbedField(true, "TPS", tps))
                .setTimestamp(currentTimestamp)
                .build();

        ThemisToDiscord.client.send(embed);
    }
}