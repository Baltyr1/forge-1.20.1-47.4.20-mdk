package net.baltyr.opmmod.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.baltyr.opmmod.client.KeyBindings;

public class ClassScreen extends Screen {

    private static final int WIN_W = 320;
    private static final int WIN_H = 210;
    private static final int TAB_W = 90;
    private static final int TAB_H = 16;

    private int activeTab = 0; // 0 = Classe, 1 = Techniques
    private int wx, wy;

    public ClassScreen() {
        super(Component.literal("Class Menu"));
    }

    @Override
    public boolean isPauseScreen() { return false; }

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
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float delta) {
        this.renderBackground(gfx);

        wx = (this.width  - WIN_W) / 2;
        wy = (this.height - WIN_H) / 2;

        // ── Ombre ─────────────────────────────────────────────────────────
        gfx.fill(wx + 5, wy + 5, wx + WIN_W + 5, wy + WIN_H + 5, 0xAA000000);

        // ── Corps de la fenêtre ───────────────────────────────────────────
        gfx.fill(wx, wy, wx + WIN_W, wy + WIN_H, 0xFF0A0A0F);

        // Légère texture : ligne horizontale de séparation de titre
        gfx.fill(wx + 8, wy + 20, wx + WIN_W - 8, wy + 21, 0xFF886600);

        // ── Bordures ──────────────────────────────────────────────────────
        drawBorder(gfx, wx - 2, wy - 2, WIN_W + 4, WIN_H + 4, 0xFF050508);
        drawBorder(gfx, wx - 1, wy - 1, WIN_W + 2, WIN_H + 2, 0xFF886600);
        drawCorners(gfx, wx - 1, wy - 1, WIN_W + 2, WIN_H + 2, 0xFFFFCC00);

        // ── Onglets ───────────────────────────────────────────────────────
        renderTab(gfx, wx,             wy - TAB_H, "✦ Classe",     0, mouseX, mouseY);
        renderTab(gfx, wx + TAB_W + 4, wy - TAB_H, "⚔ Techniques", 1, mouseX, mouseY);

        // ── Titre ─────────────────────────────────────────────────────────
        String title = activeTab == 0 ? "✦ Ma Classe" : "⚔ Techniques";
        gfx.drawCenteredString(this.font, title, wx + WIN_W / 2, wy + 6, 0xFFFFCC44);

        // ── Contenu ───────────────────────────────────────────────────────
        if (activeTab == 0) renderClassTab(gfx);
        else                renderTechniqueTab(gfx);

        super.render(gfx, mouseX, mouseY, delta);
    }

    // ─────────────────────────────────────────────────────────────────────
    // ONGLET CLASSE
    // ─────────────────────────────────────────────────────────────────────
    private void renderClassTab(GuiGraphics gfx) {
        int cy = wy + 30;

        // ── Portrait / icône de classe ────────────────────────────────────
        int px = wx + 14;
        gfx.fill(px, cy, px + 52, cy + 52, 0xFF121220);
        drawBorder(gfx, px - 1, cy - 1, 54, 54, 0xFF886600);
        drawCorners(gfx, px - 1, cy - 1, 54, 54, 0xFFFFCC00);
        gfx.drawCenteredString(this.font, "§8?", px + 26, cy + 22, 0xFF444455);

        // ── Infos texte ───────────────────────────────────────────────────
        int tx = px + 62;
        gfx.drawString(this.font, "§6Classe  §8: §7Aucune",  tx, cy,      0xFFDDDDDD, false);
        gfx.drawString(this.font, "§6Rang    §8: §7—",       tx, cy + 12, 0xFFDDDDDD, false);
        gfx.drawString(this.font, "§6XP      §8: §70 / 100", tx, cy + 24, 0xFFDDDDDD, false);

        // ── Barre XP ──────────────────────────────────────────────────────
        int bx = tx;
        int by = cy + 38;
        int bw = WIN_W - px - 62 - 14;
        drawBorder(gfx, bx - 1, by - 1, bw + 2, 8, 0xFF886600);
        gfx.fill(bx, by, bx + bw, by + 6, 0xFF111111);
        // barre vide (0 xp), à remplir plus tard :
        // gfx.fill(bx, by, bx + (int)(bw * pct), by + 6, 0xFF8800FF);
        gfx.drawString(this.font, "§8XP", bx + bw + 4, by - 1, 0xFF555566, false);

        // ── Séparateur ────────────────────────────────────────────────────
        int sep = wy + 100;
        gfx.fill(wx + 8, sep, wx + WIN_W - 8, sep + 1, 0xFF222233);

        // ── Description de la classe ──────────────────────────────────────
        gfx.drawString(this.font, "§8Description", wx + 14, sep + 6, 0xFF444455, false);
        gfx.fill(wx + 14, sep + 18, wx + WIN_W - 14, sep + 19, 0xFF1A1A2A);

        // Bloc de texte de description (placeholder)
        String[] lines = {
                "§7Aucune classe sélectionnée.",
                "§8Revenez ici après avoir choisi",
                "§8une classe pour voir ses détails."
        };
        for (int i = 0; i < lines.length; i++) {
            gfx.drawString(this.font, lines[i], wx + 14, sep + 22 + i * 11, 0xFFCCCCCC, false);
        }

        // ── Hint bas ──────────────────────────────────────────────────────
        gfx.fill(wx + 8, wy + WIN_H - 22, wx + WIN_W - 8, wy + WIN_H - 21, 0xFF222233);
        gfx.drawCenteredString(this.font,
                "§8Onglet §6⚔ Techniques §8pour gérer tes sorts",
                wx + WIN_W / 2, wy + WIN_H - 14, 0xFF555566);
    }

    // ─────────────────────────────────────────────────────────────────────
    // ONGLET TECHNIQUES
    // ─────────────────────────────────────────────────────────────────────
    private void renderTechniqueTab(GuiGraphics gfx) {
        int cy = wy + 30;

        // ── Titre section ─────────────────────────────────────────────────
        gfx.drawString(this.font, "§6Techniques disponibles", wx + 14, cy, 0xFFAA8800, false);
        gfx.fill(wx + 8, cy + 11, wx + WIN_W - 8, cy + 12, 0xFF222233);

        // ── Grille 4×2 (emplacements vides) ──────────────────────────────
        int cols = 4;
        int rows = 2;
        int slotSize = 40;
        int gap = 6;
        int gridW = cols * slotSize + (cols - 1) * gap;
        int gx = wx + (WIN_W - gridW) / 2 - 30;
        int gy = cy + 18;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int sx = gx + col * (slotSize + gap);
                int sy = gy + row * (slotSize + gap);
                gfx.fill(sx, sy, sx + slotSize, sy + slotSize, 0xFF0D0D18);
                drawBorder(gfx, sx - 1, sy - 1, slotSize + 2, slotSize + 2, 0xFF2A2A3A);
                gfx.drawCenteredString(this.font, "§8+", sx + slotSize / 2, sy + slotSize / 2 - 4, 0xFF2A2A3A);
            }
        }

        // ── Zone équipement (droite) ──────────────────────────────────────
        int ex = gx + gridW + 14;
        int ey = gy;

        gfx.drawCenteredString(this.font, "§6Équipé", ex + 22, ey - 10, 0xFFAA8800);

        // Slot équipé
        gfx.fill(ex, ey, ex + 44, ey + 44, 0xFF0A0A14);
        drawBorder(gfx, ex - 1, ey - 1, 46, 46, 0xFF886600);
        drawCorners(gfx, ex - 1, ey - 1, 46, 46, 0xFFFFCC00);
        gfx.drawCenteredString(this.font, "§8Vide", ex + 22, ey + 18, 0xFF333344);

        // Slot 2
        int ey2 = ey + 52;
        gfx.drawCenteredString(this.font, "§8Slot 2", ex + 22, ey2 - 10, 0xFF333344);
        gfx.fill(ex, ey2, ex + 44, ey2 + 44, 0xFF080810);
        drawBorder(gfx, ex - 1, ey2 - 1, 46, 46, 0xFF333344);
        gfx.drawCenteredString(this.font, "§8Vide", ex + 22, ey2 + 18, 0xFF222233);

        // ── Hint bas ─────────────────────────────────────────────────────
        gfx.fill(wx + 8, wy + WIN_H - 22, wx + WIN_W - 8, wy + WIN_H - 21, 0xFF222233);
        gfx.drawCenteredString(this.font,
                "§8Clique sur un sort pour l'équiper",
                wx + WIN_W / 2, wy + WIN_H - 14, 0xFF555566);
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
        if (active) {
            // Fusionne l'onglet actif avec la fenêtre (efface la bordure basse)
            gfx.fill(tx + 1, ty + TAB_H - 1, tx + TAB_W - 1, ty + TAB_H, bg);
        }
        gfx.drawCenteredString(this.font, label, tx + TAB_W / 2, ty + 4, text);
    }
}