package gomule.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.JPanel;
import javax.swing.JToolTip;

/**
 * Base class for inventory / stash painter panels that need to support a
 * runtime-configurable display scale (see {@link D2UI#getUiScale()}).
 *
 * <p>Behaviour:
 * <ul>
 *   <li>{@link #getPreferredSize()}, {@link #getMinimumSize()} and
 *       {@link #getMaximumSize()} all return the native dimensions multiplied
 *       by the current scale factor, so parent layouts grow the panel
 *       accordingly.</li>
 *   <li>Mouse / mouse-motion / wheel events are <em>translated back</em> to
 *       native coordinates before being dispatched to listeners. Existing
 *       click-handling code (which assumes native pixel coordinates) keeps
 *       working unchanged.</li>
 *   <li>{@link #drawScaled(Graphics, Image)} is a small helper subclasses call
 *       from their {@code paint} override; it stretches the pre-rendered
 *       background to the on-screen size.</li>
 * </ul>
 *
 * <p>Subclasses must populate {@link #nativeW} and {@link #nativeH} (e.g. in
 * their constructor) with the panel's native pixel dimensions — the same
 * dimensions the panel renders into its background image.
 */
public abstract class ScaledPainterPanel extends JPanel {

    private static final long serialVersionUID = 1L;

    /** Native (unscaled) panel width in pixels. Subclasses must set this. */
    protected int nativeW;
    /** Native (unscaled) panel height in pixels. Subclasses must set this. */
    protected int nativeH;

    @Override
    public Dimension getPreferredSize() {
        if (nativeW <= 0 || nativeH <= 0) return super.getPreferredSize();
        return scaledDim();
    }

    @Override
    public Dimension getMinimumSize() {
        if (nativeW <= 0 || nativeH <= 0) return super.getMinimumSize();
        return scaledDim();
    }

    @Override
    public Dimension getMaximumSize() {
        if (nativeW <= 0 || nativeH <= 0) return super.getMaximumSize();
        return scaledDim();
    }

    private Dimension scaledDim() {
        double s = D2UI.getUiScale();
        return new Dimension((int) Math.round(nativeW * s), (int) Math.round(nativeH * s));
    }

    /** Draw the pre-rendered native-resolution background scaled to the panel. */
    protected void drawScaled(Graphics g, Image bg) {
        if (bg == null) return;
        double s = D2UI.getUiScale();
        if (s == 1.0 || nativeW <= 0 || nativeH <= 0) {
            g.drawImage(bg, 0, 0, this);
        } else {
            g.drawImage(bg, 0, 0, (int) Math.round(nativeW * s), (int) Math.round(nativeH * s), this);
        }
    }

    @Override
    protected void processMouseEvent(MouseEvent e) {
        super.processMouseEvent(translate(e));
    }

    @Override
    protected void processMouseMotionEvent(MouseEvent e) {
        super.processMouseMotionEvent(translate(e));
    }

    @Override
    protected void processMouseWheelEvent(MouseWheelEvent e) {
        super.processMouseWheelEvent((MouseWheelEvent) translate(e));
    }

    /**
     * Tooltip placement override.
     *
     * <p>The {@link MouseEvent} reaching this component has already been
     * translated to native (pre-scale) coordinates by {@link #translate}. If we
     * let Swing position the tooltip at those coordinates, the popup appears
     * far to the upper-left of the actual on-screen cursor, ends up underneath
     * the cursor, triggers {@code mouseExited}, hides itself, then reappears
     * when the cursor "re-enters" — a flicker loop.
     *
     * <p>Fix #1: convert the translated coordinates back to screen-space
     * (multiply by scale) and add a small offset so the tooltip never overlaps
     * the cursor.
     *
     * <p>Fix #2 (anti-flicker for big tooltips): while the tooltip text stays
     * the same (i.e. cursor is still hovering the same item), return the
     * <em>same</em> {@link Point} we returned last time. Otherwise Swing's
     * {@code ToolTipManager} sees a new location on every mouse-move event and
     * destroys+recreates the popup. For long Unique/Set HTML tooltips the
     * popup overflows the parent {@code JFrame} and becomes a
     * {@code HeavyweightPopup} (a real OS window) — recreating it every pixel
     * causes a visible white→black flash. Equipment slots don't suffer the
     * same way because their popups fit inside the parent frame and stay
     * lightweight, so {@code setLocation} alone (no recreate) is enough.
     */
    @Override
    public Point getToolTipLocation(MouseEvent event) {
        String tipText = getToolTipText();
        if (tipText == null || tipText.isEmpty()) {
            lastTipText = null;
            lastTipLocation = null;
            return null;
        }
        if (tipText.equals(lastTipText) && lastTipLocation != null) {
            return lastTipLocation;
        }
        double s = D2UI.getUiScale();
        int sx = (int) Math.round(event.getX() * s) + 16;
        int sy = (int) Math.round(event.getY() * s) + 24;
        lastTipText = tipText;
        lastTipLocation = new Point(sx, sy);
        return lastTipLocation;
    }

    /** Cache for {@link #getToolTipLocation} — see field doc on the method. */
    private String lastTipText;
    private Point lastTipLocation;

    /**
     * Pre-paint the tooltip background black so HeavyweightPopup (separate OS
     * window, used when the popup overflows the parent frame — common for tall
     * Unique/Set HTML tooltips on right/bottom-edge items) doesn't flash white
     * for one frame before the dark item HTML paints over it.
     */
    @Override
    public JToolTip createToolTip() {
        JToolTip tip = super.createToolTip();
        tip.setBackground(Color.BLACK);
        tip.setForeground(Color.WHITE);
        tip.setOpaque(true);
        return tip;
    }

    private MouseEvent translate(MouseEvent e) {
        double s = D2UI.getUiScale();
        if (s == 1.0) return e;
        int nx = (int) Math.round(e.getX() / s);
        int ny = (int) Math.round(e.getY() / s);
        if (e instanceof MouseWheelEvent w) {
            return new MouseWheelEvent(
                    w.getComponent(), w.getID(), w.getWhen(), w.getModifiersEx(),
                    nx, ny, w.getXOnScreen(), w.getYOnScreen(),
                    w.getClickCount(), w.isPopupTrigger(),
                    w.getScrollType(), w.getScrollAmount(), w.getWheelRotation(),
                    w.getPreciseWheelRotation());
        }
        return new MouseEvent(
                e.getComponent(), e.getID(), e.getWhen(), e.getModifiersEx(),
                nx, ny, e.getXOnScreen(), e.getYOnScreen(),
                e.getClickCount(), e.isPopupTrigger(), e.getButton());
    }
}
