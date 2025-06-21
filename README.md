# KrazyShulkers
KrazyShulkers is a paper plugin for Minecraft that allows
players to open shulker boxes more easily, by right-clicking while holding them
or just right-clicking them in the inventory.

The plugin is 100% customizable, you can change or disable the
shulker open/close sounds and messages.

This project currently supports paper versions 1.21 and above,
only the latest builds are supported. The plugin will most probably work on custom forks,
but it is not guaranteed.

## Installation
1- Download or compile the JAR<br>
2- Drop the JAR in your server's plugin folder<br>
3- Restart your server<br>

## Configuration
Tweak the plugin's settings in the config.yml file, it will be generated
once the plugin runs for the first time, and it should be located in /plugins/KrazyShulkers.<br>
Go to [this link](https://github.com/KrazyxWolf/KrazyShulkers/blob/master/src/main/resources/config.yml)
to see the default configuration settings, every option has an explanation of what it does.

## Permissions
| Permission                   | Description                                                   |
|------------------------------|---------------------------------------------------------------|
| krazyshulkers.use            | Allows the player to use the plugin's features                |
| krazyshulkers.admin          | Allows the player to reload the plugin's configuration.       |
| krazyshulkers.notify         | Allows the player to receive alerts from illegal interactions |
| krazyshulkers.bypasscooldown | Allows the player to bypass the configured cooldown           |
| krazyshulkers.*              | Grants all permissions                                        |

## Commands
| Command     | Description              |
|-------------|--------------------------|
| /bsb reload | Reload the configuration |

## Using hex colors, gradients, rainbows and more
KrazyShulkers supports MiniMessage color format (`<color>`) as well as hex colors (`<#RRGGBB>`).

Examples: [MiniMesage format](https://docs.advntr.dev/minimessage/format.html)

## Contact info
GitHub: [Issues page](https://github.com/KrazyxWolf/KrazyShulkers/issues)

## FAQ

### How do I disable the shulker box messages?
Set the open and close messages to empty quotes, like this:
```yaml
messages:
  open_message: ''
  close_message: ''
```

### I found a bug, what should I do?
If find a bug, please open an issue on GitHub.

### I have a feature suggestion
Please open a GitHub issue or reach out to me.

### Can I submit a translation?
Of course! Please open a pull request or contact me

### Can I fork/modify your plugin?
Yes, you can, as long as you comply with the license (AGPLv3)