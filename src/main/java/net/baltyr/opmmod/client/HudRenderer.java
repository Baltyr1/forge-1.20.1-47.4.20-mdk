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

    private static final int BAR_W = 100;
    private static final int BAR_H = 5;
    private static final int SEGMENTS = 10;

    @SubscribeEvent
    public static void onRenderHealth(RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay() != VanillaGuiOverlay.PLAYER_HEALTH.type())
            return;
        event.setCanceled(true);

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null)
            return;
        Player player = mc.player;

        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();
        float pct = Math.max(0f, Math.min(1f, health / maxHealth));

        GuiGraphics gfx = event.getGuiGraphics();
        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();

        // ── Centré horizontalement, en bas ───────────────────────────────────
        int x = (screenW - BAR_W) / 2;
        int y = screenH - 39;

        // ── Couleurs dynamiques : vert → jaune → rouge ────────────────────────
        int fillTop, fillBot, fillShine;
        if (pct > 0.5f) {
            // Vert pur (100%) → Jaune (50%)
            float t = (pct - 0.5f) * 2f; // 1.0 à pleine vie, 0.0 à mi-vie
            int r = (int) (0xAA * (1f - t) + 0x11 * t); // rouge monte vers jaune
            int g = 0xCC;                                 // vert reste stable
            int b = 0x11;
            fillTop  = rgb(r, g, b);
            fillBot  = rgb((int)(r * 0.6f), (int)(g * 0.6f), b);
            fillShine = rgb(Math.min(255, r + 40), Math.min(255, g + 30), b + 10);
        } else {
            // Jaune (50%) → Rouge (0%)
            float t = pct * 2f; // 1.0 à mi-vie, 0.0 à 0 vie
            int r = 0xFF;
            int g = (int) (0xAA * t); // vert disparaît
            int b = 0x00;
            fillTop  = rgb(r, g, b);
            fillBot  = rgb((int)(r * 0.6f), (int)(g * 0.6f), b);
            fillShine = rgb(255, Math.min(255, g + 60), 40);
        }

        // ── Bordure (or RPG) ──────────────────────────────────────────────────
        gfx.fill(x - 2, y - 2, x + BAR_W + 2, y + BAR_H + 2, 0xFF0D0D00);
        gfx.fill(x - 1, y - 1, x + BAR_W + 1, y + BAR_H + 1, 0xFFAA8800);

        // ── Fond ──────────────────────────────────────────────────────────────
        gfx.fill(x, y, x + BAR_W, y + BAR_H, 0xFF111111);

        // ── Remplissage dégradé ───────────────────────────────────────────────
        int fillW = (int) (BAR_W * pct);
        if (fillW > 0) {
            int half = BAR_H / 2;
            gfx.fill(x, y + half, x + fillW, y + BAR_H, fillBot);
            gfx.fill(x, y, x + fillW, y + half, fillTop);
            gfx.fill(x, y, x + fillW, y + 1, fillShine);
        }

        // ── Séparateurs ───────────────────────────────────────────────────────
        for (int i = 1; i < SEGMENTS; i++) {
            int sx = x + (BAR_W * i / SEGMENTS);
            gfx.fill(sx, y, sx + 1, y + BAR_H, 0x77000000);
        }

        // ── Coins dorés ───────────────────────────────────────────────────────
        int gold = 0xFFFFCC00;
        gfx.fill(x - 1, y - 1, x + 2, y,          gold);
        gfx.fill(x - 1, y - 1, x,     y + 2,       gold);
        gfx.fill(x + BAR_W - 1, y - 1, x + BAR_W + 1, y,     gold);
        gfx.fill(x + BAR_W,     y - 1, x + BAR_W + 1, y + 2, gold);
        gfx.fill(x - 1, y + BAR_H, x + 2,         y + BAR_H + 1, gold);
        gfx.fill(x - 1, y + BAR_H - 1, x,         y + BAR_H + 1, gold);
        gfx.fill(x + BAR_W - 1, y + BAR_H, x + BAR_W + 1, y + BAR_H + 1, gold);
        gfx.fill(x + BAR_W,     y + BAR_H - 1, x + BAR_W + 1, y + BAR_H + 1, gold);

        // ── Texte ─────────────────────────────────────────────────────────────
        String label = "\u2665 " + (int) health + "/" + (int) maxHealth;
        int labelX = x + (BAR_W - mc.font.width(label)) / 2; // centré sur la barre
        gfx.drawString(mc.font, label, labelX, y - 10, 0xFFFFDDDD, true);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private static int rgb(int r, int g, int b) {
        return 0xFF000000 | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }
}