# polymorph but it's for fabric only but also for 1.21.10

[![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg?&style=flat-square)](https://www.gnu.org/licenses/lgpl-3.0)

> [!NOTE]
> **This is an unofficial fork of [Polymorph](https://github.com/illusivesoulworks/polymorph) by [IllusiveSoulworks](https://github.com/illusivesoulworks), updated for Minecraft 1.21.10.**

Polymorph is a mod that solves recipe conflicts by letting players choose between all potential outputs shared by the same ingredients.

With a sufficiently large amount of mods, recipe conflicts are a common occurrence and the responsibility for resolving these usually falls on the user or modpack developer, using datapacks or other tools to ensure that each recipe is unique.

Polymorph offers an alternative solution, allowing all possible crafting and smelting recipes to co-exist regardless of conflicts.

## Fork Information

This fork updates Polymorph for **Minecraft 1.21.10** (Fabric only).

### Changes from original:

- Updated to Minecraft 1.21.10 API
- Fixed smithing recipe handling for new recipe structure
- Removed NeoForge/Forge support (Fabric only)
- Various API compatibility fixes

### Original Project

- **Author:** [IllusiveSoulworks](https://github.com/illusivesoulworks)
- **Repository:** [github.com/illusivesoulworks/polymorph](https://github.com/illusivesoulworks/polymorph)
- **CurseForge:** [Polymorph](https://www.curseforge.com/minecraft/mc-mods/polymorph)

If you appreciate this mod, please support the original developer via [Ko-fi](https://ko-fi.com/C0C1NL4O).

## Features

### Crafting

![](https://i.ibb.co/TkWswkG/polymorph.gif)

When a group of ingredients matches more than one recipe, a button will appear above the output slot. Pushing this button will show a list of all possible results and selecting one will change the crafting output to match. Polymorph will also remember the last selection as long as the ingredients don't change, so repeated crafting actions are possible on the same selection.

### Smelting

![](https://i.ibb.co/QX9MNYM/polymorph-furnacedemo.gif)

When a valid input matches more than one output, a button will appear above the output slot. Pushing this button will show a list of all possible results with the currently selected result highlighted in green. Selecting one of the listed results will change the smelting output to match. This selection will be saved to the block itself and persist across world loading and unloading.

### Commands

To assist identifying potential conflicts, there's a command `/polymorph conflicts` that will try to identify recipes that conflict with each other and outputs a list of them to your logs folder.

## License

All source code and assets are licensed under [LGPL 3.0](https://www.gnu.org/licenses/lgpl-3.0), the same license as the original project.
