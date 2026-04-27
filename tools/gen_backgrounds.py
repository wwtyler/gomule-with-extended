"""
GoMule 布局背景图生成脚本
=========================
根据 LayoutProfile 坐标，从现有 background.png / background-16x13.png 中提取
网格单元格纹理，并为所有 4 种 Layout 重新合成正确的背景图。

输出文件（放到 resources/ 目录）：
  background.png           — NATIVE（原生，不变）
  background2.png          — NATIVE weapon-slot2（不变）
  background-16x13.png     — BIG_STASH（修复魔盒/腰带区域）
  background2-16x13.png    — BIG_STASH weapon-slot2
  background-16x13-inv10x8.png    — BIG_STASH_INV_10x8
  background2-16x13-inv10x8.png
  background-16x13-inv13x8.png    — BIG_STASH_INV_XL_13x8
  background2-16x13-inv13x8.png

使用方法：
  cd .../GoMule
  python tools/gen_backgrounds.py
"""

from pathlib import Path
from PIL import Image, ImageFilter
import sys

# ─────────────────────────────────────────────
# 路径
# ─────────────────────────────────────────────
SCRIPT_DIR = Path(__file__).parent
RESOURCES   = SCRIPT_DIR.parent / "build" / "tmp" / "distribution" / "GoMule" / "resources"
# 同时输出到 src 目录（若存在）
SRC_RESOURCES = SCRIPT_DIR.parent / "src" / "main" / "resources" / "backgrounds"

SRC_NATIVE    = RESOURCES / "background.png"
SRC_NATIVE2   = RESOURCES / "background2.png"
SRC_16x13     = RESOURCES / "background-16x13.png"
SRC_16x132    = RESOURCES / "background2-16x13.png"

# ─────────────────────────────────────────────
# GoMule 网格常量
# ─────────────────────────────────────────────
GRID_SIZE    = 28   # 单格像素尺寸
GRID_SPACER  = 1    # 格间距
CELL_PITCH   = GRID_SIZE + GRID_SPACER   # 29

# 网格外框偏移：从 item-draw 坐标到视觉外框左上角
OUTER_BORDER = 3

# ─────────────────────────────────────────────
# LayoutProfile 坐标（与 LayoutProfile.java 保持一致）
# ─────────────────────────────────────────────
LAYOUTS = {
    "NATIVE": dict(
        inv=(10, 4),  stash=(10, 10),  cube=(3, 4),  belt=(4, 4),
        inv_x=18,  inv_y=299,
        stash_x=326, stash_y=3,
        cube_x=326, cube_y=385,
        belt_x=534, belt_y=385,
        bg_w=616,  bg_h=418,
    ),
    "BIG_STASH": dict(
        inv=(10, 4),  stash=(16, 13),  cube=(6, 4),  belt=(4, 4),
        inv_x=18,  inv_y=299,
        stash_x=326, stash_y=3,
        cube_x=326, cube_y=385,
        belt_x=670, belt_y=385,
        bg_w=791,  bg_h=513,
    ),
    "BIG_STASH_INV_10x8": dict(
        inv=(10, 8),  stash=(16, 13),  cube=(6, 4),  belt=(4, 4),
        inv_x=18,  inv_y=299,
        stash_x=326, stash_y=3,
        cube_x=326, cube_y=385,
        belt_x=670, belt_y=385,
        bg_w=791,  bg_h=540,
    ),
    "BIG_STASH_INV_XL_13x8": dict(
        inv=(13, 8),  stash=(16, 13),  cube=(6, 4),  belt=(4, 4),
        inv_x=18,  inv_y=299,
        stash_x=400, stash_y=3,
        cube_x=400, cube_y=385,
        belt_x=754, belt_y=385,
        bg_w=875,  bg_h=540,
    ),
}


