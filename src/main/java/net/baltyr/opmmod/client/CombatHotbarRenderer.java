package net.baltyr.opmmod.client;

import net.baltyr.opmmod.OpmMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = OpmMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class CombatHotbarRenderer {

    private static final int SLOT_SIZE = 20;
    private static final int SLOT_GAP  = 3;
    private static final int SLOTS     = 9;

    private static float slideAnim = 0f;
    private static final float ANIM_SPEED = 0.15f;

    // ── Cache la hotbar vanilla en la poussant hors écran ─────────────────
    @SubscribeEvent
    public static void onHotbarPre(RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        boolean combat = CombatModeHandler.isInCombatMode();
        float target = combat ? 1f : 0f;
        slideAnim += (target - slideAnim) * ANIM_SPEED;

        if (slideAnim < 0.01f) return;

        // Clippe la zone de rendu de la hotbar vanilla à une zone vide (hors écran)
        // Alternative : clippe hors écran
        int screenH = mc.getWindow().getHeight();
        int guiScale = (int) mc.getWindow().getGuiScale();
        com.mojang.blaze3d.systems.RenderSystem.enableScissor(0, screenH + 1, 1, 1);
    }

    @SubscribeEvent
    public static void onHotbarPost(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) return;
        if (slideAnim < 0.01f) return;

        com.mojang.blaze3d.systems.RenderSystem.disableScissor();

        // Dessine la hotbar verticale
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;
        // ... reste du code inchangé

        GuiGraphics gfx = event.getGuiGraphics();
        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();

        int totalH    = SLOTS * SLOT_SIZE + (SLOTS - 1) * SLOT_GAP;
        int centeredY = (screenH - totalH) / 2;

        int panelX = 6;
        int panelY = centeredY - 4;
        int panelW = SLOT_SIZE + 8;
        int panelH = totalH + 8;

        if (slideAnim > 0.3f) {
            int alpha   = (int)(0xCC * slideAnim);
            int bgColor = (alpha << 24) | 0x000A0A0F;
            gfx.fill(panelX + 2, panelY + 2, panelX + panelW + 2, panelY + panelH + 2,
                    ((int)(0x88 * slideAnim)) << 24);
            gfx.fill(panelX, panelY, panelX + panelW, panelY + panelH, bgColor);
            drawBorder(gfx, panelX - 1, panelY - 1, panelW + 2, panelH + 2,
                    blendColor(0xFF886600, slideAnim));
            drawBorder(gfx, panelX - 2, panelY - 2, panelW + 4, panelH + 4,
                    blendColor(0xFF050508, slideAnim));
            drawCorners(gfx, panelX - 1, panelY - 1, panelW + 2, panelH + 2,
                    blendColor(0xFFFFCC00, slideAnim));
        }

        int selected = mc.player.getInventory().selected;

        for (int i = 0; i < SLOTS; i++) {
            int sx = panelX + 4;
            int sy = centeredY + i * (SLOT_SIZE + SLOT_GAP);

            boolean isSelected = (i == selected);

            gfx.fill(sx, sy, sx + SLOT_SIZE, sy + SLOT_SIZE,
                    isSelected ? 0xFF1A1A28 : 0xFF0A0A18);
            drawBorder(gfx, sx - 1, sy - 1, SLOT_SIZE + 2, SLOT_SIZE + 2,
                    isSelected ? 0xFFFFCC00 : 0xFF333355);
            if (isSelected)
                drawCorners(gfx, sx - 1, sy - 1, SLOT_SIZE + 2, SLOT_SIZE + 2, 0xFFFFEE44);

            ItemStack stack = mc.player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                gfx.renderItem(stack, sx + 2, sy + 2);
                gfx.renderItemDecorations(mc.font, stack, sx + 2, sy + 2);
            }

            if (!isSelected) {
                gfx.drawString(mc.font, String.valueOf(i + 1),
                        sx + SLOT_SIZE - 6, sy + 1, 0xFF333355, false);
            }
        }
    }

    private static int blendColor(int color, float alpha) {
        int a = (int)(((color >> 24) & 0xFF) * alpha);
        return (a << 24) | (color & 0x00FFFFFF);
    }

    private static void drawBorder(GuiGraphics gfx, int x, int y, int w, int h, int color) {
        gfx.fill(x,         y,         x + w,     y + 1,     color);
        gfx.fill(x,         y + h - 1, x + w,     y + h,     color);
        gfx.fill(x,         y,         x + 1,     y + h,     color);
        gfx.fill(x + w - 1, y,         x + w,     y + h,     color);
    }

    private static void drawCorners(GuiGraphics gfx, int x, int y, int w, int h, int color) {
        gfx.fill(x,         y,         x + 3,     y + 1,     color);
        gfx.fill(x,         y,         x + 1,     y + 3,     color);
        gfx.fill(x + w - 3, y,         x + w,     y + 1,     color);
        gfx.fill(x + w - 1, y,         x + w,     y + 3,     color);
        gfx.fill(x,         y + h - 1, x + 3,     y + h,     color);
        gfx.fill(x,         y + h - 3, x + 1,     y + h,     color);
        gfx.fill(x + w - 3, y + h - 1, x + w,     y + h,     color);
        gfx.fill(x + w - 1, y + h - 3, x + w,     y + h,     color);
    }
}