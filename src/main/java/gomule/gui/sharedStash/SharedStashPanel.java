package gomule.gui.sharedStash;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import static java.util.Collections.emptyList;

import javax.swing.ToolTipManager;

import gomule.d2i.D2SharedStash;
import gomule.gui.D2FileManager;
import gomule.gui.D2ImageCache;
import gomule.gui.LayoutProfile;
import gomule.gui.PanelTheme;
import gomule.item.D2Item;
import gomule.util.D2UI;
import gomule.util.ScaledPainterPanel;

public final class SharedStashPanel extends ScaledPainterPanel {

    public static final int BG_WIDTH = 524;
    public static final int BG_HEIGHT = 470;
    /** Background fill color for occupied grid cells (淡蓝色, matches D2RMM/game style). */
    private static final java.awt.Color ITEM_SLOT_BG = new java.awt.Color(40, 100, 220, 80);
    private final D2FileManager fileManager;
    private final D2ViewSharedStash sharedStashView;
    private int selectedStashPaneIndex = 0;
    private Image background;
    /** Gold amount string drawn directly at screen coords in paint() to avoid double-scale blur. */
    private String goldValueStr = "";

    // ── Tooltip size cache ────────────────────────────────────────────────────
    // Stores the last measured JToolTip preferred size so clampToWindow() uses
    // actual dimensions instead of a hardcoded estimate.
    private String lastMeasuredTipText = null;
    private int cachedTipW = 360, cachedTipH = 450;

    /**
     * Sprites for the materials pane, drawn directly at screen coords in paint() to avoid
     * double-scaling blur.
     */
    private final java.util.List<MatSpriteDraw> matSprites = new java.util.ArrayList<>();
    /** Quantity count labels for material slots; drawn after sprites in paint() to stay on top. */
    private final java.util.List<MatCountDraw> matCounts = new java.util.ArrayList<>();

    /** Tab labels drawn directly at screen coords in paint() to avoid double-scaling blur. */
    private final java.util.List<TabDraw> tabDraws = new java.util.ArrayList<>();

    private static final class TabDraw {
        final int x, y, w, h; // native (unscaled) coords
        final String label;
        final boolean active;

