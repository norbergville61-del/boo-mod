package dev.boo.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Draws Boo, a pixel ghost, on the title screen using only filled rectangles.
 * Timeline (seconds since the title screen opened):
 *   0.0 - 1.8  asleep, slow bob, floating z's
 *   1.8 - 2.0  wakes up with a little hop
 *   2.0 - 3.4  swoops in a loop
 *   3.4 +      idle float, blinks, waves when you hover him
 */
public final class GhostRenderer {
	private GhostRenderer() {}

	// Colors (ARGB)
	private static final int GHOST = 0xFFF2FBF8;
	private static final int GHOST_SHADE = 0xFFC9E6DE;
	private static final int INK = 0xFF0B1418;
	private static final int CHEEK = 0xFFFFC9A8;
	private static final int GLOW = 0x229FF0D4;
	private static final int MINT = 0x9FF0D4;
	private static final int PEACH = 0xFFC9A8;
	private static final int BUBBLE_BG = 0xE612222A;
	private static final int BUBBLE_EDGE = 0xFF5FB9A0;
	private static final int TEXT = 0xFFD9EBE6;

	// 14 x 14 body. '#' = ghost, 's' = soft shade on the right side
	private static final String[] BODY = {
		".....####.....",
		"...########...",
		"..##########..",
		".###########s.",
		".###########s.",
		"############ss",
		"############ss",
		"############ss",
		"############ss",
		"############ss",
		"############ss",
		"############ss",
		"############ss",
		"############ss",
	};
	// Wavy bottom, two frames that alternate
	private static final String[][] TAIL = {
		{ "###.####.####s", ".#...##...##.." },
		{ "##.####.####ss", "#...##...##..s" },
	};

	private static final String[] IDLE_LINES = {
		"lanterns on.",
		"no rush tonight.",
		"watch for creepers.",
		"nice seed.",
		"one more chunk?",
	};

	private static final int COLS = 14;
	private static final int ROWS = 16;

	public static void draw(GuiGraphics g, int screenW, int screenH, int mouseX, int mouseY, double t) {
		Minecraft mc = Minecraft.getInstance();
		Font font = mc.font;

		drawFireflies(g, screenW, screenH, t);

		int s = Math.max(2, Math.min(5, screenH / 60)); // size of one "pixel"
		int w = COLS * s, h = ROWS * s;

		// Home position: middle of the empty space left of the menu buttons
		int leftGap = screenW / 2 - 110;
		int homeX = leftGap > w + 20 ? leftGap / 2 - w / 2 : 8;
		int homeY = screenH / 2 - h / 2 + 10;

		boolean asleep = t < 1.8;
		double dx = 0, dy;
		if (asleep) {
			dy = Math.sin(t * 1.4) * 2;
		} else if (t < 2.0) {
			double p = (t - 1.8) / 0.2;
			dy = -Math.sin(p * Math.PI) * 8;
		} else if (t < 3.4) {
			double p = (t - 2.0) / 1.4;
			double fade = 1 - p;
			dx = Math.sin(p * Math.PI * 2) * 45 * fade;
			dy = -Math.sin(p * Math.PI) * 35 + Math.sin(p * Math.PI * 4) * 6 * fade;
		} else {
			dx = Math.sin(t * 0.7) * 3;
			dy = Math.sin(t * 2.2) * 4;
		}
		int x = homeX + (int) Math.round(dx);
		int y = homeY + (int) Math.round(dy);

		boolean hovered = !asleep && mouseX >= x && mouseX < x + w + s * 2 && mouseY >= y && mouseY < y + h;
		boolean blinking = !asleep && !hovered && (t % 4.2) < 0.13;
		boolean waving = hovered || (t > 3.4 && t < 5.2);
		int tailFrame = (int) (t * 4) % 2;

		// Shadow under him, shrinks as he rises
		int shadowW = (int) (w * (0.75 - Math.max(-0.2, Math.min(0.2, -dy / 60.0))));
		int shadowX = x + w / 2 - shadowW / 2, shadowY = homeY + h + s * 2;
		g.fill(shadowX, shadowY, shadowX + shadowW, shadowY + s, 0x55000000);
		g.fill(shadowX + s, shadowY - 1, shadowX + shadowW - s, shadowY + s + 1, 0x33000000);

		// Soft glow behind the body
		forEachCell(t, tailFrame, (cx, cy, c) -> g.fill(x + cx * s - 2, y + cy * s - 2, x + (cx + 1) * s + 2, y + (cy + 1) * s + 2, GLOW));

		// Body
		forEachCell(t, tailFrame, (cx, cy, c) -> g.fill(x + cx * s, y + cy * s, x + (cx + 1) * s, y + (cy + 1) * s, c == 's' ? GHOST_SHADE : GHOST));

		// Waving arm on the right side
		if (waving && (int) (t * 5) % 2 == 0) {
			px(g, x, y, s, 14, 4, GHOST); px(g, x, y, s, 14, 5, GHOST); px(g, x, y, s, 15, 3, GHOST);
		} else {
			px(g, x, y, s, 14, 8, GHOST); px(g, x, y, s, 14, 9, GHOST); px(g, x, y, s, 15, 9, GHOST);
		}

		// Eyes: look toward the mouse when awake
		int look = 0;
		if (!asleep) {
			int center = x + w / 2;
			look = mouseX < center - 30 ? -1 : mouseX > center + 30 ? 1 : 0;
		}
		int lookPx = look * Math.max(1, s / 2);
		if (asleep || blinking) {
			// closed eyes: a flat line
			g.fill(x + 3 * s, y + 6 * s, x + 5 * s, y + 6 * s + Math.max(1, s / 2), INK);
			g.fill(x + 9 * s, y + 6 * s, x + 11 * s, y + 6 * s + Math.max(1, s / 2), INK);
		} else {
			g.fill(x + 3 * s + lookPx, y + 5 * s, x + 5 * s + lookPx, y + 7 * s, INK);
			g.fill(x + 9 * s + lookPx, y + 5 * s, x + 11 * s + lookPx, y + 7 * s, INK);
			// tiny highlight
			int hl = Math.max(1, s / 2);
			g.fill(x + 3 * s + lookPx, y + 5 * s, x + 3 * s + lookPx + hl, y + 5 * s + hl, GHOST);
			g.fill(x + 9 * s + lookPx, y + 5 * s, x + 9 * s + lookPx + hl, y + 5 * s + hl, GHOST);
		}

		// Cheeks when awake
		if (!asleep) {
			g.fill(x + 1 * s, y + 8 * s, x + 3 * s, y + 9 * s, CHEEK);
			g.fill(x + 11 * s, y + 8 * s, x + 13 * s, y + 9 * s, CHEEK);
		}

		// Mouth
		if (asleep) {
			g.fill(x + 6 * s, y + 9 * s, x + 8 * s, y + 9 * s + Math.max(1, s / 2), INK);
		} else if (hovered) {
			g.fill(x + 6 * s, y + 8 * s, x + 8 * s, y + 10 * s, INK); // "o!"
		} else {
			px(g, x, y, s, 5, 8, INK); px(g, x, y, s, 8, 8, INK);
			g.fill(x + 6 * s, y + 9 * s, x + 8 * s, y + 10 * s, INK); // smile
		}

		// Floating z's while asleep
		if (asleep) {
			for (int i = 0; i < 2; i++) {
				double p = (t * 0.7 + i * 0.5) % 1.0;
				int a = (int) (Math.sin(p * Math.PI) * 255);
				if (a > 8) {
					g.drawString(font, "z", x + w + 2 + (int) (p * 10), y - (int) (p * 16), (a << 24) | MINT, false);
				}
			}
		}

		// Speech bubble
		String line = speech(mc, t, hovered);
		if (line != null) drawBubble(g, font, line, x + w / 2, y - 6, screenW);
	}

