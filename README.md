# ThemisToDiscord

Sends an embedded Themis notification to a discord webhook.

## Installation

Depends on [Themis](https://www.spigotmc.org/resources/themis-anti-cheat-1-17-1-20-bedrock-support-paper-compatibility-free-optimized.90766/). If you want to use the `%os%` placeholder in the config file you will also need [Floodgate](https://geysermc.org/download/?project=floodgate).
1. Place the ThemisToDiscord.jar file in your /plugins folder.
2. Start or restart the server
3. Run `/ttd url <webhookUrl>` (replace `<webhookUrl>` with your webhook url)
4. Configure your messages if necessary following modifications with `/ttd reload`
5. Optionally, test your messages by running their respective commands `/ttd msg <message> <player:player_name> <type:detection_type> [score:score] [ping:ping] [tps:tps]` to ensure the setup is functioning

## Handling

You can create as many messages as you need/want. For each message you have the option to define the *handling*.

To do this, you use the same thresholds and delays included in the Themis configuration.
```yaml
  default:
    # Define whether ThemisToDiscord should handle sending this message
    # Disable handling if you add this message's command to your Themis action(s)
    Handling:
      Enabled: true
      # What is the minimum score for a notification to be sent to the discord?
      execution-threshold: 10.0
      # What amount of new violations need to happen before another notification is sent?
      repetition-threshold: 5.0
      # What amount of time needs to pass before another notification is sent?
      repetition-delay: 10.0
```
Should this option be missing or disabled, ThemisToDiscord will not handle sending the message.

This means the only way for the message to be sent would be the corresponding command:

`/ttd msg <message> <player:player_name> <type:detection_type> [score:score] [ping:ping] [tps:tps]`

A simple example of having Themis handle the message would be to disable handling in the ThemisToDiscord configuration and then place the message's command in a Themis action such as the following:
```yaml
  actions:
    # What is the name of the action? This name needs to be unique.
    notify:
      # What is the minimum score for Themis to run this action?
      execution-threshold: 10.0
      # Which amount of new violations need to happen for this action to be repeated?
      repetition-threshold: 5.0
      # Which amount of time needs to pass for this action to be repeated?
      repetition-delay: 10.0
      # Which commands should be run? You can specify as many as you'd like which will be run in the order they're listed.
      # Currently, you can use the following placeholders: %player_name%, %detection_type%, %score%, %ping%, %tps%
      commands:
        - "themis notify §5[Themis] §4%player_name% §cwas flagged for §4%detection_type% §chacks!\n§c[Score: §4%score% §c| Ping: §4%ping% §c| TPS: §4%tps%§c]"
        - "ttd msg example player:%player_name% type:%detection_type% score:%score% ping:%ping% tps:%tps%"
```

## JSON

You can specify the actual JSON message that will be sent to Discord via the webhook url. This is great for designing messages with https://discohook.org.

A small example:
```yaml
    # Defines the json data used to construct the message
    # This is useful if you want to design your message/embed with https://discohook.org/
    # All other options are ignored if json is provided
    Json: >
      {
        "content": "Hello this is an example of including a JSON string!",
        "embeds": [
          {
            "title": "JSON Example",
            "description": "A little example of using JSON in a ThemisToDiscord message.",
            "color": 5055373,
            "fields": [
              {
                "name": "Example Field",
                "value": "Did you know, all placeholders will work inside JSON too!",
                "inline": true
              },
              {
                "name": "Another Field",
                "value": "This is another field!",
                "inline": true
              }
            ],
            "author": {
              "name": "%player_name%",
              "icon_url": "%avatar_url%"
            }
          }
        ],
        "attachments": []
      }
```
It is important to note that **all other options (beside webhook urls) will be ignored** should JSON be provided.

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

## Example Embeds (Completely Modifiable)

![Boat Movement](https://github.com/EarthCow/ThemisToDiscord/assets/56940983/cb271d23-cfa3-450c-b28c-a21c2b5ac694)
![Flight / Y-Movement](https://github.com/EarthCow/ThemisToDiscord/assets/56940983/6a527f65-322c-4c78-b037-110d85213a3a)
![Speed](https://github.com/EarthCow/ThemisToDiscord/assets/56940983/8695670a-098e-4ce3-8acc-a311c71442cf)
![Spoofed Packets](https://github.com/EarthCow/ThemisToDiscord/assets/56940983/be952458-7953-4a3b-82dd-91c37855d2fb)
![Timer / Blink](https://github.com/EarthCow/ThemisToDiscord/assets/56940983/f160ec26-7b59-460f-ba85-af36d49a5207)
![Reach](https://github.com/EarthCow/ThemisToDiscord/assets/56940983/bc7a5f36-b7fc-42a3-bc96-b8606f8d5862)
![Elytra Flight](https://github.com/EarthCow/ThemisToDiscord/assets/56940983/1ac7d2f5-a4ba-4cfb-8956-082489e90e83)
![Illegal Packets](https://github.com/EarthCow/ThemisToDiscord/assets/56940983/1504f7a2-a86d-4cd4-a023-b5fcd427eacb)

## Alternatives

For servers with [DiscordSRV](https://www.spigotmc.org/resources/discordsrv.18494/) installed this plugin may not be necessary.

DiscordSRV comes with the command `/discordsrv broadcast [#ChannelID/#ChannelName] <Message>` documented [here](https://docs.discordsrv.com/commands#staff-commands).

It allows dispatching a message to the specified channel or if omitted to the main channel.

Therefore, one could add the following to the `actions` key in their Themis configuration file to receive basic Discord notifications.
```yaml
  actions:
    # What is the name of the action? This name needs to be unique.
    discord:
      # What is the minimum score for Themis to run this action?
      execution-threshold: 10.0
      # Which amount of new violations need to happen for this action to be repeated?
      repetition-threshold: 5.0
      # Which amount of time needs to pass for this action to be repeated?
      repetition-delay: 10.0
      # Which commands should be run? You can specify as many as you'd like which will be run in the order they're listed.
      # Currently, you can use the following placeholders: %player_name%, %detection_type%, %score%, %ping%, %tps%
      commands:
        - "discordsrv broadcast #0000000000000000000 [Themis] **%player_name%** was flagged for *%detection_type%* hacks! [Score: %score% | Ping: %ping% | TPS: %tps%]"
```
You could simply copy the command to any existing Themis action as well.

This method was known well before the creation of this plugin. However, not all servers use DiscordSRV and ThemisToDiscord provides far more styling options including changing the name and avatar via webhook and sending color-coded embedded messages instead of plain generic ones.
