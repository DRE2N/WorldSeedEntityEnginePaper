> [!WARNING]  
> This is intended for Erethon.net. While it may work on any Paper 1.21.4+ server, no support is provided whatsoever. 


<!-- ABOUT THE PROJECT -->
## About The Project
<div align="center">
  <a href="https://github.com/AtlasEngineCa/WorldSeedEntityEngine">
    <img src=".github/hitbox.gif" alt="Logo" width="720" height="405">
  </a>
  </div>

This is a library that allows users to add bedrock models from blockbench in to **Vanilla Minecraft**!

WSEE lets you create multipart entities, using display entities.
The framework provided allows users to easily create multipart entities, define animations, and write AI that fully utilises the entity's animations.

**This is a port of the original [WSEE](https://github.com/AtlasEngineCa/WorldSeedEntityEngine) for Erethon.net, adding Paper support with fully packet-based entities.**

## Restrictions

Some restrictions are imposed by Minecraft
- Bones must be less than 64 blocks in size

## [Wiki](https://github.com/AtlasEngineCa/WorldSeedEntityEngine/wiki)
Learn what this project is, how it works and how you can use it on your server

## FAQ

Q: Why are my bones positioned incorrectly in minecraft?\
A: Entities used for bones will be placed at the pivot point of the bone in blockbench. To fix this, move the pivot point closer to the bone

Q: Why is my model not working?\
A: Make sure you have the type set to `Bedrock Model` in blockbench

Q: Why are `ModelDamageEvent` and `ModelInteractEvent` not triggering?\
A: You need to create hitboxes for the model [Hitboxes](https://github.com/AtlasEngineCa/WorldSeedEntityEngine/wiki/Bone-Types#hitbox)

## Comparisons
| Feature                                                                                                                          | Minestom Support | Paper Support | Math Animations | Cube Rotation   | Hurt Colour         | Accurate Hitboxes |
|----------------------------------------------------------------------------------------------------------------------------------|------------------|---------------|----------------|-----------------|---------------------|-------------------|
| [WSEE](https://github.com/AtlasEngineCa/WorldSeedEntityEngine)                                                                   | ✔️               | ❌            | ✔️             | ✔️ any          | ✔️ Texture Swapping | ✔️                 |
| [Model Engine](https://mythiccraft.io/index.php?resources/model-engine%E2%80%94ultimate-entity-model-manager-1-16-5-1-20-4.389/) | ️❌              | ✔️            | ❌             | ❌ 22.5 multiple | ❌ Leather Armour    | ❌                 |
| [hephaestus-engine](https://github.com/unnamed/hephaestus-engine)                                                                | ✔️               | ✔️            | ❌              | ❌ 22.5 multiple | ❌ Leather Armour    | ❌                 |

<p align="right">(<a href="#top">back to top</a>)</p>

<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
[contributors-shield]: https://img.shields.io/github/contributors/AtlasEngineCa/WorldSeedEntityEngine.svg?style=for-the-badge
[contributors-url]: https://github.com/AtlasEngineCa/WorldSeedEntityEngine/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/AtlasEngineCa/WorldSeedEntityEngine.svg?style=for-the-badge
[forks-url]: https://github.com/othneildrew/Best-README-Template/network/members
[stars-shield]: https://img.shields.io/github/stars/AtlasEngineCa/WorldSeedEntityEngine.svg?style=for-the-badge
[stars-url]: https://github.com/AtlasEngineCa/WorldSeedEntityEngine/stargazers
[issues-shield]: https://img.shields.io/github/issues/AtlasEngineCa/WorldSeedEntityEngine.svg?style=for-the-badge
[issues-url]: https://github.com/AtlasEngineCa/WorldSeedEntityEngine/issues
[license-shield]: https://img.shields.io/github/license/AtlasEngineCa/WorldSeedEntityEngine?style=for-the-badge
[license-url]: https://github.com/AtlasEngineCa/WorldSeedEntityEngine/blob/master/LICENSE