# ─────────────────────────────────────────────
# 辅助：从已知网格坐标中提取 N×M 网格图像
# ─────────────────────────────────────────────
def extract_grid(src: Image.Image, item_x: int, item_y: int, cols: int, rows: int) -> Image.Image:
    """
    从 src 的 (item_x, item_y) 位置提取一块 cols×rows 的网格图像。
    返回的图像包括外框（OUTER_BORDER 像素），实际尺寸：
      (OUTER_BORDER + cols*CELL_PITCH + OUTER_BORDER) × (OUTER_BORDER + rows*CELL_PITCH + OUTER_BORDER)
    如果目标区域超出图像边界，会裁剪/填充。
    """
    ox = item_x - OUTER_BORDER
    oy = item_y - OUTER_BORDER
    w  = OUTER_BORDER + cols * CELL_PITCH + OUTER_BORDER
    h  = OUTER_BORDER + rows * CELL_PITCH + OUTER_BORDER
    # 从 src 裁剪，若超出边界则以黑色填充
    return src.crop((ox, oy, ox + w, oy + h))


def paste_grid(dst: Image.Image, grid_img: Image.Image, item_x: int, item_y: int):
    """将 grid_img（含外框）粘贴到 dst 的正确位置"""
    ox = item_x - OUTER_BORDER
    oy = item_y - OUTER_BORDER
    dst.paste(grid_img, (ox, oy))


# ─────────────────────────────────────────────
# 背景填充：使用纯深色（避免 native 左侧面板的 UI 元素出现在缝隙区域）
# ─────────────────────────────────────────────
# D2 背景的深色基底 (R,G,B)
BG_FILL_COLOR = (17, 15, 13)   # 极深的暗棕灰


def fill_stone(dst: Image.Image, stone_tile: Image.Image, x1, y1, x2, y2):
    """用纯色填充 dst 的 (x1,y1,x2,y2) 区域（stone_tile 参数保留兼容性但不使用）"""
    from PIL import ImageDraw
    draw = ImageDraw.Draw(dst)
    if x2 > x1 and y2 > y1:
        draw.rectangle([x1, y1, x2 - 1, y2 - 1], fill=BG_FILL_COLOR)


