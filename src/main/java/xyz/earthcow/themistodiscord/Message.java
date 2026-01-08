package xyz.earthcow.themistodiscord;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.earthcow.discordwebhook.DiscordWebhook;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Objects;
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
    private final HandlingService handling;

    @NotNull
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Nullable
    private String originalTimestamp;

    public Message(@NotNull ThemisToDiscord ttd, @NotNull Utils utils, @NotNull Section message) {
        this.ttd = ttd;
        this.utils = utils;
        
        this.message = message;
        this.name = Objects.requireNonNull(message.getNameAsString());

        this.webhookUrl = determineWebhookUrl();

        // Determine and set the webhook json
        String jsonString = message.getString("Json", "");
        if (!jsonString.isEmpty() && !jsonString.equals("{}")) {
            this.webhookJson = jsonString;
        } else {
            this.webhookJson = getJsonWebhook();
        }

        // Define the handling service
        Section handlingSection = message.getSection("Handling", null);
        if (handlingSection != null && handlingSection.getBoolean("Enabled", false)) {
            this.handling = new HandlingService(handlingSection, name, ttd);
        } else {
            this.handling = null;
        }
    }

    private String determineWebhookUrl() {
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
        return localWebhookUrl;
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

        webhook.addEmbed(getEmbedFromConfig(embedSection));
        return webhook.getJsonString();
    }

    private DiscordWebhook.EmbedObject getEmbedFromConfig(@NotNull Section embedSection) {
        // Define the embed object
        DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject();

        // Set the color
        EmbedUtil.applyColor(embed, embedSection.getString("Color", ""), ttd, name);
        // Set the author
        EmbedUtil.applyAuthor(embed, embedSection.getSection("Author"));
        // Set the thumbnail url
        embed.setThumbnail(embedSection.getString("ThumbnailUrl"));
        // Set the title
        EmbedUtil.applyTitle(embed, embedSection.getSection("Title"));
        // Set the description
        embed.setDescription(embedSection.getString("Description"));
        // Set the fields
        EmbedUtil.applyFields(embed, embedSection.getStringList("Fields"));
        // Set the image url
        embed.setImage(embedSection.getString("ImageUrl"));
        // Set the footer
        EmbedUtil.applyFooter(embed, embedSection.getSection("Footer"));

        // Set the timestamp
        if (embedSection.getBoolean("Timestamp")) {
            TemporalAccessor ta = (new Date()).toInstant();
            embed.setTimestamp(ta);
            this.originalTimestamp = ta.toString();
        }

        return embed;
    }

    public void execute(@NotNull Player player, @NotNull String detectionType, double score, double ping, double tps, @Nullable CommandSender sender) {
        // Using a single thread executor ensures messages are not concurrently modified and sent in succession
        executor.submit(() -> {
            try {
                String jsonPayload = Objects.requireNonNull(
                        utils.handleAllPlaceholders(webhookJson, player, detectionType, score, ping, tps)
                );
                if (originalTimestamp != null) {
                    jsonPayload = jsonPayload.replace(originalTimestamp, (new Date()).toInstant().toString());
                }
                DiscordWebhook.execute(webhookUrl, jsonPayload);
                if (sender != null) {
                    sender.sendMessage(ChatColor.GREEN + "Message: " + name + ", was sent!");
                }
            } catch (IOException e) {
                String msg;
                if (e instanceof FileNotFoundException) {
                    msg = "Your webhook url is not valid! Update it with /ttd url <url>!";
                } else {
                    msg = getHttpErrorMsg(e.getMessage());
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

    @Nullable
    private String getHttpErrorMsg(@Nullable String message) {
        String msg = null;
        if (message != null && message.contains("HTTP response code:")) {
            try {
                int responseCode = Integer.parseInt(message.substring(message.indexOf(":") + 2, message.indexOf(":") + 5));
                msg = switch (responseCode) {
                    case 400 ->
                            "Error - 400 response - bad request. Verify all urls are either blank or valid urls.";
                    case 401 ->
                            "Error - 401 response - unauthorized. Verify webhook url and discord server status.";
                    case 403 ->
                            "Error - 403 response - forbidden. Verify webhook url and discord server status.";
                    case 404 ->
                            "Error - 404 response - not found. Verify webhook url and discord server status.";
                    case 429 ->
                            "Error - 429 response - too many requests. This webhook has sent too many messages in too short amount of time.";
                    case 500 ->
                            "Error - 505 response - internal server error. Discord services may be temporarily down.";
                    default -> "Error - " + responseCode + " response - unexpected error code.";
                };
            } catch (IndexOutOfBoundsException | NumberFormatException ex) {
                ttd.log(LogLevel.DEBUG, "Secondary exception: " + ex);
            }
        }
        return msg;
    }

    public @NotNull String getName() {
        return name;
    }

    public @Nullable HandlingService getHandlingService() {
        return handling;
    }

    public void forceExecutorShutdown() {
        // Should only be performed upon reload
        int unsentMessages = executor.shutdownNow().size();
        if (unsentMessages > 0) {
            ttd.log(LogLevel.WARN, unsentMessages + " messages were cancelled. For message: " + name);
        }
    }

}
