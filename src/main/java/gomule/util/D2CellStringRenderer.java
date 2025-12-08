/*
 * Created on 5-mrt-2007
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package gomule.util;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.Serial;

/**
 * @author Marco & Silospen
 * <p>
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class D2CellStringRenderer extends DefaultTableCellRenderer {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1351337843493777035L;

    public D2CellStringRenderer() {


    }

    public Component getTableCellRendererComponent(JTable pTable, Object pValue, boolean pIsSelected, boolean pHasFocus, int pRow, int pColumn) {

        Object lValue;
        Color lForeground = null;
        String lTooltip = null;

//              	System.out.println("yo");
//        	super.setBackground(Color.black);

        if (pValue instanceof D2CellValue value) {
            lValue = value.getValue();
            lForeground = value.getForeground();
            lTooltip = value.getTooltip();
        } else {
            lValue = pValue;
        }

        Component lRenderer = super.getTableCellRendererComponent(pTable, lValue, pIsSelected, pHasFocus, pRow, pColumn);

        lRenderer.setBackground(Color.DARK_GRAY);
        if (pIsSelected) {
            lRenderer.setBackground(Color.black);
        }
        //  lRenderer.setBackground(Color.black);

        if (lForeground != null) {
            lRenderer.setForeground(lForeground);
        } else {
            lRenderer.setForeground(Color.black);
        }

        if (lRenderer instanceof D2CellStringRenderer renderer) {
            renderer.setToolTipText(lTooltip);
        }


        return lRenderer;
    }

}