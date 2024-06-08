# LUMINA

<a rel="license" href="https://www.gnu.org/licenses/lgpl-3.0.en.html#license-text"><img alt="LGPLv3" style="height:40px" src="https://cdn.discordapp.com/attachments/648723216747069441/1140482016438141028/LGPLv3_Logo.svg" /></a>

A full replacement of the Minecraft lighting engine with an extendable API, shipping
with [Phosphor](src/main/java/com/falsepattern/lumina/internal/lighting/phosphor/PhosphorLightingEngine.java) as a
reference implementation.

[Join us on Discord](https://discord.gg/h7XAeCSfbT)

## Dependencies

- [ChunkAPI 0.4.0+](https://github.com/FalsePattern/ChunkAPI)
- [FalsePatternLib 1.0.0+](https://github.com/FalsePattern/FalsePatternLib)
- [UniMixins 0.1.11+](https://github.com/LegacyModdingMC/UniMixins)

## Incompatibilities

- [ArchaicFix](https://github.com/embeddedt/ArchaicFix): Config Change Needed
  - As both mods replace the same parts of code, naturally you will need to set `enablePhosphor` to `false` in the
    ArchaicFix config.

## Authors

- [FalsePattern](https://github.com/FalsePattern)
- [Ven](https://github.com/basdxz)

## Credits

- [embeddedt](https://github.com/embeddedt)
  - The initial port
    of [Phosphor into 1.7.10](https://github.com/embeddedt/ArchaicFix/blob/main/src/main/java/org/embeddedt/archaicfix/lighting/world/lighting/LightingEngine.java)
- [CaffeineMC](https://github.com/CaffeineMC)
  - Creating the original [Phosphor](https://github.com/CaffeineMC/phosphor-fabric)
