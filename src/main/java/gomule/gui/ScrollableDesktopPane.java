package gomule.gui;

import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.Scrollable;

/**
 * A {@link JDesktopPane} that implements {@link Scrollable} so that a
 * wrapping {@link javax.swing.JScrollPane} can display horizontal and
 * vertical scrollbars whenever the contained {@link JInternalFrame}s extend
 * beyond the visible viewport area.
 *
 * <p>The preferred size dynamically expands to cover the bounding box of all
 * visible frames (plus a small margin). When the content fits within the
 * viewport the component tracks the viewport dimensions so no scrollbars
 * appear, keeping the MDI feel unchanged for small-scale layouts.
 */
public class ScrollableDesktopPane extends JDesktopPane implements Scrollable {

    private static final long serialVersionUID = 1L;
    private static final int MARGIN = 24;

    @Override
    public Dimension getPreferredSize() {
        int maxX = 0;
        int maxY = 0;
        for (JInternalFrame f : getAllFrames()) {
            if (f.isVisible()) {
                maxX = Math.max(maxX, f.getX() + f.getWidth());
                maxY = Math.max(maxY, f.getY() + f.getHeight());
            }
        }
        if (maxX > 0) maxX += MARGIN;
        if (maxY > 0) maxY += MARGIN;
        // Ensure the pane fills the viewport when content is small
        java.awt.Container parent = getParent();
        int parentW = (parent != null) ? parent.getWidth() : 800;
        int parentH = (parent != null) ? parent.getHeight() : 600;
        return new Dimension(Math.max(maxX, parentW), Math.max(maxY, parentH));
    }

    // ── Scrollable ────────────────────────────────────────────────────────────

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 32;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 160;
    }

    /**
     * Track the viewport width only when the content fits; otherwise let the
     * scrollbar appear.
     */
    @Override
    public boolean getScrollableTracksViewportWidth() {
        java.awt.Container parent = getParent();
        if (parent == null) return true;
        return getPreferredSize().width <= parent.getWidth();
    }

    /**
     * Track the viewport height only when the content fits; otherwise let the
     * scrollbar appear.
     */
    @Override
    public boolean getScrollableTracksViewportHeight() {
        java.awt.Container parent = getParent();
        if (parent == null) return true;
        return getPreferredSize().height <= parent.getHeight();
    }
}
