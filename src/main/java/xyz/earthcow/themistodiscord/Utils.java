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

    @NotNull
    private String handleFloodgatePlaceholders(@NotNull String str, @NotNull Player player) {
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
    public String handleAllPlaceholders(
            @Nullable String str,
            @NotNull Player player,
            @NotNull String detectionType,
            double score,
            double ping,
            double tps
    ) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return handleFloodgatePlaceholders(str, player)
                .replace("%avatar_url%", config.getString("AvatarUrl"))
                .replace("%player_name%", player.getName())
                .replace("%player_uuid%", player.getUniqueId() + "")
                .replace("%detection_type%", detectionType)
                .replace("%score%", score + "")
                .replace("%ping%", ping + "")
                .replace("%tps%", tps + "");
    }

    public static boolean isInvalidWebhookUrl(@Nullable String url) {
        if (url == null) return true;
        return !DiscordWebhook.WEBHOOK_PATTERN.matcher(url).matches();
    }
}
