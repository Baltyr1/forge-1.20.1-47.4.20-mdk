package net.baltyr.opmmod.client;

import net.baltyr.opmmod.OpmMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = OpmMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class CombatHud {

    private static final int SLOT_SIZE = 40;
    private static final int SLOT_GAP  = 6;
    private static final int SLOTS     = 6;
    private static final int BAR_H     = 6;

    // Stamina (à brancher sur une capability plus tard)
    public static float stamina    = 100f;
    public static float maxStamina = 100f;

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (!CombatModeHandler.isInCombatMode()) return;
        if (mc.player == null || mc.screen != null) return;

        GuiGraphics gfx   = event.getGuiGraphics();
        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();

        int totalW = SLOTS * SLOT_SIZE + (SLOTS - 1) * SLOT_GAP;
        int baseX  = (screenW - totalW) / 2;
        int baseY  = screenH - 60;

        renderCombatBar(gfx, mc, baseX, baseY, totalW);
    }

    private static void renderCombatBar(GuiGraphics gfx, Minecraft mc,
                                        int baseX, int baseY, int totalW) {
        // ── Fond global ────────────────────────────────────────────────────
        int panelX = baseX - 10;
        int panelY = baseY - 18;
        int panelW = totalW + 20;
        int panelH = SLOT_SIZE + 28;

        // Ombre
        gfx.fill(panelX + 3, panelY + 3, panelX + panelW + 3, panelY + panelH + 3, 0x88000000);
        // Corps
        gfx.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xCC0A0A0F);
        // Bordures
        drawBorder(gfx, panelX - 1, panelY - 1, panelW + 2, panelH + 2, 0xFF886600);
        drawBorder(gfx, panelX - 2, panelY - 2, panelW + 4, panelH + 4, 0xFF050508);
        drawCorners(gfx, panelX - 1, panelY - 1, panelW + 2, panelH + 2, 0xFFFFCC00);

        // ── Barre de stamina ───────────────────────────────────────────────
        int stX = panelX + 6;
        int stY = panelY + 5;
        int stW = panelW - 12;

        float pct = Math.max(0f, Math.min(1f, stamina / maxStamina));
        int fillW = (int)(stW * pct);

        // Fond barre
        drawBorder(gfx, stX - 1, stY - 1, stW + 2, BAR_H + 2, 0xFF886600);
        gfx.fill(stX, stY, stX + stW, stY + BAR_H, 0xFF111111);

        // Remplissage avec dégradé bleu → cyan (style OPM)
        if (fillW > 0) {
            int half = BAR_H / 2;
            gfx.fill(stX, stY,        stX + fillW, stY + half,  0xFF00AADD);
            gfx.fill(stX, stY + half, stX + fillW, stY + BAR_H, 0xFF0077AA);
            gfx.fill(stX, stY,        stX + fillW, stY + 1,     0xFF88EEFF);
        }

        // Texte stamina
        String stLabel = "⚡ " + (int)stamina + " / " + (int)maxStamina;
        int labelW = mc.font.width(stLabel);
        gfx.drawString(mc.font, stLabel,
                stX + stW - labelW, stY - 10, 0xFF88CCFF, true);

        // ── Slots de techniques ────────────────────────────────────────────
        for (int i = 0; i < SLOTS; i++) {
            int sx = baseX + i * (SLOT_SIZE + SLOT_GAP);
            int sy = baseY;

            // Touche associée
            String[] keys = {"&", "é", "\"", "'", "(", "-"};
            boolean isActive = false; // à brancher plus tard

            // Fond slot
            gfx.fill(sx, sy, sx + SLOT_SIZE, sy + SLOT_SIZE,
                    isActive ? 0xFF141428 : 0xFF0A0A18);

            // Bordure slot
            drawBorder(gfx, sx - 1, sy - 1, SLOT_SIZE + 2, SLOT_SIZE + 2,
                    isActive ? 0xFFFFCC00 : 0xFF444466);
            if (isActive) {
                drawCorners(gfx, sx - 1, sy - 1, SLOT_SIZE + 2, SLOT_SIZE + 2, 0xFFFFEE44);
            }

            // Numéro de touche en haut à gauche
            gfx.drawString(mc.font, "§8" + keys[i], sx + 2, sy + 2, 0xFF555577, false);

            // Placeholder "vide"
            gfx.drawCenteredString(mc.font, "§8+",
                    sx + SLOT_SIZE / 2, sy + SLOT_SIZE / 2 - 4, 0xFF222244);

            // Contour lumineux si actif
            if (isActive) {
                gfx.fill(sx, sy, sx + SLOT_SIZE, sy + 1, 0x88FFCC00);
                gfx.fill(sx, sy + SLOT_SIZE - 1, sx + SLOT_SIZE, sy + SLOT_SIZE, 0x88FFCC00);
            }
        }

        // ── Indicateur MODE COMBAT ─────────────────────────────────────────
        String modeLabel = "§6⚔ MODE COMBAT";
        gfx.drawCenteredString(mc.font, modeLabel,
                baseX + totalW / 2, panelY - 12, 0xFFFFCC00);
    }

    // ── Helpers ───────────────────────────────────────────────────────────
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