package xyz.earthcow.themistodiscord;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.earthcow.discordwebhook.DiscordWebhook;

import java.io.IOException;
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

    public static boolean isInvalidWebhookUrl(@Nullable String url) {
        if (url == null) return true;
        return !DiscordWebhook.WEBHOOK_PATTERN.matcher(url).matches();
    }

    public static void executeWebhook(@NotNull DiscordWebhook.EmbedObject embed, @Nullable CommandSender sender) {
        if (isInvalidWebhookUrl(config.webhookUrl)) {
            if (sender != null) {
                sender.sendMessage(ChatColor.RED + "There is a problem with your configuration! Verify the webhook url and all config values.");
            }
            instance.getLogger().warning("There is a problem with your configuration! Verify the webhook url and all config values.");
            return;
        }

        DiscordWebhook webhook = new DiscordWebhook(config.webhookUrl);

        webhook.addEmbed(embed);

        instance.getServer().getScheduler().runTaskAsynchronously(instance, () -> {
            try {
                webhook.execute();
                if (sender != null) {
                    sender.sendMessage(ChatColor.GREEN + "Message was sent!");
                }
            } catch (IOException e) {
                if (sender != null) {
                    sender.sendMessage(ChatColor.RED + "There is a problem with your configuration! Verify the webhook url and all config values.");
                }
                instance.getLogger().warning("There is a problem with your configuration! Verify the webhook url and all config values.");
            }
        });
    }
}