        TabDraw(int x, int y, int w, int h, String label, boolean active) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.label = label;
            this.active = active;
        }
    }

    private static final class MatSpriteDraw {
        final Image img;
        final int x, y, w, h; // native (unscaled) coords
        final boolean ghost; // true when qty==0 — drawn semi-transparent

        MatSpriteDraw(Image img, int x, int y, int w, int h, boolean ghost) {
            this.img = img;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.ghost = ghost;
        }
    }

    /**
     * Quantity labels for material slots; drawn in paint() after sprites so they are never covered.
     */
    private static final class MatCountDraw {
        final String text;
        final int x, y, w, h; // native (unscaled) slot coords

        MatCountDraw(String text, int x, int y, int w, int h) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }

    public SharedStashPanel(D2FileManager fileManager, D2ViewSharedStash sharedStashView) {
        this.fileManager = fileManager;
        this.sharedStashView = sharedStashView;
        this.nativeW = BG_WIDTH;
        this.nativeH = BG_HEIGHT;
        setLayout(new BorderLayout());
        addMouseListener(new SharedStashPanelMouseClickHandler(this));
        addMouseMotionListener(new SharedStashMouseMotionListener(this));
        setVisible(true);
        ToolTipManager.sharedInstance().setDismissDelay(40000);
        ToolTipManager.sharedInstance().setInitialDelay(300);
        build();
    }

    public void build() {
        matSprites.clear();
        matCounts.clear();
        tabDraws.clear();
        goldValueStr = "";
        GraphicsConfiguration gc = fileManager.getGraphicsConfiguration();
        if (gc == null) {
            gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
                    .getDefaultConfiguration();
        }
        background = gc.createCompatibleImage(BG_WIDTH, BG_HEIGHT, Transparency.TRANSLUCENT);
        Graphics2D lGraphics = (Graphics2D) background.getGraphics();
        if (LayoutProfile.proceduralBackground) {
            lGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            lGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            drawProceduralBackground(lGraphics);
        } else {
            Image lEmptyBackground =
                    D2ImageCache.getImage("stash" + (selectedStashPaneIndex + 1) + "-16x13.jpg");
            if (lEmptyBackground != null) {
                lGraphics.drawImage(lEmptyBackground, 0, 0, this);
            }
        }
        if (getSharedStash() != null)
            placeItemsInView();
        repaint();
    }

    private void drawProceduralBackground(Graphics2D g) {
        PanelTheme theme = PanelTheme.active;
        Color bgColor = theme.bg;
        Color panelColor = theme.gridBg;
        Color borderColor = theme.gridBorder;
        Color gridLine = theme.gridLine;
        Color tabActive = theme.weaponActiveBg;
        Color tabInactive = theme.weaponInactiveBg;
        Color goldBar = theme.equipZoneBg;
        @SuppressWarnings("unused")
        Color textBright = theme.weaponActiveText;
        @SuppressWarnings("unused")
        Color textDim = theme.weaponInactiveText;

        // Full background
        g.setColor(bgColor);
        g.fillRect(0, 0, BG_WIDTH, BG_HEIGHT);

        // Gold bar (y=15..35)
        g.setColor(goldBar);
        g.fillRect(18, 15, BG_WIDTH - 36, 20);
        g.setColor(borderColor);
        g.drawRect(18, 15, BG_WIDTH - 36, 20);
        // "Gold:" label and value are drawn directly in paint() at screen coords to avoid
        // double-scale blur

        // Tabs (y=38..54) — include materials tab if present
        boolean hasMaterialsTab =
                getSharedStash() != null && getSharedStash().getMaterialsPane() != null;
        int numNormalPanes = (getSharedStash() != null) ? getSharedStash().getPanes().size() : 5;
        int numTabs = numNormalPanes + (hasMaterialsTab ? 1 : 0);
        numTabs = Math.max(1, Math.min(numTabs, 8));
        int tabAreaX = 16, tabAreaW = 430, tabH = 16, tabY = 38;
        int tabWidth = tabAreaW / numTabs;
        for (int i = 0; i < numTabs; i++) {
            int tabX = tabAreaX + i * tabWidth;
            boolean active = (i == selectedStashPaneIndex);
            g.setColor(active ? tabActive : tabInactive);
            g.fillRect(tabX, tabY, tabWidth - 1, tabH);
            g.setColor(active ? theme.weaponActiveBorder : theme.weaponInactiveBorder);
            g.drawRect(tabX, tabY, tabWidth - 1, tabH);
            boolean isMaterialsTab = hasMaterialsTab && i == numTabs - 1;
            String label = isMaterialsTab ? "M" : String.valueOf(i + 1);
            // Store for screen-coordinate drawing in paint() to avoid double-scale blur
            tabDraws.add(new TabDraw(tabX, tabY, tabWidth, tabH, label, active));
        }

        // Grid area (16 cols × 13 rows) — or plain panel for materials tab
        int gridX = getXCoordForCol(0); // 17
        int gridY = getYCoordForRow(0); // 59
        int gridEndX = getXCoordForCol(15) + 28; // 502
        int gridEndY = getYCoordForRow(12) + 28; // 453
        g.setColor(panelColor);
        g.fillRect(gridX, gridY, gridEndX - gridX, gridEndY - gridY);
        if (!isMaterialsTabSelected()) {
            g.setColor(gridLine);
            for (int c = 1; c < 16; c++) {
                int lx = getXCoordForCol(c) - 1;
                g.drawLine(lx, gridY, lx, gridEndY);
            }
            for (int r = 1; r < 13; r++) {
                int ly = getYCoordForRow(r) - 1;
                g.drawLine(gridX, ly, gridEndX, ly);
            }
        }
        g.setColor(borderColor);
        g.drawRect(gridX, gridY, gridEndX - gridX, gridEndY - gridY);
    }

    private void placeItemsInView() {
        if (isMaterialsTabSelected()) {
            drawMaterialsPane();
            return;
        }
        D2SharedStash.D2SharedStashPane pane = getSelectedStashPane();
        if (pane == null)
            return;
        pane.getItems().forEach(item -> {
            if (item.get_location() != 0 && item.get_body_position() != 0 && item.get_panel() != 5)
                return;
            int col = item.get_col();
            int row = item.get_row();
            int x = getXCoordForCol(col);
            int y = getYCoordForRow(row);
            // Exclude the trailing spacer so the fill stays within the grid border
            int slotW = getXCoordForCol(col + item.get_width() - 1) - x + 28;
            int slotH = getYCoordForRow(row + item.get_height() - 1) - y + 28;
            // Fill occupied-cell background
            Graphics2D bg = (Graphics2D) background.getGraphics();
            bg.setColor(ITEM_SLOT_BG);
            bg.fillRect(x, y, slotW, slotH);
            Image sprite = D2ImageCache.getSpriteImage(item.getItemCode(), item.get_gfx_num());
            if (sprite != null) {
                matSprites.add(new MatSpriteDraw(sprite, x, y, slotW, slotH, false));
            } else {
                Image image = D2ImageCache.getDC6Image(item);
                if (image != null)
                    bg.drawImage(image, x, y, this);
            }
        });
        // Draw gold amount
        Graphics2D g = (Graphics2D) background.getGraphics();
        if (LayoutProfile.proceduralBackground) {
            // Store for screen-coordinate drawing in paint() to avoid double-scale blur
            goldValueStr = Long.toString(pane.getGold());
        } else {
            g.drawString(Long.toString(pane.getGold()), 40, 32);
        }
    }

    // ── D2RMM AdvancedStashTab layout constants ──────────────────────────────
    // GEMS_GRID[quality][gemType] – rendered as 7 visual cols × 5 rows
    private static final String[][] GEMS_GRID = {{"gcw", "gcg", "gcr", "gcy", "gcv", "gcb", "skc"}, // Chipped
            {"gfw", "gfg", "gfr", "gfy", "gfv", "gfb", "skf"}, // Flawed
            {"gsw", "gsg", "gsr", "gsy", "gsv", "gsb", "sku"}, // Regular
            {"glw", "glg", "glr", "gly", "gzv", "glb", "skl"}, // Flawless
            {"gpw", "gpg", "gpr", "gpy", "gpv", "gpb", "skz"}, // Perfect
    };
    // RUNES_GRID[row][col] – rendered as 11 cols × 3 rows
    private static final String[][] RUNES_GRID =
            {{"r01", "r02", "r03", "r04", "r05", "r06", "r07", "r08", "r09", "r10", "r11"},
                    {"r12", "r13", "r14", "r15", "r16", "r17", "r18", "r19", "r20", "r21", "r22"},
                    {"r23", "r24", "r25", "r26", "r27", "r28", "r29", "r30", "r31", "r32", "r33"},};
    // Right-section rows (ua=idols/figurines height=2; pk=keys height=2)
    private static final String[] UA_CODES = {"ua1", "ua2", "ua3", "ua4", "ua5"};
    private static final String[] PK_CODES = {"pk1", "pk2", "pk3"};
    private static final String[] XA_CODES = {"xa1", "xa2", "xa3", "xa4", "xa5"};
    private static final String[] ORG_CODES = {"dhn", "bey", "mbr"};
    private static final String[] ESS_CODES = {"toa", "tes", "ceh", "bet", "fed"};
    private static final String[] POT_CODES = {"rvs", "rvl"};

    private void drawMaterialsPane() {
        D2SharedStash stash = getSharedStash();
        if (stash == null)
            return;
        D2SharedStash.D2MaterialsPane mats = stash.getMaterialsPane();
        if (mats == null)
            return;

        // Build code → item map (trim to handle 4-char padded codes)
        java.util.Map<String, D2Item> byCode = new java.util.HashMap<>();
        for (D2Item item : mats.getItems()) {
            String code = item.getItemCode();
            if (code != null)
                byCode.put(code.trim(), item);
        }

        Graphics2D g = (Graphics2D) background.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        final int SLOT = 28, GAP = 2, STEP = SLOT + GAP;
        final int x0 = getXCoordForCol(0); // 17
        final int y0 = getYCoordForRow(0); // 59

        // ── 1. GEMS GRID: 7 gem-type cols × 5 quality rows ──────────────────
        for (int col = 0; col < 7; col++) {
            for (int row = 0; row < 5; row++) {
                String code = GEMS_GRID[row][col];
                drawMatSlot(g, x0 + col * STEP, y0 + row * STEP, SLOT, SLOT, code,
                        byCode.get(code));
            }
        }

        // ── 2. RIGHT SECTION (starts 6px after gems block) ───────────────────
        int rx = x0 + 7 * STEP + 6; // x start of right section
        int rx2 = rx + 5 * STEP + 6; // x start of sub-right column

        // Row 1: ua1-5 (height=2) | pk1-3 (height=2)
        int ry = y0;
        int tallH = SLOT * 2 + GAP;
        for (int i = 0; i < UA_CODES.length; i++) {
            drawMatSlot(g, rx + i * STEP, ry, SLOT, tallH, UA_CODES[i], byCode.get(UA_CODES[i]));
        }
        for (int i = 0; i < PK_CODES.length; i++) {
            drawMatSlot(g, rx2 + i * STEP, ry, SLOT, tallH, PK_CODES[i], byCode.get(PK_CODES[i]));
        }

        // Row 2: xa1-5 | dhn, bey, mbr
        ry = y0 + tallH + 6;
        for (int i = 0; i < XA_CODES.length; i++) {
            drawMatSlot(g, rx + i * STEP, ry, SLOT, SLOT, XA_CODES[i], byCode.get(XA_CODES[i]));
        }
        for (int i = 0; i < ORG_CODES.length; i++) {
            drawMatSlot(g, rx2 + i * STEP, ry, SLOT, SLOT, ORG_CODES[i], byCode.get(ORG_CODES[i]));
        }

        // Row 3: toa,tes,ceh,bet,fed | rvs,rvl
        ry += STEP + 4;
        for (int i = 0; i < ESS_CODES.length; i++) {
            drawMatSlot(g, rx + i * STEP, ry, SLOT, SLOT, ESS_CODES[i], byCode.get(ESS_CODES[i]));
        }
        for (int i = 0; i < POT_CODES.length; i++) {
            drawMatSlot(g, rx2 + i * STEP, ry, SLOT, SLOT, POT_CODES[i], byCode.get(POT_CODES[i]));
        }

        // ── 3. RUNES GRID: 11 cols × 3 rows (below gems) ────────────────────
        int ry_runes = y0 + 5 * STEP + 6;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 11; col++) {
                String code = RUNES_GRID[row][col];
                drawMatSlot(g, x0 + col * STEP, ry_runes + row * STEP, SLOT, SLOT, code,
                        byCode.get(code));
            }
        }
    }

    /**
     * Draws a single material slot at (x,y) with size (w×h). item may be null (empty slot — shows
     * ghost).
     */
    private void drawMatSlot(Graphics2D g, int x, int y, int w, int h, String slotCode,
            D2Item item) {
        // Background
        g.setColor(PanelTheme.active.slotBg);
        g.fillRect(x, y, w, h);
        g.setColor(PanelTheme.active.slotBorder);
        g.drawRect(x, y, w - 1, h - 1);

        if (item == null) {
            // Empty slot: show ghost image by slot code (gfxNum 0 = base variant)
            Image ghostImg = D2ImageCache.getSpriteImage(slotCode, 0);
            if (ghostImg != null) {
                matSprites.add(new MatSpriteDraw(ghostImg, x, y, w, h, true));
            }
            return;
        }

        int qty = item.getAdvancedStashQuantity();

        // Item icon — try D2R sprite first, fall back to legacy DC6
        Image img = D2ImageCache.getSpriteImage(item.getItemCode(), item.get_gfx_num());
        if (img == null) {
            img = D2ImageCache.getDC6Image(item);
        }
        if (img != null) {
            matSprites.add(new MatSpriteDraw(img, x, y, w, h, false));
        }

        // Quantity badge — only when qty > 0
        if (qty > 0) {
            matCounts.add(new MatCountDraw(String.valueOf(qty), x, y, w, h));
        }
    }

    /**
     * Anchors the tooltip to the item's grid-cell position so it stays fixed while the mouse moves
     * within the same cell, then clamps the position inside the window using exact screen
     * coordinates — matching the logic Swing's {@code PopupFactory} uses to decide whether to
     * create a heavyweight (OS-level) popup.
     *
     * <h4>Why previous fixes were insufficient</h4>
     * <ul>
     * <li>Checking against <em>component</em> bottom only caught cases where the component itself
     * extended past the window — after the user drags the window down, the window top-left on
     * screen shifts, but the component's Swing-pixel position within the window stays the same, so
     * the component-bottom check passed while the tooltip still overflowed the window on
     * screen.</li>
     * <li>The previous {@code clampToRootPane} used a hardcoded {@code EST_H=300} height estimate,
     * which is too small for tall HTML item tooltips.</li>
     * </ul>
     *
     * <h4>Fix</h4> Use {@link java.awt.Component#getLocationOnScreen()} for exact screen
     * coordinates and measure the actual JToolTip preferred size (cached per tooltip text change).
     */
    @Override
    public Point getToolTipLocation(MouseEvent event) {
        double s = D2UI.getUiScale();
        int nx = event.getX();
        int ny = event.getY();
        // Cursor in component-relative screen-pixel space (same space as tipX/tipY).
        int cursorX = (int) Math.round(nx * s);
        int cursorY = (int) Math.round(ny * s);

        int tipX, tipY;

        if (!isMaterialsTabSelected()) {
            int col = getColForXCoord(nx);
            int row = getRowForYCoord(ny);
            if (col < 0 || col > 15 || row < 0 || row > 12) {
                return super.getToolTipLocation(event);
            }
            int anchorCol = col, anchorRow = row;
            D2SharedStash.D2SharedStashPane pane = getSelectedStashPane();
            if (pane != null) {
                gomule.item.D2Item it = pane.getItemCovering(col, row);
                if (it != null) {
                    anchorCol = it.get_col();
                    anchorRow = it.get_row();
                }
            }
            tipX = (int) Math.round(getXCoordForCol(anchorCol) * s);
            tipY = (int) Math.round((getYCoordForRow(anchorRow) + 30) * s);
        } else {
            // Materials tab: anchor to the slot's FIXED origin (not cursor).
            // Anchoring to the cursor means every mouse movement changes tipX/tipY,
            // which causes Swing to destroy and recreate the popup on every event,
            // producing a white-frame flash each time.
            Point slotOrigin = getMatSlotOriginAt(nx, ny);
            if (slotOrigin != null) {
                tipX = (int) Math.round(slotOrigin.x * s);
                tipY = (int) Math.round((slotOrigin.y + 30) * s);
            } else {
                // Not over a known slot — fall back to cursor offset.
                tipX = cursorX + 16;
                tipY = cursorY + 24;
            }
        }

        return clampToWindow(tipX, tipY, cursorX, cursorY);
    }

    /**
     * Returns the native-pixel origin (top-left) of the materials-tab slot that contains the given
     * native-pixel cursor position, or {@code null} if the cursor is not inside any slot. Used to
     * produce a stable tooltip anchor so the popup position does not change while the cursor moves
     * within the slot.
     */
    private Point getMatSlotOriginAt(int nx, int ny) {
        final int SLOT = 28, STEP = 30;
        final int x0 = getXCoordForCol(0);
        final int y0 = getYCoordForRow(0);
        final int tallH = SLOT * 2 + 2; // 58 — tall slots (ua, pk)
        final int rx = x0 + 7 * STEP + 6; // right section start
        final int rx2 = rx + 5 * STEP + 6; // sub-right column start
        final int ry2 = y0 + tallH + 6; // row-2 top
        final int ry3 = ry2 + STEP + 4; // row-3 top (ess / pot)
        final int ryRune = y0 + 5 * STEP + 6; // runes grid top

        // ── 1. GEMS (7 cols × 5 rows) ────────────────────────────────────────
        if (nx >= x0 && ny >= y0 && ny < y0 + 5 * STEP) {
            int col = (nx - x0) / STEP, row = (ny - y0) / STEP;
            if (col >= 0 && col < 7 && row >= 0 && row < 5) {
                int sx = x0 + col * STEP, sy = y0 + row * STEP;
                if (nx < sx + SLOT && ny < sy + SLOT)
                    return new Point(sx, sy);
            }
        }

        // ── 2a. ua — 5 tall slots, row 1 ────────────────────────────────────
        if (nx >= rx && ny >= y0 && ny < y0 + tallH) {
            int col = (nx - rx) / STEP;
            if (col >= 0 && col < UA_CODES.length) {
                int sx = rx + col * STEP;
                if (nx < sx + SLOT)
                    return new Point(sx, y0);
            }
        }
        // ── 2b. pk — 3 tall slots, row 1 ────────────────────────────────────
        if (nx >= rx2 && ny >= y0 && ny < y0 + tallH) {
            int col = (nx - rx2) / STEP;
            if (col >= 0 && col < PK_CODES.length) {
                int sx = rx2 + col * STEP;
                if (nx < sx + SLOT)
                    return new Point(sx, y0);
            }
        }
        // ── 2c. xa — 5 normal slots, row 2 ──────────────────────────────────
        if (nx >= rx && ny >= ry2 && ny < ry2 + SLOT) {
            int col = (nx - rx) / STEP;
            if (col >= 0 && col < XA_CODES.length) {
                int sx = rx + col * STEP;
                if (nx < sx + SLOT)
                    return new Point(sx, ry2);
            }
        }
        // ── 2d. org — 3 normal slots, row 2 ─────────────────────────────────
        if (nx >= rx2 && ny >= ry2 && ny < ry2 + SLOT) {
            int col = (nx - rx2) / STEP;
            if (col >= 0 && col < ORG_CODES.length) {
                int sx = rx2 + col * STEP;
                if (nx < sx + SLOT)
                    return new Point(sx, ry2);
            }
        }
        // ── 2e. ess — 5 normal slots, row 3 ─────────────────────────────────
        if (nx >= rx && ny >= ry3 && ny < ry3 + SLOT) {
            int col = (nx - rx) / STEP;
            if (col >= 0 && col < ESS_CODES.length) {
                int sx = rx + col * STEP;
                if (nx < sx + SLOT)
                    return new Point(sx, ry3);
            }
        }
        // ── 2f. pot — 2 normal slots (rvs, rvl), row 3 ──────────────────────
        if (nx >= rx2 && ny >= ry3 && ny < ry3 + SLOT) {
            int col = (nx - rx2) / STEP;
            if (col >= 0 && col < POT_CODES.length) {
                int sx = rx2 + col * STEP;
                if (nx < sx + SLOT)
                    return new Point(sx, ry3);
            }
        }
        // ── 3. RUNES (11 cols × 3 rows) ──────────────────────────────────────
        if (nx >= x0 && ny >= ryRune && ny < ryRune + 3 * STEP) {
            int col = (nx - x0) / STEP, row = (ny - ryRune) / STEP;
            if (col >= 0 && col < 11 && row >= 0 && row < 3) {
                int sx = x0 + col * STEP, sy = ryRune + row * STEP;
                if (nx < sx + SLOT && ny < sy + SLOT)
                    return new Point(sx, sy);
            }
        }
        return null;
    }

    /**
     * Clamps component-relative tooltip coordinates so the popup fits within the enclosing window,
     * preventing Swing from switching to a HeavyweightPopup.
     *
     * <p>
     * Uses <em>screen</em> coordinates (via {@link #getLocationOnScreen()} and
     * {@link java.awt.Window#getLocationOnScreen()}) to match exactly what {@code PopupFactory}
     * checks internally. The tooltip dimensions come from a cached measurement of the current
     * {@link javax.swing.JToolTip#getPreferredSize()}, refreshed whenever the tooltip text changes.
     */
    private Point clampToWindow(int tipX, int tipY, int cursorX, int cursorY) {
        refreshTipSizeCache();

        java.awt.Window window = javax.swing.SwingUtilities.getWindowAncestor(this);
        if (window == null)
            return new Point(tipX, tipY);

        java.awt.Point compScreen, winScreen;
        try {
            compScreen = getLocationOnScreen();
            winScreen = window.getLocationOnScreen();
        } catch (java.awt.IllegalComponentStateException ignored) {
            return new Point(tipX, tipY);
        }

        int winW = window.getWidth();
        int winH = window.getHeight();

        // ── Vertical ────────────────────────────────────────────────────────
        if (compScreen.y + tipY + cachedTipH > winScreen.y + winH) {
            // Flip above the CURSOR (not above tipY).
            //
            // tipY is typically ~(cellBottom+30)*s — roughly at the cell's bottom edge.
            // Using "tipY - cachedTipH" as the flip anchor places the tooltip bottom
            // near cellBottom*s, which is exactly where the cursor sits → the tooltip
            // rectangle covers the cursor → mouseExited fires → flicker loop.
            //
            // Anchoring to cursorY guarantees the tooltip bottom lands 4 px above the
            // cursor in every situation (bottom-row items, materials tab RVL, etc.).
            int flipped = cursorY - cachedTipH - 4;
            tipY = (compScreen.y + flipped >= winScreen.y) ? flipped
                    : (winScreen.y - compScreen.y + 2); // last resort: pin to window top
        }

        // ── Horizontal ──────────────────────────────────────────────────────
        if (compScreen.x + tipX + cachedTipW > winScreen.x + winW) {
            tipX = winScreen.x + winW - compScreen.x - cachedTipW - 4;
        }

        // ── Safety guard: tooltip must never contain the cursor ──────────────
        // If the window is very small and the "pin to window top" fallback still
        // leaves the cursor inside the tooltip, shift the tooltip left of the cursor.
        if (tipY <= cursorY && cursorY < tipY + cachedTipH && tipX <= cursorX
                && cursorX < tipX + cachedTipW) {
            tipX = cursorX - cachedTipW - 4;
        }

        return new Point(Math.max(0, tipX), Math.max(0, tipY));
    }

    /**
     * Measures the current tooltip's preferred size and caches it. Called from
     * {@link #clampToWindow} on every {@code getToolTipLocation} invocation, but the measurement
     * itself only runs when the tooltip text changes.
     */
    private void refreshTipSizeCache() {
        String text = getToolTipText();
        if (text == null || text.equals(lastMeasuredTipText))
            return;
        javax.swing.JToolTip tip = createToolTip();
        tip.setTipText(text);
        java.awt.Dimension d = tip.getPreferredSize();
        if (d.width > 0)
            cachedTipW = d.width + 8; // small safety margin
        if (d.height > 0)
            cachedTipH = d.height + 8;
        lastMeasuredTipText = text;
    }

    /**
     * Pre-sets the tooltip background to black so that HeavyweightPopup (used near screen edges)
     * shows a dark window immediately rather than flashing white.
     */
    @Override
    public javax.swing.JToolTip createToolTip() {
        javax.swing.JToolTip tip = super.createToolTip();
        tip.setBackground(new java.awt.Color(0, 0, 0));
        tip.setForeground(java.awt.Color.WHITE);
        tip.setOpaque(true);
        return tip;
    }

    public boolean isMaterialsTabSelected() {
        D2SharedStash stash = getSharedStash();
        return stash != null && stash.getMaterialsPane() != null
                && selectedStashPaneIndex >= stash.getPanes().size();
    }

    /**
     * Returns the D2Item in the materials pane whose slot contains pixel (px, py), or null if the
     * point is not over any slot (or no item is present there). Uses the same coordinate constants
     * as drawMaterialsPane().
     */
    public D2Item getMatItemAt(int px, int py) {
        D2SharedStash stash = getSharedStash();
        if (stash == null)
            return null;
        D2SharedStash.D2MaterialsPane mats = stash.getMaterialsPane();
        if (mats == null)
            return null;

        java.util.Map<String, D2Item> byCode = new java.util.HashMap<>();
        for (D2Item item : mats.getItems()) {
            String code = item.getItemCode();
            if (code != null)
                byCode.put(code.trim(), item);
        }

        final int SLOT = 28, GAP = 2, STEP = SLOT + GAP;
        final int x0 = getXCoordForCol(0);
        final int y0 = getYCoordForRow(0);

        // GEMS GRID: 7 cols × 5 rows
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 7; col++) {
                int sx = x0 + col * STEP, sy = y0 + row * STEP;
                if (px >= sx && px < sx + SLOT && py >= sy && py < sy + SLOT)
                    return byCode.get(GEMS_GRID[row][col]);
            }
        }

        // Right section
        int rx = x0 + 7 * STEP + 6;
        int rx2 = rx + 5 * STEP + 6;
        int tallH = SLOT * 2 + GAP;
        int ry = y0;

        // Row 1 tall: ua1-5 | pk1-3
        for (int i = 0; i < UA_CODES.length; i++) {
            int sx = rx + i * STEP;
            if (px >= sx && px < sx + SLOT && py >= ry && py < ry + tallH)
                return byCode.get(UA_CODES[i]);
        }
        for (int i = 0; i < PK_CODES.length; i++) {
            int sx = rx2 + i * STEP;
            if (px >= sx && px < sx + SLOT && py >= ry && py < ry + tallH)
                return byCode.get(PK_CODES[i]);
        }

        // Row 2: xa1-5 | dhn,bey,mbr
        ry = y0 + tallH + 6;
        for (int i = 0; i < XA_CODES.length; i++) {
            int sx = rx + i * STEP;
            if (px >= sx && px < sx + SLOT && py >= ry && py < ry + SLOT)
                return byCode.get(XA_CODES[i]);
        }
        for (int i = 0; i < ORG_CODES.length; i++) {
            int sx = rx2 + i * STEP;
            if (px >= sx && px < sx + SLOT && py >= ry && py < ry + SLOT)
                return byCode.get(ORG_CODES[i]);
        }

        // Row 3: toa,tes,ceh,bet,fed | rvs,rvl
        ry += STEP + 4;
        for (int i = 0; i < ESS_CODES.length; i++) {
            int sx = rx + i * STEP;
            if (px >= sx && px < sx + SLOT && py >= ry && py < ry + SLOT)
                return byCode.get(ESS_CODES[i]);
        }
        for (int i = 0; i < POT_CODES.length; i++) {
            int sx = rx2 + i * STEP;
            if (px >= sx && px < sx + SLOT && py >= ry && py < ry + SLOT)
                return byCode.get(POT_CODES[i]);
        }

        // RUNES GRID: 11 cols × 3 rows (below gems)
        int ryRunes = y0 + 5 * STEP + 6;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 11; col++) {
                int sx = x0 + col * STEP, sy = ryRunes + row * STEP;
                if (px >= sx && px < sx + SLOT && py >= sy && py < sy + SLOT)
                    return byCode.get(RUNES_GRID[row][col]);
            }
        }

        return null;
    }

    public static int getXCoordForCol(int col) {
        int diffx = (col / 2);
        return 17 + (col * 28) + ((diffx * 3) + ((col - diffx) * 2));
    }

    public static int getYCoordForRow(int row) {
        int diffy = (row / 2);
        return 59 + (row * 28) + ((diffy * 3) + ((row - diffy) * 2));
    }

    public static int getColForXCoord(int x) {
        if (x < 17)
            return -1;
        return ((2 * x) - 36) / 61;
    }

    public static int getRowForYCoord(int y) {
        if (y < 59)
            return -1;
        return ((2 * y) - 114) / 61;
    }

    @Override
    public void paint(Graphics pGraphics) {
        super.paint(pGraphics);
        drawScaled(pGraphics, background);
        // Draw Gold label and value directly at screen coords to avoid double-scale blur
        if (LayoutProfile.proceduralBackground) {
            Graphics2D gg = (Graphics2D) pGraphics;
            double s = D2UI.getUiScale();
            gg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int goldFontSize = Math.max(8, (int) Math.round(11 * s));
            Font goldFont = new Font("Arial", Font.BOLD, goldFontSize);
            gg.setFont(goldFont);
            FontMetrics gfm = gg.getFontMetrics();
            // Gold bar native coords: x=18, y=15, w=BG_WIDTH-36, h=20
            int barY = (int) Math.round(15 * s);
            int barH = (int) Math.round(20 * s);
            int baseline = barY + (barH + gfm.getAscent() - gfm.getDescent()) / 2;
            // "Gold:" left-aligned inside the bar
            gg.setColor(PanelTheme.active.weaponInactiveText);
            gg.drawString("Gold:", (int) Math.round(25 * s), baseline);
            // Gold value right-aligned to right edge of the bar (native right = 18+488-8 = 498)
            if (!goldValueStr.isEmpty()) {
                gg.setColor(PanelTheme.active.weaponActiveText);
                int valW = gfm.stringWidth(goldValueStr);
                int rightX = (int) Math.round(498 * s) - valW;
                gg.drawString(goldValueStr, rightX, baseline);
            }
        }
        // Draw tab labels directly at screen resolution to avoid double-scale blur
        if (!tabDraws.isEmpty()) {
            Graphics2D tg = (Graphics2D) pGraphics;
            double s = D2UI.getUiScale();
            tg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            tg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color textBright = PanelTheme.active.weaponActiveText;
            Color textDim = PanelTheme.active.weaponInactiveText;
            int fontSize = Math.max(8, (int) Math.round(10 * s));
            Font tabFont = new Font("Arial", Font.BOLD, fontSize);
            tg.setFont(tabFont);
            FontMetrics fm = tg.getFontMetrics();
            for (TabDraw td : tabDraws) {
                int sx = (int) Math.round(td.x * s);
                int sy = (int) Math.round(td.y * s);
                int sw = (int) Math.round(td.w * s);
                int sh = (int) Math.round(td.h * s);
                tg.setColor(td.active ? textBright : textDim);
                int tx = sx + (sw - fm.stringWidth(td.label)) / 2;
                int ty = sy + (sh + fm.getAscent() - fm.getDescent()) / 2;
                tg.drawString(td.label, tx, ty);
            }
        }
        // Draw item sprites directly at screen resolution to avoid double-scale blur
        if (!matSprites.isEmpty()) {
            Graphics2D sg = (Graphics2D) pGraphics;
            double s = D2UI.getUiScale();
            sg.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            sg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            Composite originalComposite = sg.getComposite();
            for (MatSpriteDraw sd : matSprites) {
                int iw = sd.img.getWidth(null);
                int ih = sd.img.getHeight(null);
                if (iw <= 0 || ih <= 0)
                    continue;
                int sw = (int) Math.round(sd.w * s);
                int sh = (int) Math.round(sd.h * s);
                double imgScale = Math.min((double) (sw - 2) / iw, (double) (sh - 2) / ih);
                int dw = (int) (iw * imgScale);
                int dh = (int) (ih * imgScale);
                int dx = (int) Math.round(sd.x * s) + (sw - dw) / 2;
                int dy = (int) Math.round(sd.y * s) + (sh - dh) / 2;
                if (sd.ghost) {
                    sg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
                } else {
                    sg.setComposite(originalComposite);
                }
                sg.drawImage(sd.img, dx, dy, dw, dh, null);
            }
            sg.setComposite(originalComposite);
        }
        // Draw material quantity labels on top of sprites
        if (!matCounts.isEmpty()) {
            Graphics2D cg = (Graphics2D) pGraphics;
            double s = D2UI.getUiScale();
            cg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int fontSize = Math.max(7, (int) Math.round(9 * s));
            cg.setFont(new Font("Arial", Font.BOLD, fontSize));
            FontMetrics fm = cg.getFontMetrics();
            for (MatCountDraw mc : matCounts) {
                int sw = (int) Math.round(mc.w * s);
                int sh = (int) Math.round(mc.h * s);
                int tx = (int) Math.round(mc.x * s) + sw - fm.stringWidth(mc.text) - 2;
                int ty = (int) Math.round(mc.y * s) + sh - fm.getDescent() - 1;
                cg.setColor(java.awt.Color.BLACK);
                cg.drawString(mc.text, tx + 1, ty + 1);
                cg.setColor(new java.awt.Color(255, 230, 60));
                cg.drawString(mc.text, tx, ty);
            }
        }
    }

    public void setCursorPickupItem() {
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public void setCursorDropItem() {
        setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
    }

    public void setCursorNormal() {
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    public java.util.List<D2Item> removeAllItems() {
        D2SharedStash sharedStash = getSharedStash();
        if (sharedStash == null || isMaterialsTabSelected())
            return emptyList();
        D2SharedStash.D2SharedStashPane stashPane = getSelectedStashPane();
        sharedStash.replacePane(selectedStashPaneIndex,
                D2SharedStash.D2SharedStashPane.fromItems(emptyList(), stashPane.getGold()));
        sharedStash.setModified(true);
        return stashPane.getItems();
    }

    public java.util.List<D2Item> tryToAddItems(java.util.List<D2Item> items) {
        D2SharedStash sharedStash = getSharedStash();
        if (sharedStash == null || isMaterialsTabSelected())
            return emptyList();
        D2SharedStash.D2SharedStashPane stashPane = getSelectedStashPane();
        java.util.List<D2Item> successfullyAddedItems = new ArrayList<>();
        for (D2Item item : items) {
            stashPane = getD2SharedStashPane(stashPane, successfullyAddedItems, item);
        }
        sharedStash.replacePane(selectedStashPaneIndex, stashPane);
        sharedStash.setModified(true);
        return successfullyAddedItems;
    }

    public D2SharedStash.D2SharedStashPane getSelectedStashPane() {
        if (getSharedStash() == null)
            return null;
        if (isMaterialsTabSelected())
            return null;
        return getSharedStash().getPane(selectedStashPaneIndex);
    }

    public D2SharedStash getSharedStash() {
        return sharedStashView.getSharedStash();
    }

    public int getSelectedStashPaneIndex() {
        return selectedStashPaneIndex;
    }

    public void setSelectedStashPaneIndex(int selectedStashPaneIndex) {
        this.selectedStashPaneIndex = selectedStashPaneIndex;
    }

    private D2SharedStash.D2SharedStashPane getD2SharedStashPane(
            D2SharedStash.D2SharedStashPane stashPane,
            java.util.List<D2Item> successfullyAddedItems, D2Item item) {
        for (int i = 0; i < 13; i++) {
            for (int j = 0; j < 16; j++) {
                if (stashPane.canDropItem(j, i, item)) {
                    stashPane = stashPane.addItem(j, i, item);
                    successfullyAddedItems.add(item);
                    return stashPane;
                }
            }
        }
        return stashPane;
    }
}
