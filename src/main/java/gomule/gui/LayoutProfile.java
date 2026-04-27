package gomule.gui;

/**
 * 布局配置预设 — 用于支持不同 D2RMM mod 组合带来的背包/魔盒/仓库尺寸差异。
 *
 * <p>每个 Profile 包含：
 * <ul>
 *   <li>网格尺寸：背包/魔盒/仓库的格子数</li>
 *   <li>像素坐标：各区域在角色面板中的左上角像素位置</li>
 *   <li>面板尺寸：背景图像需要的宽高</li>
 *   <li>背景图基础名：加载 resources/background-{base}.png（找不到则程序自动绘制）</li>
 * </ul>
 *
 * <p><b>坐标计算参考</b>（GRID_SIZE=28, GRID_SPACER=1，每格 29px）：
 * <pre>
 *  区域     左列 X=18           右列 X=stashCoordX
 *  设备格   x=18~310, y=3~270
 *  背包     x=invCoordX, y=invCoordY  (invSizeX × invSizeY)
 *  仓库     x=stashCoordX, y=3        (stashSizeX × stashSizeY)
 *  魔盒     x=cubeCoordX, y=cubeCoordY (cubeSizeX × cubeSizeY)
 *  腰带格   x=beltCoordX, y=beltCoordY (4×4)
 * </pre>
 */
public enum LayoutProfile {

    /**
     * 原生 D2R：10×4 背包 / 3×4 魔盒（不含 D2RMM mod）。
     * 使用 background.png（616×418）。
     */
    NATIVE(
            "原生 (10×4背包 / 3×4魔盒 / 原生仓库)",
            /* invSizeX, invSizeY */   10, 4,
            /* cubeSizeX, cubeSizeY */ 3, 4,
            /* stashSizeX, stashSizeY */ 10, 10,
            /* invCoordX, invCoordY */   18, 270,
            /* cubeCoordX, cubeCoordY */ 326, 385,
            /* stashCoordX, stashCoordY */ 326, 3,
            /* beltCoordX, beltCoordY */ 534, 385,
            /* bgWidth, bgHeight */ 616, 418,
            /* bgImageBase */ "native"
    ),

    /**
     * 大仓库 mod (1.1)：16×13 仓库 / 10×4 背包 / 6×4 魔盒。
     * 对应 D2RMM mod "(1.1)BigStash大仓库16x13"。
     * 使用已有的 background-16x13.png (791×513)。
     */
    BIG_STASH(
            "大仓库 16×13 (10×4背包 / 6×4魔盒)",
            10, 4,
            6, 4,
            16, 13,
            18, 270,
            326, 385,
            326, 3,
            670, 385,
            790, 512,
            "16x13"
    ),

    /**
     * 大仓库 + 大背包 (1.1 + 1.3)：16×13 仓库 / 10×8 背包 / 6×4 魔盒。
     * 对应 "(1.1)BigStash" + "(1.3)BigInventory大背包10x8"。
     * 背包在左列 (x=18, y=299~531)，魔盒在右列下方 (x=326, y=385)，
     * 左右两列 X 不重叠（左列 x<326，右列 x≥326），故背包延伸不影响魔盒位置。
     * 需要 background-16x13-inv10x8.png (791×540)；无该图时程序自动绘制。
     */
    BIG_STASH_INV_10x8(
            "大仓库 16×13 + 大背包 10×8 + 魔盒 6×4",
            10, 8,
            6, 4,
            16, 13,
            18, 270,
            326, 385,
            326, 3,
            670, 385,
            790, 540,
            "16x13-inv10x8"
    ),

    /**
     * 大仓库 + 超大背包 XL (1.1 + 1.4)：16×13 仓库 / 13×8 背包 / 6×4 魔盒。
     * 对应 "(1.1)BigStash" + "(1.4)BigInventoryXL超大背包13x8"。
     * 13×8 背包宽度 = 13*29 = 377px → 右边界 = 18+377 = 395px。
     * 仓库右移至 x=400 避免重叠，面板宽度扩展到 875px。
     * 需要 background-16x13-inv13x8.png (875×540)；无该图时程序自动绘制。
     */
    BIG_STASH_INV_XL_13x8(
            "大仓库 16×13 + 超大背包 13×8 + 魔盒 6×4",
            13, 8,
            6, 4,
            16, 13,
            18, 270,
            400, 385,
            400, 3,
            754, 385,
            875, 540,
            "16x13-inv13x8"
    );

    // ──────────────────────────────────────────────────────────────────────────
    // 字段

