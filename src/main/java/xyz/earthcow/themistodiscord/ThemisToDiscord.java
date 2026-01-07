package xyz.earthcow.themistodiscord;

import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.Objects;

public final class ThemisToDiscord extends JavaPlugin {
    private Configuration config;

    @Override
    public void onEnable() {
        try {
            this.config = new Configuration(this);
        } catch (IOException e){
            log(LogLevel.ERROR, "Could not create/load plugin config, disabling! Additional info: \n" + e);
            getPluginLoader().disablePlugin(this);
            return;
        }

        TtdCommand ttdCommand = new TtdCommand(config);
        Objects.requireNonNull(getCommand("ttd")).setExecutor(ttdCommand);
        Objects.requireNonNull(getCommand("ttd")).setTabCompleter(ttdCommand);

        getServer().getPluginManager().registerEvents(new ThemisListener(this, config), this);

        int pluginId = 28743;
        Metrics metrics = new Metrics(this, pluginId);
        metrics.addCustomChart(new SimplePie("message_count", () -> String.valueOf(config.getMessages().size())));
    }

    @Override
    public void onDisable() {
        if (config != null) {
            for (Message message : config.getMessages()) {
                message.forceExecutorShutdown();
            }
        }
    }

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
