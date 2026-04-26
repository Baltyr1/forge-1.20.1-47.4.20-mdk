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

    // Animation slide depuis la gauche
    private static float slideAnim = 0f;
    private static final float ANIM_SPEED = 0.15f;

    @SubscribeEvent
    public static void onRenderHotbar(RenderGuiOverlayEvent.Pre event) {
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        boolean combat = CombatModeHandler.isInCombatMode();

        // Animation
        float target = combat ? 1f : 0f;
        slideAnim += (target - slideAnim) * ANIM_SPEED;

        // Si on n'est pas du tout en mode combat, on laisse le rendu vanilla
        if (slideAnim < 0.01f) return;

        // Annule la hotbar vanilla
        event.setCanceled(true);

        GuiGraphics gfx    = event.getGuiGraphics();
        int screenH = mc.getWindow().getGuiScaledHeight();

        int totalH  = SLOTS * SLOT_SIZE + (SLOTS - 1) * SLOT_GAP;
        int baseY   = (screenH - totalH) / 2;

        // Slide depuis la gauche : -SLOT_SIZE-10 → 6
        int offscreenX = -(SLOT_SIZE + 14);
        int onscreenX  = 6;
        int baseX = (int)(offscreenX + (onscreenX - offscreenX) * slideAnim);

        // Fond du panel
        int panelX = baseX - 4;
        int panelY = baseY - 4;
        int panelW = SLOT_SIZE + 8;
        int panelH = totalH + 8;

        gfx.fill(panelX + 2, panelY + 2, panelX + panelW + 2, panelY + panelH + 2, 0x88000000);
        gfx.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xCC0A0A0F);
        drawBorder(gfx, panelX - 1, panelY - 1, panelW + 2, panelH + 2, 0xFF886600);
        drawBorder(gfx, panelX - 2, panelY - 2, panelW + 4, panelH + 4, 0xFF050508);
        drawCorners(gfx, panelX - 1, panelY - 1, panelW + 2, panelH + 2, 0xFFFFCC00);

        int selected = mc.player.getInventory().selected;

        for (int i = 0; i < SLOTS; i++) {
            int sx = baseX;
            int sy = baseY + i * (SLOT_SIZE + SLOT_GAP);

            boolean isSelected = (i == selected);

            // Fond slot
            gfx.fill(sx, sy, sx + SLOT_SIZE, sy + SLOT_SIZE,
                    isSelected ? 0xFF1A1A28 : 0xFF0A0A18);

            // Bordure slot
            drawBorder(gfx, sx - 1, sy - 1, SLOT_SIZE + 2, SLOT_SIZE + 2,
                    isSelected ? 0xFFFFCC00 : 0xFF333355);
            if (isSelected)
                drawCorners(gfx, sx - 1, sy - 1, SLOT_SIZE + 2, SLOT_SIZE + 2, 0xFFFFEE44);

            // Item
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                gfx.renderItem(stack, sx + 2, sy + 2);
                gfx.renderItemDecorations(mc.font, stack, sx + 2, sy + 2);
            }

            // Numéro du slot (1-9)
            if (!isSelected) {
                gfx.drawString(mc.font, String.valueOf(i + 1),
                        sx + SLOT_SIZE - 6, sy + 1, 0xFF333355, false);
            }
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
}