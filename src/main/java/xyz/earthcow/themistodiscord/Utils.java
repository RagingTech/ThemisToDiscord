package xyz.earthcow.themistodiscord;

import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.earthcow.discordwebhook.DiscordWebhook;

public class Utils {
    @Nullable
    private FloodgateApi floodgateApi;
    @NotNull
    private final YamlDocument config;

    public Utils(@NotNull ThemisToDiscord ttd, @NotNull YamlDocument configDocument) {
        if (ttd.getServer().getPluginManager().isPluginEnabled("Floodgate")) {
            ttd.log("Found Floodgate! Enabling features...");
            floodgateApi = FloodgateApi.getInstance();
        } else {
            ttd.log(LogLevel.WARN, "Floodgate not found! Some features may be disabled.");
        }
        this.config = configDocument;
    }

    @Nullable
    public String handleFloodgatePlaceholders(@Nullable String str, @NotNull Player player) {
        if (str == null) {return null;}
        String os;
        if (floodgateApi == null) {
            os = "install_floodgate";
        } else {
            if (floodgateApi.isFloodgatePlayer(player.getUniqueId())) {
                os = floodgateApi.getPlayer(player.getUniqueId()).getDeviceOs() + "";
            } else {
                os = "Java";
            }
        }
        return str
                .replace("%os%", os);
    }

    @Nullable
    public String handleAvatarUrlPlaceholders(@Nullable String str, @NotNull Player player) {
        if (str == null) {return null;}
        return str
                .replace(
                        "%avatar_url%",
                        handlePlayerPlaceholders(config.getString("AvatarUrl"), player)
                );
    }

    @Nullable
    public static String handlePlayerPlaceholders(@Nullable String str, @NotNull Player player) {
        if (str == null) {return null;}
        return str
                .replace("%player_name%", player.getName())
                .replace("%player_uuid%", player.getUniqueId() + "");
    }

    @Nullable
    public static String handleDetectionTypePlaceholders(@Nullable String str, @NotNull String detectionType) {
        if (str == null) {return null;}
        return str
                .replace("%detection_type%", detectionType);
    }

    @Nullable
    public static String handleStatPlaceholders(@Nullable String str, double score, double ping, double tps) {
        if (str == null) {return null;}
        return str
                .replace("%score%", score + "")
                .replace("%ping%", ping + "")
                .replace("%tps%", tps + "");
    }

    @Nullable
    public String handleAllPlaceholders(
            @Nullable String str,
            @NotNull Player player,
            @NotNull String detectionType,
            double score,
            double ping,
            double tps
    ) {
        if (str == null) {return null;}
        return handleStatPlaceholders(
                handleDetectionTypePlaceholders(
                        handlePlayerPlaceholders(
                                handleAvatarUrlPlaceholders(
                                    handleFloodgatePlaceholders(
                                        str, player
                                    ),
                                    player
                                ),
                                player
                        ),
                        detectionType
                ),
                score, ping, tps
        );
    }

    public static boolean isInvalidWebhookUrl(@Nullable String url) {
        if (url == null) return true;
        return !DiscordWebhook.WEBHOOK_PATTERN.matcher(url).matches();
    }
}
