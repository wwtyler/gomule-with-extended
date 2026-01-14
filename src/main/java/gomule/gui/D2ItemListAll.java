/*
 * Created on 8-jun-2007
 *
 * 
 * Window - Preferences - Java - Code Style - Code Templates
 */
package gomule.gui;

import gomule.d2s.D2Character;
import gomule.d2x.D2Stash;
import gomule.item.D2Item;
import gomule.util.D2Project;

import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * @author Marco
 * <p>
 * 
 * Window - Preferences - Java - Code Style - Code Templates
 */
@SuppressWarnings({"ForLoopReplaceableByForEach"})
public class D2ItemListAll implements D2ItemList {
    private D2FileManager iFileManager;
    private D2Project iProject;

    @SuppressWarnings("Convert2Diamond")
    private ArrayList<D2ItemList> iList = new ArrayList<D2ItemList>();
    @SuppressWarnings("Convert2Diamond")
    private ArrayList<D2ItemListListener> iD2ItemListListenerList = new ArrayList<D2ItemListListener>();
    private boolean iIgnoreItemListEvents = false;

    @SuppressWarnings({"OverridableMethodCallInConstructor", "CallToPrintStackTrace"})
    public D2ItemListAll(D2FileManager pFileManager, D2Project pProject) {
        iFileManager = pFileManager;
        iProject = pProject;

        @SuppressWarnings("Convert2Diamond")
        ArrayList<String> lFileNames = new ArrayList<String>();

        lFileNames.addAll(iProject.getCharList());
        lFileNames.addAll(iProject.getStashList());

        for (int i = 0; i < lFileNames.size(); i++) {
            try {
                D2ItemList lList = iFileManager.addItemList((String) lFileNames.get(i), null);
                iList.add(lList);
            } catch (Exception pEx) {
                System.err.println("Error with: " + ((String) lFileNames.get(i)));
                pEx.printStackTrace();
            }
        }
        fireD2ItemListEvent();
    }

    @SuppressWarnings("CallToPrintStackTrace")
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
            pEx.printStackTrace();
        }
    }

    @SuppressWarnings("CallToPrintStackTrace")
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
            pEx.printStackTrace();
        }
    }

    public ArrayList<D2ItemList> getAllContainers() {
        return iList;
    }

    @SuppressWarnings("override")
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

    @SuppressWarnings("override")
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

    @SuppressWarnings("override")
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

    @SuppressWarnings("override")
    public ArrayList<D2Item> getItemList() {
        @SuppressWarnings("Convert2Diamond")
        ArrayList<D2Item> lList = new ArrayList<D2Item>();

        D2ItemList lItemList;
        for (int i = 0; i < iList.size(); i++) {
            lItemList = (D2ItemList) iList.get(i);
            lList.addAll(lItemList.getItemList());
        }

        return lList;
    }

    @SuppressWarnings("override")
    public int getNrItems() {
        int lNrItems = 0;

        D2ItemList lItemList;
        for (int i = 0; i < iList.size(); i++) {
            lItemList = (D2ItemList) iList.get(i);
            lNrItems += lItemList.getNrItems();
        }

        return lNrItems;
    }

    @SuppressWarnings("override")
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

    @SuppressWarnings("override")
    public void addD2ItemListListener(D2ItemListListener pListener) {
        iD2ItemListListenerList.add(pListener);
        D2ItemList lItemList;
        for (int i = 0; i < iList.size(); i++) {
            lItemList = (D2ItemList) iList.get(i);
            lItemList.addD2ItemListListener(pListener);
        }
    }

    @SuppressWarnings("override")
    public void removeD2ItemListListener(D2ItemListListener pListener) {
        iD2ItemListListenerList.remove(pListener);
        D2ItemList lItemList;
        for (int i = 0; i < iList.size(); i++) {
            lItemList = (D2ItemList) iList.get(i);
            lItemList.removeD2ItemListListener(pListener);
        }
    }

    @SuppressWarnings("override")
    public void fireD2ItemListEvent() {
        if (iIgnoreItemListEvents) {
            return;
        }
        for (int i = 0; i < iD2ItemListListenerList.size(); i++) {
            D2ItemListListener lListener = (D2ItemListListener) iD2ItemListListenerList.get(i);
            lListener.itemListChanged();
        }
    }

    @SuppressWarnings("override")
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

    @SuppressWarnings("override")
    public void save(D2Project pProject) {
        D2ItemList lItemList;
        for (int i = 0; i < iList.size(); i++) {
            lItemList = (D2ItemList) iList.get(i);
            if (lItemList.isModified()) {
                lItemList.save(pProject);
            }
        }
    }

    @SuppressWarnings("override")
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

    @SuppressWarnings("override")
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

    @SuppressWarnings("override")
    public void fullDump(PrintWriter pWriter) {
        D2ItemList lItemList;
        for (int i = 0; i < iList.size(); i++) {
            lItemList = (D2ItemList) iList.get(i);
            lItemList.fullDump(pWriter);
        }
    }

    @SuppressWarnings("override")
    public void initTimestamp() {
        throw new RuntimeException("Internal error: wrong calling");
    }

    @SuppressWarnings("override")
    public boolean checkTimestamp() {
//        throw new RuntimeException("Internal error: wrong calling");
        return true;
    }

    @SuppressWarnings("override")
    public void ignoreItemListEvents() {
        iIgnoreItemListEvents = true;
    }

    @SuppressWarnings("override")
    public void listenItemListEvents() {
        iIgnoreItemListEvents = false;
    }

    @SuppressWarnings("override")
    public void addItem(D2Item pItem) {
        //Do Nothing!
    }

}
