package xyz.earthcow.themistodiscord;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import xyz.earthcow.discordwebhook.DiscordWebhook;

import java.util.Objects;

public final class ThemisToDiscord extends JavaPlugin {
    public static ThemisToDiscord instance;
    public static Configuration config;

    @Override
    public void onEnable() {
        instance = this;

        config = new Configuration();

        TtdCommand ttdCommand = new TtdCommand();
        Objects.requireNonNull(getCommand("ttd")).setExecutor(ttdCommand);
        Objects.requireNonNull(getCommand("ttd")).setTabCompleter(ttdCommand);

        getServer().getPluginManager().registerEvents(new ThemisListener(), this);

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