    /** app.properties 中用于持久化布局选择的键名。 */
    public static final String PROPERTY_NAME = "layout.profile";

    /** 显示名称（用于 UI 下拉框） */
    public final String displayName;

    /** 背包网格尺寸（格子数） */
    public final int invSizeX, invSizeY;
    /** 魔盒网格尺寸（格子数） */
    public final int cubeSizeX, cubeSizeY;
    /** 仓库网格尺寸（格子数） */
    public final int stashSizeX, stashSizeY;

    /** 背包区域左上角像素坐标 */
    public final int invCoordX, invCoordY;
    /** 魔盒区域左上角像素坐标 */
    public final int cubeCoordX, cubeCoordY;
    /** 仓库区域左上角像素坐标 */
    public final int stashCoordX, stashCoordY;
    /** 腰带格区域左上角像素坐标 */
    public final int beltCoordX, beltCoordY;

    /** 背景图面板像素尺寸 */
    public final int bgWidth, bgHeight;

    /**
     * 背景图基础名。
     * GoMule 会依次尝试加载：
     * <ol>
     *   <li>{@code resources/background-{base}.png}（武器槽1）</li>
     *   <li>{@code resources/background2-{base}.png}（武器槽2）</li>
     *   <li>找不到时降级使用程序自动绘制的纯色网格背景</li>
     * </ol>
     */
    public final String bgImageBase;

    // ──────────────────────────────────────────────────────────────────────────
    // 构造

    LayoutProfile(
            String displayName,
            int invSizeX, int invSizeY,
            int cubeSizeX, int cubeSizeY,
            int stashSizeX, int stashSizeY,
            int invCoordX, int invCoordY,
            int cubeCoordX, int cubeCoordY,
            int stashCoordX, int stashCoordY,
            int beltCoordX, int beltCoordY,
            int bgWidth, int bgHeight,
            String bgImageBase
    ) {
        this.displayName = displayName;
        this.invSizeX = invSizeX;
        this.invSizeY = invSizeY;
        this.cubeSizeX = cubeSizeX;
        this.cubeSizeY = cubeSizeY;
        this.stashSizeX = stashSizeX;
        this.stashSizeY = stashSizeY;
        this.invCoordX = invCoordX;
        this.invCoordY = invCoordY;
        this.cubeCoordX = cubeCoordX;
        this.cubeCoordY = cubeCoordY;
        this.stashCoordX = stashCoordX;
        this.stashCoordY = stashCoordY;
        this.beltCoordX = beltCoordX;
        this.beltCoordY = beltCoordY;
        this.bgWidth = bgWidth;
        this.bgHeight = bgHeight;
        this.bgImageBase = bgImageBase;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 工具方法

    /** 获取武器槽1背景图文件名 */
    public String getBg1ImageName() {
        return "background-" + bgImageBase + ".png";
    }

    /** 获取武器槽2背景图文件名 */
    public String getBg2ImageName() {
        return "background2-" + bgImageBase + ".png";
    }

    /** 返回显示名称（供 JComboBox 自动调用 toString） */
    @Override
    public String toString() {
        return displayName;
    }

    /**
     * 从 properties 键名解析 LayoutProfile，找不到时返回 {@link #BIG_STASH}（默认）。
     */
    public static LayoutProfile fromName(String name) {
        if (name == null) return BIG_STASH;
        try {
            return LayoutProfile.valueOf(name);
        } catch (IllegalArgumentException e) {
            return BIG_STASH;
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 全局 UI 缩放与渲染模式

    /**
     * 全局 UI 缩放因子。
     * <ul>
     *   <li>1.0f — 标准 1080p（默认）</li>
     *   <li>2.0f — 4K HiDPI，所有坐标和字号乘以 2</li>
     * </ul>
     * 在程序启动时（加载配置后、显示窗口前）设置一次，全局生效。
     */
    public static float scale = 2.0f;

    /**
     * 是否使用程序化矢量背景绘制。
     * <ul>
     *   <li>true（默认）— 完全由 Java2D 绘制，支持任意缩放，无需 PNG 资源文件</li>
     *   <li>false — 尝试加载 resources/ 下的 PNG 背景图（兼容旧模式）</li>
     * </ul>
     */
    public static boolean proceduralBackground = true;

    /**
     * 将基准像素坐标（1x）乘以全局缩放因子，返回实际渲染像素值。
     * 示例：{@code s(28)} 在 1x 下返回 28，在 2x（4K）下返回 56。
     */
    public static int s(int basePx) {
        return Math.round(basePx * scale);
    }
}
