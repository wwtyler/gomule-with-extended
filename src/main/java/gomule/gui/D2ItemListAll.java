/*
 * Created on 8-jun-2007
 *
 * 
 * Window - Preferences - Java - Code Style - Code Templates
 */
package gomule.gui;

import java.io.PrintWriter;
import java.util.ArrayList;

import gomule.d2s.D2Character;
import gomule.d2x.D2Stash;
import gomule.item.D2Item;
import gomule.util.D2Log;
import gomule.util.D2Project;

/**
 * @author Marco
 * <p>
 * 
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class D2ItemListAll implements D2ItemList {
    private D2FileManager iFileManager;
    private D2Project iProject;

    private ArrayList<D2ItemList> iList = new ArrayList<>();
    private ArrayList<D2ItemListListener> iD2ItemListListenerList = new ArrayList<>();
    private boolean iIgnoreItemListEvents = false;

    public D2ItemListAll(D2FileManager pFileManager, D2Project pProject) {
        iFileManager = pFileManager;
        iProject = pProject;

        ArrayList<String> lFileNames = new ArrayList<>();

        lFileNames.addAll(iProject.getCharList());
        lFileNames.addAll(iProject.getStashList());

        for (int i = 0; i < lFileNames.size(); i++) {
            try {
                D2ItemList lList = iFileManager.addItemList((String) lFileNames.get(i), null);
                iList.add(lList);
            } catch (Exception pEx) {
                System.err.println("Error with: " + ((String) lFileNames.get(i)));
                D2Log.error("D2ItemListAll", pEx, "Error loading file: %s", lFileNames.get(i));
            }
        }
        fireD2ItemListEvent();
    }

    public void connect(String pFileName) {
        try {
            D2ItemList lList = iFileManager.addItemList(pFileName, null);
            // set listeners
            for (int i = 0; i < iD2ItemListListenerList.size(); i++) {
                D2ItemListListener lListener = (D2ItemListListener) iD2ItemListListenerList.get(i);
                lList.addD2ItemListListener(lListener);
            }
            iList.add(lList);
            fireD2ItemListEvent();
        } catch (Exception pEx) {
            D2Log.error("D2ItemListAll", pEx, "connect failed: %s", pFileName);
        }
    }

    public void disconnect(String pFileName) {
        try {
            D2ItemList lList = iFileManager.getItemList(pFileName);

            if (lList != null) {
                // remove listeners
                for (int i = 0; i < iD2ItemListListenerList.size(); i++) {
                    D2ItemListListener lListener = (D2ItemListListener) iD2ItemListListenerList.get(i);
                    lList.removeD2ItemListListener(lListener);
                }

                iFileManager.removeItemList(pFileName, null);

                iList.remove(lList);
                fireD2ItemListEvent();
            }
        } catch (Exception pEx) {
            D2Log.error("D2ItemListAll", pEx, "disconnect failed: %s", pFileName);
        }
    }

    public ArrayList<D2ItemList> getAllContainers() {
        return iList;
    }

    @Override
    public String getFilename() {
        return "all";
    }

    public String getFilename(D2Item pItem) {
        D2ItemList lItemList;
        for (int i = 0; i < iList.size(); i++) {
            lItemList = (D2ItemList) iList.get(i);
            if (lItemList.containsItem(pItem)) {
                if (lItemList.getFilename().toLowerCase().endsWith(".d2s")) {
                    return ((D2Character) lItemList).getCharName() + ".d2s";
                } else {
                    return ((D2Stash) lItemList).getFileNameEnd();
                }
            }
        }

        return null;
    }

    @Override
    public boolean containsItem(D2Item pItem) {
        D2ItemList lItemList;
        for (int i = 0; i < iList.size(); i++) {
            lItemList = (D2ItemList) iList.get(i);
            if (lItemList.containsItem(pItem)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void removeItem(D2Item pItem) {
        D2ItemList lItemList;
        for (int i = 0; i < iList.size(); i++) {
            lItemList = (D2ItemList) iList.get(i);
            if (lItemList.containsItem(pItem)) {
                lItemList.removeItem(pItem);
                return;
            }
        }
    }

    @Override
    public ArrayList<D2Item> getItemList() {
        ArrayList<D2Item> lList = new ArrayList<>();

        D2ItemList lItemList;
        for (int i = 0; i < iList.size(); i++) {
            lItemList = (D2ItemList) iList.get(i);
            lList.addAll(lItemList.getItemList());
        }

        return lList;
    }

    @Override
    public int getNrItems() {
        int lNrItems = 0;

        D2ItemList lItemList;
        for (int i = 0; i < iList.size(); i++) {
            lItemList = (D2ItemList) iList.get(i);
            lNrItems += lItemList.getNrItems();
        }

        return lNrItems;
    }

    @Override
    public boolean isModified() {
        D2ItemList lItemList;
        for (int i = 0; i < iList.size(); i++) {
            lItemList = (D2ItemList) iList.get(i);
            if (lItemList.isModified()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void addD2ItemListListener(D2ItemListListener pListener) {
        iD2ItemListListenerList.add(pListener);
        D2ItemList lItemList;
        for (int i = 0; i < iList.size(); i++) {
            lItemList = (D2ItemList) iList.get(i);
            lItemList.addD2ItemListListener(pListener);
        }
    }

    @Override
    public void removeD2ItemListListener(D2ItemListListener pListener) {
        iD2ItemListListenerList.remove(pListener);
        D2ItemList lItemList;
        for (int i = 0; i < iList.size(); i++) {
            lItemList = (D2ItemList) iList.get(i);
            lItemList.removeD2ItemListListener(pListener);
        }
    }

    @Override
    public final void fireD2ItemListEvent() {
        if (iIgnoreItemListEvents) {
            return;
        }
        for (int i = 0; i < iD2ItemListListenerList.size(); i++) {
            D2ItemListListener lListener = (D2ItemListListener) iD2ItemListListenerList.get(i);
            lListener.itemListChanged();
        }
    }

    @Override
    public boolean hasD2ItemListListener() {
        D2ItemList lItemList;
        for (int i = 0; i < iList.size(); i++) {
            lItemList = (D2ItemList) iList.get(i);
            if (lItemList.hasD2ItemListListener()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void save(D2Project pProject) {
        D2ItemList lItemList;
        for (int i = 0; i < iList.size(); i++) {
            lItemList = (D2ItemList) iList.get(i);
            if (lItemList.isModified()) {
                lItemList.save(pProject);
            }
        }
    }

    @Override
    public boolean isSC() {
        D2ItemList lItemList;
        for (int i = 0; i < iList.size(); i++) {
            lItemList = (D2ItemList) iList.get(i);
            if (lItemList.isSC()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isHC() {
        D2ItemList lItemList;
        for (int i = 0; i < iList.size(); i++) {
            lItemList = (D2ItemList) iList.get(i);
            if (lItemList.isHC()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void fullDump(PrintWriter pWriter) {
        D2ItemList lItemList;
        for (int i = 0; i < iList.size(); i++) {
            lItemList = (D2ItemList) iList.get(i);
            lItemList.fullDump(pWriter);
        }
    }

    @Override
    public void initTimestamp() {
        throw new RuntimeException("Internal error: wrong calling");
    }

    @Override
    public boolean checkTimestamp() {
//        throw new RuntimeException("Internal error: wrong calling");
        return true;
    }

    @Override
    public void ignoreItemListEvents() {
        iIgnoreItemListEvents = true;
    }

    @Override
    public void listenItemListEvents() {
        iIgnoreItemListEvents = false;
    }

    @Override
    public void addItem(D2Item pItem) {
        //Do Nothing!
    }

}
