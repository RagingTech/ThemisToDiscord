package xyz.earthcow.themistodiscord;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.earthcow.discordwebhook.DiscordWebhook;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TtdCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String cmd, @NotNull String[] args) {
        if (args.length < 1) return false;

        switch (args[0]) {
            case "url":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "/ttd url <url>");
                    return true;
                }
                String webhookUrl = args[1];
                if (ThemisToDiscord.isInvalidWebhookUrl(webhookUrl)) {
                    sender.sendMessage(ChatColor.RED + "That is not a valid webhook url!");
                    return true;
                }
                ThemisToDiscord.config.webhookUrl = webhookUrl;
                ThemisToDiscord.instance.getConfig().set("webhookUrl", webhookUrl);
                ThemisToDiscord.instance.saveConfig();
                sender.sendMessage(ChatColor.GREEN + "Successfully set the webhook url!");
                break;
            case "test":
                DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject();
                embed
                        .setColor(Color.GREEN)
                        .setAuthor(sender.getName(), null, null)
                        .setTitle("ThemisToDiscord Test")
                        .setDescription("Testing the ThemisToDiscord webhook functionality");

                sender.sendMessage(ChatColor.AQUA + "Sending test message...");
                ThemisToDiscord.executeWebhook(embed, sender);
                break;
            case "reload":
                ThemisToDiscord.config.load();
                sender.sendMessage(ChatColor.GREEN + "Successfully reloaded the configuration file!");
                break;
            default:
                return false;
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String cmd, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("url", "test", "reload");
        }
        return new ArrayList<>();
    }
}
