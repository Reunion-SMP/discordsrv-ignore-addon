# DiscordSRV Ignore Addon
Paper plugin to toggle DiscordSRV messages entirely or from specific users.

Requires [Paper 1.21.3+](https://papermc.io/software/paper), [DiscordSRV 1.29.0+](https://github.com/DiscordSRV/DiscordSRV/), and [Redis 7.2+](https://redis.io/).

## Commands
| Command                                                              | Usage                                   | Description                                              |
|----------------------------------------------------------------------|-----------------------------------------|----------------------------------------------------------|
| `/discordtoggle`, `/dtoggle`                                         | `/<command>`                            | Toggle receiving of Discord messages entirely            |
| `/discordignore`, `/dignore`, `discordunignore`, `dunignore`         | `/<command> (PLAYER \| DISCORD_UID)...` | Toggle receiving of Discord messages from specific users |
| `/discordignorelist`, `/dignorelist`, `/discordignored`, `/dignored` | `/<command>`                            | List users whose Discord message you've ignored          |

## Permissions
| Permission                       | Description                       | Default |
|----------------------------------|-----------------------------------|---------|
| `discordsrv-ignore-addon.toggle` | Grants access to `/discordtoggle` | `true`  |
| `discordsrv-ignore-addon.ignore` | Grants access to `/discordignore` | `true`  |