	private static String speech(Minecraft mc, double t, boolean hovered) {
		if (t < 1.8) return null;
		if (hovered) return "boo!";
		if (t < 6.5) return "welcome back, " + mc.getUser().getName();
		double cycle = (t - 6.5) % 12.0;
		if (cycle > 8.0 && cycle < 11.5) {
			int idx = (int) ((t - 6.5) / 12.0) % IDLE_LINES.length;
			return IDLE_LINES[idx];
		}
		return null;
	}

	private static void drawBubble(GuiGraphics g, Font font, String text, int centerX, int bottomY, int screenW) {
		int tw = font.width(text);
		int bw = tw + 10, bh = 15;
		int bx = Math.max(4, Math.min(screenW - bw - 4, centerX - bw / 2));
		int by = bottomY - bh - 4;
		g.fill(bx - 1, by - 1, bx + bw + 1, by + bh + 1, BUBBLE_EDGE);
		g.fill(bx, by, bx + bw, by + bh, BUBBLE_BG);
		// little tail pointing down at Boo
		int tailX = Math.max(bx + 4, Math.min(bx + bw - 8, centerX - 2));
		g.fill(tailX, by + bh, tailX + 4, by + bh + 2, BUBBLE_EDGE);
		g.fill(tailX + 1, by + bh + 2, tailX + 3, by + bh + 4, BUBBLE_EDGE);
		g.drawString(font, text, bx + 5, by + 4, TEXT, false);
	}

	private static void drawFireflies(GuiGraphics g, int screenW, int screenH, double t) {
		for (int i = 0; i < 14; i++) {
			double sx = (i * 0.137 + 0.05) % 1.0, sy = (i * 0.291 + 0.11) % 1.0;
			int fx = (int) ((sx * screenW + Math.sin(t * 0.3 + i * 1.7) * 30 + screenW) % screenW);
			int fy = (int) ((sy * screenH + Math.cos(t * 0.25 + i * 2.3) * 20 + screenH) % screenH);
			double pulse = 0.5 + 0.5 * Math.sin(t * 1.6 + i);
			int a = (int) (pulse * 200);
			int glowA = (int) (pulse * 50);
			g.fill(fx - 2, fy - 2, fx + 4, fy + 4, (glowA << 24) | PEACH);
			g.fill(fx, fy, fx + 2, fy + 2, (a << 24) | PEACH);
		}
	}

	private static void px(GuiGraphics g, int x, int y, int s, int cx, int cy, int color) {
		g.fill(x + cx * s, y + cy * s, x + (cx + 1) * s, y + (cy + 1) * s, color);
	}

	private interface CellFn { void cell(int cx, int cy, char c); }

	private static void forEachCell(double t, int frame, CellFn fn) {
		for (int r = 0; r < ROWS; r++) {
			String row = r < BODY.length ? BODY[r] : TAIL[frame][r - BODY.length];
			for (int c = 0; c < COLS; c++) {
				char ch = row.charAt(c);
				if (ch != '.') fn.cell(c, r, ch);
			}
		}
	}
}
