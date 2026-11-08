# Testing checklist

The source package should not be described as fully verified until these checks have been run with the built JAR.

## Build checks

- [ ] `java -version` reports JDK 21.
- [ ] `./build.ps1` (or `build.bat`) completes successfully.
- [ ] `build/libs/dualhotbar-1.0.4.jar` exists and opens as a ZIP/JAR.
- [ ] The JAR metadata reports Minecraft 1.21.1 and NeoForge 21.1.x compatibility.

## In-game checks

- [ ] Minecraft 1.21.1 starts without a mod-loading error.
- [ ] HUD displays 18 columns × 2 rows.
- [ ] Item icons are centered in their slot frames.
- [ ] Selection frame aligns with the selected item slot.
- [ ] The middle join between the two background sections has no visible gap or overlap artifact.
- [ ] Left Ctrl + number keys select the intended second-row columns.
- [ ] Double-tap number selection behaves as described in README.
- [ ] Mouse-wheel selection wraps correctly through all 36 slots.
- [ ] Item movement and item use work in survival mode.
- [ ] Offhand, attack indicator, health/armor/hunger, and experience HUD elements do not overlap the expanded hotbar.
- [ ] Opening inventory/containers and changing screens does not leave an item in the wrong inventory slot.
- [ ] Test with common HUD, inventory, and keybinding mods used in the target pack.
- [ ] Test on a multiplayer server where only the client has this port installed, if client-only use is intended.

Record the tested Minecraft version, exact NeoForge version, Java version, mod list, and test date in the release notes.
