package xyz.earthcow.themistodiscord;

import com.gmail.olexorus.themis.api.CheckType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
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
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "/ttd msg <message> <player:player_name> <type:detection_type> [score:score] [ping:ping] [tps:tps]");
                    return true;
                }

                Message message = ThemisToDiscord.config.getMessages().stream().filter(msg -> msg.getName().equals(args[1])).findFirst().orElse(null);
                if (message == null) {
                    sender.sendMessage(ChatColor.RED + "The message: " + args[1] + " does not exist!");
                    return true;
                }

                // Rebuild full command to properly extract everything
                String fullCommand = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                Map<String, String> parsedArgs = parseArguments(fullCommand);

                String playerName = parsedArgs.get("player");
                String type = parsedArgs.get("type");
                double score = parseDouble(parsedArgs.get("score"));
                double ping = parseDouble(parsedArgs.get("ping"));
                double tps = parseDouble(parsedArgs.get("tps"));

                if (playerName == null || type == null) {
                    sender.sendMessage(ChatColor.RED + "/ttd msg <message> <player:player_name> <type:detection_type> [score:score] [ping:ping] [tps:tps]");
                    return true;
                }

                Player player = Bukkit.getPlayer(playerName);
                if (player == null || !player.isOnline()) {
                    sender.sendMessage(ChatColor.RED + "The player: " + playerName + " is not online or does not exist!");
                    return true;
                }

                if (Arrays.stream(CheckType.values()).map(CheckType::getDescription).noneMatch(desc -> desc.equals(type))) {
                    sender.sendMessage(ChatColor.RED + "The detection type: " + type + " does not exist!");
                    return true;
                }

                message.execute(player, type, score, ping, tps, (sender instanceof Player) ? sender : null);
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

    private Map<String, String> parseArguments(String input) {
        Map<String, String> result = new HashMap<>();
        String[] parts = input.split(" ");
        String key = null;
        StringBuilder value = new StringBuilder();

        for (String part : parts) {
            if (part.contains(":")) {
                // If we already have a key-value pair, store it
                if (key != null) {
                    result.put(key, value.toString().trim());
                }

                // Extract the key and start a new value
                String[] keyValue = part.split(":", 2);
                key = keyValue[0].toLowerCase();
                value = new StringBuilder(keyValue.length > 1 ? keyValue[1] : "");
            } else if (key != null) {
                // Append additional words to the value
                value.append(" ").append(part);
            }
        }

        // Store the last key-value pair
        if (key != null) {
            result.put(key, value.toString().trim());
        }

        return result;
    }

    private double parseDouble(String value) {
        try {
            return value != null ? Double.parseDouble(value) : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("url", "msg", "reload");
        }

        if (args[0].equalsIgnoreCase("msg")) {
            if (args.length == 2) {
                return ThemisToDiscord.config.getMessages().stream().map(Message::getName).collect(Collectors.toList());
            }

            List<String> completions = new ArrayList<>();
            boolean hasPlayer = false, hasType = false, hasScore = false, hasPing = false, hasTps = false;

            for (String arg : args) {
                if (arg.startsWith("player:")) hasPlayer = true;
                if (arg.startsWith("type:")) hasType = true;
                if (arg.startsWith("score:")) hasScore = true;
                if (arg.startsWith("ping:")) hasPing = true;
                if (arg.startsWith("tps:")) hasTps = true;
            }

            if (!hasPlayer) {
                completions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(player -> "player:" + player.getName())
                    .collect(Collectors.toList()));
            } else if (!hasType) {
                completions.addAll(Arrays.stream(CheckType.values()).map(checkType -> "type:" + checkType.getDescription()).collect(Collectors.toList()));
            } else {
                if (!hasScore) completions.add("score:");
                if (!hasPing) completions.add("ping:");
                if (!hasTps) completions.add("tps:");
            }

            return completions.stream()
                .filter(c -> c.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
