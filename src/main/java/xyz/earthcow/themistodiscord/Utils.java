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
    private String getPlayerOs(@NotNull Player player) {
        String os;
        if (floodgateApi == null) {
            os = "install_floodgate";
        } else {
            if (floodgateApi.isFloodgatePlayer(player.getUniqueId())) {
                os = floodgateApi.getPlayer(player.getUniqueId()).getDeviceOs().toString();
            } else {
                os = "Java";
            }
        }
        return os;
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
        return str
                .replace("%avatar_url%", config.getString("AvatarUrl", ""))
                .replace("%os%", getPlayerOs(player))
                .replace("%player_name%", player.getName())
                .replace("%player_uuid%", player.getUniqueId().toString())
                .replace("%detection_type%", detectionType)
                .replace("%category_color%", hexToInteger(config.getString("categoryColors." + detectionType)).toString())
                .replace("%score%", Double.toString(score))
                .replace("%ping%", Double.toString(ping))
                .replace("%tps%", Double.toString(tps));
    }

    private Integer hexToInteger(String hex) {
        try {
            return Integer.parseInt(hex.replace("#", ""), 16);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static boolean isInvalidWebhookUrl(@Nullable String url) {
        if (url == null) return true;
        return !DiscordWebhook.WEBHOOK_PATTERN.matcher(url).matches();
    }
}
