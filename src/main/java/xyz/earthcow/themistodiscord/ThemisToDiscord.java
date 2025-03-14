package xyz.earthcow.themistodiscord;

import org.bukkit.plugin.java.JavaPlugin;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.Nullable;
import xyz.earthcow.discordwebhook.DiscordWebhook;

import java.util.Objects;

public final class ThemisToDiscord extends JavaPlugin {
    public static ThemisToDiscord instance;
    public static Configuration config;
    public static FloodgateApi floodgateApi;

    @Override
    public void onEnable() {
        instance = this;

        config = new Configuration();

        TtdCommand ttdCommand = new TtdCommand();
        Objects.requireNonNull(getCommand("ttd")).setExecutor(ttdCommand);
        Objects.requireNonNull(getCommand("ttd")).setTabCompleter(ttdCommand);

        getServer().getPluginManager().registerEvents(new ThemisListener(), this);

        if (getServer().getPluginManager().isPluginEnabled("Floodgate")) {
            log("Found Floodgate! Enabling features...");
            floodgateApi = FloodgateApi.getInstance();
        } else {
            log(LogLevel.WARN, "Floodgate not found! Some features may be disabled.");
        }
    }

    @Override
    public void onDisable() {}

    public static void log(String message) {
        instance.getLogger().info(message);
    }

    public static void log(LogLevel logLevel, String message) {
        switch (logLevel) {
            case DEBUG:
                if (config.get().getBoolean("debug")) {
                    instance.getLogger().warning("[DEBUG] " + message);
                }
            case WARN:
                instance.getLogger().warning(message);
                break;
            case ERROR:
                instance.getLogger().severe(message);
                break;
            default:
                log(message);
                break;
        }
    }

    public static boolean isInvalidWebhookUrl(@Nullable String url) {
        if (url == null) return true;
        return !DiscordWebhook.WEBHOOK_PATTERN.matcher(url).matches();
    }
}
