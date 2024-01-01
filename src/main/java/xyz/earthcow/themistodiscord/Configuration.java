package xyz.earthcow.themistodiscord;

import org.bukkit.configuration.file.FileConfiguration;

import java.awt.*;
import java.util.HashMap;
import java.util.Objects;

public class Configuration {
    public String webhookUrl;

    public double executionThreshold;
    public double repetitionThreshold;
    public double repetitionDelay;
    public final HashMap<String, Color> categoryColors = new HashMap<>();

    public Configuration() {
        ThemisToDiscord.instance.saveDefaultConfig();
        FileConfiguration fileConfig = ThemisToDiscord.instance.getConfig();
        fileConfig.addDefault("execution-threshold", 10.0);
        fileConfig.addDefault("repetition-threshold", 5.0);
        fileConfig.addDefault("repetition-delay", 10.0);

        fileConfig.addDefault("categoryColors.Boat Movement", "#875229");
        fileConfig.addDefault("categoryColors.Flight / Y-Movement", "#3276bf");
        fileConfig.addDefault("categoryColors.Speed", "#d6e600");
        fileConfig.addDefault("categoryColors.Spoofed Packets", "#ff000");
        fileConfig.addDefault("categoryColors.Timer / Blink", "#d61ad6");
        fileConfig.addDefault("categoryColors.Reach", "#6f1ad6");
        fileConfig.addDefault("categoryColors.Elytra Flight", "#afaeb0");
        fileConfig.addDefault("categoryColors.Illegal Packets", "#141414");

        fileConfig.options().copyDefaults(true);
        ThemisToDiscord.instance.saveConfig();

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

        executionThreshold = fileConfig.getDouble("execution-threshold");
        repetitionThreshold = fileConfig.getDouble("repetition-threshold");
        repetitionDelay = fileConfig.getDouble("repetition-delay");

        categoryColors.clear();
        try {
            categoryColors.put("Boat Movement", Color.decode(Objects.requireNonNull(fileConfig.getString("categoryColors.Boat Movement"))));
            categoryColors.put("Flight / Y-Movement", Color.decode(Objects.requireNonNull(fileConfig.getString("categoryColors.Boat Movement"))));
            categoryColors.put("Speed", Color.decode(Objects.requireNonNull(fileConfig.getString("categoryColors.Speed"))));
            categoryColors.put("Spoofed Packets", Color.decode(Objects.requireNonNull(fileConfig.getString("categoryColors.Spoofed Packets"))));
            categoryColors.put("Timer / Blink", Color.decode(Objects.requireNonNull(fileConfig.getString("categoryColors.Timer / Blink"))));
            categoryColors.put("Reach", Color.decode(Objects.requireNonNull(fileConfig.getString("categoryColors.Reach"))));
            categoryColors.put("Elytra Flight", Color.decode(Objects.requireNonNull(fileConfig.getString("categoryColors.Elytra Flight"))));
            categoryColors.put("Illegal Packets", Color.decode(Objects.requireNonNull(fileConfig.getString("categoryColors.Illegal Packets"))));
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
            categoryColors.put("Illegal Packets", Color.decode("#141414"));
        }
    }
}
