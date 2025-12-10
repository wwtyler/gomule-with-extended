/*
 * Created on 11-mei-2007
 *
 * 
 * Window - Preferences - Java - Code Style - Code Templates
 */
package gomule.gui;

import javax.swing.*;
import java.io.Serial;

/**
 * @author Marco
 * <p>
 * 
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class D2RadioButton extends JRadioButton {
    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 3895689813117999958L;
    private Object iData;

    public D2RadioButton(Object pData) {
        super(pData.toString());
        iData = pData;
    }


    public Object getData() {
        return iData;
    }

}
