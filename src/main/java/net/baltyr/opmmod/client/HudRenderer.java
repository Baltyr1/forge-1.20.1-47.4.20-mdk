package net.baltyr.opmmod.client;

import net.baltyr.opmmod.OpmMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = OpmMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class HudRenderer {

    private static float animOffset = 0f;
    private static final float ANIM_SPEED = 0.12f;

    private static final int PANEL_X = 4;
    private static final int PANEL_W = 26;
    private static final int PANEL_H = 158;

    // Barre de combat : SLOTS=6, SLOT_SIZE=30, SLOT_GAP=4 → totalW = 6*30 + 5*4 = 200
    private static final int COMBAT_TOTAL_W = 200;
    private static final int HALF_BAR_W     = COMBAT_TOTAL_W / 2 - 2;
    private static final int BAR_H          = 4;

    private static int panelTopY(int screenH) {
        return (screenH - PANEL_H) / 2;
    }

    // ── Barre de vie ──────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onHealthPre(RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay() != VanillaGuiOverlay.PLAYER_HEALTH.type()) return;
        event.setCanceled(true);

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        boolean combat = CombatModeHandler.isInCombatMode();
        animOffset += ((combat ? 1f : 0f) - animOffset) * ANIM_SPEED;
        float anim = animOffset;

        Player player = mc.player;
        float hp    = player.getHealth();
        float maxHp = player.getMaxHealth();
        float pct   = Math.max(0f, Math.min(1f, hp / maxHp));

        GuiGraphics gfx = event.getGuiGraphics();
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();

        int combatBaseX  = (sw - COMBAT_TOTAL_W) / 2;
        int combatPanelY = sh - 55 - 14;
        int targetY      = combatPanelY - BAR_H - 3;

        int bx = lerpi(sw / 2 - 91, combatBaseX, anim);
        int by = lerpi(sh - 39,     targetY, anim);
        int bw = lerpi(81,          HALF_BAR_W, anim);
        int bh = lerpi(5,           BAR_H, anim);
        if (bh < 1) bh = 1;

        int fillTop, fillBot, shine;
        if (pct > 0.5f) {
            float t = (pct - 0.5f) * 2f;
            int r   = (int)(0xAA * (1f - t) + 0x11 * t);
            fillTop = rgb(r,               0xCC, 0x11);
            fillBot = rgb((int)(r * 0.6f), 0x77, 0x08);
            shine   = rgb(Math.min(255, r + 50), 0xEE, 0x33);
        } else {
            float t = pct * 2f;
            fillTop = rgb(0xFF, (int)(0xAA * t), 0x00);
            fillBot = rgb(0x99, (int)(0x66 * t), 0x00);
            shine   = rgb(0xFF, Math.min(255, (int)(0xCC * t) + 40), 0x20);
        }

        gfx.fill(bx - 1, by - 1, bx + bw + 1, by + bh + 1, 0xFFAA7700);
        gfx.fill(bx, by, bx + bw, by + bh, 0xFF111111);

        int fw = (int)(bw * pct);
        if (fw > 0) {
            int half = Math.max(1, bh / 2);
            gfx.fill(bx, by + half, bx + fw, by + bh, fillBot);
            gfx.fill(bx, by,        bx + fw, by + half, fillTop);
            gfx.fill(bx, by,        bx + fw, by + 1,    shine);
        }
        drawCorners(gfx, bx - 1, by - 1, bw + 2, bh + 2, 0xFFFFCC00);

        String lbl = "♥ " + (int)hp + "/" + (int)maxHp;
        int la = (int)(255 * Math.min(1f, Math.max(0f, (anim - 0.5f) * 2f)));
        if (la > 0)
            gfx.drawString(mc.font, lbl, bx, by - 9, (la << 24) | 0xFFDDDD, false);
        else
            gfx.drawString(mc.font, lbl, bx, by - 9, 0xFFFFDDDD, true);
    }

    // ── Barre de faim ─────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onFoodPre(RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay() != VanillaGuiOverlay.FOOD_LEVEL.type()) return;
        if (animOffset < 0.01f) return;
        event.setCanceled(true);

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        int foodLevel = mc.player.getFoodData().getFoodLevel();
        float pct  = Math.max(0f, Math.min(1f, foodLevel / 20f));
        float anim = animOffset;

        GuiGraphics gfx = event.getGuiGraphics();
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();

        int combatBaseX  = (sw - COMBAT_TOTAL_W) / 2;
        int combatPanelY = sh - 55 - 14;
        int targetY      = combatPanelY - BAR_H - 3;

        int bx = combatBaseX + HALF_BAR_W + 4;
        int by = targetY;
        int bw = HALF_BAR_W;
        int bh = BAR_H;

        int fillColor = pct > 0.5f ? 0xFFCC7733 : 0xFFFF5500;

        gfx.fill(bx - 1, by - 1, bx + bw + 1, by + bh + 1, blend(0xFF664400, anim));
        gfx.fill(bx, by, bx + bw, by + bh, 0xFF0A0604);

        int fw = (int)(bw * pct);
        if (fw > 0) {
            gfx.fill(bx, by, bx + fw, by + bh, blend(fillColor, anim));
            gfx.fill(bx, by, bx + fw, by + 1,  blend(0xFFFFCC88, anim));
        }
        drawCorners(gfx, bx - 1, by - 1, bw + 2, bh + 2, blend(0xFFFFAA33, anim));

        String lbl = foodLevel + "/20";
        int la  = (int)(255 * Math.min(1f, Math.max(0f, (anim - 0.5f) * 2f)));
        int lx  = bx + bw - mc.font.width(lbl);
        if (la > 0)
            gfx.drawString(mc.font, lbl, lx, by - 9, (la << 24) | 0xFFCC88, false);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static int lerpi(int a, int b, float t) {
        return (int)(a + (b - a) * t);
    }

    private static int rgb(int r, int g, int b) {
        return 0xFF000000 | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    private static int blend(int color, float alpha) {
        int a = (int)(((color >> 24) & 0xFF) * alpha);
        return (a << 24) | (color & 0x00FFFFFF);
    }

    private static void drawCorners(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x,         y,         x + 3, y + 1, c);
        g.fill(x,         y,         x + 1, y + 3, c);
        g.fill(x + w - 3, y,         x + w, y + 1, c);
        g.fill(x + w - 1, y,         x + w, y + 3, c);
        g.fill(x,         y + h - 1, x + 3, y + h, c);
        g.fill(x,         y + h - 3, x + 1, y + h, c);
        g.fill(x + w - 3, y + h - 1, x + w, y + h, c);
        g.fill(x + w - 1, y + h - 3, x + w, y + h, c);
    }
}