package gomule.gui.sharedStash;

import gomule.d2i.D2SharedStash;
import gomule.gui.D2ViewClipboard;
import gomule.gui.ItemRightClickMenu;
import gomule.item.D2Item;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

class SharedStashPanelMouseClickHandler extends MouseAdapter {

    private final SharedStashPanel sharedStashPanel;

    public SharedStashPanelMouseClickHandler(SharedStashPanel sharedStashPanel) {
        this.sharedStashPanel = sharedStashPanel;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) handleLeftClick(e);
        if (e.getButton() == MouseEvent.BUTTON3) handleRightClick(e);
    }

    private void handleRightClick(MouseEvent e) {
        D2SharedStash sharedStash = sharedStashPanel.getSharedStash();
        if (sharedStash == null) return;
        int col = SharedStashPanel.getColForXCoord(e.getX());
        int row = SharedStashPanel.getRowForYCoord(e.getY());
        if (col < 0 || row < 0 || col > 16 || row > 13) return;
        D2SharedStash.D2SharedStashPane stashPane = sharedStashPanel.getSelectedStashPane();
        D2Item item = stashPane.getItemCovering(col, row);
        if (item != null) {
            new ItemRightClickMenu(item, this::deleteMenuItemAction).show(sharedStashPanel, e.getX(), e.getY() + 35);
        }
    }

    private void deleteMenuItemAction(D2Item d2Item) {
        removeItem(sharedStashPanel.getSelectedStashPane(), d2Item);
    }

    private void handleLeftClick(MouseEvent e) {
        D2SharedStash sharedStash = sharedStashPanel.getSharedStash();
        if (sharedStash == null) return;
        Integer possibleStashTabClick = getPossibleStashTabClick(e.getX(), e.getY());
        setStashTab(possibleStashTabClick);
        if (isClickOnGoldButton(e.getX(), e.getY())) showGoldDialog();

        int col = SharedStashPanel.getColForXCoord(e.getX());
        int row = SharedStashPanel.getRowForYCoord(e.getY());
        if (col < 0 || row < 0 || col > 16 || row > 13) return;
        D2SharedStash.D2SharedStashPane stashPane = sharedStashPanel.getSelectedStashPane();
        D2Item item = stashPane.getItemCovering(col, row);
        if (item != null) {
            moveItemToClipboard(stashPane, item);
        } else if (D2ViewClipboard.getItem() != null) {
            tryMoveItemFromClipboard(stashPane, col, row);
        }
    }

    private boolean isClickOnGoldButton(int x, int y) {
        return x >= 18 && x <= 510 && y >= 18 && y <= 34;
    }

    private void showGoldDialog() {
        JOptionPane.showConfirmDialog(sharedStashPanel, new SharedStashGoldTransferPanel(sharedStashPanel), "Transfer Gold",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE);
    }

    private void tryMoveItemFromClipboard(D2SharedStash.D2SharedStashPane stashPane, int col, int row) {
        D2SharedStash sharedStash = sharedStashPanel.getSharedStash();
        D2Item item = D2ViewClipboard.getItem();
        if (stashPane.canDropItem(col, row, item)) {
            D2SharedStash.D2SharedStashPane d2SharedStashPane = stashPane.addItem(col, row, D2ViewClipboard.removeItem());
            sharedStash.replacePane(sharedStashPanel.getSelectedStashPaneIndex(), d2SharedStashPane);
            sharedStash.setModified(true);
            sharedStashPanel.setCursorPickupItem();
        }
    }

    private void moveItemToClipboard(D2SharedStash.D2SharedStashPane stashPane, D2Item item) {
        removeItem(stashPane, item);
        D2ViewClipboard.addItem(item);
    }

    private void removeItem(D2SharedStash.D2SharedStashPane stashPane, D2Item item) {
        D2SharedStash sharedStash = sharedStashPanel.getSharedStash();
        D2SharedStash.D2SharedStashPane d2SharedStashPane = stashPane.removeItem(item);
        sharedStash.replacePane(sharedStashPanel.getSelectedStashPaneIndex(), d2SharedStashPane);
        sharedStash.setModified(true);
        sharedStashPanel.setCursorDropItem();
    }

    private void setStashTab(Integer possibleStashTabClick) {
        if (possibleStashTabClick == null) return;
        if (sharedStashPanel.getSelectedStashPaneIndex() == possibleStashTabClick) return;
        sharedStashPanel.setSelectedStashPaneIndex(possibleStashTabClick);
        sharedStashPanel.build();
    }

    private Integer getPossibleStashTabClick(int x, int y) {
        if (x >= 16 && x <= 446 && y >= 33 && y <= 54) {
            if (x <= 77) return 0;
            if (x <= 139) return 1;
            if (x <= 200) return 2;
            if (x <= 260) return 3;
            if (x <= 323) return 4;
            if (x <= 385) return 5;
            return 6;
        }
        return null;
    }
}
