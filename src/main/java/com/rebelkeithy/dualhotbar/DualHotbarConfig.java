/*
 * Unofficial NeoForge 1.21.1 port of Dual Hotbar, originally by RebelKeithy.
 * This source file is part of the port and was updated for the v1.0.4 release
 * (2026-11-08), including source attribution and distribution notices.
 * SPDX-License-Identifier: GPL-3.0-only
 * See the repository's NOTICE.md and LICENSE files for provenance and license.
 */
package com.rebelkeithy.dualhotbar;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Client-side options for the NeoForge port. */
public final class DualHotbarConfig {
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.BooleanValue ENABLE_DOUBLE_TAP;
    public static final ModConfigSpec.BooleanValue ENABLE_KEY_COMBO;
    public static final ModConfigSpec.BooleanValue ENABLE_SCROLL_SELECTION;
    public static final ModConfigSpec.IntValue DOUBLE_TAP_TIME;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("general");
        ENABLED = builder
                .comment("Enable or disable Dual Hotbar.")
                .define("enabled", true);
        // The layout is deliberately fixed: 36 slots in 18 columns × 2 rows.
        // Legacy layout keys (numberOfHotbars / stackedRendering /
        // compactFourRowsAsTwo) are intentionally no longer read, so stale
        // client config values cannot silently restore the old 9-column layout.
        ENABLE_DOUBLE_TAP = builder
                .comment("Double-tap a number key to move to the next hotbar row.")
                .define("enableDoubleTap", true);
        ENABLE_KEY_COMBO = builder
                .comment("Hold the extra hotbar key (default Left Control) and press 1-9 to select row 2.")
                .define("enableKeyCombo", true);
        ENABLE_SCROLL_SELECTION = builder
                .comment("Use the mouse wheel to scroll through every slot in all configured hotbar rows.")
                .define("enableScrollSelection", true);
        DOUBLE_TAP_TIME = builder
                .comment("Maximum time between number-key presses for double-tap selection, in milliseconds.")
                .defineInRange("doubleTapTime", 900, 100, 2000);
        builder.pop();

        SPEC = builder.build();
    }

    private DualHotbarConfig() {
    }
}
