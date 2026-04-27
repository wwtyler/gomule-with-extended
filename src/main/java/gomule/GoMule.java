/*******************************************************************************
 *
 * Copyright 2007 Andy Theuninck & Randall
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

package gomule;

import gomule.gui.D2FileManager;
import gomule.gui.D2ViewChar;
import gomule.gui.FileManagerProperties;
import gomule.gui.LayoutProfile;
import gomule.gui.LookAndFeelOptions;
import gomule.gui.PanelTheme;
import gomule.util.D2UI;
import randall.util.RandallUtil;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Properties;

public class GoMule {
    /**
     * Main Class, runs GoMule
     *
     * @param args Can set L+F
     */
    public static void main(String[] pArgs) {
        try {
            Properties fileManagerPropertiesFile = FileManagerProperties.loadFileManagerProperties();

            // 尽早应用布局配置（在任何 UI 或角色文件加载之前）
            LayoutProfile layoutProfile = LayoutProfile.fromName(
                    fileManagerPropertiesFile.getProperty(LayoutProfile.PROPERTY_NAME));
            D2ViewChar.applyLayout(layoutProfile);

            // 加载面板主题配置
            PanelTheme.active = PanelTheme.fromName(
                    fileManagerPropertiesFile.getProperty(PanelTheme.PROPERTY_NAME));

            String lLookAndFeel = LookAndFeelOptions.valueOf(fileManagerPropertiesFile.getProperty(LookAndFeelOptions.PROPERTY_NAME, LookAndFeelOptions.CLASSIC.name())).getLookAndFeelName();

            String[] lArgs = pArgs;

            if (lArgs == null || lArgs.length == 0) {
                lArgs = readArgumentsFromFile("arguments.txt");
            }

            if (lArgs != null && lArgs.length != 0) {
                for (int i = 0; i < lArgs.length; i++) {
                    if (lArgs[i].equalsIgnoreCase("-system")) {
                        lLookAndFeel = UIManager.getSystemLookAndFeelClassName();
                    } else if (lArgs[i] != null) {
                        LookAndFeelInfo[] lList = UIManager.getInstalledLookAndFeels();
                        for (int j = 0; j < lList.length; j++) {
                            LookAndFeelInfo lInfo = lList[j];
                            System.err.println("LookAndFeel: " + lInfo.getName());
                            if (lArgs[i].equalsIgnoreCase(lInfo.getName())) {
                                lLookAndFeel = lInfo.getClassName();
                            }
                        }
                    }

                }
            }
            UIManager.setLookAndFeel(lLookAndFeel);
            // Apply global menu/UI font size if configured (ui.menu.font.size > 0)
            int menuFontSize = D2UI.getMenuFontSize();
            if (menuFontSize > 0) {
                java.util.Enumeration<?> keys = UIManager.getDefaults().keys();
                while (keys.hasMoreElements()) {
                    Object key = keys.nextElement();
                    Object value = UIManager.get(key);
                    if (value instanceof Font) {
                        Font f = (Font) value;
                        UIManager.put(key, f.deriveFont((float) menuFontSize));
                    }
                }
            }
            UIManager.put("ToolTip.background", Color.black);
            UIManager.put("ToolTip.foreground", Color.white);
            UIManager.put("ToolTip.font", new Font(Font.SANS_SERIF, Font.PLAIN, D2UI.getTooltipFontSize()));
            UIManager.put("info", Color.black);
            ToolTipManager.sharedInstance().setInitialDelay(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Randall: generally adviced for swing, doing anything with GUI inside the swing-thread
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                D2FileManager.getInstance();
            }
        });
    }

    private static String[] readArgumentsFromFile(String pFilename) {
        try {
            // build up here
            File lFile = new File(pFilename);
            if (!lFile.exists()) {
                return null;
            }
            if (!lFile.isFile()) {
                System.err.println("Found arguments.txt is not a file!");
                return null;
            }
            if (!lFile.canRead()) {
                System.err.println("Can not read File: arguments.txt");
                return null;
            }

            BufferedReader lIn = new BufferedReader(new FileReader(pFilename));
            String lLine = lIn.readLine();
            lIn.close();

            ArrayList<String> lString = RandallUtil.split(lLine, " ", false);

            // Convert here
            String lReturn[] = new String[lString.size()];
            for (int i = 0; i < lString.size(); i++) {
                lReturn[i] = (String) lString.get(i);
            }
            return lReturn;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

