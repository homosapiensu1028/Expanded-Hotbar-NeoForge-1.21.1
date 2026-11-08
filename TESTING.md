# Testing checklist

The source package should not be described as fully verified until these checks have been run with the built JAR.

## Build checks

- [ ] `java -version` reports JDK 21.
- [ ] `./build.ps1` (or `build.bat`) completes successfully.
- [ ] `build/libs/dualhotbar-1.0.5.jar` exists and opens as a ZIP/JAR.
- [ ] The JAR metadata reports Minecraft 1.21.1 and NeoForge 21.1.x compatibility.

## Static checks

- [ ] Run `py tools/verify_hud_layer_compat.py`; it must report `PASS`.

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

## Third-party HUD overlay compatibility (v1.0.5)

- [ ] Test with AppleSkin/AppleCore-compatible food HUD overlays enabled.
- [ ] Test at least one unrelated mod that registers a GUI layer directly above `VanillaGuiLayers.HOTBAR`; confirm its effect appears between the replacement panel and item icons.
- [ ] Test a modded layer inserted above `PLAYER_HEALTH`, `FOOD_LEVEL`, or `EXPERIENCE_BAR`; confirm it moves upward with the expanded bottom HUD.
- [ ] Confirm item icons and stack-count/durability decorations remain visible above hotbar underlay effects.
- [ ] Confirm the HUD returns to normal when Dual Hotbar is disabled, a screen is opened, or the player leaves the world.
- [ ] Confirm the pose stack remains balanced with optional HUD layers disabled or cancelled.
- [ ] Test a mod that injects directly into `Gui.renderHotbar`; record this as a known compatibility limitation if it replaces/cancels that vanilla layer.
