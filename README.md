# ThemisToDiscord

Sends an embedded Themis notification to a discord webhook.

## Installation

Depends on [Themis](https://www.spigotmc.org/resources/themis-anti-cheat-1-17-1-20-bedrock-support-paper-compatibility-free-optimized.90766/)
1. Place the ThemisToDiscord.jar file in your /plugins folder.
2. Start or restart the server
3. Run `/ttd url <webhookUrl>` (replace `<webhookUrl>` with your webhook url)
4. Optionally, run `/ttd test` to ensure the setup is functioning

Alternatively, editing the config file located at /plugins/ThemisToDiscord/config.yml and then running `/ttd reload` is a suitable replacement for step 3.

## Default Colors

All of these colors can be modified via the config file.

| Check Type | Hex Color | Textual Color |
| --- | --- | --- |
| Boat Movement       | `#875229`  | Light Brown |
| Flight / Y-Movement | `#3276bf`  | Light Blue  |
| Speed               | `#d6e600`  | Neon Yellow |
| Spoofed Packets     | `#ff0000`  | Neon Red    |
| Timer / Blink       | `#d61ad6`  | Neon Pink   |
| Reach               | `#6f1ad6`  | Neon Purple |
| Elytra Flight       | `#afaeb0`  | Light Gray  |
| Illegal Packets     | `#141414`  | Black Shade |

## Example Embeds

![Boat Movement](https://github.com/EarthCow/ThemisToDiscord/assets/56940983/cb271d23-cfa3-450c-b28c-a21c2b5ac694)
![Flight / Y-Movement](https://github.com/EarthCow/ThemisToDiscord/assets/56940983/6a527f65-322c-4c78-b037-110d85213a3a)
![Speed](https://github.com/EarthCow/ThemisToDiscord/assets/56940983/8695670a-098e-4ce3-8acc-a311c71442cf)
![Spoofed Packets](https://github.com/EarthCow/ThemisToDiscord/assets/56940983/be952458-7953-4a3b-82dd-91c37855d2fb)
![Timer / Blink](https://github.com/EarthCow/ThemisToDiscord/assets/56940983/f160ec26-7b59-460f-ba85-af36d49a5207)
![Reach](https://github.com/EarthCow/ThemisToDiscord/assets/56940983/bc7a5f36-b7fc-42a3-bc96-b8606f8d5862)
![Elytra Flight](https://github.com/EarthCow/ThemisToDiscord/assets/56940983/1ac7d2f5-a4ba-4cfb-8956-082489e90e83)
![Illegal Packets](https://github.com/EarthCow/ThemisToDiscord/assets/56940983/c53f364b-d85b-4f1a-80e4-2e21008b7018)
