package xyz.earthcow.themistodiscord;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TtdCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String cmd, @NotNull String[] args) {
        if (args.length < 1) return false;

        switch (args[0]) {
            case "url" -> {
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
                ThemisToDiscord.initializeWebhook(webhookUrl);
                sender.sendMessage(ChatColor.GREEN + "Successfully set the webhook url!");
            }
            case "test" -> {
                WebhookClient client = ThemisToDiscord.client;
                if (client == null || client.isShutdown()) {
                    sender.sendMessage(ChatColor.RED + "There is no active client. Use /ttd url <url> to specify the webhook url.");
                    return true;
                }
                WebhookEmbed embed = new WebhookEmbedBuilder()
                        .setColor(0x00FF00)
                        .setAuthor(new WebhookEmbed.EmbedAuthor(sender.getName(), null, null))
                        .setTitle(new WebhookEmbed.EmbedTitle("ThemisToDiscord Test", null))
                        .setDescription("Testing the ThemisToDiscord webhook functionality")
                        .build();

                sender.sendMessage(ChatColor.AQUA + "Sending test message...");
                client.send(embed).whenComplete((readonlyMessage, throwable) -> {
                    if (readonlyMessage == null || throwable != null) {
                        sender.sendMessage(ChatColor.RED + "Message failed to send.");
                        return;
                    }
                    sender.sendMessage(ChatColor.GREEN + "Message was sent. Id: " + readonlyMessage.getId());
                });
            }
            default -> {
                return false;
            }
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String cmd, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("url", "test");
        }
        return new ArrayList<>();
    }
}
