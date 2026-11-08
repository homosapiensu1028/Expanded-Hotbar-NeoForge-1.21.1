/*
 * Unofficial NeoForge 1.21.1 port of Dual Hotbar, originally by RebelKeithy.
 * This source file is part of the port and was updated for the v1.0.4 release
 * (2026-11-08), including source attribution and distribution notices.
 * SPDX-License-Identifier: GPL-3.0-only
 * See the repository's NOTICE.md and LICENSE files for provenance and license.
 */
package com.rebelkeithy.dualhotbar;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

@Mod(DualHotbarMod.MODID)
public final class DualHotbarMod {
    public static final String MODID = "dualhotbar";

    public DualHotbarMod(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, DualHotbarConfig.SPEC);
    }

    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ClientModEvents {
        public static final KeyMapping SELECT_ROW_KEY = new KeyMapping(
                "key.dualhotbar.select_row",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_CONTROL,
                "key.categories.dualhotbar"
        );

        @SubscribeEvent
        public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
            event.register(SELECT_ROW_KEY);
        }
    }
}
