package gomule.gui.desktop.frames;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import gomule.gui.D2ItemContainer;
import gomule.gui.desktop.generic.GoMuleDesktop;
import gomule.gui.desktop.generic.GoMuleDesktopListener;
import gomule.gui.desktop.generic.GoMuleView;
import gomule.gui.desktop.generic.GoMuleViewDisplayHandler;
import gomule.gui.desktop.generic.GoMuleViewStash;
import gomule.util.D2Log;

/**
 * internal frame implementation for the desktop
 *
 * @author mbr
 */
public class GoMuleFrameDesktop implements GoMuleDesktop {
    // Hide own stuff
    private final JDesktopPane iDesktop;

    public GoMuleFrameDesktop() {
        iDesktop = new JDesktopPane();
        iDesktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);
    }

    @Override
    public JComponent getDisplay() {
        return iDesktop;
    }

    @Override
    public void addView(GoMuleView pView) {
        GoMuleInternalFrame lInternalFrame = new GoMuleInternalFrame(pView);
        int lPos = 10 + (iDesktop.getComponentCount() * 10);
        lInternalFrame.setLocation(lPos, lPos);
        iDesktop.add(lInternalFrame);

        showView(lInternalFrame);
    }

    protected GoMuleInternalFrame getFrame(GoMuleView pView) {
        for (int i = 0; i < iDesktop.getComponentCount(); i++) {
            GoMuleInternalFrame lFrame = (GoMuleInternalFrame) iDesktop.getComponent(i);
            if (lFrame.getView() == pView) {
                return lFrame;
            }
        }
        return null;
    }

    @Override
    public void showView(GoMuleView pView) {
        showView(getFrame(pView));
    }

    protected void showView(GoMuleInternalFrame pFrame) {
        pFrame.toFront();
        try {
            pFrame.setSelected(true);
        } catch (PropertyVetoException e) {
            //Shouldn't worry too much if this happens I guess?
            D2Log.warn("GoMuleFrameDesktop", "setSelected vetoed: %s", e.getMessage());
        }
    }

    @Override
    public void closeView(String pFileName) {
        for (int i = 0; i < iDesktop.getComponentCount(); i++) {
            GoMuleInternalFrame lFrame = (GoMuleInternalFrame) iDesktop.getComponent(i);
            D2ItemContainer lItemContainer = lFrame.getView().getItemContainer();

            if (lItemContainer.getFileName().equalsIgnoreCase(pFileName)) {
                lItemContainer.closeView();
            }
        }
    }

    @Override
    public void removeView(GoMuleView pView) {
        iDesktop.remove(getFrame(pView));
//		pView.getItemContainer().closeView();
    }

    @Override
    public void closeViewAll() {
        for (int i = 0; i < iDesktop.getComponentCount(); i++) {
            GoMuleInternalFrame lFrame = (GoMuleInternalFrame) iDesktop.getComponent(i);
            D2ItemContainer lItemContainer = lFrame.getView().getItemContainer();

            lItemContainer.closeView();
        }
    }

    @Override
    public Iterator<GoMuleView> getIteratorView() {
        ArrayList<GoMuleView> lList = new ArrayList<>();

        for (int i = 0; i < iDesktop.getComponentCount(); i++) {
            GoMuleInternalFrame lFrame = (GoMuleInternalFrame) iDesktop.getComponent(i);
            lList.add(lFrame.getView());
        }
        return lList.iterator();
    }

    @Override
    public GoMuleView getSelectedView() {
        GoMuleInternalFrame lFrame = (GoMuleInternalFrame) iDesktop.getSelectedFrame();
        if (lFrame != null) {
            return lFrame.getView();
        }
        return null;
    }

    protected class GoMuleInternalFrame extends JInternalFrame implements GoMuleViewDisplayHandler {
        private final GoMuleView iView;
        private final ArrayList<GoMuleDesktopListener> iListenerList;

        public GoMuleInternalFrame(GoMuleView pView) {
            super(pView.getItemContainer().getFileName(), (pView instanceof GoMuleViewStash), true, false, true);
            iView = pView;
            iListenerList = new ArrayList<>();

//			setTitle( "Internal Frame" );
            setContentPane(iView.getDisplay());
            setOpaque(true);

            addInternalFrameListener(new InternalFrameAdapter() {
                @Override
                public void internalFrameClosing(InternalFrameEvent e) {
                    for (int i = 0; i < iListenerList.size(); i++) {
                        ((GoMuleDesktopListener) iListenerList.get(i)).viewClosing(iView);
                    }
                }

                @Override
                public void internalFrameActivated(InternalFrameEvent e) {
                    for (int i = 0; i < iListenerList.size(); i++) {
                        ((GoMuleDesktopListener) iListenerList.get(i)).viewActivated(iView);
                    }
                }
            });

            pack();
            setVisible(true);
        }

        public void initializeDisplayHandler() {
            iView.setDisplayHandler(this); // pass on this reference to the view
        }

        @Override
        public void addDesktopListener(GoMuleDesktopListener pListener) {
            iListenerList.add(pListener);
        }

        public GoMuleView getView() {
            return iView;
        }

    }

}
