/*******************************************************************************
 *
 * Copyright 2007 Andy Theuninck, Randall & Silospen
 *
 * This file is part of gomule.
 *
 * gomule is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * gomule is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with gomlue; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301
 * USA
 *
 ******************************************************************************/
package gomule.gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.Serial;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ToolTipManager;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import gomule.d2s.D2Character;
import static gomule.d2s.D2Character.STASHSIZEX;
import static gomule.d2s.D2Character.STASHSIZEY;
import gomule.item.D2Item;
import gomule.item.D2ItemRenderer;
import gomule.util.ScaledPainterPanel;
import randall.util.RandallPanel;

/**
 * @author Marco
 */
public class D2ViewChar extends JInternalFrame implements D2ItemContainer, D2ItemListListener {
    /**
     *
     */
    @Serial
    private static final long serialVersionUID = -7350581523641897831L;
    // ── 布局相关字段（非 final，可在启动时由 LayoutProfile 设置） ───────────
    // 默认对应 BIG_STASH (1.1) + BigInventory 10x8 (1.3) + BigCube 6x4 (1.2) 组合。
    private static LayoutProfile sActiveProfile = LayoutProfile.BIG_STASH;
    private static int BG_WIDTH = LayoutProfile.BIG_STASH.bgWidth;
    private static int BG_HEIGHT = LayoutProfile.BIG_STASH.bgHeight;
    private static final int BG_MERC_WIDTH = 339;
    private static final int BG_MERC_HEIGHT = 254;
    private static final int BG_CURSOR_WIDTH = 78;
    private static final int BG_CURSOR_HEIGHT = 135;
    private static int STASH_X = LayoutProfile.BIG_STASH.stashCoordX;
    private static int STASH_Y = LayoutProfile.BIG_STASH.stashCoordY;
    private static int INV_X = LayoutProfile.BIG_STASH.invCoordX;
    private static int INV_Y = LayoutProfile.BIG_STASH.invCoordY;
    private static final int HEAD_X = 135;
    private static final int HEAD_Y = 3;
    private static final int NECK_X = 205;
    private static final int NECK_Y = 30;
    private static final int BODY_X = 135;
    private static final int BODY_Y = 75;
    private static final int L_ARM_X = 250;
    private static final int L_ARM_Y = 50;
    private static final int R_ARM_X = 20;
    private static final int R_ARM_Y = 50;
    private static final int GLOVES_X = 18;
    private static final int GLOVES_Y = 175;
    private static final int BELT_X = 135;
    private static final int BELT_Y = 175;
    private static final int BOOTS_X = 250;
    private static final int BOOTS_Y = 175;
    private static final int L_RING_X = 91;
    private static final int L_RING_Y = 175;
    private static final int R_RING_X = 205;
    private static final int R_RING_Y = 175;
    private static int BELT_GRID_X = LayoutProfile.BIG_STASH.beltCoordX;
    private static int BELT_GRID_Y = LayoutProfile.BIG_STASH.beltCoordY;
    private static int CUBE_X = LayoutProfile.BIG_STASH.cubeCoordX;
    private static int CUBE_Y = LayoutProfile.BIG_STASH.cubeCoordY;
    private static final int GRID_SIZE = 28;

    /**
     * 根据选定的布局配置更新所有坐标和网格尺寸。 应在 Swing 窗口显示之前（main 入口或属性加载后）调用一次。
     */
    public static void applyLayout(LayoutProfile profile) {
        sActiveProfile = profile;
        D2Character.applyLayout(profile);
        BG_WIDTH = profile.bgWidth;
        BG_HEIGHT = profile.bgHeight;
        STASH_X = profile.stashCoordX;
        STASH_Y = profile.stashCoordY;
        INV_X = profile.invCoordX;
        INV_Y = profile.invCoordY;
        CUBE_X = profile.cubeCoordX;
        CUBE_Y = profile.cubeCoordY;
        BELT_GRID_X = profile.beltCoordX;
        BELT_GRID_Y = profile.beltCoordY;
    }

    private static final int GRID_SPACER = 1;

    // ── Merc panel (Merc.png rendered at 339×254 = 33% of 1029×770) ──
    // Positions derived from original image pixel coordinates * (339/1029, 254/770).
    // Item draw = slot_center_in_panel - item_halfsize (GRID_SIZE=28, so half=14/28/42/56).
    //   Head(2×2): center(510,160)/orig → (168,53) panel; top-left=(168-28, 53-28)=(140,25)
    //   Neck(1×1): center(693,249)      → (228,82)       ; top-left=(228-14, 82-14)=(214,68)
    //   Body(2×3): center(510,408)      → (168,135)      ; top-left=(168-28,135-42)=(140,93)
    //   R.Arm(2×4): center(133,304)     → (44,100)       ; top-left=(44-28, 100-56)=(16,44)
    //   L.Arm(2×4): center(897,304)     → (295,100)      ; top-left=(295-28, 100-56)=(267,44)
    //   Gloves(2×2): center(133,609)    → (44,201)       ; top-left=(44-28, 201-28)=(16,173)
    //   Belt(2×1): center(510,692)      → (168,228)      ; top-left=(168-28, 228-14)=(140,214)
    //   Boots(2×2): center(897,609)     → (295,201)      ; top-left=(295-28, 201-28)=(267,173)
    //   L.Ring(1×1): center(323,692)    → (106,228)      ; top-left=(106-14, 228-14)=(92,214)
    //   R.Ring(1×1): center(693,692)    → (228,228)      ; top-left=(228-14, 228-14)=(214,214)
    private static final int MERC_HEAD_X   = 140;  private static final int MERC_HEAD_Y   = 25 -5;
    private static final int MERC_NECK_X   = 214;  private static final int MERC_NECK_Y   = 68;
    private static final int MERC_BODY_X   = 140;  private static final int MERC_BODY_Y   = 93 +20;
    private static final int MERC_R_ARM_X  = 16;   private static final int MERC_R_ARM_Y  = 44;
    private static final int MERC_L_ARM_X  = 267;  private static final int MERC_L_ARM_Y  = 44;
    private static final int MERC_GLOVES_X = 16;   private static final int MERC_GLOVES_Y = 173 +8;
    private static final int MERC_BELT_X   = 140;  private static final int MERC_BELT_Y   = 214;
    private static final int MERC_BOOTS_X  = 267;  private static final int MERC_BOOTS_Y  = 173 +8;
    private static final int MERC_L_RING_X = 92;   private static final int MERC_L_RING_Y = 214 -6;
    private static final int MERC_R_RING_X = 214;  private static final int MERC_R_RING_Y = 214 -6;
    // Full-slot hit regions: frame boundaries in original image * (339/1029, 254/770)
    private static final int MSLOT_HEAD_X1=131,   MSLOT_HEAD_Y1=10,   MSLOT_HEAD_X2=205,   MSLOT_HEAD_Y2=96 -15;
    private static final int MSLOT_NECK_X1=208,   MSLOT_NECK_Y1=63,   MSLOT_NECK_X2=249,   MSLOT_NECK_Y2=101;
    private static final int MSLOT_TORSO_X1=131,  MSLOT_TORSO_Y1=98,  MSLOT_TORSO_X2=205,  MSLOT_TORSO_Y2=172 +20;
    private static final int MSLOT_RARM_X1=9,     MSLOT_RARM_Y1=26,   MSLOT_RARM_X2=78,    MSLOT_RARM_Y2=175 -20;
    private static final int MSLOT_LARM_X1=261,   MSLOT_LARM_Y1=26,   MSLOT_LARM_X2=330,   MSLOT_LARM_Y2=175 -20;
    private static final int MSLOT_GLOVES_X1=9,   MSLOT_GLOVES_Y1=176,MSLOT_GLOVES_X2=78,  MSLOT_GLOVES_Y2=226;
    private static final int MSLOT_BOOTS_X1=261,  MSLOT_BOOTS_Y1=176, MSLOT_BOOTS_X2=330,  MSLOT_BOOTS_Y2=226;
    private static final int MSLOT_LRING_X1=83,   MSLOT_LRING_Y1=207, MSLOT_LRING_X2=130,  MSLOT_LRING_Y2=250;
    private static final int MSLOT_BELT_X1=131,   MSLOT_BELT_Y1=207,  MSLOT_BELT_X2=205,   MSLOT_BELT_Y2=250;
    private static final int MSLOT_RRING_X1=208,  MSLOT_RRING_Y1=207, MSLOT_RRING_X2=249,  MSLOT_RRING_Y2=250;

    private static final int CURSOR_X = 12;
    private static final int CURSOR_Y = 14;
    private D2CharPainterPanel iCharPainter;
    private D2MercPainterPanel iMercPainter;
    private D2CharCursorPainterPanel iCharCursorPainter;
    private D2Character iCharacter;
    private JTextArea iMessage;
    private JTextArea CJT = new JTextArea();
    private JTextArea MJT = new JTextArea();
    private String iFileName;
    private D2FileManager iFileManager;

    private int iWeaponSlot = 1;
    private int iSkillSlot = 0;

    private JTextField iGold;
    private JTextField iGoldBank;
    private JTextField iGoldMax;
    private JTextField iGoldBankMax;
    private JRadioButton iConnectGold;
    private JRadioButton iConnectGoldBank;
    private JTextField iTransferFree;

    private JButton iGoldTransferBtns[];
    private JTabbedPane lTabs = new JTabbedPane();
    private D2QuestPainterPanel lQuestPanel;
    private D2SkillPainterPanel lSkillPanel;
    private D2WayPainterPanel lWayPanel;
    private D2DeathPainterPanel iDeathPainter;
    private JTextArea lDump;
    private RandallPanel lDumpPanel;

