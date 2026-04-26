# CYS — Cure Your Self

[![CurseForge](https://img.shields.io/curseforge/dt/1525646?logo=curseforge&label=CurseForge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/cys-cure-your-self)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-62b47a)](https://www.minecraft.net/)
[![NeoForge](https://img.shields.io/badge/NeoForge-26.1.2.11--beta-orange)](https://neoforged.net/)
[![License](https://img.shields.io/badge/License-All%20Rights%20Reserved-red)]()

Tired of your items burning in lava or despawning when you die? **CYS — Cure Your Self** saves your inventory by storing it in a corpse — but unlike a typical gravestone mod, your corpse **re-awakens as a zombie** that you have to hunt down and kill to get your stuff back.

> Download on [CurseForge](https://www.curseforge.com/minecraft/mc-mods/cys-cure-your-self)

---

## How It Works

When you die, a zombie spawns at your location wearing your skin and displaying your name (tinted green). This zombie holds everything you had — your items and, optionally, your experience — and won't give any of it up until it's killed.

To stop the zombie from burning in lava (which would defeat the whole point), it is **immune to all damage except from players**. Only a player can kill it. Once killed, it drops its full inventory and grants its stored experience directly to the killer.

---

## Features

### Core Mechanic
- A player-skinned zombie spawns at your death point carrying all your items
- Zombie is immune to environmental damage (fire, lava, fall damage, etc.) — only players can kill it
- Killing the zombie drops its inventory and grants experience to the killer

### Hardcore Mode
When hardcore mode is enabled, another living player on the server can locate the zombie and **cure it** (just like curing a zombie villager) to revive the dead player — teleporting them back into survival at the zombie's location with their full inventory restored. This can be disabled in config.

### Peaceful Mode
An optional config setting makes the zombie non-hostile — it will not attack the player. Useful for a more relaxed experience or servers that want the corpse mechanic without the combat.

### Experience Control
The mod gives you three options for how experience is handled on death:

| Mode | Behaviour |
|------|-----------|
| `NONE` | Experience drops normally, as in vanilla Minecraft |
| `STORE_PARTIAL` | The experience that *would have* dropped on death is stored in the zombie |
| `STORE_FULL` | *All* experience the player had is stored in the zombie — fully recoverable |

---

## Configuration

CYS uses NeoForge's built-in config system. You can toggle:

- Hardcore mode (zombie curing to revive players)
- Peaceful mode (non-hostile zombie)
- Zombie damage immunity
- Experience storage mode (`NONE` / `STORE_PARTIAL` / `STORE_FULL`)
- The mod itself (global on/off switch)

---

## Compatibility

The mod has not been tested alongside other mods that affect death behaviour or extend the player inventory, such as other gravestone/corpse mods or inventory mods like **Curios**. These may cause conflicts.

Compatibility issues can be reported in the [Issues tab](https://github.com/marios-andr/cys-cureyourself/issues) and will be addressed as time allows.

---

## Requirements

| Requirement | Version |
|-------------|---------|
| Minecraft | 1.21.1 |
| NeoForge | 26.1.2.11-beta or later |
| Java | 21+ |

---

## FAQ

**Do you plan on porting to Fabric or backporting to older versions?**
A Fabric port is planned for when time allows. Backporting is not planned, but may be considered if there is significant demand.

**Where do I report bugs or suggest features?**
Use the [Issues tab](https://github.com/marios-andr/cys-cureyourself/issues) on GitHub.

**I have more questions — where can I ask?**
In the comments section on the [CurseForge page](https://www.curseforge.com/minecraft/mc-mods/cys-cure-your-self).

---

## License

All Rights Reserved. Please contact the author before using or redistributing any part of this project.
