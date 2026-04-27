package net.baltyr.opmmod.client;

import net.baltyr.opmmod.OpmMod;
import net.baltyr.opmmod.classes.ModCapabilities;
import net.baltyr.opmmod.classes.OpmClass;
import net.baltyr.opmmod.client.abilities.AbilityHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = OpmMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class CombatHud {

    private static final int SLOT_SIZE = 30;
    private static final int SLOT_GAP  = 4;
    private static final int SLOTS     = 6;
    private static final int BAR_H     = 8;

    public static float stamina    = 100f;
    public static float maxStamina = 100f;

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.EXPERIENCE_BAR.type()) return;

        Minecraft mc = Minecraft.getInstance();
        if (!CombatModeHandler.isInCombatMode()) return;
        if (mc.player == null || mc.screen != null) return;

        GuiGraphics gfx = event.getGuiGraphics();
        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();

        int totalW = SLOTS * SLOT_SIZE + (SLOTS - 1) * SLOT_GAP;
        int baseX  = (screenW - totalW) / 2;
        int baseY  = screenH - 55;

        renderCombatBar(gfx, mc, baseX, baseY, totalW);
    }

    private static void renderCombatBar(GuiGraphics gfx, Minecraft mc,
                                        int baseX, int baseY, int totalW) {
        int panelX = baseX - 8;
        int panelY = baseY - 14;
        int panelW = totalW + 16;
        int panelH = SLOT_SIZE + 26;

        gfx.fill(panelX + 3, panelY + 3, panelX + panelW + 3, panelY + panelH + 3, 0x88000000);
        gfx.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xCC0A0A0F);
        drawBorder(gfx, panelX - 1, panelY - 1, panelW + 2, panelH + 2, 0xFF886600);
        drawBorder(gfx, panelX - 2, panelY - 2, panelW + 4, panelH + 4, 0xFF050508);
        drawCorners(gfx, panelX - 1, panelY - 1, panelW + 2, panelH + 2, 0xFFFFCC00);

        // ── Barre de stamina ───────────────────────────────────────────────
        int stX = panelX + 5;
        int stY = panelY + 4;
        int stW = panelW - 10;

        float pct  = Math.max(0f, Math.min(1f, stamina / maxStamina));
        int fillW  = (int)(stW * pct);

        drawBorder(gfx, stX - 1, stY - 1, stW + 2, BAR_H + 2, 0xFF886600);
        gfx.fill(stX, stY, stX + stW, stY + BAR_H, 0xFF111111);

        if (fillW > 0) {
            int half = BAR_H / 2;
            gfx.fill(stX, stY,        stX + fillW, stY + half,  0xFF00AADD);
            gfx.fill(stX, stY + half, stX + fillW, stY + BAR_H, 0xFF0077AA);
            gfx.fill(stX, stY,        stX + fillW, stY + 1,     0xFF88EEFF);
        }

        String stLabel = "⚡ " + (int)stamina + " / " + (int)maxStamina;
        int textY = stY + (BAR_H - 8) / 2;
        gfx.drawCenteredString(mc.font, stLabel, stX + stW / 2, textY, 0xFFFFFFFF);

        // ── Slots ──────────────────────────────────────────────────────────
        String[] keys = {"1", "2", "3", "4", "5", "6"};
        for (int i = 0; i < SLOTS; i++) {
            int sx = baseX + i * (SLOT_SIZE + SLOT_GAP);
            int sy = baseY;

            boolean onCD = i < 5 && AbilityHandler.isOnCooldown(i);
            gfx.fill(sx, sy, sx + SLOT_SIZE, sy + SLOT_SIZE,
                    onCD ? 0xFF1A0A00 : 0xFF0A0A18);
            drawBorder(gfx, sx - 1, sy - 1, SLOT_SIZE + 2, SLOT_SIZE + 2,
                    onCD ? 0xFF884400 : 0xFF444466);

            // Overlay cooldown
            if (onCD) {
                float cdPct = AbilityHandler.getCooldownProgress(i);
                int cdH = (int)(SLOT_SIZE * cdPct);
                gfx.fill(sx, sy, sx + SLOT_SIZE, sy + cdH, 0x99000000);

                int secs = AbilityHandler.getCooldownSeconds(i);
                String cdStr = secs + "s";
                int cx = sx + (SLOT_SIZE - mc.font.width(cdStr)) / 2;
                int cy = sy + SLOT_SIZE / 2 - 4;
                gfx.drawString(mc.font, cdStr, cx, cy, 0xFFFF6600, false);
            }

            // Nom capacité
            // Nom capacité selon la classe
            String[] classAbilityNames = getAbilityNames(mc);
            if (i < classAbilityNames.length && !classAbilityNames[i].isEmpty()) {
                gfx.drawString(mc.font, classAbilityNames[i], sx + 2, sy + 2,
                        onCD ? 0xFF886633 : 0xFF8888AA, false);
            }

            // Touche
            gfx.drawString(mc.font, "§8" + keys[i],
                    sx + SLOT_SIZE - mc.font.width(keys[i]) - 2,
                    sy + SLOT_SIZE - 9,
                    onCD ? 0xFF553300 : 0xFF555577, false);
        }
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
    private static String[] getAbilityNames(Minecraft mc) {
        if (mc.player == null) return new String[]{"", "", "", "", "", ""};

        OpmClass cls = mc.player.getCapability(ModCapabilities.PLAYER_CLASS)
                .map(cap -> cap.getPlayerClass())
                .orElse(OpmClass.NONE);

        return switch (cls) {
            case SAITAMA -> new String[]{"Poing", "Consec.", "Dash", "Table", "Sérieux", ""};
            // Autres classes à venir
            default -> new String[]{"", "", "", "", "", ""};
        };
    }
}