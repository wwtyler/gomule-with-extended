/*******************************************************************************
 *
 * Copyright 2007 Randall
 *
 * This file is part of gomule.
 *
 * gomule is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * gomule is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * gomlue; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 *
 ******************************************************************************/
package randall.flavie;

import java.util.ArrayList;

/**
 * @author Marco
 * <p>
 * Window - Preferences - Java - Code Style - Code Templates
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class TotalObject {
    private final String iDisplay;
    private final String iShort;
    private final String iStyle;

    private final ArrayList iChildren = new ArrayList();

    public TotalObject(String pDisplay, String pShort, String pStyle) {
        iDisplay = pDisplay;
        iShort = pShort;
        iStyle = pStyle;
    }

    public void addChild(Object pChild) {
        iChildren.add(pChild);
    }

    /**
     * @return Returns the children.
     */
    public ArrayList getChildren() {
        return iChildren;
    }

    /**
     * @return Returns the display.
     */
    public String getDisplay() {
        return iDisplay;
    }

    /**
     * @return Returns the short.
     */
    public String getShort() {
        return iShort;
    }

    /**
     * @return Returns the type.
     */
    public String getStyle() {
        return iStyle;
    }

    public PercentageCounter getPercentageCounter() {
        PercentageCounter lCounter = new PercentageCounter();

        for (int i = 0; i < iChildren.size(); i++) {
            switch (iChildren.get(i)) {
                case TotalObject totalObject -> lCounter.add(totalObject.getPercentageCounter());
                case CatObject catObject -> lCounter.add(catObject.getCounter());
                default -> {
                }
            }
        }

        return lCounter;
    }
}
