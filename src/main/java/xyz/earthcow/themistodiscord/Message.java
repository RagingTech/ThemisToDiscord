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

public class Message {
    @NotNull
    private final Section message;
    @NotNull
    private final String name;
    @NotNull
    private final String webhookUrl;

    @NotNull
    private DiscordWebhook webhook;

    @Nullable
    private final Section handling;

    // For use with handling
    private final HashMap<UUID, HashMap<CheckType, Long>> lastSentTimesPerPlayer = new HashMap<>();
    private final HashMap<UUID, HashMap<CheckType, Integer>> repetitionCountersPerPlayer = new HashMap<>();

    public Message(@NotNull Section message) {
        this.message = message;
        this.name = message.getNameAsString();

        // Discover the webhook url to be used
        String localWebhookUrl;
        if (message.getBoolean("CustomWebhook.Enabled", false)) {
            localWebhookUrl = message.getString("CustomWebhook.Url", "");
            if (ThemisToDiscord.isInvalidWebhookUrl(localWebhookUrl)) {
                if (!localWebhookUrl.isEmpty()) {
                    ThemisToDiscord.log(LogLevel.WARN,  "Invalid custom webhook url for message: " + name + "! This message will use the global webhook url.");
                }
                localWebhookUrl = message.getRoot().getString("webhookUrl");
            }
        } else {
            localWebhookUrl = message.getRoot().getString("webhookUrl");
        }
        // Set the webhook url for this message
        this.webhookUrl = localWebhookUrl;
        this.webhook = new DiscordWebhook(localWebhookUrl);

        // Define the handling section
        this.handling = message.getSection("Handling", null);

    }

    private void handleMessageContent(
        @NotNull Player player,
        @NotNull String detectionType,
        double score,
        double ping,
        double tps
    ) {
        // Define a new webhook object to clear previous contents (mostly for embeds)
        this.webhook = new DiscordWebhook(webhookUrl);

        // Set the custom webhook parameters
        if (message.getBoolean("CustomWebhook.Enabled", false)) {
            webhook.setUsername(
                Utils.handleAllPlaceholders(
                    message.getString("CustomWebhook.Name"),
                    player, detectionType, score, ping, tps)
            );
            webhook.setAvatarUrl(
                Utils.handleAllPlaceholders(
                    message.getString("CustomWebhook.AvatarUrl"),
                    player, detectionType, score, ping, tps)
            );
        }

        // Set the message content
        webhook.setContent(
            Utils.handleAllPlaceholders(
                message.getString("Content"),
                player, detectionType, score, ping, tps
            )
        );

        Section embedSection = message.getSection("Embed");
        if (embedSection == null) {
            return;
        }

        // Define the embed object
        DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject();

        // Set the color
        String colorStr = embedSection.getString("Color").replaceAll("%category_color%", message.getRoot().getString("categoryColors." + detectionType));
        try {
            embed.setColor(
                Color.decode(colorStr)
            );
        } catch (NumberFormatException e) {
            ThemisToDiscord.log(LogLevel.WARN, "Invalid color string: " + colorStr +  " for message: " + name + ". Using black.");
            ThemisToDiscord.log(LogLevel.DEBUG, "Exception: " + e);
            embed.setColor(Color.BLACK);
        }

        // Set the author
        if (embedSection.get("Author", null) != null) {
            embed.setAuthor(
                Utils.handleAllPlaceholders(
                    embedSection.getString("Author.Name"),
                    player, detectionType, score, ping, tps
                ),
                Utils.handleAllPlaceholders(
                    embedSection.getString("Author.Url"),
                    player, detectionType, score, ping, tps
                ),
                Utils.handleAllPlaceholders(
                    embedSection.getString("Author.ImageUrl"),
                    player, detectionType, score, ping, tps
                )
            );
        }

        // Set the thumbnail url
        embed.setThumbnail(embedSection.getString("ThumbnailUrl", null));

        // Set the title
        if (embedSection.get("Title", null) != null) {
            embed.setTitle(
                Utils.handleAllPlaceholders(
                    embedSection.getString("Title.Text", null),
                    player, detectionType, score, ping, tps
                )
            );
            embed.setUrl(
                Utils.handleAllPlaceholders(
                    embedSection.getString("Title.Url", null),
                    player, detectionType, score, ping, tps
                )
            );
        }

        // Set the description
        embed.setDescription(
            Utils.handleAllPlaceholders(
                embedSection.getString("Description", null),
                player, detectionType, score, ping, tps
            )
        );

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

                    embed.addField(
                        Utils.handleAllPlaceholders(
                            parts[0],
                            player, detectionType, score, ping, tps
                        ),
                        Utils.handleAllPlaceholders(
                            parts[1],
                            player, detectionType, score, ping, tps
                        ),
                        inline
                    );
                } else {
                    boolean inline = Boolean.parseBoolean(field);
                    embed.addField("\u200e", "\u200e", inline);
                }
            }

        }

        // Set the image url
        embed.setImage(
            Utils.handleAllPlaceholders(
                embedSection.getString("ImageUrl", null),
                player, detectionType, score, ping, tps
            )
        );

        // Set the footer
        if (embedSection.get("Footer", null) != null) {
            embed.setFooter(
                Utils.handleAllPlaceholders(
                    embedSection.getString("Footer.Text", null),
                    player, detectionType, score, ping, tps
                ),
                Utils.handleAllPlaceholders(
                    embedSection.getString("Footer.IconUrl", null),
                    player, detectionType, score, ping, tps
                )
            );
        }

        // Set the timestamp
        if (embedSection.getBoolean("Timestamp")) {
            embed.setTimestamp((new Date()).toInstant());
        }

        webhook.addEmbed(embed);
    }

    public void execute(@NotNull Player player, @NotNull String detectionType, double score, double ping, double tps, @Nullable CommandSender sender) {
        ThemisToDiscord.instance.getServer().getScheduler().runTaskAsynchronously(ThemisToDiscord.instance, () -> {
            handleMessageContent(player, detectionType, score, ping, tps);
            try {
                webhook.execute();
                if (sender != null) {
                    sender.sendMessage(ChatColor.GREEN + "Message: " + name + ", was sent!");
                }
            } catch (IOException e) {
                String msg;
                if (e instanceof FileNotFoundException) {
                    msg = "Your webhook url is not valid! Update it with /ttd url <url>!";
                } else {
                    String message = e.getMessage();
                    if (message != null && message.contains("HTTP response code:")) {
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
                    } else {
                        msg = "Unknown error has occurred. Please make a bug report at https://github.com/RagingTech/ThemisToDiscord/issues.";
                    }
                }
                msg = msg + " For message: " + name;
                if (sender != null) {
                    sender.sendMessage(ChatColor.RED + msg);
                }
                ThemisToDiscord.log(LogLevel.ERROR, msg);
                ThemisToDiscord.log(LogLevel.DEBUG, "Exception: " + e);
                ThemisToDiscord.log(LogLevel.DEBUG, "Webhook: " + webhook.getJsonString());
            }
        });
    }

    public @NotNull String getName() {
        return name;
    }

    public @Nullable Section getHandling() {
        return handling;
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
