/*
 * Created on 11-mei-2007
 *
 * 
 * Window - Preferences - Java - Code Style - Code Templates
 */
package gomule.gui;

import java.io.Serial;

import javax.swing.JRadioButton;

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
    private final Object iData;

    public D2RadioButton(Object pData) {
        super(pData.toString());
        iData = pData;
    }


    public Object getData() {
        return iData;
    }

}