# ─────────────────────────────────────────────
# 主生成函数
# ─────────────────────────────────────────────
def build_background(
    layout_name: str,
    weapon_slot: int,          # 1 or 2
    src_native: Image.Image,   # background.png (RGB)
    src_16x13: Image.Image,    # background-16x13.png (RGB)
) -> Image.Image:
    """生成指定 layout + weapon_slot 的背景图"""
    lp = LAYOUTS[layout_name]
    W, H = lp["bg_w"], lp["bg_h"]
    inv_cols, inv_rows   = lp["inv"]
    stash_cols, stash_rows = lp["stash"]
    cube_cols, cube_rows = lp["cube"]
    belt_cols, belt_rows = lp["belt"]

    # ── 1. 创建画布：用纯深色基底 ──────────────────────
    canvas = Image.new("RGB", (W, H), BG_FILL_COLOR)

    # ── 2. 粘贴左侧设备面板（宽约 316px，来自 native） ──
    LEFT_PANEL_W = 316
    # 取对应 weapon-slot 的 native 背景（两者布局相同）
    native_src = src_native  # weapon_slot 不影响面板结构，只影响武器图标
    lp_native = LAYOUTS["NATIVE"]
    # 左侧面板高度取 native 背景高度
    panel_h = min(H, src_native.height)
    left_panel = src_native.crop((0, 0, LEFT_PANEL_W, panel_h))
    canvas.paste(left_panel, (0, 0))
    # 若新背景更高，左侧面板下方已是 BG_FILL_COLOR（无需额外操作）

    # ── 3. 仓库网格（stash） ──────────────────────────
    # 以 bg-16x13 的仓库区域为模板（16×13 → 所有 BigStash 变体通用）
    # NATIVE 用 native bg 自己的仓库
    if stash_cols == 10 and stash_rows == 10:
        stash_grid = extract_grid(src_native, lp_native["stash_x"], lp_native["stash_y"], 10, 10)
    else:
        # 16×13 仓库：直接从 bg-16x13 提取
        stash_grid = extract_grid(src_16x13, LAYOUTS["BIG_STASH"]["stash_x"], LAYOUTS["BIG_STASH"]["stash_y"], 16, 13)
    paste_grid(canvas, stash_grid, lp["stash_x"], lp["stash_y"])

    # ── 4. 背包网格（inventory） ─────────────────────
    if layout_name == "NATIVE":
        inv_src = src_native
        inv_ref = lp_native
    else:
        inv_src = src_16x13
        inv_ref = LAYOUTS["BIG_STASH"]

    # 提取参考 inv（10×4）
    ref_inv_cols, ref_inv_rows = 10, 4
    ref_inv_grid = extract_grid(inv_src, inv_ref["inv_x"], inv_ref["inv_y"], ref_inv_cols, ref_inv_rows)

    # 如果需要更大的背包，从仓库区域拼出来
    if inv_cols != ref_inv_cols or inv_rows != ref_inv_rows:
        inv_grid = build_grid_from_stash(src_16x13,
                                          LAYOUTS["BIG_STASH"]["stash_x"],
                                          LAYOUTS["BIG_STASH"]["stash_y"],
                                          inv_cols, inv_rows)
    else:
        inv_grid = ref_inv_grid

    paste_grid(canvas, inv_grid, lp["inv_x"], lp["inv_y"])

    # ── 5. 魔盒网格（cube） ──────────────────────────
    # 从仓库区域提取对应大小的网格块
    if stash_cols >= cube_cols and stash_rows >= cube_rows:
        cube_grid = build_grid_from_stash(src_16x13,
                                           LAYOUTS["BIG_STASH"]["stash_x"],
                                           LAYOUTS["BIG_STASH"]["stash_y"],
                                           cube_cols, cube_rows)
    else:
        # NATIVE 3×4
        cube_grid = extract_grid(src_native, lp_native["cube_x"], lp_native["cube_y"], cube_cols, cube_rows)
    paste_grid(canvas, cube_grid, lp["cube_x"], lp["cube_y"])

    # ── 6. 腰带网格（belt） ──────────────────────────
    belt_grid = build_grid_from_stash(src_16x13,
                                       LAYOUTS["BIG_STASH"]["stash_x"],
                                       LAYOUTS["BIG_STASH"]["stash_y"],
                                       belt_cols, belt_rows)
    paste_grid(canvas, belt_grid, lp["belt_x"], lp["belt_y"])

    return canvas


def build_grid_from_stash(stash_src: Image.Image,
                           stash_item_x: int, stash_item_y: int,
                           cols: int, rows: int) -> Image.Image:
    """
    以 stash_src 的仓库网格为纹理来源，构造一块 cols×rows 的网格图像。

    策略：
    - 仓库第 0 行顶部含分页 tab 装饰（亮横条/圆圈），跳过前 SKIP_ROWS 行，
      从干净的 row SKIP_ROWS 开始提取单元格内容。
    - 外框（左/上 3px 边）从仓库实际外框区域单独采样，确保视觉一致。
    """
    SKIP_ROWS = 1  # 跳过第 0 行（含 stash tab 装饰），从 row1 开始取内容

    cell_w = cols * CELL_PITCH
    cell_h = rows * CELL_PITCH
    out_w  = OUTER_BORDER + cell_w + OUTER_BORDER
    out_h  = OUTER_BORDER + cell_h + OUTER_BORDER

    stash_ox = stash_item_x - OUTER_BORDER   # e.g. 323
    stash_oy = stash_item_y - OUTER_BORDER   # e.g. 0

    # ── 1. 单元格内容（从 row SKIP_ROWS 起，干净区域）──────────────
    cell_src_x = stash_item_x
    cell_src_y = stash_item_y + SKIP_ROWS * CELL_PITCH   # y=3+29=32
    cell_area  = stash_src.crop((cell_src_x, cell_src_y,
                                  cell_src_x + cell_w, cell_src_y + cell_h))

    # ── 2. 创建结果画布，填充深色基底 ──────────────────────────────
    result = Image.new("RGB", (out_w, out_h), (17, 17, 17))
    result.paste(cell_area, (OUTER_BORDER, OUTER_BORDER))

    # ── 3. 上外框（3px）：取自仓库顶部亮边（y=0..2，干净） ──────────
    top_frame = stash_src.crop((stash_ox, stash_oy,
                                  stash_ox + out_w, stash_oy + OUTER_BORDER))
    result.paste(top_frame, (0, 0))

    # ── 4. 左外框（3px）：取自仓库左边（x=stash_ox..+3，从 cell_src_y 起） ──
    left_frame = stash_src.crop((stash_ox, cell_src_y,
                                   stash_ox + OUTER_BORDER, cell_src_y + cell_h))
    result.paste(left_frame, (0, OUTER_BORDER))
    # 左上角补丁（top-left corner）
    tl = stash_src.crop((stash_ox, stash_oy,
                           stash_ox + OUTER_BORDER, stash_oy + OUTER_BORDER))
    result.paste(tl, (0, 0))

    # ── 5. 右外框（3px）：与左外框使用相同纹理 ─────────────────────
    result.paste(left_frame, (out_w - OUTER_BORDER, OUTER_BORDER))
    result.paste(tl, (out_w - OUTER_BORDER, 0))

    # ── 6. 下外框（3px）：优先从单元格下方采样；不足时镜像顶框 ───────
    below_y = cell_src_y + cell_h
    if below_y + OUTER_BORDER <= stash_src.height:
        bot = stash_src.crop((stash_ox, below_y,
                               stash_ox + out_w, below_y + OUTER_BORDER))
    else:
        bot = top_frame.transpose(Image.FLIP_TOP_BOTTOM)
    result.paste(bot, (0, out_h - OUTER_BORDER))

    return result


