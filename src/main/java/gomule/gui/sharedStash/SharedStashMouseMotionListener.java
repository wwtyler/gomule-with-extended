package gomule.gui.sharedStash;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import gomule.d2i.D2SharedStash;
import gomule.gui.D2ViewClipboard;
import static gomule.gui.sharedStash.SharedStashPanel.getColForXCoord;
import static gomule.gui.sharedStash.SharedStashPanel.getRowForYCoord;
import gomule.item.D2Item;
import gomule.item.D2ItemRenderer;

class SharedStashMouseMotionListener extends MouseMotionAdapter {
    private final SharedStashPanel sharedStashPanel;
    /** Last item for which a tooltip was rendered — avoids redundant setToolTipText() calls. */
    private D2Item lastTooltipItem = null;

    public SharedStashMouseMotionListener(SharedStashPanel sharedStashPanel) {
        this.sharedStashPanel = sharedStashPanel;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // Materials tab: hit-test against material slots
        if (sharedStashPanel.isMaterialsTabSelected()) {
            D2Item matItem = sharedStashPanel.getMatItemAt(e.getX(), e.getY());
            if (matItem != null) {
                sharedStashPanel.setCursorPickupItem();
                if (matItem != lastTooltipItem) {
                    lastTooltipItem = matItem;
                    sharedStashPanel.setToolTipText(D2ItemRenderer.itemDumpHtml(matItem, false));
                }
            } else {
                if (lastTooltipItem != null) {
                    lastTooltipItem = null;
                    sharedStashPanel.setToolTipText(null);
                }
                sharedStashPanel.setCursorNormal();
            }
            return;
        }

        int col = getColForXCoord(e.getX());
        int row = getRowForYCoord(e.getY());
        if (col < 0 || row < 0 || col > 15 || row > 12) {
            if (lastTooltipItem != null) {
                lastTooltipItem = null;
                sharedStashPanel.setToolTipText(null);
            }
            sharedStashPanel.setCursorNormal();
            return;
        }
        D2SharedStash.D2SharedStashPane stashPane = sharedStashPanel.getSelectedStashPane();
        if (stashPane == null) {
            if (lastTooltipItem != null) {
                lastTooltipItem = null;
                sharedStashPanel.setToolTipText(null);
            }
            sharedStashPanel.setCursorNormal();
            return;
        }
        D2Item item = stashPane.getItemCovering(col, row);
        if (item != null) {
            sharedStashPanel.setCursorPickupItem();
            if (item != lastTooltipItem) {
                lastTooltipItem = item;
                sharedStashPanel.setToolTipText(D2ItemRenderer.itemDumpHtml(item, false));
            }
        } else {
            if (lastTooltipItem != null) {
                lastTooltipItem = null;
                sharedStashPanel.setToolTipText(null);
            }
            D2Item itemOnClipboard = D2ViewClipboard.getItem();
            boolean canDropItem = itemOnClipboard != null && stashPane.canDropItem(col, row, itemOnClipboard);
            if (itemOnClipboard != null && canDropItem) {
                sharedStashPanel.setCursorDropItem();
            } else {
                sharedStashPanel.setCursorNormal();
            }
        }
    }
}
