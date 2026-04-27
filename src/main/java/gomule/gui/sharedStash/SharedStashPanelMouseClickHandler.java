package gomule.gui.sharedStash;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JOptionPane;

import gomule.d2i.D2SharedStash;
import gomule.gui.D2ViewClipboard;
import gomule.gui.ItemRightClickMenu;
import gomule.item.D2Item;

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
        if (sharedStashPanel.isMaterialsTabSelected()) return;
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

        if (sharedStashPanel.isMaterialsTabSelected()) {
            D2Item clipItem = D2ViewClipboard.getItem();
            if (clipItem != null && clipItem.getAdvancedStashQuantity() > 0) {
                // Clipboard holds a material item — put it back into the pane.
                tryReturnMaterialToPane(clipItem, sharedStash);
                return;
            }
            D2Item matItem = sharedStashPanel.getMatItemAt(e.getX(), e.getY());
            if (matItem != null) {
                pickOneMaterialToClipboard(matItem, sharedStash);
            }
            return;
        }

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

    /**
     * Picks up one item from a materials-pane stack and places it on the clipboard.
     * If qty > 1 the original stack is decremented and a fresh single-qty copy is
     * sent to the clipboard. If qty == 1 the item itself is removed and sent directly.
     */
    private void pickOneMaterialToClipboard(D2Item matItem, D2SharedStash sharedStash) {
        int qty = matItem.getAdvancedStashQuantity();
        if (qty <= 1) {
            sharedStash.getMaterialsPane().removeItem(matItem);
            D2ViewClipboard.addItem(matItem);
        } else {
            matItem.setAdvancedStashQuantity(qty - 1);
            // Create a single-qty copy for the clipboard by re-parsing the original bytes.
            try {
                D2Item singleItem = new D2Item(sharedStash.getFilename(),
                        new gomule.util.D2BitReader(matItem.get_bytes().clone()), 75);
                singleItem.setAdvancedStashQuantity(1);
                D2ViewClipboard.addItem(singleItem);
            } catch (Exception ex) {
                ex.printStackTrace();
                return; // do not mark modified if copy failed
            }
        }
        sharedStash.setModified(true);
        sharedStashPanel.setCursorDropItem();
        sharedStashPanel.build();
    }

    /**
     * Returns a clipboard material item to the materials pane.
     * If an existing stack with the same item code is found, its quantity is incremented by 1.
     * Otherwise the item is inserted directly into the pane.
     */
    private void tryReturnMaterialToPane(D2Item clipItem, D2SharedStash sharedStash) {
        D2SharedStash.D2MaterialsPane pane = sharedStash.getMaterialsPane();
        if (pane == null) return;
        String code = clipItem.getItemCode() == null ? null : clipItem.getItemCode().trim();
        D2Item existing = null;
        if (code != null) {
            for (D2Item it : pane.getItems()) {
                if (code.equals(it.getItemCode() == null ? null : it.getItemCode().trim())) {
                    existing = it;
                    break;
                }
            }
        }
        D2ViewClipboard.removeItem();
        if (existing != null) {
            existing.setAdvancedStashQuantity(existing.getAdvancedStashQuantity() + 1);
        } else {
            pane.addItem(clipItem);
        }
        sharedStash.setModified(true);
        sharedStashPanel.setCursorPickupItem();
        sharedStashPanel.build();
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
        D2SharedStash stash = sharedStashPanel.getSharedStash();
        if (stash == null) return null;
        boolean hasMaterials = stash.getMaterialsPane() != null;
        int numNormal = stash.getPanes().size();
        int numTabs = Math.max(1, Math.min(numNormal + (hasMaterials ? 1 : 0), 8));
        int tabAreaX = 16, tabAreaW = 430, tabY = 38, tabH = 16;
        int tabWidth = tabAreaW / numTabs;
        if (y >= tabY && y <= tabY + tabH && x >= tabAreaX && x < tabAreaX + tabWidth * numTabs) {
            int clicked = (x - tabAreaX) / tabWidth;
            return (clicked >= 0 && clicked < numTabs) ? clicked : null;
        }
        return null;
    }
}
