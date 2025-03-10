package xyz.earthcow.themistodiscord;

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
import java.util.stream.Collectors;

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
                ThemisToDiscord.config.get().set("webhookUrl", webhookUrl);
                ThemisToDiscord.config.save();
                sender.sendMessage(ChatColor.GREEN + "Successfully set the webhook url!");
                break;
            case "msg":
                // TODO: Implement /ttd msg

                break;
            case "reload":
                ThemisToDiscord.config.reload();
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
            return Arrays.asList("url", "msg", "reload");
        } else if (args.length >= 2 && args[0].equals("msg")) {
            if (args.length == 2) {
                return ThemisToDiscord.config.getMessages().stream().map(Message::getName).collect(Collectors.toList());
            } else if (args.length <= 4) {
                return Arrays.asList("player:", "type:");
            } else {
                return Arrays.asList("score:", "ping:", "tps:");
            }
        }
        return new ArrayList<>();
    }
}
