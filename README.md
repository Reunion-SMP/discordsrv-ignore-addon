# DiscordSRV Ignore Addon
Paper plugin to toggle DiscordSRV messages entirely or from specific users.

Requires [Paper 1.21.3+](https://papermc.io/software/paper) and [DiscordSRV 1.29.0+](https://github.com/DiscordSRV/DiscordSRV/).

Uses Sqlite for database

## Commands
| Command                                                              | Usage                                   | Description                                              |
|----------------------------------------------------------------------|-----------------------------------------|----------------------------------------------------------|
| `/discordunignore`                                                   | `/<command>`                            | Unignore Someone's Discord Messages                      |
| `/discordignore`                                                     | `/<command> (PLAYER \| DISCORD_UID)...` | Ignore Someones's Discord Messages                       |
| `/discordignorelist`                                                 | `/<command>`                            | List users whose Discord messages you've ignored         |

## Permissions
| Permission                       | Description                       | Default |
|----------------------------------|-----------------------------------|---------|
| `discordsrv-ignore-addon.ignore` | Grants access to `/discordignore` | `true`  |
