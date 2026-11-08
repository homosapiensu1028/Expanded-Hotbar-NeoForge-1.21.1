# HUD compatibility notes (v1.0.5)

## What changed

The previous implementation rendered both the panel and item icons during `RenderGuiLayerEvent.Pre` for `VanillaGuiLayers.HOTBAR`. Any GUI layer registered above HOTBAR therefore rendered after the item icons, unlike the intended common overlay order. In addition, only a hard-coded list of vanilla layers received the vertical HUD translation.

The v1.0.5 implementation uses a two-phase draw:

- `HOTBAR` Pre: draw the replacement panel, selection frame, and empty offhand frame.
- `JUMP_METER` Pre: after layers inserted above HOTBAR, draw the item icons, offhand item, and attack indicator.
- From `JUMP_METER` through `EXPERIENCE_LEVEL` (including `SPECTATOR_TOOLTIP`): keep one balanced Y translation active, so modded layers inserted in the bottom-HUD band inherit it.
- `EFFECTS` Pre: restore the pose stack; `RenderGuiEvent.Post` is a fallback cleanup.

This is based on NeoForge 1.21.1's documented vanilla GUI layer order: HOTBAR, JUMP_METER, EXPERIENCE_BAR, PLAYER_HEALTH, ARMOR_LEVEL, FOOD_LEVEL, VEHICLE_HEALTH, AIR_LEVEL, SELECTED_ITEM_NAME, SPECTATOR_TOOLTIP, then EXPERIENCE_LEVEL and later HUD layers.

## Compatibility scope

This strategy supports mods using NeoForge's named GUI-layer registration around HOTBAR and the bottom-HUD band. It does not guarantee compatibility with arbitrary Mixin injections directly into `Gui.renderHotbar`, another mod that cancels/replaces `VanillaGuiLayers.HOTBAR`, or overlays drawn through a separate global rendering callback that bypasses the named layer order. Those cases require a targeted integration or a shared HUD API.

## Required in-game tests

Use AppleSkin (or the actual AppleCore-derived mod in the user's pack) and at least one unrelated HUD overlay mod. Verify panel → overlay → item icon layering, correct vertical positioning, hotbar selection, and pose-stack cleanup. Do not mark the release stable until this has been tested in a real 1.21.1 NeoForge client.
