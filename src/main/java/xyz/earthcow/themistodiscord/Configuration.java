package xyz.earthcow.themistodiscord;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class Configuration {
    private final ThemisToDiscord plugin;
    private YamlDocument config;

    public Configuration() {
        this.plugin = ThemisToDiscord.instance;

        try {
            config = YamlDocument.create(
                    new File(plugin.getDataFolder(), "config.yml"),
                    Objects.requireNonNull(plugin.getResource("config.yml")),
                    GeneralSettings.DEFAULT,
                    LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT,
                    UpdaterSettings.builder().setVersioning(new BasicVersioning("config-version"))
                            .setOptionSorting(UpdaterSettings.OptionSorting.SORT_BY_DEFAULTS)
                            .build()
            );

            config.update();
            config.save();
        } catch (IOException e){
            ThemisToDiscord.log(LogLevel.ERROR, "Could not create/load plugin config, disabling! Additional info: \n" + e);
            plugin.getPluginLoader().disablePlugin(plugin);
            return;
        }

        if (ThemisToDiscord.isInvalidWebhookUrl(config.getString("webhookUrl"))) {
            ThemisToDiscord.log(LogLevel.WARN, "Webhook url is missing or invalid! Set one using /ttd url <url>");
        }
    }

    public YamlDocument get() {
        return config;
    }

    public void save() {
        try {
            config.save();
        } catch (IOException e) {
            ThemisToDiscord.log(LogLevel.ERROR, "Failed to save plugin config! Additional info: \n" + e);
        }
    }

    public void reload() {
        try {
            config.reload();
            if (ThemisToDiscord.isInvalidWebhookUrl(config.getString("webhookUrl"))) {
                ThemisToDiscord.log(LogLevel.WARN, "Webhook url is missing or invalid! Set one using /ttd url <url>");
            }
        } catch (IOException e) {
            ThemisToDiscord.log(LogLevel.ERROR, "Failed to reload plugin config! Additional info: \n" + e);
        }
    }

}
