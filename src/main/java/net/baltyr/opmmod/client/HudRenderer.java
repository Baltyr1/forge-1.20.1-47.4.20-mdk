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
import com.mojang.blaze3d.vertex.PoseStack;

@Mod.EventBusSubscriber(modid = OpmMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class HudRenderer {

    private static final int BAR_W = 100; // plus long
    private static final int BAR_H = 5; // plus compact
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
        int screenH = mc.getWindow().getGuiScaledHeight();

        // Position exacte des cœurs vanilla
        int x = 9;
        int y = screenH - 39;

        // ── Couleurs dynamiques selon le % de vie ─────────────────────────────
        // vert (100%) → jaune (50%) → rouge (0%)
        int fillTop, fillBot, fillShine;
        if (pct > 0.5f) {
            // Vert → Jaune
            float t = (pct - 0.5f) * 2f; // 1.0 à 0.0
            int r = lerp(0xFF, 0x22, t);
            int g = lerp(0xCC, 0xAA, t);
            int b = 0x11;
            fillTop = rgb(r, g, b);
            fillBot = rgb((int) (r * 0.6f), (int) (g * 0.6f), b);
            fillShine = rgb(Math.min(255, r + 60), Math.min(255, g + 40), b + 20);
        } else {
            // Jaune → Rouge
            float t = pct * 2f; // 1.0 à 0.0
            int r = 0xFF;
            int g = lerp(0xAA, 0x00, t);
            int b = 0x00;
            fillTop = rgb(r, g, b);
            fillBot = rgb((int) (r * 0.6f), (int) (g * 0.6f), b);
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
        gfx.fill(x - 1, y - 1, x + 2, y, gold);
        gfx.fill(x - 1, y - 1, x, y + 2, gold);
        gfx.fill(x + BAR_W - 1, y - 1, x + BAR_W + 1, y, gold);
        gfx.fill(x + BAR_W, y - 1, x + BAR_W + 1, y + 2, gold);
        gfx.fill(x - 1, y + BAR_H, x + 2, y + BAR_H + 1, gold);
        gfx.fill(x - 1, y + BAR_H - 1, x, y + BAR_H + 1, gold);
        gfx.fill(x + BAR_W - 1, y + BAR_H, x + BAR_W + 1, y + BAR_H + 1, gold);
        gfx.fill(x + BAR_W, y + BAR_H - 1, x + BAR_W + 1, y + BAR_H + 1, gold);

        // ── Texte ─────────────────────────────────────────────────────────────
        String label = "\u2665 " + (int) health + "/" + (int) maxHealth;
        gfx.drawString(mc.font, label, x, y - 10, 0xFFFFDDDD, true);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private static int lerp(int a, int b, float t) {
        return (int) (a * t + b * (1f - t));
    }

    private static int rgb(int r, int g, int b) {
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }
}