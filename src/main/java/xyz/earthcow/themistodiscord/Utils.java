package xyz.earthcow.themistodiscord;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Utils {

    @Nullable
    public static String handleFloodgatePlaceholders(@Nullable String str, @NotNull Player player) {
        if (str == null) {return null;}
        String os;
        if (ThemisToDiscord.floodgateApi == null) {
            os = "install_floodgate";
        } else {
            if (ThemisToDiscord.floodgateApi.isFloodgatePlayer(player.getUniqueId())) {
                os = ThemisToDiscord.floodgateApi.getPlayer(player.getUniqueId()).getDeviceOs() + "";
            } else {
                os = "Java";
            }
        }
        return str
                .replaceAll("%os%", os);
    }

    @Nullable
    public static String handleAvatarUrlPlaceholders(@Nullable String str, @NotNull Player player) {
        if (str == null) {return null;}
        return str
                .replaceAll(
                        "%avatar_url%",
                        handlePlayerPlaceholders(ThemisToDiscord.config.get().getString("AvatarUrl"), player)
                );
    }

    @Nullable
    public static String handlePlayerPlaceholders(@Nullable String str, @NotNull Player player) {
        if (str == null) {return null;}
        return str
                .replaceAll("%player_name%", player.getName())
                .replaceAll("%player_uuid%", player.getUniqueId() + "");
    }

    @Nullable
    public static String handleDetectionTypePlaceholders(@Nullable String str, @NotNull String detectionType) {
        if (str == null) {return null;}
        return str
                .replaceAll("%detection_type%", detectionType);
    }

    @Nullable
    public static String handleStatPlaceholders(@Nullable String str, double score, double ping, double tps) {
        if (str == null) {return null;}
        return str
                .replaceAll("%score%", score + "")
                .replaceAll("%ping%", ping + "")
                .replaceAll("%tps%", tps + "");
    }

    @Nullable
    public static String handleAllPlaceholders(
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

}
