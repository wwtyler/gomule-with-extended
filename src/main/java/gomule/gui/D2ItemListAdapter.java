/*
 * Created on 6-jun-2007
 *
 * Window - Preferences - Java - Code Style - Code Templates
 */
package gomule.gui;

import java.io.File;
import java.util.ArrayList;

import gomule.util.D2Project;

/**
 * @author Marco
 * <p>
 * 
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class D2ItemListAdapter implements D2ItemList {
    protected String iFileName;

    private long iTimestamp;

    private ArrayList<D2ItemListListener> iListeners = new ArrayList<>();
    private boolean iModified;

    private boolean iIgnoreItemListEvents = false;

    protected D2ItemListAdapter(String pFileName) {
        iFileName = pFileName;
        initTimestamp();
    }

    @Override
    public final void save(D2Project pProject) {
        saveInternal(pProject);
        initTimestamp();
    }

    protected abstract void saveInternal(D2Project pProject);

    @Override
    public final void initTimestamp() {
        iTimestamp = (new File(iFileName)).lastModified();
    }

    @Override
    public boolean checkTimestamp() {
        long lTimestamp = (new File(iFileName)).lastModified();
        return iTimestamp == lTimestamp;
    }

    public Object getItemListInfo() {
        return iListeners;
    }

    @SuppressWarnings("unchecked")
    public void putItemListInfo(Object pItemListInfo) {
        iListeners = (ArrayList<D2ItemListListener>) pItemListInfo;
    }

    @Override
    public boolean isModified() {
        return iModified;
    }

    public void setModified(boolean pModified) {
        iModified = pModified;
        fireD2ItemListEvent();
    }

    @Override
    public void addD2ItemListListener(D2ItemListListener pListener) {
        iListeners.add(pListener);
    }

    @Override
    public void removeD2ItemListListener(D2ItemListListener pListener) {
        iListeners.remove(pListener);
    }

    @Override
    public boolean hasD2ItemListListener() {
        return !iListeners.isEmpty();
    }

    @Override
    public void fireD2ItemListEvent() {
        if (iIgnoreItemListEvents) {
            return;
        }
        for (int i = 0; i < iListeners.size(); i++) {
            ((D2ItemListListener) iListeners.get(i)).itemListChanged();
        }
    }

    @Override
    public void ignoreItemListEvents() {
        iIgnoreItemListEvents = true;
    }

    @Override
    public void listenItemListEvents() {
        iIgnoreItemListEvents = false;
    }


}
