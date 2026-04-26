package net.baltyr.opmmod.client.screen;

import net.baltyr.opmmod.classes.ModCapabilities;
import net.baltyr.opmmod.classes.OpmClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class ClassScreen extends Screen {

    private static final int WIN_W = 340;
    private static final int WIN_H = 240;
    private static final int TAB_W = 90;
    private static final int TAB_H = 16;

    private int activeTab = 0;
    private int wx, wy;

    private int scrollOffset = 0;
    private static final int VISIBLE_CLASSES = 5;
    private static final int ROW_H = 14;

    public ClassScreen() {
        super(Component.literal("Class Menu"));
    }

    @Override
    public boolean isPauseScreen() { return false; }

    private OpmClass getPlayerClass() {
        Player player = Minecraft.getInstance().player;
        if (player == null) return OpmClass.NONE;
        return player.getCapability(ModCapabilities.PLAYER_CLASS)
                .map(cap -> cap.getPlayerClass())
                .orElse(OpmClass.NONE);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (mx >= wx && mx < wx + TAB_W && my >= wy - TAB_H && my < wy) {
            activeTab = 0; return true;
        }
        if (mx >= wx + TAB_W + 4 && mx < wx + TAB_W * 2 + 4 && my >= wy - TAB_H && my < wy) {
            activeTab = 1; return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        if (activeTab == 0) {
            int maxScroll = Math.max(0, OpmClass.values().length - 1 - VISIBLE_CLASSES);
            scrollOffset = (int) Math.max(0, Math.min(maxScroll, scrollOffset - delta));
            return true;
        }
        return super.mouseScrolled(mx, my, delta);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float delta) {
        this.renderBackground(gfx);

        wx = (this.width  - WIN_W) / 2;
        wy = (this.height - WIN_H) / 2;

        gfx.fill(wx + 5, wy + 5, wx + WIN_W + 5, wy + WIN_H + 5, 0xAA000000);
        gfx.fill(wx, wy, wx + WIN_W, wy + WIN_H, 0xFF0A0A0F);
        gfx.fill(wx + 8, wy + 20, wx + WIN_W - 8, wy + 21, 0xFF886600);
        drawBorder(gfx, wx - 2, wy - 2, WIN_W + 4, WIN_H + 4, 0xFF050508);
        drawBorder(gfx, wx - 1, wy - 1, WIN_W + 2, WIN_H + 2, 0xFF886600);
        drawCorners(gfx, wx - 1, wy - 1, WIN_W + 2, WIN_H + 2, 0xFFFFCC00);

        renderTab(gfx, wx,             wy - TAB_H, "✦ Classe",     0, mouseX, mouseY);
        renderTab(gfx, wx + TAB_W + 4, wy - TAB_H, "⚔ Techniques", 1, mouseX, mouseY);

        String title = activeTab == 0 ? "✦ Ma Classe" : "⚔ Techniques";
        gfx.drawCenteredString(this.font, title, wx + WIN_W / 2, wy + 6, 0xFFFFCC44);

        if (activeTab == 0) renderClassTab(gfx, mouseX, mouseY);
        else                renderTechniqueTab(gfx, mouseX, mouseY);

        super.render(gfx, mouseX, mouseY, delta);
    }

    // ─────────────────────────────────────────────────────────────────────
    // ONGLET CLASSE
    // ─────────────────────────────────────────────────────────────────────
    private void renderClassTab(GuiGraphics gfx, int mouseX, int mouseY) {
        OpmClass currentClass = getPlayerClass();
        int cy = wy + 28;
        int px = wx + 14;

        gfx.fill(px, cy, px + 52, cy + 52, 0xFF121220);
        drawBorder(gfx, px - 1, cy - 1, 54, 54, 0xFF886600);
        drawCorners(gfx, px - 1, cy - 1, 54, 54, 0xFFFFCC00);

        Player player = Minecraft.getInstance().player;
        if (player != null) {
            float deltaX = (float)(px + 26) - mouseX;
            float deltaY = (float)(cy + 26) - mouseY;

            float bodyYRot = (float) Math.atan(deltaX / 40.0f) * 20.0f;
            float headYRot = (float) Math.atan(deltaX / 40.0f) * 40.0f;
            float headXRot = -(float) Math.atan(deltaY / 40.0f) * 20.0f;

            float savedYBodyRot  = player.yBodyRot;
            float savedYBodyRotO = player.yBodyRotO;
            float savedYRot      = player.getYRot();
            float savedXRot      = player.getXRot();
            float savedYHeadRot  = player.yHeadRot;
            float savedYHeadRotO = player.yHeadRotO;

            player.yBodyRot  = 180.0f + bodyYRot;
            player.yBodyRotO = 180.0f + bodyYRot;
            player.setYRot(180.0f + headYRot);
            player.setXRot(headXRot);
            player.yHeadRot  = player.getYRot();
            player.yHeadRotO = player.getYRot();

            org.joml.Quaternionf rotation  = new org.joml.Quaternionf().rotateZ((float) Math.PI);
            org.joml.Quaternionf cameraRot = new org.joml.Quaternionf();

            int guiScale = (int) Minecraft.getInstance().getWindow().getGuiScale();
            int screenH  = Minecraft.getInstance().getWindow().getHeight();
            com.mojang.blaze3d.systems.RenderSystem.enableScissor(
                    px * guiScale,
                    screenH - (cy + 52) * guiScale,
                    52 * guiScale,
                    52 * guiScale
            );

            InventoryScreen.renderEntityInInventory(
                    gfx, px + 26, cy + 46, 18, rotation, cameraRot, player
            );

            com.mojang.blaze3d.systems.RenderSystem.disableScissor();

            player.yBodyRot  = savedYBodyRot;
            player.yBodyRotO = savedYBodyRotO;
            player.setYRot(savedYRot);
            player.setXRot(savedXRot);
            player.yHeadRot  = savedYHeadRot;
            player.yHeadRotO = savedYHeadRotO;
        }

        int tx = px + 62;
        String classLabel = currentClass != OpmClass.NONE
                ? currentClass.getFormattedName() : "§7Aucune";
        gfx.drawString(this.font, "§6Classe  §8: " + classLabel,  tx, cy,      0xFFDDDDDD, false);
        gfx.drawString(this.font, "§6Rang    §8: §7—",             tx, cy + 12, 0xFFDDDDDD, false);
        gfx.drawString(this.font, "§6XP      §8: §70 §8/ §7100",   tx, cy + 24, 0xFFDDDDDD, false);

        int bx = tx;
        int by = cy + 38;
        int bw = WIN_W - 100;
        drawBorder(gfx, bx - 1, by - 1, bw + 2, 8, 0xFF886600);
        gfx.fill(bx, by, bx + bw, by + 6, 0xFF111111);

        int descY = cy + 56;
        gfx.fill(wx + 8, descY, wx + WIN_W - 8, descY + 1, 0xFF222233);

        if (currentClass != OpmClass.NONE && !currentClass.getDescription().isEmpty()) {
            gfx.drawString(this.font, "§6Description", wx + 14, descY + 4, 0xFFAA8800, false);
            java.util.List<net.minecraft.util.FormattedCharSequence> lines =
                    this.font.split(Component.literal("§7" + currentClass.getDescription()), WIN_W - 28);
            for (int i = 0; i < Math.min(lines.size(), 3); i++) {
                gfx.drawString(this.font, lines.get(i), wx + 14, descY + 16 + i * 10, 0xFFCCCCCC, false);
            }
        } else {
            gfx.drawCenteredString(this.font,
                    "§8Aucune classe sélectionnée",
                    wx + WIN_W / 2, descY + 12, 0xFF444455);
        }

        int sep = descY + 50;
        gfx.fill(wx + 8, sep, wx + WIN_W - 8, sep + 1, 0xFF222233);
        gfx.drawString(this.font, "§6Toutes les classes", wx + 14, sep + 5, 0xFFAA8800, false);

        int listX = wx + 14;
        int listY = sep + 18;
        int listW = WIN_W - 32;
        int listH = VISIBLE_CLASSES * ROW_H + 2;

        gfx.fill(listX - 1, listY - 1, listX + listW + 1, listY + listH + 1, 0xFF050510);
        drawBorder(gfx, listX - 1, listY - 1, listW + 2, listH + 2, 0xFF222233);

        OpmClass[] classes = OpmClass.values();

        for (int i = 0; i < VISIBLE_CLASSES; i++) {
            int idx = i + scrollOffset + 1;
            if (idx >= classes.length) break;

            OpmClass cls = classes[idx];
            int rowY = listY + i * ROW_H;

            boolean isHovered = mouseX >= listX && mouseX < listX + listW
                    && mouseY >= rowY && mouseY < rowY + ROW_H;
            boolean isCurrent = cls == currentClass;

            if (isCurrent)
                gfx.fill(listX, rowY, listX + listW, rowY + ROW_H - 1, 0xFF1A1A10);
            else if (isHovered)
                gfx.fill(listX, rowY, listX + listW, rowY + ROW_H - 1, 0xFF111120);

            gfx.fill(listX + 3, rowY + 5, listX + 7, rowY + 9,
                    isCurrent ? 0xFFFFCC00 : 0xFF444455);

            String name = isCurrent
                    ? "§l" + cls.getFormattedName() + " §r§8◄"
                    : cls.getFormattedName();
            gfx.drawString(this.font, name, listX + 12, rowY + 3, 0xFFCCCCCC, false);

            if (i < VISIBLE_CLASSES - 1)
                gfx.fill(listX + 2, rowY + ROW_H - 1, listX + listW - 2, rowY + ROW_H, 0xFF1A1A2A);
        }

        int maxScroll = Math.max(1, classes.length - 1 - VISIBLE_CLASSES);
        if (maxScroll > 0) {
            int sbX    = listX + listW + 3;
            int thumbH = Math.max(10, listH * VISIBLE_CLASSES / (classes.length - 1));
            int thumbY = listY + (listH - thumbH) * scrollOffset / maxScroll;
            gfx.fill(sbX, listY, sbX + 3, listY + listH, 0xFF111118);
            gfx.fill(sbX, thumbY, sbX + 3, thumbY + thumbH, 0xFF886600);
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // ONGLET TECHNIQUES
    // ─────────────────────────────────────────────────────────────────────
    private void renderTechniqueTab(GuiGraphics gfx, int mouseX, int mouseY) {
        OpmClass currentClass = getPlayerClass();
        int cy = wy + 28;

        gfx.drawString(this.font, "§6Techniques disponibles", wx + 14, cy, 0xFFAA8800, false);
        gfx.fill(wx + 8, cy + 11, wx + WIN_W - 8, cy + 12, 0xFF222233);

        if (currentClass == OpmClass.NONE) {
            gfx.drawCenteredString(this.font,
                    "§7Aucune technique disponible",
                    wx + WIN_W / 2, cy + 40, 0xFF555566);
            gfx.drawCenteredString(this.font,
                    "§8Choisissez d'abord une classe",
                    wx + WIN_W / 2, cy + 54, 0xFF444455);
            return;
        }

        gfx.drawString(this.font,
                "§8Classe : " + currentClass.getFormattedName(),
                wx + 14, cy + 16, 0xFFAAAAAA, false);

        int SLOT_SIZE = 40;
        int SLOT_GAP  = 5;
        int totalW    = 6 * SLOT_SIZE + 5 * SLOT_GAP;
        int startX    = wx + (WIN_W - totalW) / 2;
        int startY    = cy + 34;

        String[] keys = {"&", "é", "\"", "'", "(", "-"};

        for (int i = 0; i < 6; i++) {
            int sx = startX + i * (SLOT_SIZE + SLOT_GAP);
            int sy = startY;

            boolean hov = mouseX >= sx && mouseX < sx + SLOT_SIZE
                    && mouseY >= sy && mouseY < sy + SLOT_SIZE;

            gfx.fill(sx, sy, sx + SLOT_SIZE, sy + SLOT_SIZE,
                    hov ? 0xFF141428 : 0xFF0A0A18);
            drawBorder(gfx, sx - 1, sy - 1, SLOT_SIZE + 2, SLOT_SIZE + 2,
                    hov ? 0xFF886600 : 0xFF333355);
            if (hov) drawCorners(gfx, sx - 1, sy - 1, SLOT_SIZE + 2, SLOT_SIZE + 2, 0xFFFFCC00);

            gfx.drawString(this.font, "§8" + keys[i], sx + 3, sy + 3, 0xFF444466, false);
            gfx.drawCenteredString(this.font, "§8+",
                    sx + SLOT_SIZE / 2, sy + SLOT_SIZE / 2 - 4, 0xFF222244);
            gfx.drawCenteredString(this.font, "§8" + (i + 1),
                    sx + SLOT_SIZE / 2, sy + SLOT_SIZE - 9, 0xFF333355);
        }

        int infoY = startY + SLOT_SIZE + 12;
        gfx.fill(wx + 8, infoY - 2, wx + WIN_W - 8, infoY - 1, 0xFF222233);

        int hoveredSlot = -1;
        for (int i = 0; i < 6; i++) {
            int sx = startX + i * (SLOT_SIZE + SLOT_GAP);
            if (mouseX >= sx && mouseX < sx + SLOT_SIZE
                    && mouseY >= startY && mouseY < startY + SLOT_SIZE) {
                hoveredSlot = i;
                break;
            }
        }

        if (hoveredSlot >= 0) {
            gfx.drawString(this.font,
                    "§6Slot " + (hoveredSlot + 1) + " §8[" + keys[hoveredSlot] + "]",
                    wx + 14, infoY + 4, 0xFFAA8800, false);
            gfx.drawString(this.font,
                    "§7Aucune technique équipée",
                    wx + 14, infoY + 16, 0xFF888899, false);
        } else {
            gfx.drawCenteredString(this.font,
                    "§8Survole un slot pour voir les détails",
                    wx + WIN_W / 2, infoY + 8, 0xFF333355);
        }

        int hintY = wy + WIN_H - 22;
        gfx.fill(wx + 8, hintY - 2, wx + WIN_W - 8, hintY - 1, 0xFF222233);
        gfx.drawCenteredString(this.font,
                "§8Active le mode combat avec §6C §8pour utiliser tes techniques",
                wx + WIN_W / 2, hintY + 4, 0xFF444455);
    }

    // ─────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────
    private void drawBorder(GuiGraphics gfx, int x, int y, int w, int h, int color) {
        gfx.fill(x,         y,         x + w,     y + 1,     color);
        gfx.fill(x,         y + h - 1, x + w,     y + h,     color);
        gfx.fill(x,         y,         x + 1,     y + h,     color);
        gfx.fill(x + w - 1, y,         x + w,     y + h,     color);
    }

    private void drawCorners(GuiGraphics gfx, int x, int y, int w, int h, int color) {
        gfx.fill(x,         y,         x + 3,     y + 1,     color);
        gfx.fill(x,         y,         x + 1,     y + 3,     color);
        gfx.fill(x + w - 3, y,         x + w,     y + 1,     color);
        gfx.fill(x + w - 1, y,         x + w,     y + 3,     color);
        gfx.fill(x,         y + h - 1, x + 3,     y + h,     color);
        gfx.fill(x,         y + h - 3, x + 1,     y + h,     color);
        gfx.fill(x + w - 3, y + h - 1, x + w,     y + h,     color);
        gfx.fill(x + w - 1, y + h - 3, x + w,     y + h,     color);
    }

    private void renderTab(GuiGraphics gfx, int tx, int ty, String label,
                           int tabIdx, int mouseX, int mouseY) {
        boolean active  = (activeTab == tabIdx);
        boolean hovered = mouseX >= tx && mouseX < tx + TAB_W
                && mouseY >= ty && mouseY < ty + TAB_H;

        int bg     = active  ? 0xFF0A0A0F : (hovered ? 0xFF1A1A2A : 0xFF050508);
        int border = active  ? 0xFFFFCC00 : 0xFF886600;
        int text   = active  ? 0xFFFFCC44 : (hovered ? 0xFFAA9933 : 0xFF665522);

        gfx.fill(tx, ty, tx + TAB_W, ty + TAB_H, bg);
        drawBorder(gfx, tx, ty, TAB_W, TAB_H, border);
        if (active)
            gfx.fill(tx + 1, ty + TAB_H - 1, tx + TAB_W - 1, ty + TAB_H, bg);
        gfx.drawCenteredString(this.font, label, tx + TAB_W / 2, ty + 4, text);
    }
}