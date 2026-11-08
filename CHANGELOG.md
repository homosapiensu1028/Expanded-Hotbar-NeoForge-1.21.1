# Changelog

All notable changes to this project are documented here.

## 1.0.5 - HUD overlay compatibility

- Split hotbar rendering into a background phase and an item-foreground phase.
- Draw item icons after third-party GUI layers registered above the vanilla HOTBAR layer, preserving the panel → overlay → item order.
- Shift the whole bottom-HUD layer band from JUMP_METER through EXPERIENCE_LEVEL (including SPECTATOR_TOOLTIP) so third-party layers inserted among status/experience layers inherit the same translation.
- Added a RenderGuiEvent.Post cleanup path to keep the pose stack balanced if another mod removes or defers the normal HUD boundary layer.
- This is generic NeoForge GUI-layer compatibility; mods that inject directly into the vanilla hotbar method may still need a dedicated integration.

## [1.0.4] - 2026-11-08

- Adjusted item-icon drawing to match the vanilla hotbar inset.
- Adjusted the second background tile to remove the center seam/gap.
- Recalculated the overall 18-slot bar footprint to keep the outer margins symmetric.
- Kept the fixed 36-slot, 18-columns × 2-rows layout independent of legacy client config values.

## Earlier versions

Earlier development builds were distributed before this repository packaging. Their detailed change history is not reconstructed here.
