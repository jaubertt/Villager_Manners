# Villager Manners

By Jaubert.

Villagers stand beside their workstation, their bed, the bell and furniture instead of on top of them.

- Villagers walking to one of these blocks stop at a free spot next to it, close enough to work or sleep.
- A villager already on top of one steps off, and one left on its edge slides off.
- Villagers don't work while standing on their own workstation.
- Baby villagers still jump on beds.
- Villagers don't claim or sleep in a bed that an online player has set as their spawn point. A villager already using it gives it up.
- Walking or jumping over these blocks on the way somewhere else is unchanged.
- Villagers find a way around open trapdoors instead of walking into them and getting stuck.
- When there's no free spot next to the block (like a 1x1 trading hall cell or a bed in a nook), villagers act as in vanilla, so they still work, restock and sleep.

## Settings

Each kind of block has its own switch, all on by default. With Mod Menu installed, open Mods, select Villager Manners and click the settings button.

| Switch | Blocks |
| --- | --- |
| Workstations | Every job site block, modded ones too |
| Beds | Beds |
| Baby Villagers Jump on Beds | Lets baby villagers keep jumping on beds (requires Beds) |
| Leave Player Beds | Beds that online players have set as their spawn point |
| Bells | Bells |
| Chests | Chests, trapped chests, ender chests and copper chests, modded ones too |
| Crafting Tables and Furnaces | Crafting tables and furnaces, modded ones too |
| Anvils, Enchanting Tables, Decorated Pots, Jukeboxes | Anvils, enchanting tables, decorated pots and jukeboxes |
| Stairs | Stairs, which village houses use as chairs |
| Pressure Plates | Pressure plates, which village houses use as tabletops |
| Open Trapdoors | Villagers find a way around open trapdoors instead of walking into them |

Data packs can add blocks to a switch with the block tags in data/villager_manners/tags/block/stand_beside/.

Settings are saved in config/villager_manners.json. On a dedicated server, edit that file while the server is stopped.

Only the server needs the mod. Players joining don't have to install it.

## Requirements

- Fabric API
  https://modrinth.com/mod/fabric-api
- Optional: Mod Menu, for the settings button
  https://modrinth.com/mod/modmenu

## Building

The jar is built by Gradle into build/libs:

```
./gradlew build
```

## Project layout

- src/main: everything that also runs on servers (the villager rules and the config).
- src/client: the settings screen.
- libs/modmenu-api-stub.jar: two interfaces of Mod Menu's API, used only to compile. They are left out of every jar; the real ones come from Mod Menu when it is installed. Mod Menu is MIT licensed (libs/MODMENU-LICENSE.txt).

## Issues

Report bugs and ideas here:
https://github.com/jaubertt/Villager_Manners/issues

## License

All rights reserved (see LICENSE).
