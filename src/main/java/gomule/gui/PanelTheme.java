package gomule.gui;

import java.awt.Color;

/**
 * 面板绘制主题，控制角色装备区/仓库/魔盒/腰带格的程序化绘制颜色。
 * <p>
 * 使用方式：所有绘制代码通过 {@code PanelTheme.active.xxx} 取色，
 * 切换主题只需将 {@code PanelTheme.active} 指向新枚举值，再调用 {@code build()}。
 * 属性键：{@value #PROPERTY_NAME}（存入 app.properties）。
 */
public enum PanelTheme {

    GOLD("金棕色 Gold",
            new Color(108, 84,  54),   // bg
            new Color(120, 95,  62),   // equipZoneBg
            new Color(188, 152, 98),   // divider
            new Color(135, 108, 72),   // slotBg
            new Color(200, 162, 108),  // slotBorder
            new Color(155, 128, 88),   // slotIcon
            new Color(118, 95,  62),   // gridBg
            new Color(192, 155, 105),  // gridBorder
            new Color(142, 116, 78),   // gridLine
            new Color(248, 215, 148),  // zoneLabel
            new Color(240, 205, 132),  // slotLabel
            new Color(185, 145, 80),   // weaponActiveBg
            new Color(122, 98,  65),   // weaponInactiveBg
            new Color(235, 198, 128),  // weaponActiveBorder
            new Color(178, 145, 95),   // weaponInactiveBorder
            new Color(248, 218, 142),  // weaponActiveText
            new Color(205, 168, 115)   // weaponInactiveText
    ),

    LIGHT_GRAY("浅灰色 Light Gray",
            new Color(195, 195, 195),  // bg
            new Color(212, 212, 212),  // equipZoneBg
            new Color(130, 130, 130),  // divider
            new Color(222, 222, 222),  // slotBg
            new Color(145, 145, 145),  // slotBorder
            new Color(165, 165, 165),  // slotIcon
            new Color(202, 202, 202),  // gridBg
            new Color(135, 135, 135),  // gridBorder
            new Color(182, 182, 182),  // gridLine
            new Color(40,  40,  70),   // zoneLabel
            new Color(55,  55,  80),   // slotLabel
            new Color(155, 160, 195),  // weaponActiveBg
            new Color(198, 198, 208),  // weaponInactiveBg
            new Color(70,  70,  130),  // weaponActiveBorder
            new Color(145, 145, 165),  // weaponInactiveBorder
            new Color(20,  20,  80),   // weaponActiveText
            new Color(80,  80,  110)   // weaponInactiveText
    ),

    MONO("黑白 Mono",
            new Color(28,  28,  28),   // bg
            new Color(40,  40,  40),   // equipZoneBg
            new Color(95,  95,  95),   // divider
            new Color(52,  52,  52),   // slotBg
            new Color(155, 155, 155),  // slotBorder
            new Color(88,  88,  88),   // slotIcon
            new Color(34,  34,  34),   // gridBg
            new Color(115, 115, 115),  // gridBorder
            new Color(58,  58,  58),   // gridLine
            new Color(218, 218, 218),  // zoneLabel
            new Color(200, 200, 200),  // slotLabel
            new Color(165, 165, 165),  // weaponActiveBg
            new Color(55,  55,  55),   // weaponInactiveBg
            new Color(230, 230, 230),  // weaponActiveBorder
            new Color(108, 108, 108),  // weaponInactiveBorder
            new Color(255, 255, 255),  // weaponActiveText
            new Color(145, 145, 145)   // weaponInactiveText
    );

    /** app.properties 中存储主题名称的 key。 */
    public static final String PROPERTY_NAME = "panel.theme";

    /** 当前激活的主题，所有绘制代码使用此字段取色。 */
    public static PanelTheme active = GOLD;

    public final String displayName;

    // ── 全局底色 / 装备区 ──────────────────────────────────────────────────
    public final Color bg;
    public final Color equipZoneBg;
    public final Color divider;

    // ── 装备槽 ────────────────────────────────────────────────────────────
    public final Color slotBg;
    public final Color slotBorder;
    public final Color slotIcon;
    public final Color slotLabel;

    // ── 网格区域 ──────────────────────────────────────────────────────────
    public final Color gridBg;
    public final Color gridBorder;
    public final Color gridLine;
    public final Color zoneLabel;

    // ── 武器槽指示器 ──────────────────────────────────────────────────────
    public final Color weaponActiveBg;
    public final Color weaponInactiveBg;
    public final Color weaponActiveBorder;
    public final Color weaponInactiveBorder;
    public final Color weaponActiveText;
    public final Color weaponInactiveText;

    PanelTheme(String displayName,
               Color bg, Color equipZoneBg, Color divider,
               Color slotBg, Color slotBorder, Color slotIcon,
               Color gridBg, Color gridBorder, Color gridLine,
               Color zoneLabel, Color slotLabel,
               Color weaponActiveBg, Color weaponInactiveBg,
               Color weaponActiveBorder, Color weaponInactiveBorder,
               Color weaponActiveText, Color weaponInactiveText) {
        this.displayName = displayName;
        this.bg = bg;
        this.equipZoneBg = equipZoneBg;
        this.divider = divider;
        this.slotBg = slotBg;
        this.slotBorder = slotBorder;
        this.slotIcon = slotIcon;
        this.gridBg = gridBg;
        this.gridBorder = gridBorder;
        this.gridLine = gridLine;
        this.zoneLabel = zoneLabel;
        this.slotLabel = slotLabel;
        this.weaponActiveBg = weaponActiveBg;
        this.weaponInactiveBg = weaponInactiveBg;
        this.weaponActiveBorder = weaponActiveBorder;
        this.weaponInactiveBorder = weaponInactiveBorder;
        this.weaponActiveText = weaponActiveText;
        this.weaponInactiveText = weaponInactiveText;
    }

    /** 根据名称解析主题，找不到时返回 {@link #GOLD}。大小写不敏感（容忍 app.properties 手改成小写）。 */
    public static PanelTheme fromName(String name) {
        if (name == null || name.isBlank()) return GOLD;
        try {
            return PanelTheme.valueOf(name.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return GOLD;
        }
    }

    @Override
    public String toString() {
        return displayName;
    }
}
