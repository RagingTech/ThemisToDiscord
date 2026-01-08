package xyz.earthcow.themistodiscord;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.earthcow.discordwebhook.DiscordWebhook;

import java.awt.*;
import java.util.List;

public class EmbedUtil {

    public static void applyColor(@NotNull DiscordWebhook.EmbedObject embed, @NotNull String color, @NotNull ThemisToDiscord ttd, @NotNull String msgName) {
        if (color.contains("%category_color%")) {
            embed.setColorStr(color);
        } else {
            try {
                embed.setColor(
                        Color.decode(color)
                );
            } catch (NumberFormatException e) {
                ttd.log(LogLevel.WARN, "Invalid color string: " + color +  " for message: " + msgName + ". Using black.");
                ttd.log(LogLevel.DEBUG, "Exception: " + e);
                embed.setColor(Color.BLACK);
            }
        }
    }

    public static void applyAuthor(@NotNull DiscordWebhook.EmbedObject embed, @Nullable Section section) {
        if (section != null) {
            embed.setAuthor(
                    section.getString("Name"),
                    section.getString("Url"),
                    section.getString("ImageUrl")
            );
        }
    }

    public static void applyTitle(@NotNull DiscordWebhook.EmbedObject embed, @Nullable Section section) {
        if (section != null) {
            embed.setTitle(section.getString("Text"));
            embed.setUrl(section.getString("Url"));
        }
    }

    public static void applyFields(@NotNull DiscordWebhook.EmbedObject embed, @NotNull List<String> fields) {
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
    }

    public static void applyFooter(@NotNull DiscordWebhook.EmbedObject embed, @Nullable Section section) {
        if (section != null) {
            embed.setFooter(
                    section.getString("Text"),
                    section.getString("IconUrl")
            );
        }
    }

}
