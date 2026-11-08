/*
 * Unofficial NeoForge 1.21.1 port of Dual Hotbar, originally by RebelKeithy.
 * This source file is part of the port and was updated for the v1.0.5 release
 * (2026-11-08), including source attribution and distribution notices.
 * SPDX-License-Identifier: GPL-3.0-only
 * See the repository's NOTICE.md and LICENSE files for provenance and license.
 */
package com.rebelkeithy.dualhotbar;

import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * Client-only implementation. Extra inventory slots are moved into the real
 * hotbar with the normal SWAP click packet, so item use remains server-authoritative
 * and the server does not need this mod installed.
 */
@EventBusSubscriber(modid = DualHotbarMod.MODID, value = Dist.CLIENT)
public final class DualHotbarClient {
    private static final ResourceLocation HOTBAR_SPRITE = ResourceLocation.withDefaultNamespace("hud/hotbar");
    private static final ResourceLocation SELECTION_SPRITE = ResourceLocation.withDefaultNamespace("hud/hotbar_selection");
    private static final ResourceLocation OFFHAND_LEFT_SPRITE = ResourceLocation.withDefaultNamespace("hud/hotbar_offhand_left");
    private static final ResourceLocation OFFHAND_RIGHT_SPRITE = ResourceLocation.withDefaultNamespace("hud/hotbar_offhand_right");
    private static final ResourceLocation ATTACK_BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("hud/hotbar_attack_indicator_background");
    private static final ResourceLocation ATTACK_PROGRESS_SPRITE = ResourceLocation.withDefaultNamespace("hud/hotbar_attack_indicator_progress");

    private static final boolean[] keyWasDown = new boolean[9];
    private static final long[] lastNumberKeyPress = new long[9];

    /** Logical slot selected in the expanded hotbar (0 through rows*9-1). */
    private static int virtualSlot;
    /** Inventory row/column currently swapped into the actual hotbar. Row 0 means none. */
    private static int appliedRow;
    private static int appliedColumn = -1;
    private static boolean wasInWorld;
    private static double scrollRemainder;

    /** Set after the replacement background is drawn at HOTBAR. */
    private static boolean awaitingHotbarForeground;
    /** One balanced pose transform is held across the bottom-HUD layer band. */
    private static boolean hudShiftActive;
    private static GuiGraphics hudShiftGraphics;

    private DualHotbarClient() {
    }

    /**
     * The port's intended layout is always 36 slots arranged as 18 columns × 2 rows.
     * Keep this independent of old client config files that may still contain
     * numberOfHotbars = 2 from earlier 9-column builds.
     */
    private static int configuredRows() {
        return 4; // four logical 9-slot groups = 36 displayed slots
    }

    private static boolean isEnabled(Minecraft client) {
        return DualHotbarConfig.ENABLED.get()
                && configuredRows() > 1
                && client.player != null
                && !client.player.isSpectator()
                && !client.options.hideGui;
    }

    private static boolean canSwap(Minecraft client, LocalPlayer player) {
        return client.gameMode != null
                && client.getConnection() != null
                && player.containerMenu == player.inventoryMenu
                && !player.isSpectator();
    }

    private static int activeRow() {
        return virtualSlot / 9;
    }

    private static int activeColumn() {
        return virtualSlot % 9;
    }

    /** Four logical 9-slot inventory rows are always shown as two visual rows of 18. */
    private static boolean compactFourRowLayout() {
        return true;
    }

    private static int hudShift() {
        return 22; // exactly two visual rows are drawn
    }

    private static void setVirtualSlot(Minecraft client, int slot) {
        if (client.player == null) {
            return;
        }

        int maxSlots = configuredRows() * 9;
        virtualSlot = Math.floorMod(slot, maxSlots);
        client.player.getInventory().selected = activeColumn();
        applyInventorySwap(client);
    }

    /**
     * Swaps only the selected column between the chosen inventory row and the
     * actual hotbar. The rest of the inventory stays in its original order.
     */
    private static void applyInventorySwap(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || !canSwap(client, player)) {
            return;
        }

        int wantedRow = activeRow();
        int wantedColumn = activeColumn();

        if (appliedRow == wantedRow && appliedColumn == wantedColumn) {
            player.getInventory().selected = wantedColumn;
            return;
        }

        restorePreviousSwap(client);

        if (wantedRow > 0) {
            swapInventorySlotWithHotbar(client, player, wantedRow, wantedColumn);
            appliedRow = wantedRow;
            appliedColumn = wantedColumn;
        }

