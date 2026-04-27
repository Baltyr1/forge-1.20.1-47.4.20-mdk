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

    // ── Layout constants (utilisés aussi par HudRenderer) ────────────────────
    public static final int PANEL_X = 4;
    public static final int PANEL_W = 34;   // largeur du panneau
    static final        int SLOT_W  = 20;
    static final        int SLOT_H  = 20;
    static final        int SLOT_GAP = 2;
    static final        int SLOTS   = 9;
    private static final int TOTAL_H = SLOTS * SLOT_H + (SLOTS - 1) * SLOT_GAP; // 196
    public  static final int PANEL_H = TOTAL_H + 8;                               // 204

    private static float slideAnim = 0f;
    private static final float ANIM_SPEED = 0.12f;

    public static float getSlideAnim() { return slideAnim; }

    /** Y du bord supérieur du panneau hotbar. */
    public static int getPanelTopY(int screenH) {
        return (screenH - PANEL_H) / 2;
    }

    // ── Rendu ─────────────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onHotbarPre(RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        boolean combat = CombatModeHandler.isInCombatMode();
        slideAnim += ((combat ? 1f : 0f) - slideAnim) * ANIM_SPEED;

        // ← Annule TOUJOURS si en combat ou en transition
        if (slideAnim >= 0.01f) {
            event.setCanceled(true);
        }

        if (slideAnim < 0.01f) return; // pas encore assez animé pour dessiner
        if (mc.screen != null) return;

        GuiGraphics gfx = event.getGuiGraphics();
        int sh = mc.getWindow().getGuiScaledHeight();
        int py = getPanelTopY(sh);

        drawPanel(gfx, mc, py);
        drawLabel(gfx, mc, py);
        drawSlots(gfx, mc, py);
    }

    // ── Panneau ───────────────────────────────────────────────────────────────

    private static void drawPanel(GuiGraphics gfx, Minecraft mc, int py) {
        // Ombre portée
        gfx.fill(PANEL_X + 3, py + 3,
                 PANEL_X + PANEL_W + 3, py + PANEL_H + 3,
                 blend(0x44000000, slideAnim));
        // Fond principal (quasi-noir, légèrement bleuté)
        gfx.fill(PANEL_X, py,
                 PANEL_X + PANEL_W, py + PANEL_H,
                 blend(0xE3010109, slideAnim));
        // Bordure extérieure (noir profond)
        drawBorder(gfx, PANEL_X - 2, py - 2, PANEL_W + 4, PANEL_H + 4,
                blend(0xFF030304, slideAnim));
        // Bordure intérieure (or foncé)
        drawBorder(gfx, PANEL_X - 1, py - 1, PANEL_W + 2, PANEL_H + 2,
                blend(0xFF9C6900, slideAnim));
        // Coins or vifs
        drawCorners(gfx, PANEL_X - 1, py - 1, PANEL_W + 2, PANEL_H + 2,
                blend(0xFFFFCC00, slideAnim));
    }

    private static void drawLabel(GuiGraphics gfx, Minecraft mc, int py) {
        if (slideAnim < 0.4f) return;
        float t = (slideAnim - 0.4f) / 0.6f;

        // Texte "COMBAT"
        String lbl = "COMBAT";
        int lx = PANEL_X + (PANEL_W - mc.font.width(lbl)) / 2;
        gfx.drawString(mc.font, lbl, lx, py - 15, blend(0xFFFFCC00, t), false);

        // Ligne séparatrice or sous le label
        gfx.fill(PANEL_X + 3, py - 4,
                 PANEL_X + PANEL_W - 3, py - 3,
                 blend(0xBBFFCC00, t));
    }

    // ── Slots ─────────────────────────────────────────────────────────────────

    private static void drawSlots(GuiGraphics gfx, Minecraft mc, int py) {
        int sx       = PANEL_X + 7;
        int selected = mc.player.getInventory().selected;

        for (int i = 0; i < SLOTS; i++) {
            int sy  = py + 4 + i * (SLOT_H + SLOT_GAP);
            boolean sel = (i == selected);

            // Fond du slot
            gfx.fill(sx, sy, sx + SLOT_W, sy + SLOT_H,
                    sel ? blend(0xFF141432, slideAnim)
                        : blend(0xFF06060E, slideAnim));

            // Bordure du slot
            drawBorder(gfx, sx - 1, sy - 1, SLOT_W + 2, SLOT_H + 2,
                    sel ? blend(0xFFFFCC00, slideAnim)
                        : blend(0xFF1A1A44, slideAnim));

            if (sel) {
                // Lueur intérieure dorée subtile
                gfx.fill(sx + 1, sy + 1, sx + SLOT_W - 1, sy + SLOT_H - 1,
                        blend(0x12FFCC00, slideAnim));
                // Coins brillants du slot sélectionné
                drawCorners(gfx, sx - 1, sy - 1, SLOT_W + 2, SLOT_H + 2,
                        blend(0xFFFFEE44, slideAnim));
            } else {
                // Numéro de slot (coin haut-droit, très atténué)
                int na = (int)(255 * slideAnim * 0.28f);
                gfx.drawString(mc.font, String.valueOf(i + 1),
                        sx + SLOT_W - 5, sy + 1,
                        (na << 24) | 0x3030A0, false);
            }

            // Item
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                gfx.renderItem(stack, sx + 1, sy + 1);
                gfx.renderItemDecorations(mc.font, stack, sx + 1, sy + 1);
            }
        }
    }

    // ── Utilitaires partagés ──────────────────────────────────────────────────

    /** Multiplie le composant alpha de `color` par `alpha`. */
    public static int blend(int color, float alpha) {
        int a = (int)(((color >> 24) & 0xFF) * alpha);
        return (a << 24) | (color & 0x00FFFFFF);
    }

    static void drawBorder(GuiGraphics g, int x, int y, int w, int h, int c) {
        g.fill(x,         y,         x + w,     y + 1,     c);
        g.fill(x,         y + h - 1, x + w,     y + h,     c);
        g.fill(x,         y + 1,     x + 1,     y + h - 1, c);
        g.fill(x + w - 1, y + 1,     x + w,     y + h - 1, c);
    }

    static void drawCorners(GuiGraphics g, int x, int y, int w, int h, int c) {
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
