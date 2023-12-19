package xyz.earthcow.themistodiscord;

import org.bukkit.configuration.file.FileConfiguration;

import java.awt.*;
import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Pattern;

public class Configuration {
    public String webhookUrl;

    public final HashMap<String, Color> categoryColors = new HashMap<>();
    public final HashMap<String, Pattern> regexPatterns = new HashMap<>();

    public Configuration() {
        ThemisToDiscord.instance.saveDefaultConfig();
        load();
    }
    public void load() {
        ThemisToDiscord.instance.reloadConfig();
        FileConfiguration fileConfig = ThemisToDiscord.instance.getConfig();

        webhookUrl = fileConfig.getString("webhookUrl");

        if (ThemisToDiscord.isInvalidWebhookUrl(webhookUrl)) {
            ThemisToDiscord.instance.getLogger().warning("Webhook url is missing or invalid! Set one using /ttd url <url>");
            return;
        }

        ThemisToDiscord.initializeWebhook(webhookUrl);

        categoryColors.clear();
        try {
            categoryColors.put("Boat Movement", Color.decode(Objects.requireNonNull(fileConfig.getString("categoryColors.Boat Movement"))));
            categoryColors.put("Flight / Y-Movement", Color.decode(Objects.requireNonNull(fileConfig.getString("categoryColors.Boat Movement"))));
            categoryColors.put("Speed", Color.decode(Objects.requireNonNull(fileConfig.getString("categoryColors.Speed"))));
            categoryColors.put("Spoofed Packets", Color.decode(Objects.requireNonNull(fileConfig.getString("categoryColors.Spoofed Packets"))));
            categoryColors.put("Timer / Blink", Color.decode(Objects.requireNonNull(fileConfig.getString("categoryColors.Timer / Blink"))));
            categoryColors.put("Reach", Color.decode(Objects.requireNonNull(fileConfig.getString("categoryColors.Reach"))));
            categoryColors.put("Elytra Flight", Color.decode(Objects.requireNonNull(fileConfig.getString("categoryColors.Elytra Flight"))));
        } catch (Exception ignored) {
            ThemisToDiscord.instance.getLogger().warning("Invalid colors in the config file. Using default values.");
            categoryColors.clear();
            categoryColors.put("Boat Movement", Color.decode("#875229"));
            categoryColors.put("Flight / Y-Movement", Color.decode("#3276bf"));
            categoryColors.put("Speed", Color.decode("#d6e600"));
            categoryColors.put("Spoofed Packets", Color.decode("#ff0000"));
            categoryColors.put("Timer / Blink", Color.decode("#d61ad6"));
            categoryColors.put("Reach", Color.decode("#6f1ad6"));
            categoryColors.put("Elytra Flight", Color.decode("#afaeb0"));
        }

        regexPatterns.clear();
        try {
            regexPatterns.put("playerName", Pattern.compile(Objects.requireNonNull(fileConfig.getString("regex.playerName"))));
            regexPatterns.put("hackCategory", Pattern.compile(Objects.requireNonNull(fileConfig.getString("regex.hackCategory"))));
            regexPatterns.put("score", Pattern.compile(Objects.requireNonNull(fileConfig.getString("regex.score"))));
            regexPatterns.put("ping", Pattern.compile(Objects.requireNonNull(fileConfig.getString("regex.ping"))));
            regexPatterns.put("tps", Pattern.compile(Objects.requireNonNull(fileConfig.getString("regex.tps"))));
        } catch (Exception ignored) {
            ThemisToDiscord.instance.getLogger().warning("Invalid regex in the config file. Using default values.");
            regexPatterns.clear();
            regexPatterns.put("playerName", Pattern.compile("\\[Themis\\] (.*?) was"));
            regexPatterns.put("hackCategory", Pattern.compile("for (.*?) hacks"));
            regexPatterns.put("score", Pattern.compile("Score: (.*?) \\|"));
            regexPatterns.put("ping", Pattern.compile("Ping: (.*?) \\|"));
            regexPatterns.put("tps", Pattern.compile("TPS: (.*?)\\]"));
        }
    }
}
