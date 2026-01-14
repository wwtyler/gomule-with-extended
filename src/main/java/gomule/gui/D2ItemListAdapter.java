/*
 * Created on 6-jun-2007
 *
 * Window - Preferences - Java - Code Style - Code Templates
 */
package gomule.gui;

import gomule.util.D2Project;

import java.io.File;
import java.util.ArrayList;

/**
 * @author Marco
 * <p>
 * 
 * Window - Preferences - Java - Code Style - Code Templates
 */
@SuppressWarnings({"ForLoopReplaceableByForEach"})
public abstract class D2ItemListAdapter implements D2ItemList {
    protected String iFileName;

    private long iTimestamp;

    @SuppressWarnings("Convert2Diamond")
    private ArrayList<D2ItemListListener> iListeners = new ArrayList<D2ItemListListener>();
    private boolean iModified;

    private boolean iIgnoreItemListEvents = false;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    protected D2ItemListAdapter(String pFileName) {
        iFileName = pFileName;
        initTimestamp();
    }

    @SuppressWarnings("override")
    public final void save(D2Project pProject) {
        saveInternal(pProject);
        initTimestamp();
    }

    protected abstract void saveInternal(D2Project pProject);

    @SuppressWarnings("override")
    public void initTimestamp() {
        iTimestamp = (new File(iFileName)).lastModified();
    }

    @SuppressWarnings("override")
    public boolean checkTimestamp() {
        long lTimestamp = (new File(iFileName)).lastModified();
        return iTimestamp == lTimestamp;
    }

    public Object getItemListInfo() {
        return iListeners;
    }

    public void putItemListInfo(Object pItemListInfo) {
        iListeners = (ArrayList<D2ItemListListener>) pItemListInfo;
    }

    @SuppressWarnings("override")
    public boolean isModified() {
        return iModified;
    }

    public void setModified(boolean pModified) {
        iModified = pModified;
        fireD2ItemListEvent();
    }

    @SuppressWarnings("override")
    public void addD2ItemListListener(D2ItemListListener pListener) {
        iListeners.add(pListener);
    }

    @SuppressWarnings("override")
    public void removeD2ItemListListener(D2ItemListListener pListener) {
        iListeners.remove(pListener);
    }

    @SuppressWarnings("override")
    public boolean hasD2ItemListListener() {
        return !iListeners.isEmpty();
    }

    @SuppressWarnings("override")
    public void fireD2ItemListEvent() {
        if (iIgnoreItemListEvents) {
            return;
        }
        for (int i = 0; i < iListeners.size(); i++) {
            ((D2ItemListListener) iListeners.get(i)).itemListChanged();
        }
    }

    @SuppressWarnings("override")
    public void ignoreItemListEvents() {
        iIgnoreItemListEvents = true;
    }

    @SuppressWarnings("override")
    public void listenItemListEvents() {
        iIgnoreItemListEvents = false;
    }


}