    public D2ViewChar(D2FileManager pMainFrame, String pFileName) {
        super(pFileName, false, true, false, true);

        addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameClosing(InternalFrameEvent e) {
                iFileManager.saveAll();
                closeView();
            }
        });

        ToolTipManager.sharedInstance().setDismissDelay(40000);
        ToolTipManager.sharedInstance().setInitialDelay(300);
        // ToolTip

        iFileManager = pMainFrame;
        iFileName = pFileName;


        JPanel lCharPanel = new JPanel();
        lCharPanel.setLayout(new BorderLayout());
        iCharPainter = new D2CharPainterPanel();
        lCharPanel.add(iCharPainter, BorderLayout.CENTER);
        lTabs.addTab("Character", lCharPanel);
        setContentPane(lTabs);
        iCharPainter.build();

        JPanel lStatPanel = new JPanel();
        lStatPanel.setLayout(new BorderLayout());
        lTabs.addTab("Stats", lStatPanel);
        Box charMainBox = Box.createHorizontalBox();
        Box charMainBox2 = Box.createHorizontalBox();
        // charMainBox.setLayout(new )
        Box charStatsBox = Box.createHorizontalBox();
        Box charLabelBox = Box.createVerticalBox();
        Box charValueBox = Box.createVerticalBox();

        lSkillPanel = new D2SkillPainterPanel();

        // charMainBox.add(charStatsBox);

        CJT.setEditable(false);
        CJT.setFont(new Font("monospaced", Font.PLAIN, 11));

        charMainBox.add(CJT);
        charMainBox2.add(lSkillPanel);

        charStatsBox.add(charLabelBox);
        charStatsBox.add(Box.createRigidArea(new Dimension(10, 0)));
        charStatsBox.add(charValueBox);
        lStatPanel.add(charMainBox, BorderLayout.LINE_START);
        lStatPanel.add(charMainBox2, BorderLayout.LINE_END);


        JPanel lQuestWPanel = new JPanel();

        lQuestWPanel.setLayout(new BorderLayout());
        lQuestPanel = new D2QuestPainterPanel();
        lWayPanel = new D2WayPainterPanel();
        lQuestWPanel.add(lQuestPanel, BorderLayout.LINE_START);
        lQuestWPanel.add(lWayPanel, BorderLayout.EAST);
        lTabs.addTab("Quest", lQuestWPanel);
        lQuestPanel.build();
        lWayPanel.build();
        lQuestWPanel.setBackground(Color.BLACK);


        JPanel lCursorPanel = new JPanel();
        lCursorPanel.setLayout(new BorderLayout());
        iCharCursorPainter = new D2CharCursorPainterPanel();
        lCursorPanel.add(new JLabel("For item viewing, no items can be put or removed from here"),
                BorderLayout.NORTH);

        iDeathPainter = new D2DeathPainterPanel();
        lCursorPanel.add(iDeathPainter, BorderLayout.WEST);
        Box B1 = Box.createHorizontalBox();
        Box B2 = Box.createHorizontalBox();

        Box V1 = Box.createVerticalBox();

        B2.add(new JLabel("Cursor:"));
        B2.add(Box.createRigidArea(new Dimension(40, 0)));
        V1.add(B2);
        V1.add(B1);
        B1.add(iCharCursorPainter);
        B1.add(Box.createRigidArea(new Dimension(40, 0)));
        lCursorPanel.add(V1, BorderLayout.EAST);

        // lCursorPanel.add(Box.createRigidArea(new Dimension(10, 0)), BorderLayout.EAST);


        lTabs.addTab("Corpse", lCursorPanel);
        iCharCursorPainter.build();

        JPanel lMercPanel = new JPanel();
        lMercPanel.setLayout(new BorderLayout());
        iMercPainter = new D2MercPainterPanel();

        Box mercMainBox = Box.createHorizontalBox();
        Box mercMainBox2 = Box.createHorizontalBox();
        Box mercStatsBox = Box.createHorizontalBox();
        Box mercLabelBox = Box.createVerticalBox();
        Box mercValueBox = Box.createVerticalBox();

        MJT.setEditable(false);
        MJT.setFont(new Font("monospaced", Font.PLAIN, 11));

        mercMainBox.add(MJT);
        mercStatsBox.add(mercLabelBox);
        mercStatsBox.add(Box.createRigidArea(new Dimension(10, 0)));
        mercStatsBox.add(mercValueBox);

        // Centre the merc equipment painter in the right area so it doesn't float to bottom-right
        JPanel mercEquipCenter = new JPanel(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints mercGbc = new java.awt.GridBagConstraints();
        mercGbc.anchor = java.awt.GridBagConstraints.NORTH;
        mercGbc.weightx = 1.0; mercGbc.weighty = 1.0;
        mercEquipCenter.add(iMercPainter, mercGbc);   // top-center

        lMercPanel.add(mercMainBox, BorderLayout.LINE_START);
        lMercPanel.add(mercEquipCenter, BorderLayout.CENTER);
        lTabs.addTab("Mercenary", lMercPanel);


        ButtonGroup lConnectGroup = new ButtonGroup();

        RandallPanel lBankPanel = new RandallPanel();
        iGold = new JTextField();
        iGold.setEditable(false);
        iGoldMax = new JTextField();
        iGoldMax.setEditable(false);
        iConnectGold = new JRadioButton();
        lConnectGroup.add(iConnectGold);

        iGoldBank = new JTextField();
        iGoldBank.setEditable(false);
        iGoldBankMax = new JTextField();
        iGoldBankMax.setEditable(false);
        iConnectGoldBank = new JRadioButton();
        lConnectGroup.add(iConnectGoldBank);
        iConnectGoldBank.setSelected(true);

        lBankPanel.addToPanel(new JLabel("Gold: "), 0, 0, 1, RandallPanel.NONE);
        lBankPanel.addToPanel(iConnectGold, 1, 0, 1, RandallPanel.NONE);
        lBankPanel.addToPanel(iGold, 2, 0, 1, RandallPanel.HORIZONTAL);
        lBankPanel.addToPanel(iGoldMax, 3, 0, 1, RandallPanel.HORIZONTAL);
        lBankPanel.addToPanel(new JLabel("Gold Stash: "), 0, 1, 1, RandallPanel.NONE);
        lBankPanel.addToPanel(iConnectGoldBank, 1, 1, 1, RandallPanel.NONE);
        lBankPanel.addToPanel(iGoldBank, 2, 1, 1, RandallPanel.HORIZONTAL);
        lBankPanel.addToPanel(iGoldBankMax, 3, 1, 1, RandallPanel.HORIZONTAL);

        RandallPanel lTransferPanel = new RandallPanel(true);
        lTransferPanel.setBorder("Transfer");

        iGoldTransferBtns = new JButton[8];

        iGoldTransferBtns[0] = new JButton("to char");
        iGoldTransferBtns[0].addActionListener(pEvent -> transferToChar(10000));
        JTextField lField10000 = new JTextField("10.000");
        lField10000.setEditable(false);
        iGoldTransferBtns[1] = new JButton("from char");
        iGoldTransferBtns[1].addActionListener(pEvent -> transferFromChar(10000));

        iGoldTransferBtns[2] = new JButton("to char");
        iGoldTransferBtns[2].addActionListener(pEvent -> transferToChar(100000));
        JTextField lField100000 = new JTextField("100.000");
        lField100000.setEditable(false);
        iGoldTransferBtns[3] = new JButton("from char");
        iGoldTransferBtns[3].addActionListener(pEvent -> transferFromChar(100000));

        iGoldTransferBtns[4] = new JButton("to char");
        iGoldTransferBtns[4].addActionListener(pEvent -> transferToChar(1000000));
        JTextField lField1000000 = new JTextField("1.000.000");
        lField1000000.setEditable(false);
        iGoldTransferBtns[5] = new JButton("from char");
        iGoldTransferBtns[5].addActionListener(pEvent -> transferFromChar(1000000));

        iGoldTransferBtns[6] = new JButton("to char");
        iGoldTransferBtns[6].addActionListener(pEvent -> transferToChar(getTransferFree()));
        iTransferFree = new JTextField("10000");
        iGoldTransferBtns[7] = new JButton("from char");
        iGoldTransferBtns[7].addActionListener(pEvent -> transferFromChar(getTransferFree()));

        lTransferPanel.addToPanel(iGoldTransferBtns[0], 0, 0, 1, RandallPanel.NONE);
        lTransferPanel.addToPanel(lField10000, 1, 0, 1, RandallPanel.HORIZONTAL);
        lTransferPanel.addToPanel(iGoldTransferBtns[1], 2, 0, 1, RandallPanel.NONE);

        lTransferPanel.addToPanel(iGoldTransferBtns[2], 0, 1, 1, RandallPanel.NONE);
        lTransferPanel.addToPanel(lField100000, 1, 1, 1, RandallPanel.HORIZONTAL);
        lTransferPanel.addToPanel(iGoldTransferBtns[3], 2, 1, 1, RandallPanel.NONE);

        lTransferPanel.addToPanel(iGoldTransferBtns[4], 0, 2, 1, RandallPanel.NONE);
        lTransferPanel.addToPanel(lField1000000, 1, 2, 1, RandallPanel.HORIZONTAL);
        lTransferPanel.addToPanel(iGoldTransferBtns[5], 2, 2, 1, RandallPanel.NONE);

        lTransferPanel.addToPanel(iGoldTransferBtns[6], 0, 3, 1, RandallPanel.NONE);
        lTransferPanel.addToPanel(iTransferFree, 1, 3, 1, RandallPanel.HORIZONTAL);
        lTransferPanel.addToPanel(iGoldTransferBtns[7], 2, 3, 1, RandallPanel.NONE);

        lBankPanel.addToPanel(lTransferPanel, 0, 10, 3, RandallPanel.HORIZONTAL);

        lBankPanel.finishDefaultPanel();
        lTabs.addTab("Bank", lBankPanel);
        lDumpPanel = new RandallPanel();
        lDump = new JTextArea();
        JScrollPane dumpScroll = new JScrollPane(lDump);
        dumpScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        dumpScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        lDumpPanel.addToPanel(dumpScroll, 0, 0, 1, RandallPanel.BOTH);
        lDump.setFont(new Font("monospaced", Font.PLAIN, 11));
        // HTMLEditorKit htmlEditor = new HTMLEditorKit();
        // lDump.setEditorKit(htmlEditor);
        // lDump.setPreferredSize(new Dimension(520,360));

        dumpScroll.setPreferredSize(new Dimension(520, 360));
        lDump.setAutoscrolls(false);
        lDump.setVisible(true);
        // lDump.setBounds(6,7,175,179);
        // dumpScroll.setBounds(6,7,175,179);
        lTabs.addTab("Dump", lDumpPanel);

        lTabs.addMouseListener(new MyMouse());

        iMessage = new JTextArea();
        JScrollPane lScroll = new JScrollPane(iMessage);
        RandallPanel lMessagePanel = new RandallPanel();
        JButton lConnect = new JButton("Connect");
        lConnect.addActionListener(pEvent -> connect());
        JButton lDisconnect = new JButton("Disconnect");
        lDisconnect.addActionListener(pEvent -> disconnect(null));

        lMessagePanel.addToPanel(lConnect, 0, 0, 1, RandallPanel.HORIZONTAL);
        lMessagePanel.addToPanel(lDisconnect, 1, 0, 1, RandallPanel.HORIZONTAL);
        lMessagePanel.addToPanel(lScroll, 0, 1, 2, RandallPanel.BOTH);
        lTabs.addTab("Messages", lMessagePanel);
        iMessage.setText("Nothing done, disconnected");

        pack();
        setVisible(true);

        connect();
        itemListChanged();
    }

    public void paintMercStats() {

        MJT.setText(iCharacter.getMercStatString());
    }

    public void paintCharStats() {

        CJT.setText(iCharacter.getStatString());
        lSkillPanel.build();

    }

    @Override
    public final void connect() {
        if (iCharacter != null) {
            return;
        }
        try {
            iCharacter = (D2Character) iFileManager.addItemList(iFileName, this);


            paintMercStats();
            paintCharStats();
            lSkillPanel.build();
            lQuestPanel.build();
            lWayPanel.build();

            iGold.setText(Integer.toString(iCharacter.getGold()));
            iGoldMax.setText(Integer.toString(iCharacter.getGoldMax()));
            iGoldBank.setText(Integer.toString(iCharacter.getGoldBank()));
            iGoldBankMax.setText(Integer.toString(iCharacter.getGoldBankMax()));

            for (JButton btn : iGoldTransferBtns) {
                btn.setEnabled(true);
            }

            itemListChanged();
            iMessage.setText("Character loaded");
        } catch (Exception pEx) {
            disconnect(pEx);
            pEx.printStackTrace();
        }
    }

    @Override
    public void disconnect(Exception pEx) {
        if (iCharacter != null) {
            iFileManager.removeItemList(iFileName, this);
        }

        iCharacter = null;

        String lText = "Character disconnected";

        if (pEx != null) {
            lText += "\n";
            StackTraceElement[] trace = pEx.getStackTrace();
            for (StackTraceElement ste : trace) {
                lText += "\tat " + ste + "\n";
            }
        }
        iMessage.setText(lText);

        iGold.setText("");
        iGoldMax.setText("");
        iGoldBank.setText("");
        iGoldBankMax.setText("");

        for (JButton btn : iGoldTransferBtns) {
            btn.setEnabled(false);
        }

        itemListChanged();
        // System.gc();
    }

    public void transferToChar(int pGoldTransfer) {
        if (pGoldTransfer > 0) {
            try {
                // to char
                int lBank = iFileManager.getProject().getBankValue();
                if (pGoldTransfer > lBank) {
                    // don't allow more as the bank has
                    pGoldTransfer = lBank;
                }
                int lGoldChar;
                int lGoldMax;
                if (iConnectGold.isSelected()) {
                    lGoldChar = iCharacter.getGold();
                    lGoldMax = iCharacter.getGoldMax();
                } else {
                    lGoldChar = iCharacter.getGoldBank();
                    lGoldMax = iCharacter.getGoldBankMax();
                }
                // char limit
                if (lGoldChar + pGoldTransfer > lGoldMax) {
                    pGoldTransfer = lGoldMax - lGoldChar;
                }
                int lNewGold = lGoldChar + pGoldTransfer;
                int lNewGoldBank = lBank - pGoldTransfer;
                if (iConnectGold.isSelected()) {
                    iCharacter.setGold(lNewGold);
                    iGold.setText(Integer.toString(iCharacter.getGold()));
                } else {
                    iCharacter.setGoldBank(lNewGold);
                    iGoldBank.setText(Integer.toString(iCharacter.getGoldBank()));
                }
                iFileManager.getProject().setBankValue(lNewGoldBank);
            } catch (Exception pEx) {
                D2FileManager.displayErrorDialog(pEx);
            }
        }

    }

    public void transferFromChar(int pGoldTransfer) {
        if (pGoldTransfer > 0) {
            try {
                int lGoldChar;
                if (iConnectGold.isSelected()) {
                    lGoldChar = iCharacter.getGold();
                } else {
                    lGoldChar = iCharacter.getGoldBank();
                }

                if (pGoldTransfer > lGoldChar) {
                    // don't allow more as the char has
                    pGoldTransfer = lGoldChar;
                }

                // from char
                int lBank = iFileManager.getProject().getBankValue();

                int lNewGold = lGoldChar - pGoldTransfer;
                int lNewGoldBank = lBank + pGoldTransfer;

                if (iConnectGold.isSelected()) {
                    iCharacter.setGold(lNewGold);
                    iGold.setText(Integer.toString(iCharacter.getGold()));
                } else {
                    iCharacter.setGoldBank(lNewGold);
                    iGoldBank.setText(Integer.toString(iCharacter.getGoldBank()));
                }
                iFileManager.getProject().setBankValue(lNewGoldBank);
            } catch (Exception pEx) {
                D2FileManager.displayErrorDialog(pEx);
            }
        }

    }

    public int getTransferFree() {
        try {
            return Integer.parseInt(iTransferFree.getText());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public boolean isHC() {
        return iCharacter.isHC();
    }

    @Override
    public boolean isSC() {
        return iCharacter.isSC();
    }

    @Override
    public String getFileName() {
        return iFileName;
    }

    @Override
    public boolean isModified() {
        return iCharacter != null && iCharacter.isModified();
    }

    @Override
    public D2ItemList getItemLists() {
        return iCharacter;
    }

    @Override
    public void closeView() {
        disconnect(null);
        iFileManager.removeFromOpenWindows(this);
    }

    @Override
    public final void itemListChanged() {

        String lTitle;
        if (iCharacter == null) {
            lTitle = "Disconnected";
        } else {
            lTitle = iCharacter.getCharName();
            if (iCharacter == null) {
                lTitle += " (Error Reading File)";
            } else {
                lTitle += ((iCharacter.isModified()) ? "*" : "");
                if (iCharacter.isSC()) {
                    lTitle += " (SC)";
                } else if (iCharacter.isHC()) {
                    lTitle += " (HC)";
                }
                lTitle += iCharacter.getTitleString();
            }
        }
        setTitle(lTitle);
        iCharPainter.build();
        if (iMercPainter != null) {
            iMercPainter.build();
        }
        iCharCursorPainter.build();
        iDeathPainter.build();


    }

    /** 重建所有面板背景（切换 PanelTheme 后调用）。 */
    public void rebuildBackground() {
        iCharPainter.build();
        if (iMercPainter != null) {
            iMercPainter.build();
        }
        iCharPainter.repaint();
        if (iMercPainter != null) {
            iMercPainter.repaint();
        }
    }

    public void setCursorPickupItem() {
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public void setCursorDropItem() {
        setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
    }

    public void setCursorNormal() {
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    public void dumpChar() {

        if (iCharacter == null) {
            // Character failed to load (e.g. d2s parse error). Show a placeholder
            // so clicking the Dump tab does not throw NPE.
            lDump.setText(
                    "(no character loaded — file may have failed to parse; check logs/gomule.log)");
            lDump.setCaretPosition(0);
            lDump.validate();
            return;
        }
        String iChaString = iCharacter.fullDumpStr().replaceAll("<BR>", "\n");
        lDump.setText(iChaString);
        lDump.setCaretPosition(0);
        lDump.validate();
    }

    public D2Character getChar() {
        return iCharacter;
    }

    public void putOnCharacter(int areaCode, ArrayList<D2Item> dropList) {

        iCharacter.ignoreItemListEvents();
        int dPanel = 0;
        int rMax = 0;
        int cMax = 0;
        switch (areaCode) {
            case 0 -> { dPanel = 5; rMax = STASHSIZEY; cMax = STASHSIZEX; } // stash
            case 1 -> { dPanel = 1; rMax = D2Character.INVSIZEY; cMax = D2Character.INVSIZEX; } // inv
            case 2 -> { dPanel = 4; rMax = D2Character.CUBESIZEY; cMax = D2Character.CUBESIZEX; } // cube
        }
        try {
            for (int z = dropList.size() - 1; z > -1; z--) {
                D2Item lDropItem = (D2Item) dropList.get(z);
                for (int x = 0; x < rMax; x++) {
                    for (int y = 0; y < cMax; y++) {
                        if (iCharacter.checkCharGrid(dPanel, y, x, lDropItem)) {
                            lDropItem.set_panel((short) dPanel);
                            lDropItem.set_location((short) 0);
                            lDropItem.set_body_position((short) 0);
                            lDropItem.set_row((short) x);
                            lDropItem.set_col((short) y);
                            iCharacter.markCharGrid(lDropItem);
                            D2ViewClipboard.removeItem(lDropItem);
                            iCharacter.addCharItem(lDropItem);
                            iCharacter.equipItem(lDropItem);
                            paintCharStats();
                            x = rMax;
                            y = cMax;
                        }
                    }
                }
            }
        } finally {
            iCharacter.listenItemListEvents();
            iCharacter.fireD2ItemListEvent();
        }
    }

    class MyMouse extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {

            if (lTabs.getSelectedIndex() == 6) {
                dumpChar();
            }

        }

    }

    class D2ItemPanel {
        private boolean iIsChar;
        private boolean iIsCursor;
        private int iPanel;
        private int iRow;
        private int iCol;
        private boolean iIsCorpse;
        private boolean iIsMerc;

        public D2ItemPanel(MouseEvent pEvent, boolean pIsChar, boolean pIsCursor,
                boolean pIsCorpse) {
            this(pEvent, pIsChar, pIsCursor, pIsCorpse, false);
        }

        public D2ItemPanel(MouseEvent pEvent, boolean pIsChar, boolean pIsCursor,
                boolean pIsCorpse, boolean pIsMerc) {
            iIsChar = pIsChar;
            iIsCursor = pIsCursor;
            iIsCorpse = pIsCorpse;
            iIsMerc = pIsMerc;
            int x = pEvent.getX();
            int y = pEvent.getY();

            iPanel = getMousePanel(x, y);
            setRowCol(x, y);
        }

        public int getPanel() {
            return iPanel;
        }

        public int getRow() {
            return iRow;
        }

        public int getColumn() {
            return iCol;
        }

        public boolean isItem() {
            if (iIsCursor) {
                return (iCharacter.getCursorItem() != null);
            }
            if (iIsChar) {
                return iCharacter.checkCharPanel(iPanel, iRow, iCol, null);
            }
            if (iIsCorpse) {
                return iCharacter.checkCorpsePanel(iPanel, iRow, iCol, null);
            }
            return iCharacter.checkMercPanel(iPanel, iRow, iCol, null);
        }

        public int getItemIndex() {
            if (iIsChar) {
                return iCharacter.getCharItemIndex(iPanel, iRow, iCol);
            }
            if (iIsCorpse) {
                return iCharacter.getCorpseItemIndex(iPanel, iRow, iCol);
            }
            return iCharacter.getMercItemIndex(iPanel, iRow, iCol);
        }

        public D2Item getItem() {
            if (iIsCursor) {
                return iCharacter.getCursorItem();
            }
            if (iIsChar) {
                return iCharacter.getCharItem(iCharacter.getCharItemIndex(iPanel, iRow, iCol));
            }
            if (iIsCorpse) {
                return iCharacter.getCorpseItem(iCharacter.getCorpseItemIndex(iPanel, iRow, iCol));
            }
            return iCharacter.getMercItem(iCharacter.getMercItemIndex(iPanel, iRow, iCol));
        }

        // calculate which panel (stash, inventory, equipment slot, etc)
        // the coordinates x and y lie in
        // belt_grid = panel 2
        // offset body positions by 10
        // return -1 on failure
        private int getMousePanel(int x, int y) {
            if (iIsMerc) return getMercMousePanel(x, y);
            if (iIsCursor) {
                if (iIsChar && x >= CURSOR_X && x < CURSOR_X + 2 * GRID_SIZE + 2 * GRID_SPACER
                        && y >= CURSOR_Y && y < CURSOR_Y + 4 * GRID_SIZE + 4 * GRID_SPACER) {
                    return D2Character.BODY_CURSOR;
                }
                return -1;
            }
            if (iIsChar && x >= STASH_X
                    && x < STASH_X + STASHSIZEX * GRID_SIZE + STASHSIZEX * GRID_SPACER
                    && y >= STASH_Y
                    && y < STASH_Y + STASHSIZEY * GRID_SIZE + STASHSIZEY * GRID_SPACER) {
                return D2Character.BODY_STASH_CONTENT;
            }
            if (iIsChar && x >= BELT_GRID_X && x < BELT_GRID_X + 4 * GRID_SIZE + 4 * GRID_SPACER
                    && y >= BELT_GRID_Y && y < BELT_GRID_Y + 4 * GRID_SIZE + 4 * GRID_SPACER) {
                return D2Character.BODY_BELT_CONTENT;
            }
            if (iIsChar && x >= INV_X
                    && x < INV_X + D2Character.INVSIZEX * (GRID_SIZE + GRID_SPACER) && y >= INV_Y
                    && y < INV_Y + D2Character.INVSIZEY * (GRID_SIZE + GRID_SPACER)) {
                return D2Character.BODY_INV_CONTENT;
            }
            if (iIsChar && x >= CUBE_X
                    && x < CUBE_X + D2Character.CUBESIZEX * (GRID_SIZE + GRID_SPACER) && y >= CUBE_Y
                    && y < CUBE_Y + D2Character.CUBESIZEY * (GRID_SIZE + GRID_SPACER)) {
                return D2Character.BODY_CUBE_CONTENT;
            }
            // merc & char
            if (x >= HEAD_X && x < HEAD_X + 2 * GRID_SIZE + 2 * GRID_SPACER && y >= HEAD_Y
                    && y < HEAD_Y + 2 * GRID_SIZE + 2 * GRID_SPACER) {
                return D2Character.BODY_HEAD;
            }
            // merc & char & corpse
            if (x >= NECK_X && x < NECK_X + 1 * GRID_SIZE + 1 * GRID_SPACER && y >= NECK_Y
                    && y < NECK_Y + 1 * GRID_SIZE + 1 * GRID_SPACER) {
                return D2Character.BODY_NECK;
            }
            // merc & char
            if (x >= L_ARM_X && x < L_ARM_X + 2 * GRID_SIZE + 2 * GRID_SPACER && y >= L_ARM_Y
                    && y < L_ARM_Y + 4 * GRID_SIZE + 4 * GRID_SPACER) {
                if ((!iIsChar && !iIsCorpse) || iWeaponSlot == 1) {
                    // merc
                    return D2Character.BODY_LARM;
                } else {
                    return D2Character.BODY_LARM2;
                }
            }

            // merc & char
            if (x >= R_ARM_X && x < R_ARM_X + 2 * GRID_SIZE + 2 * GRID_SPACER && y >= R_ARM_Y
                    && y < R_ARM_Y + 4 * GRID_SIZE + 4 * GRID_SPACER) {
                if ((!iIsChar && !iIsCorpse) || iWeaponSlot == 1) {
                    // merc
                    return D2Character.BODY_RARM;
                } else {
                    return D2Character.BODY_RARM2;
                }
            }
            // merc & char
            if (x >= BODY_X && x < BODY_X + 2 * GRID_SIZE + 2 * GRID_SPACER && y >= BODY_Y
                    && y < BODY_Y + 3 * GRID_SIZE + 3 * GRID_SPACER) {
                return D2Character.BODY_TORSO;
            }
            // merc & char & corpse
            if (x >= GLOVES_X && x < GLOVES_X + 2 * GRID_SIZE + 2 * GRID_SPACER && y >= GLOVES_Y
                    && y < GLOVES_Y + 2 * GRID_SIZE + 2 * GRID_SPACER) {
                return D2Character.BODY_GLOVES;
            }
            if (x >= BELT_X && x < BELT_X + 2 * GRID_SIZE + 2 * GRID_SPACER && y >= BELT_Y
                    && y < BELT_Y + 1 * GRID_SIZE + 1 * GRID_SPACER) {
                return D2Character.BODY_BELT;
            }
            if (x >= BOOTS_X && x < BOOTS_X + 2 * GRID_SIZE + 2 * GRID_SPACER && y >= BOOTS_Y
                    && y < BOOTS_Y + 2 * GRID_SIZE + 2 * GRID_SPACER) {
                return D2Character.BODY_BOOTS;
            }
            if (x >= L_RING_X && x < L_RING_X + 1 * GRID_SIZE + 1 * GRID_SPACER && y >= L_RING_Y
                    && y < L_RING_Y + 1 * GRID_SIZE + 1 * GRID_SPACER) {
                return D2Character.BODY_LRING;
            }
            if (x >= R_RING_X && x < R_RING_X + 1 * GRID_SIZE + 1 * GRID_SPACER && y >= R_RING_Y
                    && y < R_RING_Y + 1 * GRID_SIZE + 1 * GRID_SPACER) {
                return D2Character.BODY_RRING;
            }
            return -1;
        }

        /** Hit detection for the Merc.png panel (514×385 native coords). */
        private int getMercMousePanel(int x, int y) {
            if (x >= MSLOT_HEAD_X1   && x < MSLOT_HEAD_X2   && y >= MSLOT_HEAD_Y1   && y < MSLOT_HEAD_Y2)   return D2Character.BODY_HEAD;
            if (x >= MSLOT_NECK_X1   && x < MSLOT_NECK_X2   && y >= MSLOT_NECK_Y1   && y < MSLOT_NECK_Y2)   return D2Character.BODY_NECK;
            if (x >= MSLOT_RARM_X1   && x < MSLOT_RARM_X2   && y >= MSLOT_RARM_Y1   && y < MSLOT_RARM_Y2)   return D2Character.BODY_RARM;
            if (x >= MSLOT_LARM_X1   && x < MSLOT_LARM_X2   && y >= MSLOT_LARM_Y1   && y < MSLOT_LARM_Y2)   return D2Character.BODY_LARM;
            if (x >= MSLOT_TORSO_X1  && x < MSLOT_TORSO_X2  && y >= MSLOT_TORSO_Y1  && y < MSLOT_TORSO_Y2)  return D2Character.BODY_TORSO;
            if (x >= MSLOT_GLOVES_X1 && x < MSLOT_GLOVES_X2 && y >= MSLOT_GLOVES_Y1 && y < MSLOT_GLOVES_Y2) return D2Character.BODY_GLOVES;
            if (x >= MSLOT_BOOTS_X1  && x < MSLOT_BOOTS_X2  && y >= MSLOT_BOOTS_Y1  && y < MSLOT_BOOTS_Y2)  return D2Character.BODY_BOOTS;
            if (x >= MSLOT_LRING_X1  && x < MSLOT_LRING_X2  && y >= MSLOT_LRING_Y1  && y < MSLOT_LRING_Y2)  return D2Character.BODY_LRING;
            if (x >= MSLOT_BELT_X1   && x < MSLOT_BELT_X2   && y >= MSLOT_BELT_Y1   && y < MSLOT_BELT_Y2)   return D2Character.BODY_BELT;
            if (x >= MSLOT_RRING_X1  && x < MSLOT_RRING_X2  && y >= MSLOT_RRING_Y1  && y < MSLOT_RRING_Y2)  return D2Character.BODY_RRING;
            return -1;
        }

        // get row/col
        private void setRowCol(int x, int y) {
            // int row, col;
            // int temp_item = -1;
            // non-equppied: calculate row and column
            // then fetch the item if that item space
            // has an item on it
            if (iPanel < 10) {
                switch (iPanel) {
                    case 1 -> { iRow = (x - INV_X) / (GRID_SIZE + GRID_SPACER); iCol = (y - INV_Y) / (GRID_SIZE + GRID_SPACER); } // inventory
                    case 2 -> { iRow = (x - BELT_GRID_X) / (GRID_SIZE + GRID_SPACER); iCol = 3 - ((y - BELT_GRID_Y) / (GRID_SIZE + GRID_SPACER)); } // belted
                    case 4 -> { iRow = (x - CUBE_X) / (GRID_SIZE + GRID_SPACER); iCol = (y - CUBE_Y) / (GRID_SIZE + GRID_SPACER); } // cube
                    case 5 -> { iRow = (x - STASH_X) / (GRID_SIZE + GRID_SPACER); iCol = (y - STASH_Y) / (GRID_SIZE + GRID_SPACER); } // stash
                }
            }
            // equipped
            // row and column are irrelevant, so they can be zero
            else {
                iRow = 0;
                iCol = 0;

            }
        }

    }

    /** Sprite image deferral record — native (unscaled) coords, rendered BICUBIC at screen coords in paint(). */
    private static final class SpriteItemDraw {
        final Image img;
        final int x, y, w, h;
        SpriteItemDraw(Image img, int x, int y, int w, int h) {
            this.img = img; this.x = x; this.y = y; this.w = w; this.h = h;
        }
    }

    /**
     * Tries D2R sprite first (deferred BICUBIC via {@code sprites}); falls back to DC6 drawn onto {@code g}.
     * @param wCells item width in grid cells
     * @param hCells item height in grid cells
     */
    private static void placeItemImage(Graphics2D g, java.util.List<SpriteItemDraw> sprites,
            D2Item item, int x, int y, int wCells, int hCells) {
        Image sprite = D2ImageCache.getSpriteImage(item.getItemCode(), item.get_gfx_num());
        if (sprite != null) {
            sprites.add(new SpriteItemDraw(sprite, x, y, wCells * GRID_SIZE, hCells * GRID_SIZE));
        } else {
            Image dc6 = D2ImageCache.getDC6Image(item);
            if (dc6 != null) g.drawImage(dc6, x, y, null);
        }
    }

    /** Background fill color for occupied inventory/stash/cube/belt grid cells (淡蓝色). */
    private static final java.awt.Color ITEM_SLOT_BG = new java.awt.Color(40, 100, 220, 80);

    /** Fills the cell-area on {@code g} with the occupied-slot background color. */
    private static void fillItemSlotBg(Graphics2D g, int x, int y, int wCells, int hCells) {
        int pw = wCells * (GRID_SIZE + GRID_SPACER) - GRID_SPACER;
        int ph = hCells * (GRID_SIZE + GRID_SPACER) - GRID_SPACER;
        g.setColor(ITEM_SLOT_BG);
        g.fillRect(x, y, pw, ph);
    }

    /** Draws deferred sprite items at screen resolution using BICUBIC interpolation. */
    private static void paintSpriteItems(Graphics pGraphics, java.util.List<SpriteItemDraw> sprites) {
        if (sprites.isEmpty()) return;
        Graphics2D sg = (Graphics2D) pGraphics;
        double s = gomule.util.D2UI.getUiScale();
        sg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        sg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        for (SpriteItemDraw sd : sprites) {
            int iw = sd.img.getWidth(null);
            int ih = sd.img.getHeight(null);
            if (iw <= 0 || ih <= 0) continue;
            int sw = (int) Math.round(sd.w * s);
            int sh = (int) Math.round(sd.h * s);
            double imgScale = Math.min((double)(sw - 2) / iw, (double)(sh - 2) / ih);
            int dw = (int)(iw * imgScale);
            int dh = (int)(ih * imgScale);
            int dx = (int) Math.round(sd.x * s) + (sw - dw) / 2;
            int dy = (int) Math.round(sd.y * s) + (sh - dh) / 2;
            sg.drawImage(sd.img, dx, dy, dw, dh, null);
        }
    }

    class D2CharPainterPanel extends ScaledPainterPanel {
        /**
         *
         */
        @Serial
        private static final long serialVersionUID = 2159433491696507246L;
        private Image iBackground;

        /** Text labels drawn directly at screen coords in paint() to avoid double-scaling blur. */
        private final java.util.List<TextDraw> textDraws = new java.util.ArrayList<>();
        /** Sprite images drawn at screen coords (BICUBIC) in paint() to avoid double-scale blur. */
        private final java.util.List<SpriteItemDraw> spriteItems = new java.util.ArrayList<>();

        private static final class TextDraw {
            final String text;
            final int x, y, w, h; // native bounding box
            final int fontSize; // native font size
            final Color color;
            final int halign; // 0=left, 1=center, 2=right
            final int valign; // 0=top, 1=bottom
            final int padX, padY;

            TextDraw(String text, int x, int y, int w, int h, int fontSize, Color color, int halign,
                    int valign, int padX, int padY) {
                this.text = text;
                this.x = x;
                this.y = y;
                this.w = w;
                this.h = h;
                this.fontSize = fontSize;
                this.color = color;
                this.halign = halign;
                this.valign = valign;
                this.padX = padX;
                this.padY = padY;
            }
        }

        public D2CharPainterPanel() {
            setSize(BG_WIDTH, BG_HEIGHT);
            Dimension lSize = new Dimension(BG_WIDTH, BG_HEIGHT);
            setPreferredSize(lSize);
            this.nativeW = BG_WIDTH;
            this.nativeH = BG_HEIGHT;

            addMouseListener(new MouseAdapter() {


                @Override
                public void mouseReleased(MouseEvent pEvent) {
                    if (iCharacter == null) {
                        return;
                    }
                    // System.err.println("Mouse Clicked: " + pEvent.getX() + ",
                    // " + pEvent.getY() );
                    if (pEvent.getButton() == MouseEvent.BUTTON1 /*
                                                                  * && pEvent.getClickCount() == 1
                                                                  */) {
                        int lX = pEvent.getX();
                        int lY = pEvent.getY();
                        // 武器切换按钮：坐标与 drawWeaponSlotIndicator 保持同步
                        {
                            int panelCenterX = (STASH_X - 4) / 2;
                            int indY = 238;
                            int btnW = 28, btnH = 18, gap = 5;
                            int startX = panelCenterX - (btnW * 2 + gap) / 2;
                            int btn1X = startX;
                            int btn2X = startX + btnW + gap;
                            if (lX >= btn1X && lX <= btn1X + btnW && lY >= indY
                                    && lY <= indY + btnH) {
                                setWeaponSlot(1);
                            } else if (lX >= btn2X && lX <= btn2X + btnW && lY >= indY
                                    && lY <= indY + btnH) {
                                setWeaponSlot(2);
                            }
                        }
                        // determine where the mouse click is
                        D2ItemPanel lItemPanel = new D2ItemPanel(pEvent, true, false, false);
                        if (lItemPanel.getPanel() != -1) {
                            // if there is an item to grab, grab it
                            if (lItemPanel.isItem()) {
                                D2Item lTemp = lItemPanel.getItem();


                                /**
                                 * Code to remove potions when belt is removed! Thanks to Krikke.
                                 */
                                // System.out.println("isEquipped: " + lTemp.isEquipped() + "
                                // isABelt: " + lTemp.isABelt());
                                if (lTemp.isEquipped() && lTemp.isABelt()) {
                                    for (int y = 0; y < iCharacter.getBeltPotions().size(); y++) {
                                        D2ViewClipboard.addItem(
                                                (D2Item) iCharacter.getBeltPotions().get(y));
                                        iCharacter.unmarkCharGrid(
                                                (D2Item) iCharacter.getBeltPotions().get(y));
                                    }
                                    for (int i = 0; i < 4; i++) {
                                        for (int j = 1; j < 4; j++) {
                                            if (iCharacter.getCharItemIndex(2, i, j) != -1) {
                                                iCharacter.removeCharItem(
                                                        iCharacter.getCharItemIndex(2, i, j));
                                            }
                                        }
                                    }
                                }


                                iCharacter.unmarkCharGrid(lTemp);
                                iCharacter.removeCharItem(lItemPanel.getItemIndex());
                                D2ViewClipboard.addItem(lTemp);
                                setCursorDropItem();
                                if (lTemp.statModding()) {
                                    iCharacter.updateCharStats("P", lTemp);
                                    paintCharStats();
                                }

                                // // redraw
                                // build();
                                // repaint();
                            } else if (D2ViewClipboard.getItem() != null) {
                                // System.err.println("Drop item");
                                // since there is an item on the mouse, try to
                                // drop it here

                                D2Item lDropItem = D2ViewClipboard.getItem();
                                // int lDropWidth = lDropItem.get_width();
                                // int lDropHeight = lDropItem.get_height();
                                // int r = 0, c = 0;
                                boolean drop = false;
                                // non-equipped items, handle differently
                                // because they require a row and column
                                if (lItemPanel.getPanel() < 10) {

                                    // if that area of the character is empty,
                                    // then update fields of the item and set
                                    // the 'drop' variable to true
                                    if (iCharacter.checkCharGrid(lItemPanel.getPanel(),
                                            lItemPanel.getRow(), lItemPanel.getColumn(),
                                            lDropItem)) {
                                        switch (lItemPanel.getPanel()) {
                                            case 2:
                                                lDropItem.set_location((short) 2);
                                                lDropItem.set_body_position((short) 0);
                                                lDropItem
                                                        .set_col((short) (4 * lItemPanel.getColumn()
                                                                + lItemPanel.getRow()));
                                                lDropItem.set_row((short) 0);
                                                lDropItem.set_panel((short) 0);
                                                break;
                                            case 1:
                                            case 4:
                                            case 5:
                                                lDropItem.set_location((short) 0);
                                                lDropItem.set_body_position((short) 0);
                                                lDropItem.set_row((short) lItemPanel.getColumn());
                                                lDropItem.set_col((short) lItemPanel.getRow());
                                                lDropItem.set_panel((short) lItemPanel.getPanel());
                                                break;
                                        }
                                        drop = true;
                                    }
                                }
                                // equipped items, a bit simpler
                                // if that equipment slot is empty, update the
                                // item's fields and set drop to true
                                // r and c are set to width and height
                                // for find_corner to deal with variable-size
                                // objects in the hands
                                // (note lack of item-type checking)
                                else {
                                    if (!iCharacter.checkCharPanel(lItemPanel.getPanel(), 0, 0,
                                            lDropItem)) {
                                        lDropItem.set_location((short) 1);
                                        lDropItem.set_body_position(
                                                (short) (lItemPanel.getPanel() - 10));
                                        lDropItem.set_col((short) 0);
                                        lDropItem.set_row((short) 0);
                                        lDropItem.set_panel((short) 0);
                                        drop = true;
                                        // r = lDropWidth;
                                        // c = lDropHeight;
                                    }
                                }
                                // if the space to set the item is empty
                                if (drop) {
                                    iCharacter.markCharGrid(lDropItem);
                                    // move the item to a new charcter, if
                                    // needed
                                    iCharacter.addCharItem(D2ViewClipboard.removeItem());

                                    // redraw
                                    // build();
                                    // repaint();

                                    setCursorPickupItem();
                                    if (lDropItem.statModding()) {
                                        iCharacter.updateCharStats("D", lDropItem);
                                        paintCharStats();
                                    }
                                    // my_char.show_grid();
                                }
                            }
                        }
                    } else if (pEvent.getButton() == MouseEvent.BUTTON3) {
                        D2ItemPanel lItemPanel = new D2ItemPanel(pEvent, true, false, false);
                        if (lItemPanel.getPanel() != -1) {
                            if (lItemPanel.isItem()) {
                                new ItemRightClickMenu(lItemPanel.getItem(),
                                        this::deleteMenuItemAction).show(D2ViewChar.this,
                                                pEvent.getX(), pEvent.getY() + 35);
                            }
                        }
                    }
                }

                private void deleteMenuItemAction(D2Item d2Item) {
                    iCharacter.removeItem(d2Item);
                    if (d2Item.statModding()) {
                        iCharacter.updateCharStats("P", d2Item);
                        paintCharStats();
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    setCursorNormal();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setCursorNormal();
                }
            });
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent pEvent) {
                    if (iCharacter == null) {
                        return;
                    }
                    // restoreSubcomponentFocus();
                    D2Item lCurrentMouse = null;

                    D2ItemPanel lItemPanel = new D2ItemPanel(pEvent, true, false, false);
                    if (lItemPanel.getPanel() != -1) {
                        if (lItemPanel.isItem()) {
                            lCurrentMouse = lItemPanel.getItem();
                        }

                        if (lItemPanel.isItem()) {
                            setCursorPickupItem();
                        } else {
                            if (D2ViewClipboard.getItem() == null) {
                                setCursorNormal();
                            } else {
                                D2Item lDropItem = D2ViewClipboard.getItem();
                                // int lDropWidth = lDropItem.get_width();
                                // int lDropHeight = lDropItem.get_height();

                                boolean drop = false;

                                if (lItemPanel.getPanel() < 10) {
                                    if (iCharacter.checkCharGrid(lItemPanel.getPanel(),
                                            lItemPanel.getRow(), lItemPanel.getColumn(),
                                            lDropItem)) {
                                        drop = true;
                                    }
                                } else {
                                    if (!iCharacter.checkCharPanel(lItemPanel.getPanel(), 0, 0,
                                            lDropItem)) {
                                        drop = true;
                                    }
                                }
                                if (drop) {
                                    setCursorDropItem();
                                } else {
                                    setCursorNormal();
                                }
                            }
                        }
                    } else {
                        setCursorNormal();
                    }
                    if (lCurrentMouse == null) {
                        D2CharPainterPanel.this.setToolTipText(null);
                    } else {
                        D2CharPainterPanel.this
                                .setToolTipText(D2ItemRenderer.itemDumpHtml(lCurrentMouse, false));
                    }
                }
            });
        }

        public void setWeaponSlot(int pWeaponSlot) {
            if (iWeaponSlot == pWeaponSlot) {
                return;
            }

            iWeaponSlot = pWeaponSlot;
            build();
            // REMOVE ITEMS AND ADD ITEMS


            iCharacter.changeWep();
            paintCharStats();

            // repaint();
        }

        public void build() {
            textDraws.clear();
            spriteItems.clear();
            int lWidth = BG_WIDTH;
            int lHeight = BG_HEIGHT;

            // 程序化模式（默认）：跳过 PNG 加载，直接矢量绘制
            Image lEmptyBackground = null;
            if (!LayoutProfile.proceduralBackground) {
                String bgName = (iWeaponSlot == 1) ? sActiveProfile.getBg1ImageName()
                        : sActiveProfile.getBg2ImageName();
                lEmptyBackground = D2ImageCache.getImage(bgName);
                if (lEmptyBackground != null) {
                    lWidth = lEmptyBackground.getWidth(D2CharPainterPanel.this);
                    lHeight = lEmptyBackground.getHeight(D2CharPainterPanel.this);
                }
            }

            iBackground = iFileManager.getGraphicsConfiguration().createCompatibleImage(lWidth,
                    lHeight, Transparency.TRANSLUCENT);

            Graphics2D lGraphics = (Graphics2D) iBackground.getGraphics();

            if (lEmptyBackground != null) {
                lGraphics.drawImage(lEmptyBackground, 0, 0, D2CharPainterPanel.this);
            } else {
                drawProceduralBackground(lGraphics, lWidth, lHeight, iWeaponSlot);
            }

            if (iCharacter != null) {
                for (int i = 0; i < iCharacter.getCharItemNr(); i++) {
                    D2Item temp_item = iCharacter.getCharItem(i);
                    int location = temp_item.get_location();
                    if (location == 0) {
                        int panel = temp_item.get_panel();
                        int x = temp_item.get_col();
                        int y = temp_item.get_row();
                        int w = temp_item.get_width(), h = temp_item.get_height();
                        switch (panel) {
                            case 1: { // inventory
                                int px = INV_X + x * GRID_SIZE + x * GRID_SPACER;
                                int py = INV_Y + y * GRID_SIZE + y * GRID_SPACER;
                                fillItemSlotBg(lGraphics, px, py, w, h);
                                placeItemImage(lGraphics, spriteItems, temp_item, px, py, w, h);
                                break; }
                            case 4: { // cube
                                int px = CUBE_X + x * GRID_SIZE + x * GRID_SPACER;
                                int py = CUBE_Y + y * GRID_SIZE + y * GRID_SPACER;
                                fillItemSlotBg(lGraphics, px, py, w, h);
                                placeItemImage(lGraphics, spriteItems, temp_item, px, py, w, h);
                                break; }
                            case 5: { // stash
                                int px = STASH_X + x * GRID_SIZE + x * GRID_SPACER;
                                int py = STASH_Y + y * GRID_SIZE + y * GRID_SPACER;
                                fillItemSlotBg(lGraphics, px, py, w, h);
                                placeItemImage(lGraphics, spriteItems, temp_item, px, py, w, h);
                                break; }
                        }
                    } else if (location == 2) { // belt (all 1x1)
                        int x = temp_item.get_col();
                        int y = x / 4;
                        x = x % 4;
                        int bpx = BELT_GRID_X + x * GRID_SIZE + x * GRID_SPACER;
                        int bpy = BELT_GRID_Y + (3 - y) * GRID_SIZE + (3 - y) * GRID_SPACER;
                        fillItemSlotBg(lGraphics, bpx, bpy, 1, 1);
                        placeItemImage(lGraphics, spriteItems, temp_item, bpx, bpy, 1, 1);
                    } else { // on the body
                        int body_position = temp_item.get_body_position();
                        int w, h, wbias, hbias;
                        switch (body_position) {
                            case 1: // head (2x2)
                                placeItemImage(lGraphics, spriteItems, temp_item, HEAD_X, HEAD_Y, 2, 2);
                                break;
                            case 2: // neck/amulet (1x1)
                                placeItemImage(lGraphics, spriteItems, temp_item, NECK_X, NECK_Y, 1, 1);
                                break;
                            case 3: // body (2x3)
                                placeItemImage(lGraphics, spriteItems, temp_item, BODY_X, BODY_Y, 2, 3);
                                break;
                            case 4:
                            case 11: // right arm
                                if ((iWeaponSlot == 1 && body_position == 4)
                                        || (iWeaponSlot == 2 && body_position == 11)) {
                                    w = temp_item.get_width();
                                    h = temp_item.get_height();
                                    wbias = 0;
                                    hbias = 0;
                                    if (w == 1) wbias += GRID_SIZE / 2;
                                    if (h == 3) hbias += GRID_SIZE / 2;
                                    else if (h == 2) hbias += GRID_SIZE;
                                    placeItemImage(lGraphics, spriteItems, temp_item,
                                            R_ARM_X + wbias, R_ARM_Y + hbias, w, h);
                                }
                                break;
                            case 5:
                            case 12: // left arm
                                if ((iWeaponSlot == 1 && body_position == 5)
                                        || (iWeaponSlot == 2 && body_position == 12)) {
                                    w = temp_item.get_width();
                                    h = temp_item.get_height();
                                    wbias = 0;
                                    hbias = 0;
                                    if (w == 1) wbias += GRID_SIZE / 2;
                                    if (h == 3) hbias += GRID_SIZE / 2;
                                    else if (h == 2) hbias += GRID_SIZE;
                                    placeItemImage(lGraphics, spriteItems, temp_item,
                                            L_ARM_X + wbias, L_ARM_Y + hbias, w, h);
                                }
                                break;
                            case 6: // left ring (1x1)
                                placeItemImage(lGraphics, spriteItems, temp_item, L_RING_X, L_RING_Y, 1, 1);
                                break;
                            case 7: // right ring (1x1)
                                placeItemImage(lGraphics, spriteItems, temp_item, R_RING_X, R_RING_Y, 1, 1);
                                break;
                            case 8: // belt equip (2x1)
                                placeItemImage(lGraphics, spriteItems, temp_item, BELT_X, BELT_Y, 2, 1);
                                break;
                            case 9: // boots (2x2)
                                placeItemImage(lGraphics, spriteItems, temp_item, BOOTS_X, BOOTS_Y, 2, 2);
                                break;
                            case 10: // gloves (2x2)
                                placeItemImage(lGraphics, spriteItems, temp_item, GLOVES_X, GLOVES_Y, 2, 2);
                                break;
                        }
                    }
                }
            }
            repaint();
        }

        @Override
        public void paint(Graphics pGraphics) {
            super.paint(pGraphics);
            drawScaled(pGraphics, iBackground);
            paintSpriteItems(pGraphics, spriteItems);
            // Draw text labels directly at screen resolution to avoid double-scale blur
            if (!textDraws.isEmpty()) {
                Graphics2D tg = (Graphics2D) pGraphics;
                double s = gomule.util.D2UI.getUiScale();
                tg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                tg.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                for (TextDraw td : textDraws) {
                    int scaledFontSize = Math.max(8, (int) Math.round(td.fontSize * s));
                    tg.setFont(new Font("SansSerif", Font.BOLD, scaledFontSize));
                    FontMetrics fm = tg.getFontMetrics();
                    int sx = (int) Math.round(td.x * s);
                    int sy = (int) Math.round(td.y * s);
                    int sw = (int) Math.round(td.w * s);
                    int sh = (int) Math.round(td.h * s);
                    int tx = switch (td.halign) {
                        case 0 -> sx + (int) Math.round(td.padX * s); // left
                        case 2 -> sx + sw - fm.stringWidth(td.text) - (int) Math.round(td.padX * s); // right
                        default -> sx + (sw - fm.stringWidth(td.text)) / 2; // center
                    };
                    int ty;
                    if (td.valign == 0) { // top
                        ty = sy + (int) Math.round(td.padY * s) + fm.getAscent();
                    } else { // bottom
                        ty = sy + sh - (int) Math.round(td.padY * s);
                    }
                    tg.setColor(td.color);
                    tg.drawString(td.text, tx, ty);
                }
            }
        }

        /**
         * 当对应 profile 的背景图文件不存在时，程序自动绘制占位背景。 绘制深色底色 + 各网格区域的浅色边框，让用户能够正常使用功能。
         */
        private void drawProceduralBackground(Graphics2D g, int w, int h, int weaponSlot) {
            // ── 全局渲染质量 ──────────────────────────────────────────────────
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            // ── 全局底色 ──────────────────────────────────────────────────────
            PanelTheme theme = PanelTheme.active;
            g.setColor(theme.bg);
            g.fillRect(0, 0, w, h);

            // ── 装备槽区域背景（左柱，x=0 至仓库左边界） ──────────────────────
            int equipPanelW = STASH_X - 4;
            g.setColor(theme.equipZoneBg);
            g.fillRect(0, 0, equipPanelW, h);

            // 左柱 / 右柱 分割线
            g.setColor(theme.divider);
            g.setStroke(new BasicStroke(1.0f));
            g.drawLine(equipPanelW + 1, 2, equipPanelW + 1, h - 2);

            // ── 装备槽 ────────────────────────────────────────────────────────
            // 武器手（左）— slot1=主武器 / slot2=副武器
            drawEquipSlot(g, R_ARM_X, R_ARM_Y, 2, 4, weaponSlot == 1 ? "Main Hand" : "Hand 2-A",
                    "WEAPON", theme);
            // 副手（右）— slot1=盾牌/副武器 / slot2=主武器
            drawEquipSlot(g, L_ARM_X, L_ARM_Y, 2, 4, weaponSlot == 1 ? "Off Hand" : "Hand 2-B",
                    "SHIELD", theme);
            drawEquipSlot(g, HEAD_X, HEAD_Y, 2, 2, "Head", "HELM", theme);
            drawEquipSlot(g, NECK_X, NECK_Y, 1, 1, "", "AMULET", theme);
            drawEquipSlot(g, BODY_X, BODY_Y, 2, 3, "Torso", "TORSO", theme);
            drawEquipSlot(g, GLOVES_X, GLOVES_Y, 2, 2, "Gloves", "GLOVES", theme);
            drawEquipSlot(g, BELT_X, BELT_Y, 2, 1, "", "BELT_EQUIP", theme);
            drawEquipSlot(g, BOOTS_X, BOOTS_Y, 2, 2, "Boots", "BOOTS", theme);
            drawEquipSlot(g, L_RING_X, L_RING_Y, 1, 1, "", "RING", theme);
            drawEquipSlot(g, R_RING_X, R_RING_Y, 1, 1, "", "RING", theme);

            // ── 武器槽切换指示器（I / II） ────────────────────────────────────
            drawWeaponSlotIndicator(g, weaponSlot, theme);

            // ── 网格区域（仓库 / 背包 / 魔盒 / 腰带格）────────────────────────
            drawGridZone(g, STASH_X, STASH_Y, D2Character.STASHSIZEX, D2Character.STASHSIZEY,
                    "Stash", theme);
            drawGridZone(g, INV_X, INV_Y, D2Character.INVSIZEX, D2Character.INVSIZEY, "Inventory", theme);
            drawGridZone(g, CUBE_X, CUBE_Y, D2Character.CUBESIZEX, D2Character.CUBESIZEY, "Cube", theme);
            drawGridZone(g, BELT_GRID_X, BELT_GRID_Y, D2Character.BELTSIZEX, D2Character.BELTSIZEY,
                    "Belt", theme);
        }

        /** 绘制单个装备槽：圆角矩形边框 + 底色 + 轮廓图标 + 标签文字。 */
        private void drawEquipSlot(Graphics2D g, int x, int y, int cols, int rows, String label,
                String iconType, PanelTheme theme) {
            final int CELL = GRID_SIZE + GRID_SPACER;
            int sw = cols * CELL - GRID_SPACER;
            int sh = rows * CELL - GRID_SPACER;

            // 槽位底色
            g.setColor(theme.slotBg);
            g.fillRoundRect(x, y, sw, sh, 5, 5);

            // 槽位边框
            g.setColor(theme.slotBorder);
            g.setStroke(new BasicStroke(1.0f));
            g.drawRoundRect(x, y, sw, sh, 5, 5);

            // 内部装饰图标（极淡，仅作底色纹理）
            drawSlotIcon(g, x + sw / 2, y + sh / 2, sw - 10, sh - 10, iconType, theme);

            // 标签文字（右下角小字，仅大槽位绘制）—— 存入 textDraws，由 paint() 在屏幕坐标绘制
            if (!label.isEmpty()) {
                textDraws.add(new TextDraw(label, x, y, sw, sh, 11, theme.slotLabel, 2, 1,
                        3, 3));
            }
        }

        /**
         * 在槽位中央绘制极淡的轮廓图标（装饰底纹，不干扰物品图标）。 颜色刻意压暗，视觉上若隐若现。
         */
        private void drawSlotIcon(Graphics2D g, int cx, int cy, int aw, int ah, String iconType,
                PanelTheme theme) {
            g.setColor(theme.slotIcon);
            g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int hw = aw / 2;
            int hh = ah / 2;
            switch (iconType) {
                case "HELM": {
                    // 圆弧盔顶 + 面甲横条 + 护颊
                    int r = Math.min(hw, hh) * 3 / 5;
                    g.drawArc(cx - r, cy - r, r * 2, r * 2, 10, 160);
                    g.drawLine(cx - r - 2, cy + r / 3, cx + r + 2, cy + r / 3);
                    g.drawLine(cx - r, cy, cx - r - 2, cy + r / 3);
                    g.drawLine(cx + r, cy, cx + r + 2, cy + r / 3);
                    break;
                }
                case "AMULET": {
                    // 菱形（项链坠）
                    int d = Math.min(hw, hh) * 2 / 3;
                    g.drawLine(cx, cy - d, cx + d, cy);
                    g.drawLine(cx + d, cy, cx, cy + d);
                    g.drawLine(cx, cy + d, cx - d, cy);
                    g.drawLine(cx - d, cy, cx, cy - d);
                    break;
                }
                case "TORSO": {
                    // 板甲：梯形肩 + 侧边 + 腰线 + 中线
                    int sw2 = hw * 3 / 4, bw = hw * 5 / 6;
                    int ty = cy - hh * 2 / 3, by = cy + hh * 2 / 3;
                    g.drawLine(cx - sw2, ty, cx + sw2, ty);
                    g.drawLine(cx - bw, by, cx + bw, by);
                    g.drawLine(cx - sw2, ty, cx - bw, by);
                    g.drawLine(cx + sw2, ty, cx + bw, by);
                    g.drawLine(cx, ty + 4, cx, by - 4);
                    break;
                }
                case "WEAPON": {
                    // 剑：对角剑身 + 十字护手 + 圆柄端
                    int reach = Math.min(hw, hh) * 4 / 5;
                    g.drawLine(cx - reach / 2, cy + reach, cx + reach / 2, cy - reach);
                    g.drawLine(cx - reach / 2, cy + reach / 5, cx + reach / 2, cy - reach / 2);
                    g.drawOval(cx - reach / 2 - 2, cy + reach - 3, 5, 5);
                    break;
                }
                case "SHIELD": {
                    // 盾牌五边形
                    int sw3 = hw * 2 / 3, sh3 = hh * 2 / 3;
                    int[] xs = {cx - sw3, cx + sw3, cx + sw3, cx, cx - sw3};
                    int[] ys = {cy - sh3, cy - sh3, cy, cy + sh3, cy};
                    g.drawPolygon(xs, ys, 5);
                    break;
                }
                case "GLOVES": {
                    // 掌心矩形 + 三根手指
                    int gw = hw * 2 / 3, gh = hh / 2;
                    g.drawRoundRect(cx - gw, cy - gh / 2, gw * 2, gh, 3, 3);
                    int fw = gw / 3, fh = gh;
                    for (int i = -1; i <= 1; i++) {
                        g.drawRect(cx + i * fw - fw / 2, cy - gh / 2 - fh, fw - 1, fh);
                    }
                    break;
                }
                case "BELT_EQUIP": {
                    // 腰带扣：圆角外框 + 中央扣环
                    int bw = hw * 2 / 3, bh = hh / 3;
                    g.drawRoundRect(cx - bw, cy - bh, bw * 2, bh * 2, 4, 4);
                    g.drawOval(cx - bh / 2, cy - bh / 2, bh, bh);
                    break;
                }
                case "BOOTS": {
                    // 靴子：靴筒竖线 + 斜向靴底 + 靴筒顶横线
                    int bh2 = hh * 2 / 3, tx = cx - hw / 4;
                    g.drawLine(tx, cy - bh2, tx, cy + bh2 / 3);
                    g.drawLine(tx, cy + bh2 / 3, cx + hw * 2 / 3, cy + bh2);
                    g.drawLine(tx, cy - bh2, cx + hw / 4, cy - bh2);
                    break;
                }
                case "RING": {
                    // 戒指：单圆环
                    int r = Math.min(hw, hh) * 2 / 3;
                    g.drawOval(cx - r, cy - r, r * 2, r * 2);
                    break;
                }
                default:
                    break;
            }
            g.setStroke(new BasicStroke(1.0f));
        }

        /** 在装备面板下方绘制武器槽切换按钮指示器（[ I ] [ II ]）。 */
        private void drawWeaponSlotIndicator(Graphics2D g, int weaponSlot, PanelTheme theme) {
            // 位于 GLOVES 行下方的空白区域，水平居中于装备面板
            int panelCenterX = (STASH_X - 4) / 2;
            int indY = 238;
            int btnW = 28, btnH = 18, gap = 5;
            int totalW = btnW * 2 + gap;
            int startX = panelCenterX - totalW / 2;

            for (int slot = 1; slot <= 2; slot++) {
                boolean active = (slot == weaponSlot);
                int bx = startX + (slot - 1) * (btnW + gap);

                // 按钮背景
                g.setColor(active ? theme.weaponActiveBg : theme.weaponInactiveBg);
                g.fillRoundRect(bx, indY, btnW, btnH, 4, 4);

                // 按钮边框
                g.setColor(active ? theme.weaponActiveBorder : theme.weaponInactiveBorder);
                g.setStroke(new BasicStroke(active ? 1.2f : 0.8f));
                g.drawRoundRect(bx, indY, btnW, btnH, 4, 4);

                // 按钮文字 —— 存入 textDraws，由 paint() 在屏幕坐标绘制
                String lbl = (slot == 1) ? "I" : "II";
                Color btnTextColor = active ? theme.weaponActiveText : theme.weaponInactiveText;
                textDraws
                        .add(new TextDraw(lbl, bx, indY, btnW, btnH, 10, btnTextColor, 1, 1, 0, 5));
            }
            g.setStroke(new BasicStroke(1.0f));
        }

        /** 在指定位置绘制 cols×rows 的格子区域（带区域标签）。 */
        private void drawGridZone(Graphics2D g, int ox, int oy, int cols, int rows, String label,
                PanelTheme theme) {
            final int CELL = GRID_SIZE + GRID_SPACER;
            int zoneW = cols * CELL;
            int zoneH = rows * CELL;

            // 区域底色（与装备面板区分）
            g.setColor(theme.gridBg);
            g.fillRect(ox, oy, zoneW, zoneH);

            // 外框
            g.setColor(theme.gridBorder);
            g.setStroke(new BasicStroke(1.0f));
            g.drawRect(ox, oy, zoneW, zoneH);

            // 内部格线（淡）
            g.setColor(theme.gridLine);
            for (int c = 1; c < cols; c++)
                g.drawLine(ox + c * CELL, oy + 1, ox + c * CELL, oy + zoneH - 1);
            for (int r = 1; r < rows; r++)
                g.drawLine(ox + 1, oy + r * CELL, ox + zoneW - 1, oy + r * CELL);

            // 区域标签（左上角）—— 存入 textDraws，由 paint() 在屏幕坐标绘制
            textDraws.add(new TextDraw(label, ox, oy, zoneW, zoneH, 13, theme.zoneLabel, 0,
                    0, 4, 2));
        }
    }

    class D2MercPainterPanel extends ScaledPainterPanel {
        /**
         *
         */
        @Serial
        private static final long serialVersionUID = 2412158000132602811L;
        private Image iBackground;
        private final java.util.List<SpriteItemDraw> spriteItems = new java.util.ArrayList<>();

        public D2MercPainterPanel() {
            setSize(BG_MERC_WIDTH, BG_MERC_HEIGHT);
            Dimension lSize = new Dimension(BG_MERC_WIDTH, BG_MERC_HEIGHT);
            setPreferredSize(lSize);
            this.nativeW = BG_MERC_WIDTH;
            this.nativeH = BG_MERC_HEIGHT;

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent pEvent) {
                    if (iCharacter == null) {
                        return;
                    }
                    // System.err.println("Mouse Clicked: " + pEvent.getX() + ",
                    // " + pEvent.getY() );
                    if (pEvent.getButton() == MouseEvent.BUTTON1 /*
                                                                  * && pEvent.getClickCount() == 1
                                                                  */) {
                        // determine where the mouse click is
                        D2ItemPanel lItemPanel = new D2ItemPanel(pEvent, false, false, false, true);
                        if (lItemPanel.getPanel() == 1337) {
                            return;
                        }
                        if (lItemPanel.getPanel() != -1) {
                            // if there is an item to grab, grab it
                            if (lItemPanel.isItem()) {


                                D2Item lTemp = lItemPanel.getItem();

                                iCharacter.unmarkMercGrid(lTemp);
                                iCharacter.removeMercItem(lItemPanel.getItemIndex());
                                D2ViewClipboard.addItem(lTemp);
                                if (lTemp.statModding()) {
                                    iCharacter.updateMercStats("P", lTemp);
                                    paintMercStats();
                                }
                                setCursorDropItem();

                                // redraw
                                // build();
                                // repaint();
                            } else if (D2ViewClipboard.getItem() != null) {
                                // System.err.println("Drop item");
                                // since there is an item on the mouse, try to
                                // drop it here

                                D2Item lDropItem = D2ViewClipboard.getItem();
                                // int lDropWidth = lDropItem.get_width();
                                // int lDropHeight = lDropItem.get_height();
                                // int r = 0, c = 0;
                                boolean drop = false;
                                // equipped items, a bit simpler
                                // if that equipment slot is empty, update the
                                // item's fields and set drop to true
                                // r and c are set to width and height
                                // for find_corner to deal with variable-size
                                // objects in the hands
                                // (note lack of item-type checking)
                                if (!iCharacter.checkMercPanel(lItemPanel.getPanel(), 0, 0,
                                        lDropItem)) {
                                    lDropItem.set_location((short) 1);
                                    lDropItem.set_body_position(
                                            (short) (lItemPanel.getPanel() - 10));
                                    lDropItem.set_col((short) 0);
                                    lDropItem.set_row((short) 0);
                                    lDropItem.set_panel((short) 0);
                                    drop = true;
                                }
                                // if the space to set the item is empty
                                if (drop) {
                                    iCharacter.markMercGrid(lDropItem);
                                    // move the item to a new charcter, if
                                    // needed
                                    iCharacter.addMercItem(D2ViewClipboard.removeItem());

                                    if (lDropItem.statModding()) {
                                        iCharacter.updateMercStats("D", lDropItem);
                                        paintMercStats();
                                    }
                                    setCursorPickupItem();
                                    // my_char.show_grid();
                                }
                            }
                        }
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    setCursorNormal();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setCursorNormal();
                }
            });
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent pEvent) {
                    if (iCharacter == null) {
                        return;
                    }
                    // restoreSubcomponentFocus();
                    D2Item lCurrentMouse = null;

                    D2ItemPanel lItemPanel = new D2ItemPanel(pEvent, false, false, false, true);

                    if (lItemPanel.getPanel() == 1337) {
                        if (iCharacter.getGolemItem() == null) {
                            return;
                        }
                        D2MercPainterPanel.this.setToolTipText(
                                D2ItemRenderer.itemDumpHtml(iCharacter.getGolemItem(), false));
                        return;
                    }

                    if (lItemPanel.getPanel() != -1) {
                        if (lItemPanel.isItem()) {
                            lCurrentMouse = lItemPanel.getItem();
                        }

                        if (lItemPanel.isItem()) {
                            setCursorPickupItem();
                        } else {
                            if (D2ViewClipboard.getItem() == null) {
                                setCursorNormal();
                            } else {
                                D2Item lDropItem = D2ViewClipboard.getItem();
                                // int lDropWidth = lDropItem.get_width();
                                // int lDropHeight = lDropItem.get_height();

                                boolean drop = false;

                                if (!iCharacter.checkMercPanel(lItemPanel.getPanel(), 0, 0,
                                        lDropItem)) {
                                    drop = true;
                                }
                                if (drop) {
                                    setCursorDropItem();
                                } else {
                                    setCursorNormal();
                                }
                            }
                        }
                    } else {
                        setCursorNormal();
                    }
                    if (lCurrentMouse == null) {
                        D2MercPainterPanel.this.setToolTipText(null);
                    } else {
                        D2MercPainterPanel.this
                                .setToolTipText(D2ItemRenderer.itemDumpHtml(lCurrentMouse, false));
                    }
                }
            });
        }

        public void build() {
            spriteItems.clear();
            // Merc.png: 1029x770 high-res merc equipment screen; rendered here at
            // native 514x385 (half size) to fit comfortably at UI-scale 1.0.
            // All slot coordinates come from the dedicated MERC_* / MSLOT_* constants.
            Image lSrc = D2ImageCache.getImage("Merc.png");
            if (lSrc == null) {
                // Fallback: Merc.png not found in resources — use dead.jpg
                lSrc = D2ImageCache.getImage("dead.jpg");
            }

            iBackground = iFileManager.getGraphicsConfiguration().createCompatibleImage(
                    BG_MERC_WIDTH, BG_MERC_HEIGHT, Transparency.BITMASK);

            Graphics2D lGraphics = (Graphics2D) iBackground.getGraphics();
            lGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            lGraphics.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);
            lGraphics.drawImage(lSrc, 0, 0, BG_MERC_WIDTH, BG_MERC_HEIGHT, D2MercPainterPanel.this);

            if (iCharacter != null) {
                for (int i = 0; i < iCharacter.getMercItemNr(); i++) {
                    D2Item temp_item = iCharacter.getMercItem(i);
                    int body_position = temp_item.get_body_position();
                    int w, h, wbias, hbias;
                    switch (body_position) {
                        case 1: // head (2x2)
                            placeItemImage(lGraphics, spriteItems, temp_item, MERC_HEAD_X, MERC_HEAD_Y, 2, 2);
                            break;
                        case 2: // neck/amulet (1x1)
                            placeItemImage(lGraphics, spriteItems, temp_item, MERC_NECK_X, MERC_NECK_Y, 1, 1);
                            break;
                        case 3: // body (2x3)
                            placeItemImage(lGraphics, spriteItems, temp_item, MERC_BODY_X, MERC_BODY_Y, 2, 3);
                            break;
                        case 4: // right arm (left visual column)
                            w = temp_item.get_width();
                            h = temp_item.get_height();
                            wbias = 0;
                            hbias = 0;
                            if (w == 1) wbias += GRID_SIZE / 2;
                            if (h == 3) hbias += GRID_SIZE / 2;
                            else if (h == 2) hbias += GRID_SIZE;
                            placeItemImage(lGraphics, spriteItems, temp_item,
                                    MERC_R_ARM_X + wbias, MERC_R_ARM_Y + hbias, w, h);
                            break;
                        case 5: // left arm (right visual column)
                            w = temp_item.get_width();
                            h = temp_item.get_height();
                            wbias = 0;
                            hbias = 0;
                            if (w == 1) wbias += GRID_SIZE / 2;
                            if (h == 3) hbias += GRID_SIZE / 2;
                            else if (h == 2) hbias += GRID_SIZE;
                            placeItemImage(lGraphics, spriteItems, temp_item,
                                    MERC_L_ARM_X + wbias, MERC_L_ARM_Y + hbias, w, h);
                            break;
                        case 6: // left ring (1x1)
                            placeItemImage(lGraphics, spriteItems, temp_item, MERC_L_RING_X, MERC_L_RING_Y, 1, 1);
                            break;
                        case 7: // right ring (1x1)
                            placeItemImage(lGraphics, spriteItems, temp_item, MERC_R_RING_X, MERC_R_RING_Y, 1, 1);
                            break;
                        case 8: // belt (2x1)
                            placeItemImage(lGraphics, spriteItems, temp_item, MERC_BELT_X, MERC_BELT_Y, 2, 1);
                            break;
                        case 9: // boots (2x2)
                            placeItemImage(lGraphics, spriteItems, temp_item, MERC_BOOTS_X, MERC_BOOTS_Y, 2, 2);
                            break;
                        case 10: // gloves (2x2)
                            placeItemImage(lGraphics, spriteItems, temp_item, MERC_GLOVES_X, MERC_GLOVES_Y, 2, 2);
                            break;
                    }
                }
            }
            repaint();
        }

        @Override
        public void paint(Graphics pGraphics) {
            super.paint(pGraphics);
            drawScaled(pGraphics, iBackground);
            paintSpriteItems(pGraphics, spriteItems);
        }
    }

    class D2DeathPainterPanel extends ScaledPainterPanel {
        /**
         *
         */
        @Serial
        private static final long serialVersionUID = -4532690271347197756L;
        private Image iBackground;
        private final java.util.List<SpriteItemDraw> spriteItems = new java.util.ArrayList<>();

        public D2DeathPainterPanel() {
            setSize(318, 247);
            Dimension lSize = new Dimension(318, 247);
            setPreferredSize(lSize);
            this.nativeW = 318;
            this.nativeH = 247;

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent pEvent) {
                    if (iCharacter == null) {
                        return;
                    }
                    // System.err.println("Mouse Clicked: " + pEvent.getX() + ",
                    // " + pEvent.getY() );
                    if (pEvent.getButton() == MouseEvent.BUTTON1 /*
                                                                  * && pEvent.getClickCount() == 1
                                                                  */) {
                        int lX = pEvent.getX();
                        int lY = pEvent.getY();
                        if (((lX >= 16 && lX <= 45) || (lX >= 247 && lX <= 276))
                                && (lY >= 24 && lY <= 44)) {
                            setWeaponSlot(1);
                        } else if (((lX >= 51 && lX <= 80) || (lX >= 282 && lX <= 311))
                                && (lY >= 24 && lY <= 44)) {
                            setWeaponSlot(2);
                        }


                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    setCursorNormal();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setCursorNormal();
                }
            });
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent pEvent) {
                    if (iCharacter == null) {
                        return;
                    }
                    // restoreSubcomponentFocus();
                    D2Item lCurrentMouse = null;

                    D2ItemPanel lItemPanel = new D2ItemPanel(pEvent, false, false, true);
                    if (lItemPanel.getPanel() != -1) {
                        if (lItemPanel.isItem()) {
                            lCurrentMouse = lItemPanel.getItem();
                        }

                    } else {
                        setCursorNormal();
                    }
                    if (lCurrentMouse == null) {
                        D2DeathPainterPanel.this.setToolTipText(null);
                    } else {
                        D2DeathPainterPanel.this
                                .setToolTipText(D2ItemRenderer.itemDumpHtml(lCurrentMouse, false));
                    }
                }
            });
        }

        public void setWeaponSlot(int pWeaponSlot) {
            iWeaponSlot = pWeaponSlot;
            build();
        }

        public void build() {
            spriteItems.clear();
            Image lEmptyBackground;
            if (iWeaponSlot == 1) {
                lEmptyBackground = D2ImageCache.getImage("dead.jpg");
            } else {
                lEmptyBackground = D2ImageCache.getImage("dead2.jpg");
            }

            int lWidth = lEmptyBackground.getWidth(D2DeathPainterPanel.this);
            int lHeight = lEmptyBackground.getHeight(D2DeathPainterPanel.this);

            iBackground = iFileManager.getGraphicsConfiguration().createCompatibleImage(lWidth,
                    lHeight, Transparency.TRANSLUCENT);
            // iBackground = new BufferedImage(lWidth, lHeight, BufferedImage.TYPE_3BYTE_BGR);

            Graphics2D lGraphics = (Graphics2D) iBackground.getGraphics();

            lGraphics.drawImage(lEmptyBackground, 0, 0, D2DeathPainterPanel.this);

            if (iCharacter != null) {
                for (int i = 0; i < iCharacter.getCorpseItemNr(); i++) {
                    D2Item temp_item = iCharacter.getCorpseItem(i);
                    int location = temp_item.get_location();
                    if (location == 0) {
                        int panel = temp_item.get_panel();
                        int x = temp_item.get_col();
                        int y = temp_item.get_row();
                        int w = temp_item.get_width(), h = temp_item.get_height();
                        switch (panel) {
                            case 1: { // inventory
                                int px = INV_X + x * GRID_SIZE + x * GRID_SPACER;
                                int py = INV_Y + y * GRID_SIZE + y * GRID_SPACER;
                                fillItemSlotBg(lGraphics, px, py, w, h);
                                placeItemImage(lGraphics, spriteItems, temp_item, px, py, w, h);
                                break; }
                            case 4: { // cube
                                int px = CUBE_X + x * GRID_SIZE + x * GRID_SPACER;
                                int py = CUBE_Y + y * GRID_SIZE + y * GRID_SPACER;
                                fillItemSlotBg(lGraphics, px, py, w, h);
                                placeItemImage(lGraphics, spriteItems, temp_item, px, py, w, h);
                                break; }
                            case 5: { // stash
                                int px = STASH_X + x * GRID_SIZE + x * GRID_SPACER;
                                int py = STASH_Y + y * GRID_SIZE + y * GRID_SPACER;
                                fillItemSlotBg(lGraphics, px, py, w, h);
                                placeItemImage(lGraphics, spriteItems, temp_item, px, py, w, h);
                                break; }
                        }
                    } else if (location == 2) { // belt (all 1x1)
                        int x = temp_item.get_col();
                        int y = x / 4;
                        x = x % 4;
                        int bpx = BELT_GRID_X + x * GRID_SIZE + x * GRID_SPACER;
                        int bpy = BELT_GRID_Y + (3 - y) * GRID_SIZE + (3 - y) * GRID_SPACER;
                        fillItemSlotBg(lGraphics, bpx, bpy, 1, 1);
                        placeItemImage(lGraphics, spriteItems, temp_item, bpx, bpy, 1, 1);
                    } else { // on the body
                        int body_position = temp_item.get_body_position();
                        int w, h, wbias, hbias;
                        switch (body_position) {
                            case 1: // head (2x2)
                                placeItemImage(lGraphics, spriteItems, temp_item, HEAD_X, HEAD_Y, 2, 2);
                                break;
                            case 2: // neck/amulet (1x1)
                                placeItemImage(lGraphics, spriteItems, temp_item, NECK_X, NECK_Y, 1, 1);
                                break;
                            case 3: // body (2x3)
                                placeItemImage(lGraphics, spriteItems, temp_item, BODY_X, BODY_Y, 2, 3);
                                break;
                            case 4:
                            case 11: // right arm
                                if ((iWeaponSlot == 1 && body_position == 4)
                                        || (iWeaponSlot == 2 && body_position == 11)) {
                                    w = temp_item.get_width();
                                    h = temp_item.get_height();
                                    wbias = 0;
                                    hbias = 0;
                                    if (w == 1) wbias += GRID_SIZE / 2;
                                    if (h == 3) hbias += GRID_SIZE / 2;
                                    else if (h == 2) hbias += GRID_SIZE;
                                    placeItemImage(lGraphics, spriteItems, temp_item,
                                            R_ARM_X + wbias, R_ARM_Y + hbias, w, h);
                                }
                                break;
                            case 5:
                            case 12: // left arm
                                if ((iWeaponSlot == 1 && body_position == 5)
                                        || (iWeaponSlot == 2 && body_position == 12)) {
                                    w = temp_item.get_width();
                                    h = temp_item.get_height();
                                    wbias = 0;
                                    hbias = 0;
                                    if (w == 1) wbias += GRID_SIZE / 2;
                                    if (h == 3) hbias += GRID_SIZE / 2;
                                    else if (h == 2) hbias += GRID_SIZE;
                                    placeItemImage(lGraphics, spriteItems, temp_item,
                                            L_ARM_X + wbias, L_ARM_Y + hbias, w, h);
                                }
                                break;
                            case 6: // left ring (1x1)
                                placeItemImage(lGraphics, spriteItems, temp_item, L_RING_X, L_RING_Y, 1, 1);
                                break;
                            case 7: // right ring (1x1)
                                placeItemImage(lGraphics, spriteItems, temp_item, R_RING_X, R_RING_Y, 1, 1);
                                break;
                            case 8: // belt equip (2x1)
                                placeItemImage(lGraphics, spriteItems, temp_item, BELT_X, BELT_Y, 2, 1);
                                break;
                            case 9: // boots (2x2)
                                placeItemImage(lGraphics, spriteItems, temp_item, BOOTS_X, BOOTS_Y, 2, 2);
                                break;
                            case 10: // gloves (2x2)
                                placeItemImage(lGraphics, spriteItems, temp_item, GLOVES_X, GLOVES_Y, 2, 2);
                                break;
                        }
                    }
                }
            }
            repaint();
        }

        @Override
        public void paint(Graphics pGraphics) {
            super.paint(pGraphics);
            drawScaled(pGraphics, iBackground);
            paintSpriteItems(pGraphics, spriteItems);
        }
    }

    class D2SkillPainterPanel extends ScaledPainterPanel {
        /**
         *
         */
        @Serial
        private static final long serialVersionUID = 8956659406243697754L;
        private Image iBackground;
        private Image lEmptyBackground;

        public D2SkillPainterPanel() {
            setSize(284, 383);
            Dimension lSize = new Dimension(284, 383);
            setPreferredSize(lSize);
            this.nativeW = 284;
            this.nativeH = 383;
            // this.build();

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent pEvent) {
                    if (iCharacter == null) {
                        return;
                    }
                    // System.err.println("Mouse Clicked: " + pEvent.getX() + ",
                    // " + pEvent.getY() );
                    if (pEvent.getButton() == MouseEvent.BUTTON1 /*
                                                                  * && pEvent.getClickCount() == 1
                                                                  */) {
                        int lX = pEvent.getX();
                        int lY = pEvent.getY();
                        if ((lX >= 208 && lX <= 283) && (lY >= 300 && lY <= 388)) {
                            setSkillSlot(0);
                        } else if ((lX >= 208 && lX <= 283) && (lY >= 201 && lY <= 291)) {
                            setSkillSlot(1);
                        } else if ((lX >= 208 && lX <= 283) && (lY >= 100 && lY <= 192)) {
                            setSkillSlot(2);
                        }
                        // determine where the mouse click is
                    }
                }


                private void setSkillSlot(int i) {


                    iSkillSlot = i;
                    build();
                }


                @Override
                public void mouseEntered(MouseEvent e) {
                    setCursorNormal();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setCursorNormal();
                }
            });

        }

        public void build() {


            switch ((int) iCharacter.getCharCode()) {
                case 0 -> {
                    switch (iSkillSlot) {
                        case 0 -> lEmptyBackground = D2ImageCache.getImage("AmaArr.jpg");
                        case 1 -> lEmptyBackground = D2ImageCache.getImage("AmaPass.jpg");
                        case 2 -> lEmptyBackground = D2ImageCache.getImage("AmaJav.jpg");
                    }
                    // cClass = "ama";
                }
                case 1 -> {
                    switch (iSkillSlot) {
                        case 0 -> lEmptyBackground = D2ImageCache.getImage("SorFir.jpg");
                        case 1 -> lEmptyBackground = D2ImageCache.getImage("SorLig.jpg");
                        case 2 -> lEmptyBackground = D2ImageCache.getImage("SorCol.jpg");
                    }
                    // cClass = "sor";
                }
                case 2 -> {
                    switch (iSkillSlot) {
                        case 0 -> lEmptyBackground = D2ImageCache.getImage("NecCur.jpg");
                        case 1 -> lEmptyBackground = D2ImageCache.getImage("NecPoi.jpg");
                        case 2 -> lEmptyBackground = D2ImageCache.getImage("NecSum.jpg");
                    }
                    // cClass = "nec";
                }
                case 3 -> {
                    switch (iSkillSlot) {
                        case 0 -> lEmptyBackground = D2ImageCache.getImage("PalCom.jpg");
                        case 1 -> lEmptyBackground = D2ImageCache.getImage("PalOff.jpg");
                        case 2 -> lEmptyBackground = D2ImageCache.getImage("PalDef.jpg");
                    }
                    // cClass = "pal";
                }
                case 4 -> {
                    switch (iSkillSlot) {
                        case 0 -> lEmptyBackground = D2ImageCache.getImage("BarCom.jpg");
                        case 1 -> lEmptyBackground = D2ImageCache.getImage("BarMas.jpg");
                        case 2 -> lEmptyBackground = D2ImageCache.getImage("BarWar.jpg");
                    }
                    // cClass = "bar";
                }
                case 5 -> {
                    switch (iSkillSlot) {
                        case 0 -> lEmptyBackground = D2ImageCache.getImage("DruSum.jpg");
                        case 1 -> lEmptyBackground = D2ImageCache.getImage("DruSha.jpg");
                        case 2 -> lEmptyBackground = D2ImageCache.getImage("DruEle.jpg");
                    }
                    // cClass = "dru";
                }
                case 6 -> {
                    switch (iSkillSlot) {
                        case 0 -> lEmptyBackground = D2ImageCache.getImage("AssTra.jpg");
                        case 1 -> lEmptyBackground = D2ImageCache.getImage("AssSha.jpg");
                        case 2 -> lEmptyBackground = D2ImageCache.getImage("AssMar.jpg");
                    }
                    // cClass = "ass";
                }
                case 7 -> {
                    // Warlock (MDK V3 official-expansion class).
                    // Skill tab order from charstats.txt: StrSklTabItem24, StrSklTabItem22,
                    // StrSklTabItem23
                    // slot 0 = Demon (StrSklTabItem24)
                    // slot 1 = Eldritch (StrSklTabItem22)
                    // slot 2 = Chaos (StrSklTabItem23)
                    // TODO: replace these placeholders (currently copies of Amazon backgrounds)
                    // with real Warlock skill-tree backgrounds extracted from CASC
                    // (data/hd/global/ui/spells/skill_trees/waskilltree.sprite).
                    switch (iSkillSlot) {
                        case 0 -> lEmptyBackground = D2ImageCache.getImage("WarDem.jpg");
                        case 1 -> lEmptyBackground = D2ImageCache.getImage("WarEld.jpg");
                        case 2 -> lEmptyBackground = D2ImageCache.getImage("WarCha.jpg");
                    }
                    // cClass = "war";
                }
            }


            // lEmptyBackground = D2ImageCache.getImage("AmaArr.jpg");

            // Fallback for modded/custom classes (class code > 6, e.g. MDK Warlock=7):
            // no skill-panel background image exists, so use the Amazon one as placeholder
            // to avoid NullPointerException in the skill painter.
            if (lEmptyBackground == null) {
                lEmptyBackground = D2ImageCache.getImage("AmaArr.jpg");
                if (lEmptyBackground == null) {
                    return;
                }
            }

            int lWidth = lEmptyBackground.getWidth(D2SkillPainterPanel.this);
            int lHeight = lEmptyBackground.getHeight(D2SkillPainterPanel.this);

            iBackground = iFileManager.getGraphicsConfiguration().createCompatibleImage(lWidth,
                    lHeight, Transparency.BITMASK);
            // iBackground = new BufferedImage(lEmptyBackground.getWidth(lWidth, lHeight,
            // BufferedImage.TYPE_3BYTE_BGR);

            Graphics2D lGraphics = (Graphics2D) iBackground.getGraphics();

            lGraphics.drawImage(lEmptyBackground, 0, 0, D2SkillPainterPanel.this);

            if (iCharacter != null) {
                drawText(lGraphics, iSkillSlot);

            }

            repaint();
        }

        private void drawText(Graphics2D lGraphics, int skillSlot) {

            switch (iSkillSlot) {
                case 0 -> {
                    lGraphics.drawString(iCharacter.getCharSkillRem() + "", 238, 69);
                    for (int x = 0; x < 10; x = x + 1) {
                        lGraphics.setColor(Color.white);
                        lGraphics.drawString(iCharacter.getInitSkillListA()[x] + "/",
                                iCharacter.getSkillLocs()[x].x - 10,
                                iCharacter.getSkillLocs()[x].y + 2);
                        if (iCharacter.getInitSkillListA()[x] != (iCharacter.getSkillListA()[x])) {
                            lGraphics.setColor(Color.orange.brighter());
                        }
                        lGraphics.drawString(iCharacter.getSkillListA()[x] + "",
                                iCharacter.getSkillLocs()[x].x + 11,
                                iCharacter.getSkillLocs()[x].y + 2);

                    }
                }
                case 1 -> {
                    lGraphics.drawString(iCharacter.getCharSkillRem() + "", 238, 69);
                    for (int x = 0; x < 10; x = x + 1) {
                        lGraphics.setColor(Color.white);
                        lGraphics.drawString(iCharacter.getInitSkillListB()[x] + "/",
                                iCharacter.getSkillLocs()[x + 10].x - 10,
                                iCharacter.getSkillLocs()[x + 10].y + 2);

                        if (iCharacter.getInitSkillListB()[x] != (iCharacter.getSkillListB()[x])) {
                            lGraphics.setColor(Color.orange.brighter());
                        } else {
                            lGraphics.setColor(Color.white);
                        }
                        lGraphics.drawString(iCharacter.getSkillListB()[x] + "",
                                iCharacter.getSkillLocs()[x + 10].x + 11,
                                iCharacter.getSkillLocs()[x + 10].y + 2);

                    }
                }
                case 2 -> {
                    lGraphics.drawString(iCharacter.getCharSkillRem() + "", 238, 69);
                    for (int x = 0; x < 10; x = x + 1) {
                        lGraphics.setColor(Color.white);
                        lGraphics.drawString(iCharacter.getInitSkillListC()[x] + "/",
                                iCharacter.getSkillLocs()[x + 20].x - 10,
                                iCharacter.getSkillLocs()[x + 20].y + 2);

                        if (iCharacter.getInitSkillListC()[x] != (iCharacter.getSkillListC()[x])) {
                            lGraphics.setColor(Color.orange.brighter());
                        } else {
                            lGraphics.setColor(Color.white);
                        }

                        lGraphics.drawString(iCharacter.getSkillListC()[x] + "",
                                iCharacter.getSkillLocs()[x + 20].x + 11,
                                iCharacter.getSkillLocs()[x + 20].y + 2);
                    }
                }
            }


        }

        @Override
        public void paint(Graphics pGraphics) {
            super.paint(pGraphics);
            drawScaled(pGraphics, iBackground);
        }
    }

    class D2QuestPainterPanel extends ScaledPainterPanel {
        /**
         *
         */
        @Serial
        private static final long serialVersionUID = 1154694362190497678L;
        private final int cowKingX = 140;
        private final int cowkingY = 233;
        private Image iBackground;
        private int bgNum = 1;
        private Image lEmptyBackground;
        private Image tick;
        private Point[][] questLoc = {
                {new Point(24, 93), new Point(113, 93), new Point(197, 93), new Point(24, 178),
                        new Point(113, 178), new Point(197, 178)},
                {new Point(47, 93), new Point(136, 93), new Point(220, 93), new Point(47, 178),
                        new Point(136, 178), new Point(220, 178)},
                {new Point(71, 93), new Point(160, 93), new Point(244, 93), new Point(71, 178),
                        new Point(160, 178), new Point(244, 178)}};

        public D2QuestPainterPanel() {
            tick = D2ImageCache.getImage("tick.jpg");
            setSize(286, 383);
            Dimension lSize = new Dimension(284, 383);
            setPreferredSize(lSize);
            this.nativeW = 284;
            this.nativeH = 383;


            addMouseListener(new MouseAdapter() {


                @Override
                public void mouseReleased(MouseEvent pEvent) {
                    if (iCharacter == null) {
                        return;
                    }
                    if (pEvent.getButton() == MouseEvent.BUTTON1) {
                        int lX = pEvent.getX();
                        int lY = pEvent.getY();
                        if ((lX >= 10 && lX <= 60) && (lY >= 0 && lY <= 26)) {
                            setQuestSlot(1);
                        } else if ((lX >= 65 && lX <= 115) && (lY >= 0 && lY <= 26)) {
                            setQuestSlot(2);
                        } else if ((lX >= 120 && lX <= 170) && (lY >= 0 && lY <= 26)) {
                            setQuestSlot(3);
                        } else if ((lX >= 175 && lX <= 225) && (lY >= 0 && lY <= 26)) {
                            setQuestSlot(4);
                        } else if ((lX >= 230 && lX <= 280) && (lY >= 0 && lY <= 26)) {
                            setQuestSlot(5);
                        }
                        // determine where the mouse click is
                    }
                }


                private void setQuestSlot(int i) {

                    bgNum = i;
                    build();
                }


                @Override
                public void mouseEntered(MouseEvent e) {
                    setCursorNormal();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setCursorNormal();
                }
            });

        }

        public void build() {
            lEmptyBackground = D2ImageCache.getImage("q" + bgNum + ".jpg");
            int lWidth = lEmptyBackground.getWidth(D2QuestPainterPanel.this);
            int lHeight = lEmptyBackground.getHeight(D2QuestPainterPanel.this);

            iBackground = iFileManager.getGraphicsConfiguration().createCompatibleImage(lWidth,
                    lHeight, Transparency.BITMASK);
            // iBackground = new BufferedImage(lEmptyBackground.getWidth(lWidth, lHeight,
            // BufferedImage.TYPE_3BYTE_BGR);

            Graphics2D lGraphics = (Graphics2D) iBackground.getGraphics();

            lGraphics.drawImage(lEmptyBackground, 0, 0, D2QuestPainterPanel.this);

            if (iCharacter != null) {
                drawCompleted(lGraphics, bgNum);

            }
            repaint();
        }

        private void drawCompleted(Graphics2D lGraphics, int questSlot) {

            for (int f = 0; f < 3; f = f + 1) {
                for (int y = 0; y < iCharacter.getQuests()[f][questSlot - 1].length; y = y + 1) {
                    if (iCharacter.getQuests()[f][questSlot - 1][y])
                        lGraphics.drawImage(tick, questLoc[f][y].x, questLoc[f][y].y,
                                D2QuestPainterPanel.this);
                }
                if (iCharacter.getCowKingDead(f) && (questSlot) == 1) {
                    lGraphics.drawImage(tick, cowKingX + (24 * f), cowkingY,
                            D2QuestPainterPanel.this);
                }
            }
        }

        @Override
        public void paint(Graphics pGraphics) {
            super.paint(pGraphics);
            drawScaled(pGraphics, iBackground);
        }
    }

    class D2WayPainterPanel extends ScaledPainterPanel {
        /**
         *
         */
        @Serial
        private static final long serialVersionUID = -7583208611559380148L;
        private Image iBackground;
        private int bgNum = 1;
        private Image lEmptyBackground;
        private Image tick;
        private Point[] questLoc = {new Point(36, 59), new Point(36, 87), new Point(36, 116),
                new Point(36, 145), new Point(36, 174), new Point(36, 202), new Point(36, 231),
                new Point(36, 260), new Point(36, 287)};

        public D2WayPainterPanel() {
            this.setBackground(Color.BLACK);
            tick = D2ImageCache.getImage("ticksm.jpg");
            setSize(286, 383);
            Dimension lSize = new Dimension(263, 360);
            setPreferredSize(lSize);
            this.nativeW = 263;
            this.nativeH = 360;
            // this.build();

            // addMouseMotionListener(new MouseMotionAdapter()
            // {
            // public void mouseMoved(MouseEvent pEvent)
            // {
            // System.out.println(pEvent.getX() + " , " + pEvent.getY());
            // }
            // });

            addMouseListener(new MouseAdapter() {


                @Override
                public void mouseReleased(MouseEvent pEvent) {
                    if (iCharacter == null) {
                        return;
                    }
                    if (pEvent.getButton() == MouseEvent.BUTTON1) {
                        int lX = pEvent.getX();
                        int lY = pEvent.getY();
                        if ((lX >= 8 && lX <= 54) && (lY >= 0 && lY <= 26)) {
                            setQuestSlot(1);
                        } else if ((lX >= 58 && lX <= 106) && (lY >= 0 && lY <= 26)) {
                            setQuestSlot(2);
                        } else if ((lX >= 109 && lX <= 155) && (lY >= 0 && lY <= 26)) {
                            setQuestSlot(3);
                        } else if ((lX >= 159 && lX <= 207) && (lY >= 0 && lY <= 26)) {
                            setQuestSlot(4);
                        } else if ((lX >= 210 && lX <= 256) && (lY >= 0 && lY <= 26)) {
                            setQuestSlot(5);
                        }
                        // determine where the mouse click is
                    }
                }


                private void setQuestSlot(int i) {

                    bgNum = i;
                    build();
                }


                @Override
                public void mouseEntered(MouseEvent e) {
                    setCursorNormal();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setCursorNormal();
                }
            });

        }

        public void build() {
            lEmptyBackground = D2ImageCache.getImage("w" + bgNum + ".jpg");
            int lWidth = lEmptyBackground.getWidth(D2WayPainterPanel.this);
            int lHeight = lEmptyBackground.getHeight(D2WayPainterPanel.this);

            iBackground = iFileManager.getGraphicsConfiguration().createCompatibleImage(lWidth,
                    lHeight, Transparency.BITMASK);
            // iBackground = new BufferedImage(lEmptyBackground.getWidth(lWidth, lHeight,
            // BufferedImage.TYPE_3BYTE_BGR);

            Graphics2D lGraphics = (Graphics2D) iBackground.getGraphics();

            lGraphics.drawImage(lEmptyBackground, 0, 0, D2WayPainterPanel.this);

            if (iCharacter != null) {
                drawCompleted(lGraphics, bgNum);

            }
            repaint();
        }

        private void drawCompleted(Graphics2D lGraphics, int questSlot) {


            for (int f = 0; f < 3; f = f + 1) {
                for (int y = 0; y < iCharacter.getWaypoints()[f][questSlot - 1].length; y = y + 1) {
                    if (iCharacter.getWaypoints()[f][questSlot - 1][y]) {
                        if (f == 0) {
                            lGraphics.drawImage(tick, questLoc[y].x, questLoc[y].y,
                                    D2WayPainterPanel.this);
                        } else if (f == 1) {
                            lGraphics.drawImage(tick, questLoc[y].x + 10, questLoc[y].y,
                                    D2WayPainterPanel.this);
                        } else if (f == 2) {
                            lGraphics.drawImage(tick, questLoc[y].x + 20, questLoc[y].y,
                                    D2WayPainterPanel.this);
                        }
                    }

                }
            }



        }

        @Override
        public void paint(Graphics pGraphics) {
            super.paint(pGraphics);
            drawScaled(pGraphics, iBackground);
        }
    }

    class D2CharCursorPainterPanel extends ScaledPainterPanel {
        /**
         *
         */
        @Serial
        private static final long serialVersionUID = 3335835168313769724L;
        private Image iBackground;
        private final java.util.List<SpriteItemDraw> spriteItems = new java.util.ArrayList<>();

        public D2CharCursorPainterPanel() {
            setSize(BG_CURSOR_WIDTH, BG_CURSOR_HEIGHT);
            Dimension lSize = new Dimension(BG_CURSOR_WIDTH, BG_CURSOR_HEIGHT);
            setPreferredSize(lSize);
            this.nativeW = BG_CURSOR_WIDTH;
            this.nativeH = BG_CURSOR_HEIGHT;

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent pEvent) {
                    if (iCharacter == null) {
                        return;
                    }
                    // System.err.println("Mouse Clicked: " + pEvent.getX() + ",
                    // " + pEvent.getY() );
                    if (pEvent.getButton() == MouseEvent.BUTTON1 /*
                                                                  * && pEvent.getClickCount() == 1
                                                                  */) {
                        // determine where the mouse click is
                        D2ItemPanel lItemPanel = new D2ItemPanel(pEvent, true, true, false);
                        if (lItemPanel.getPanel() != -1) {
                            // if there is an item to grab, grab it
                            if (lItemPanel.isItem()) {
                                D2Item lTemp = iCharacter.getCursorItem();
                                iCharacter.setCursorItem(null);
                                D2ViewClipboard.addItem(lTemp);
                                setCursorDropItem();

                                // redraw
                                build();
                                repaint();
                            } else if (D2ViewClipboard.getItem() != null) {
                                iCharacter.setCursorItem(D2ViewClipboard.removeItem());
                                build();
                                repaint();

                                setCursorPickupItem();
                            }
                        }

                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    setCursorNormal();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setCursorNormal();
                }
            });
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(MouseEvent pEvent) {
                    if (iCharacter == null) {
                        return;
                    }
                    // restoreSubcomponentFocus();
                    D2Item lCurrentMouse = null;

                    D2ItemPanel lItemPanel = new D2ItemPanel(pEvent, true, true, false);
                    if (lItemPanel.getPanel() != -1) {
                        if (lItemPanel.isItem()) {
                            lCurrentMouse = lItemPanel.getItem();
                        }

                        if (lItemPanel.isItem()) {
                            setCursorPickupItem();
                        } else {
                            if (D2ViewClipboard.getItem() == null) {
                                setCursorNormal();
                            } else {
                                setCursorDropItem();

                            }
                        }
                    } else {
                        setCursorNormal();
                    }
                    if (lCurrentMouse == null) {
                        D2CharCursorPainterPanel.this.setToolTipText(null);
                    } else {
                        D2CharCursorPainterPanel.this
                                .setToolTipText(D2ItemRenderer.itemDumpHtml(lCurrentMouse, false));
                    }
                }
            });
        }

        public void build() {
            spriteItems.clear();
            Image lEmptyBackground = D2ImageCache.getImage("cursor.jpg");

            int lWidth = lEmptyBackground.getWidth(D2CharCursorPainterPanel.this);
            int lHeight = lEmptyBackground.getHeight(D2CharCursorPainterPanel.this);

            iBackground = iFileManager.getGraphicsConfiguration().createCompatibleImage(lWidth,
                    lHeight, Transparency.BITMASK);

            Graphics2D lGraphics = (Graphics2D) iBackground.getGraphics();

            lGraphics.drawImage(lEmptyBackground, 0, 0, D2CharCursorPainterPanel.this);

            if (iCharacter != null) {
                D2Item lCursorItem = iCharacter.getCursorItem();
                if (lCursorItem != null) {
                    placeItemImage(lGraphics, spriteItems, lCursorItem, CURSOR_X, CURSOR_Y,
                            lCursorItem.get_width(), lCursorItem.get_height());
                }
            }
            repaint();
        }

        @Override
        public void paint(Graphics pGraphics) {
            super.paint(pGraphics);
            drawScaled(pGraphics, iBackground);
            paintSpriteItems(pGraphics, spriteItems);
        }
    }

}
