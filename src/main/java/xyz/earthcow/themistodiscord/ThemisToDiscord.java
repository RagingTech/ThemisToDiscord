package xyz.earthcow.themistodiscord;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class ThemisToDiscord extends JavaPlugin {
    private Configuration config;

    @Override
    public void onEnable() {
        this.config = new Configuration(this);

        TtdCommand ttdCommand = new TtdCommand(config);
        Objects.requireNonNull(getCommand("ttd")).setExecutor(ttdCommand);
        Objects.requireNonNull(getCommand("ttd")).setTabCompleter(ttdCommand);

        getServer().getPluginManager().registerEvents(new ThemisListener(this, config), this);
    }

    @Override
    public void onDisable() {}

    public void log(String message) {
        getLogger().info(message);
    }

    public void log(LogLevel logLevel, String message) {
        switch (logLevel) {
            case DEBUG:
                if (config.get().getBoolean("debug")) {
                    getLogger().warning("[DEBUG] " + message);
                }
                break;
            case WARN:
                getLogger().warning(message);
                break;
            case ERROR:
                getLogger().severe(message);
                break;
            default:
                log(message);
                break;
        }
    }

}
