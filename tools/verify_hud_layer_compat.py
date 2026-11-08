#!/usr/bin/env python3
"""Static guardrails for the v1.0.5 NeoForge HUD-layer draw order."""
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "src/main/java/com/rebelkeithy/dualhotbar/DualHotbarClient.java"
text = SOURCE.read_text(encoding="utf-8")

def method_body(name: str) -> str:
    match = re.search(r"(?:public|private|protected) static void " + re.escape(name) + r"\([^)]*\)\s*\{", text)
    assert match, f"Missing method: {name}"
    start = text.index("{", match.start())
    depth = 0
    for index in range(start, len(text)):
        if text[index] == "{":
            depth += 1
        elif text[index] == "}":
            depth -= 1
            if depth == 0:
                return text[start:index + 1]
    raise AssertionError(f"Unclosed method: {name}")

background = method_body("onHotbarBackground")
suppress = method_body("suppressVanillaHotbar")
compat = method_body("onHudLayerCompatibility")
render_items = method_body("renderExpandedHotbarItems")

assert "EventPriority.HIGHEST" in text and "EventPriority.LOWEST" in text
assert "VanillaGuiLayers.HOTBAR" in background
assert "renderExpandedHotbarBackground" in background
assert "event.setCanceled(true)" in suppress
assert "@SubscribeEvent(priority = EventPriority.LOWEST)" in text.split("public static void suppressVanillaHotbar", 1)[0][-120:]
assert "VanillaGuiLayers.JUMP_METER" in compat
assert "VanillaGuiLayers.EFFECTS" in compat
assert compat.index("renderExpandedHotbarItems") < compat.index("beginHudShift")
assert "graphics.renderItem(player, stack" in render_items
assert "finishHudShift(event.getGuiGraphics())" in method_body("onHudRenderPost")
assert "shouldShiftLayer" not in text, "Old hard-coded layer allow-list must stay removed"

print("HUD compatibility static checks: PASS")
print("- HOTBAR background and cancellation are separate phases")
print("- item foreground is rendered at JUMP_METER after HOTBAR overlays")
print("- bottom-HUD pose shift is opened after item rendering and closed at EFFECTS")
print("- RenderGuiEvent.Post cleanup is present")
