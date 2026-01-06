package xyz.earthcow.themistodiscord;

import com.gmail.olexorus.themis.api.CheckType;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.earthcow.discordwebhook.DiscordWebhook;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Message {
    @NotNull
    private final ThemisToDiscord ttd;
    @NotNull
    private final Utils utils;
    
    @NotNull
    private final Section message;
    @NotNull
    private final String name;
    @NotNull
    private final String webhookUrl;
    @NotNull
    private final String webhookJson;

    @Nullable
    private final Section handling;

    // For use with handling
    private final HashMap<UUID, HashMap<CheckType, Long>> lastSentTimesPerPlayer = new HashMap<>();
    private final HashMap<UUID, HashMap<CheckType, Integer>> repetitionCountersPerPlayer = new HashMap<>();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public Message(@NotNull ThemisToDiscord ttd, @NotNull Utils utils, @NotNull Section message) {
        this.ttd = ttd;
        this.utils = utils;
        
        this.message = message;
        this.name = message.getNameAsString();

        // Discover the webhook url to be used
        String localWebhookUrl;
        if (message.getBoolean("CustomWebhook.Enabled", false)) {
            localWebhookUrl = message.getString("CustomWebhook.Url", "");
            if (Utils.isInvalidWebhookUrl(localWebhookUrl)) {
                if (!localWebhookUrl.isEmpty()) {
                    ttd.log(LogLevel.WARN,  "Invalid custom webhook url for message: " + name + "! This message will use the global webhook url.");
                }
                localWebhookUrl = message.getRoot().getString("webhookUrl");
            }
        } else {
            localWebhookUrl = message.getRoot().getString("webhookUrl");
        }
        // Set the webhook url for this message
        this.webhookUrl = localWebhookUrl;

        // Determine and set the webhook json
        String jsonString = message.getString("Json", "");
        if (!jsonString.isEmpty() && !jsonString.equals("{}")) {
            this.webhookJson = jsonString;
        } else {
            this.webhookJson = getJsonWebhook();
        }

        // Define the handling section
        this.handling = message.getSection("Handling", null);

    }

    private String getJsonWebhook() {
        DiscordWebhook webhook = new DiscordWebhook();

        // Set the custom webhook parameters
        if (message.getBoolean("CustomWebhook.Enabled", false)) {
            webhook.setUsername(message.getString("CustomWebhook.Name"));
            webhook.setAvatarUrl(message.getString("CustomWebhook.AvatarUrl"));
        }

        // Set the message content
        webhook.setContent(message.getString("Content"));

        Section embedSection = message.getSection("Embed");
        if (embedSection == null) {
            return webhook.getJsonString();
        }

        // Define the embed object
        DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject();

        // Set the color
        String colorStr = embedSection.getString("Color", "");
        if (colorStr.contains("%category_color%")) {
            embed.setColorStr(colorStr);
        } else {
            try {
                embed.setColor(
                    Color.decode(colorStr)
                );
            } catch (NumberFormatException e) {
                ttd.log(LogLevel.WARN, "Invalid color string: " + colorStr +  " for message: " + name + ". Using black.");
                ttd.log(LogLevel.DEBUG, "Exception: " + e);
                embed.setColor(Color.BLACK);
            }
        }

        // Set the author
        if (embedSection.get("Author", null) != null) {
            embed.setAuthor(
                embedSection.getString("Author.Name"),
                embedSection.getString("Author.Url"),
                embedSection.getString("Author.ImageUrl")
            );
        }

        // Set the thumbnail url
        embed.setThumbnail(embedSection.getString("ThumbnailUrl", null));

        // Set the title
        if (embedSection.get("Title", null) != null) {
            embed.setTitle(embedSection.getString("Title.Text", null));
            embed.setUrl(embedSection.getString("Title.Url", null));
        }

        // Set the description
        embed.setDescription(embedSection.getString("Description", null));

        // Set the fields
        List<String> fields = embedSection.getStringList("Fields");
        if (!fields.isEmpty()) {

            for (String field : fields) {
                if (field.contains(";")) {

                    String[] parts = field.split(";");
                    if (parts.length < 2) {
                        continue;
                    }

                    boolean inline = parts.length < 3 || Boolean.parseBoolean(parts[2]);

                    embed.addField(parts[0], parts[1], inline);
                } else {
                    boolean inline = Boolean.parseBoolean(field);
                    embed.addField("\u200e", "\u200e", inline);
                }
            }

        }

        // Set the image url
        embed.setImage(embedSection.getString("ImageUrl", null));

        // Set the footer
        if (embedSection.get("Footer", null) != null) {
            embed.setFooter(
                embedSection.getString("Footer.Text", null),
                embedSection.getString("Footer.IconUrl", null)
            );
        }

        // Set the timestamp
        if (embedSection.getBoolean("Timestamp")) {
            embed.setTimestamp((new Date()).toInstant());
        }

        webhook.addEmbed(embed);
        return webhook.getJsonString();
    }

    public void execute(@NotNull Player player, @NotNull String detectionType, double score, double ping, double tps, @Nullable CommandSender sender) {
        // Using a single thread executor ensures messages are not concurrently modified and sent in succession
        executor.submit(() -> {
            try {
                DiscordWebhook.execute(webhookUrl, utils.handleAllPlaceholders(webhookJson, player, detectionType, score, ping, tps));
                if (sender != null) {
                    sender.sendMessage(ChatColor.GREEN + "Message: " + name + ", was sent!");
                }
            } catch (IOException e) {
                String msg = null;
                if (e instanceof FileNotFoundException) {
                    msg = "Your webhook url is not valid! Update it with /ttd url <url>!";
                } else {
                    String message = e.getMessage();
                    if (message != null && message.contains("HTTP response code:")) {
                        try {
                            int responseCode = Integer.parseInt(message.substring(message.indexOf(":") + 2, message.indexOf(":") + 5));
                            switch (responseCode) {
                                case 400:
                                    msg = "Error - 400 response - bad request. Verify all urls are either blank or valid urls.";
                                    break;
                                case 401:
                                    msg = "Error - 401 response - unauthorized. Verify webhook url and discord server status.";
                                    break;
                                case 403:
                                    msg = "Error - 403 response - forbidden. Verify webhook url and discord server status.";
                                    break;
                                case 404:
                                    msg = "Error - 404 response - not found. Verify webhook url and discord server status.";
                                    break;
                                case 429:
                                    msg = "Error - 429 response - too many requests. This webhook has sent too many messages in too short amount of time.";
                                    break;
                                case 500:
                                    msg = "Error - 505 response - internal server error. Discord services may be temporarily down.";
                                    break;
                                default:
                                    msg = "Error - " + responseCode + " response - unexpected error code.";
                                    break;
                            }
                        } catch (IndexOutOfBoundsException | NumberFormatException ex) {
                            ttd.log(LogLevel.DEBUG, "Secondary exception: " + ex);
                        }
                    }
                }
                if (msg == null) {
                    msg = "Unknown error has occurred. Please make a bug report at https://github.com/RagingTech/ThemisToDiscord/issues.";
                }
                msg = msg + " For message: " + name;
                if (sender != null) {
                    sender.sendMessage(ChatColor.RED + msg);
                }
                ttd.log(LogLevel.ERROR, msg);
                ttd.log(LogLevel.DEBUG, "Exception: " + e);
                ttd.log(LogLevel.DEBUG, "Webhook: " + webhookJson);
            }
        });
    }

    public @NotNull String getName() {
        return name;
    }

    public @Nullable Section getHandling() {
        return handling;
    }

    public void forceExecutorShutdown() {
        // Should only be performed upon reload
        int unsentMessages = executor.shutdownNow().size();
        if (unsentMessages > 0) {
            ttd.log(LogLevel.WARN, unsentMessages + " messages were cancelled. For message: " + name);
        }
    }

    public long getLastSentTimeForPlayer(Player player, CheckType checkType) {
        HashMap<CheckType, Long> times = lastSentTimesPerPlayer.get(player.getUniqueId());
        if (times == null) {
            return 0;
        }
        return times.getOrDefault(checkType, 0L);
    }

    public void updateLastSentTimeForPlayer(Player player, CheckType checkType) {
        HashMap<CheckType, Long> times = lastSentTimesPerPlayer.getOrDefault(player.getUniqueId(), new HashMap<>());
        times.put(checkType, System.currentTimeMillis());
        lastSentTimesPerPlayer.put(player.getUniqueId(), times);
    }

    public int getRepetitionCountForPlayer(Player player, CheckType checkType) {
        HashMap<CheckType, Integer> repetitionCounts = repetitionCountersPerPlayer.get(player.getUniqueId());
        if (repetitionCounts == null) {
            return -2;
        }
        return repetitionCounts.getOrDefault(checkType, -2);
    }

    public void putRepetitionCountForPlayer(Player player, CheckType checkType, int repetitionCount) {
        HashMap<CheckType, Integer> repetitionCounts = repetitionCountersPerPlayer.getOrDefault(player.getUniqueId(), new HashMap<>());
        repetitionCounts.put(checkType, repetitionCount);
        repetitionCountersPerPlayer.put(player.getUniqueId(), repetitionCounts);
    }

}