        player.getInventory().selected = wantedColumn;
    }

    private static void swapInventorySlotWithHotbar(Minecraft client, LocalPlayer player, int inventoryRow, int column) {
        MultiPlayerGameMode gameMode = client.gameMode;
        if (gameMode == null) {
            return;
        }

        // Player inventory menu slot IDs 9-35 are the three main-inventory rows.
        // The button is the hotbar index to exchange with (0-8).
        int inventoryMenuSlot = inventoryRow * 9 + column;
        gameMode.handleInventoryMouseClick(
                player.inventoryMenu.containerId,
                inventoryMenuSlot,
                column,
                ClickType.SWAP,
                player
        );
    }

    private static void restorePreviousSwap(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || appliedRow <= 0 || appliedColumn < 0) {
            appliedRow = 0;
            appliedColumn = -1;
            return;
        }

        if (!canSwap(client, player)) {
            return;
        }

        swapInventorySlotWithHotbar(client, player, appliedRow, appliedColumn);
        player.getInventory().selected = Math.floorMod(virtualSlot, 9);
        appliedRow = 0;
        appliedColumn = -1;
    }

    private static void normalizeSelection(Minecraft client) {
        if (client.player == null) {
            return;
        }
        restorePreviousSwap(client);
        virtualSlot = Math.floorMod(virtualSlot, 9);
        client.player.getInventory().selected = virtualSlot;
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft client = Minecraft.getInstance();
        if (!isEnabled(client) || !DualHotbarConfig.ENABLE_SCROLL_SELECTION.get()) {
            return;
        }
        if (client.screen != null || client.player == null) {
            return;
        }

        double delta = event.getScrollDeltaY();
        if (delta == 0.0D) {
            return;
        }

        // Consume fractional wheel input too, so vanilla does not change the
        // physical hotbar while we are accumulating a partial step.
        event.setCanceled(true);
        scrollRemainder += delta;
        int steps = (int) scrollRemainder;
        if (steps == 0) {
            return;
        }
        scrollRemainder -= steps;
        setVirtualSlot(client, virtualSlot - steps);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;

        if (player == null) {
            wasInWorld = false;
            virtualSlot = 0;
            appliedRow = 0;
            appliedColumn = -1;
            scrollRemainder = 0.0D;
            for (int i = 0; i < 9; i++) {
                keyWasDown[i] = false;
            }
            return;
        }

        if (!wasInWorld) {
            wasInWorld = true;
            virtualSlot = Math.floorMod(player.getInventory().selected, 9);
            appliedRow = 0;
            appliedColumn = -1;
        }

        int rows = configuredRows();
        if (!DualHotbarConfig.ENABLED.get() || rows <= 1) {
            normalizeSelection(client);
            for (int i = 0; i < 9; i++) {
                keyWasDown[i] = client.options.keyHotbarSlots[i].isDown();
            }
            return;
        }

        if (virtualSlot >= rows * 9) {
            virtualSlot = Math.floorMod(virtualSlot, rows * 9);
        }

        // If the player opens any screen, return the inventory to its normal
        // arrangement. The selected virtual row is re-applied after closing it.
        if (client.screen != null) {
            restorePreviousSwap(client);
            return;
        }

        boolean handledNumberKey = false;
        long now = System.currentTimeMillis();
        for (int i = 0; i < 9; i++) {
            boolean isDown = client.options.keyHotbarSlots[i].isDown();
            if (isDown && !keyWasDown[i]) {
                handledNumberKey = true;
                int nextRow = activeRow();

                if (DualHotbarConfig.ENABLE_KEY_COMBO.get() && DualHotbarMod.ClientModEvents.SELECT_ROW_KEY.isDown()) {
                    nextRow = Math.min(1, rows - 1);
                } else if (DualHotbarConfig.ENABLE_DOUBLE_TAP.get()
                        && lastNumberKeyPress[i] != 0L
                        && now - lastNumberKeyPress[i] <= DualHotbarConfig.DOUBLE_TAP_TIME.get()) {
                    nextRow = (nextRow + 1) % rows;
                }

                lastNumberKeyPress[i] = now;
                virtualSlot = nextRow * 9 + i;
                player.getInventory().selected = i;
                applyInventorySwap(client);
            }
            keyWasDown[i] = isDown;
        }

        // Keep the expanded selection in step with other mods or external
        // changes to the vanilla selected hotbar slot.
        int physicalSelection = Math.floorMod(player.getInventory().selected, 9);
        if (!handledNumberKey && physicalSelection != virtualSlot % 9) {
            virtualSlot = activeRow() * 9 + physicalSelection;
        }
        if (canSwap(client, player)) {
            applyInventorySwap(client);
        }
    }

    @SubscribeEvent
    public static void onScreenOpening(ScreenEvent.Opening event) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            restorePreviousSwap(client);
        }
    }

    /**
     * Draw only the replacement hotbar background at the vanilla HOTBAR position.
     * This happens early so other mods that draw in HOTBAR's Pre event or register
     * a layer directly above HOTBAR can draw over the panel before item icons.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onHotbarBackground(RenderGuiLayerEvent.Pre event) {
        if (!event.getName().equals(VanillaGuiLayers.HOTBAR)) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        finishHudShift(event.getGuiGraphics()); // fail-safe against an interrupted previous frame
        awaitingHotbarForeground = false;
        if (!isEnabled(client)) {
            return;
        }

        renderExpandedHotbarBackground(client, event.getGuiGraphics());
        awaitingHotbarForeground = true;
    }

    /**
     * Suppress only vanilla's own HOTBAR layer, after ordinary Pre listeners have
     * had an opportunity to draw their overlays. The replacement background has
     * already been drawn by onHotbarBackground.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void suppressVanillaHotbar(RenderGuiLayerEvent.Pre event) {
        if (event.getName().equals(VanillaGuiLayers.HOTBAR) && isEnabled(Minecraft.getInstance())) {
            event.setCanceled(true);
        }
    }

    /**
     * In 1.21.1 the layer order is HOTBAR -> JUMP_METER -> EXPERIENCE_BAR ->
     * PLAYER_HEALTH ... -> SELECTED_ITEM_NAME -> EFFECTS. Mods that
     * register layers above HOTBAR therefore render before JUMP_METER. Draw item
     * icons at that boundary so those effects remain between the hotbar panel and
     * item icons, matching the intended vanilla overlay order.
     *
     * The HUD offset then stays active through the complete bottom-HUD band. This
     * automatically moves third-party layers inserted among health/food/XP layers
     * instead of maintaining a fragile list of known mod IDs or layer names.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onHudLayerCompatibility(RenderGuiLayerEvent.Pre event) {
        ResourceLocation layer = event.getName();
        GuiGraphics graphics = event.getGuiGraphics();

        // Always close the transform before status-effect icons and other top-level HUD layers.
        if (layer.equals(VanillaGuiLayers.EFFECTS)) {
            finishHudShift(graphics);
            return;
        }

        if (!layer.equals(VanillaGuiLayers.JUMP_METER) || !awaitingHotbarForeground) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        if (isEnabled(client)) {
            // Third-party layers registered directly above HOTBAR have now rendered.
            // Draw item icons after those layers, but before the vanilla stat HUD.
            renderExpandedHotbarItems(client, graphics);
            beginHudShift(graphics);
        }
        awaitingHotbarForeground = false;
    }

    /**
     * Last-resort cleanup if another mod removes or defers the EFFECTS layer.
     * NeoForge fires this after all normal HUD layers have rendered.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onHudRenderPost(RenderGuiEvent.Post event) {
        finishHudShift(event.getGuiGraphics());
        awaitingHotbarForeground = false;
    }

    private static void beginHudShift(GuiGraphics graphics) {
        if (hudShiftActive || hudShift() <= 0) {
            return;
        }
        graphics.pose().pushPose();
        graphics.pose().translate(0.0D, -hudShift(), 0.0D);
        hudShiftGraphics = graphics;
        hudShiftActive = true;
    }

    private static void finishHudShift(GuiGraphics graphics) {
        if (!hudShiftActive) {
            return;
        }
        GuiGraphics target = hudShiftGraphics != null ? hudShiftGraphics : graphics;
        target.pose().popPose();
        hudShiftGraphics = null;
        hudShiftActive = false;
    }

    /** Draws the two-row panel and selection frame, but deliberately no item icons. */
    private static void renderExpandedHotbarBackground(Minecraft client, GuiGraphics graphics) {
        LocalPlayer player = client.player;
        if (player == null) {
            return;
        }

        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        int compactWidth = 362; // 18 slots × 20px plus 2px outer border
        int startX = width / 2 - compactWidth / 2;

        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, -90.0F);

        // Tile vanilla 9-slot artwork twice per row without stretching textures.
        for (int displayRow = 0; displayRow < 2; displayRow++) {
            int y = height - 22 * (displayRow + 1);
            graphics.blitSprite(HOTBAR_SPRITE, startX, y, 182, 22);
            graphics.blitSprite(HOTBAR_SPRITE, startX + 180, y, 182, 22);
        }

        int selectedColumn = Math.floorMod(virtualSlot, 18);
        int selectedRow = Math.floorDiv(virtualSlot, 18);
        int selectedX = startX - 1 + selectedColumn * 20;
        int selectedY = height - 22 * (selectedRow + 1) - 1;
        graphics.blitSprite(SELECTION_SPRITE, selectedX, selectedY, 24, 23);

        // Keep the offhand frame outside the expanded 18-slot row.
        ItemStack offhand = player.getOffhandItem();
        HumanoidArm offhandSide = player.getMainArm().getOpposite();
        if (offhand.isEmpty()) {
            if (offhandSide == HumanoidArm.LEFT) {
                graphics.blitSprite(OFFHAND_LEFT_SPRITE, startX - 29, height - 23, 29, 24);
            } else {
                graphics.blitSprite(OFFHAND_RIGHT_SPRITE, startX + compactWidth, height - 23, 29, 24);
            }
        }
        graphics.pose().popPose();
    }

    /**
     * Draws item contents after third-party layers anchored directly above HOTBAR.
     * This preserves the vanilla visual order: panel -> overlay effects -> items.
     */
    private static void renderExpandedHotbarItems(Minecraft client, GuiGraphics graphics) {
        LocalPlayer player = client.player;
        if (player == null) {
            return;
        }

        int rows = configuredRows();
        int compactColumns = 18;
        int compactWidth = 362;
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        int startX = width / 2 - compactWidth / 2;

        int seed = 1;
        for (int inventoryIndex = 0; inventoryIndex < rows * 9; inventoryIndex++) {
            int physicalIndex = physicalInventoryIndex(inventoryIndex);
            ItemStack stack = player.getInventory().getItem(physicalIndex);
            int displayRow = inventoryIndex / compactColumns;
            int displayColumn = inventoryIndex % compactColumns;
            int x = startX + displayColumn * 20 + 3;
            int y = height - 22 * (displayRow + 1) + 3;
            graphics.renderItem(player, stack, x, y, seed++);
            graphics.renderItemDecorations(client.font, stack, x, y);
        }

        ItemStack offhand = player.getOffhandItem();
        if (!offhand.isEmpty()) {
            HumanoidArm offhandSide = player.getMainArm().getOpposite();
            int x = offhandSide == HumanoidArm.LEFT ? startX - 26 : startX + compactWidth + 10;
            int y = height - 16 - 3;
            graphics.renderItem(player, offhand, x, y, seed);
            graphics.renderItemDecorations(client.font, offhand, x, y);
        }

        // Keep the vanilla hotbar attack indicator beside the lower row.
        if (client.options.attackIndicator().get() == AttackIndicatorStatus.HOTBAR) {
            float strength = player.getAttackStrengthScale(0.0F);
            if (strength < 1.0F) {
                HumanoidArm mainArm = player.getMainArm();
                int x = mainArm == HumanoidArm.RIGHT
                        ? startX - 51
                        : startX + compactWidth + 33;
                int y = height - 20;
                int filled = (int) (strength * 19.0F);
                graphics.blitSprite(ATTACK_BACKGROUND_SPRITE, x, y, 18, 18);
                if (filled > 0) {
                    graphics.blitSprite(ATTACK_PROGRESS_SPRITE, 18, 18, 0, 18 - filled,
                            x, y + 18 - filled, 18, filled);
                }
            }
        }
    }

    /**
     * Returns the physical inventory index after the selected logical slot has
     * been swapped into the vanilla hotbar. The display layout does not affect
     * this linear inventory mapping.
     */
    private static int physicalInventoryIndex(int inventoryIndex) {
        if (appliedRow > 0 && appliedColumn >= 0) {
            int sourceIndex = appliedRow * 9 + appliedColumn;
            if (inventoryIndex == sourceIndex) {
                return appliedColumn;
            }
            if (inventoryIndex == appliedColumn) {
                return sourceIndex;
            }
        }
        return inventoryIndex;
    }
}
