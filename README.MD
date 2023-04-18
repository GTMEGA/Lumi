# LUMINA
## Illuminate your world.

![LGPLv3](https://www.gnu.org/graphics/lgplv3-with-text-154x68.png)

[![forthebadge](https://forthebadge.com/images/badges/ctrl-c-ctrl-v.svg)](https://forthebadge.com)

[![forthebadge](https://forthebadge.com/images/badges/you-didnt-ask-for-this.svg)](https://forthebadge.com)

[![forthebadge](https://forthebadge.com/images/badges/powered-by-black-magic.svg)](https://forthebadge.com)


A backport of the Phosphor lighting engine, extracted from ArchaicFix, and repurposed into an API/library for use in 
other projects.

Special thanks to @embeddedt for creating the original backport of Phosphor, this project is an attempt on turning said
backport from an optimization into a fully-fledged library capable of standing on its own.

## Incompatibilities
- ArchaicFix (fixable in config): Naturally, you will need to set `enablePhosphor` to `false` in the ArchaicFix config.
- FastCraft (wontfix): That mod is highly obfuscated and at this point, obsolete, just use a combination of the open
source 1.7.10 optimization mods.