# ─────────────────────────────────────────────
# 主流程
# ─────────────────────────────────────────────
def main():
    if not SRC_NATIVE.exists():
        print(f"ERROR: {SRC_NATIVE} not found. Run Gradle 'createDistribution' first.", file=sys.stderr)
        sys.exit(1)
    if not SRC_16x13.exists():
        print(f"ERROR: {SRC_16x13} not found.", file=sys.stderr)
        sys.exit(1)

    print("Loading source images...")
    native    = Image.open(SRC_NATIVE).convert("RGB")
    native2   = Image.open(SRC_NATIVE2).convert("RGB")
    bg16x13   = Image.open(SRC_16x13).convert("RGB")
    bg16x132  = Image.open(SRC_16x132).convert("RGB")

    # 输出目录
    out_dir = RESOURCES
    out_dir.mkdir(parents=True, exist_ok=True)

    outputs = [
        # (layout_name, weapon_slot, src_native, src_16x13, out_filename)
        ("NATIVE",                1, native,  bg16x13,  "background.png"),
        ("NATIVE",                2, native2, bg16x132, "background2.png"),
        ("BIG_STASH",             1, native,  bg16x13,  "background-16x13.png"),
        ("BIG_STASH",             2, native2, bg16x132, "background2-16x13.png"),
        ("BIG_STASH_INV_10x8",    1, native,  bg16x13,  "background-16x13-inv10x8.png"),
        ("BIG_STASH_INV_10x8",    2, native2, bg16x132, "background2-16x13-inv10x8.png"),
        ("BIG_STASH_INV_XL_13x8", 1, native,  bg16x13,  "background-16x13-inv13x8.png"),
        ("BIG_STASH_INV_XL_13x8", 2, native2, bg16x132, "background2-16x13-inv13x8.png"),
    ]

    for layout_name, weapon_slot, src_n, src_s, out_name in outputs:
        print(f"  Generating {out_name} ({layout_name}, slot={weapon_slot})...")
        try:
            img = build_background(layout_name, weapon_slot, src_n, src_s)
            out_path = out_dir / out_name
            img.save(out_path, "PNG")
            print(f"    → {out_path} ({img.size[0]}×{img.size[1]})")
        except Exception as e:
            print(f"    ERROR: {e}", file=sys.stderr)
            raise

    print("\nDone.")


if __name__ == "__main__":
    main()
