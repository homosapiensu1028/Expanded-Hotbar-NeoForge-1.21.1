# Dual Hotbar — NeoForge 1.21.1 Port

An **unofficial community port** of [Dual Hotbar by RebelKeithy](https://www.curseforge.com/minecraft/mc-mods/dual-hotbar) for Minecraft Java Edition 1.21.1 and NeoForge.

> This is an unofficial project. It is not affiliated with, endorsed by, or approved by RebelKeithy, Mojang, or Microsoft.

## Project status

- Project version: **1.0.4**
- Minecraft: **1.21.1**
- Mod loader: **NeoForge 21.1.x** (minimum declared version: 21.1.65)
- Java: **21 (JDK)**
- License: **GPL-3.0-only**
- Release status recommendation: **Beta until the packaged JAR has been tested in-game with the intended mod list**.

The source package has not been represented as an in-game verified release. Build it and run the test checklist in [`TESTING.md`](TESTING.md) before publishing a stable file.

## Features

- Expands the usable hotbar to **36 inventory slots**.
- Displays those slots as **18 columns × 2 rows**.
- Mouse-wheel selection across the expanded slot range.
- Hold the extra-row key (default **Left Ctrl**) and press **1–9** to select the second visual row.
- Double-tap a number key to cycle to the next logical row while retaining the column.
- Adjusted HUD placement for the 18×2 layout.
- Uses normal inventory swap interactions so item use remains server-authoritative; the server is not intended to require this client-side port.

## Installation

1. Install Minecraft **1.21.1** with a compatible **NeoForge 21.1.x** instance.
2. Download the JAR from the project's CurseForge page or GitHub Release.
3. Put the JAR into the instance's `mods` folder.
4. Remove older or duplicate Dual Hotbar port JARs before launching.
5. Start the game and follow the test checklist if using a beta build.

Do not install the original legacy Forge JAR alongside this port.

## Controls

- Mouse wheel: move through the expanded hotbar slots.
- Left Ctrl + `1`–`9`: select the corresponding column in the second visual row.
- Double-tap `1`–`9`: cycle to the next logical row while retaining that column.
- Normal hotbar number keys continue to select the corresponding column in the current row.

The extra-row key can be changed in **Options → Controls → Key Binds**.

## Configuration

After first launch, the client configuration is stored at `config/dualhotbar-client.toml`.

- `enabled`: enable or disable the mod.
- `enableDoubleTap`: enable double-tap row cycling.
- `enableKeyCombo`: enable the modifier-key + number-key selection.
- `enableScrollSelection`: enable mouse-wheel slot selection.
- `doubleTapTime`: double-tap interval in milliseconds.

The v1.0.4 layout is fixed at 36 slots (18×2). Legacy layout settings from earlier builds are ignored.

## Build on Windows

### Requirements

- JDK 21 installed and selected in `JAVA_HOME` / `PATH`.
- Internet access for the first build (Gradle, Minecraft, and NeoForge dependencies are downloaded).
- PowerShell 5.1 or PowerShell 7.

### Build

Extract or clone the repository, open PowerShell in the repository root, and run:

```powershell
java -version
# Confirm that the output reports Java 21.

.\build.ps1
```

`build.ps1` downloads and uses **Gradle 8.14.3** in the project-local `.tools` directory. The generated JAR is:

```text
build\libs\dualhotbar-1.0.4.jar
```

The `.tools` and `build` directories are generated locally and are excluded from Git by `.gitignore`.

## Source layout

```text
src/main/java/       Java source
src/main/resources/  NeoForge metadata and language files
build.gradle         Gradle / NeoForge build configuration
build.ps1            Windows build script
build.bat            Windows build launcher
LICENSE               Full GPL-3.0 license text
NOTICE.md             Original-project attribution and port notices
CHANGELOG.md          Release history
TESTING.md             Manual verification checklist
```

## Credits and provenance

- Original project: **Dual Hotbar**, by **RebelKeithy**.
- Original project page: https://www.curseforge.com/minecraft/mc-mods/dual-hotbar
- Original source project: https://github.com/RebelKeithy/DualHotbar
- This repository contains an unofficial NeoForge 1.21.1 port and subsequent modifications. See [`NOTICE.md`](NOTICE.md).

## License

This port is distributed under **GNU General Public License v3.0 only (GPL-3.0-only)**. The complete license text is in [`LICENSE`](LICENSE).

When redistributing modified or compiled versions, comply with GPL-3.0, preserve applicable upstream notices, identify modifications, and provide the corresponding source code as required by the license. See [NOTICE.md](NOTICE.md).

## Support and contributions

Please include the Minecraft version, NeoForge version, Java version, mod list, steps to reproduce, and `latest.log` (with private information removed) when reporting a bug. See [`CONTRIBUTING.md`](CONTRIBUTING.md).
