# GoMule UI 布局与字体自适应规范

## 背景

GoMule 使用 Java Swing，通过 `D2UI.getUiScale()` 支持全局 UI 缩放（例如 1.8×）。  
大量历史代码使用了固定像素尺寸（`setSize` / `setPreferredSize` / `setMaximumSize` / `setMinimumSize`），  
当字体/缩放比例提高时，内容被裁剪或溢出，导致布局破碎。

---

## 一、核心问题：四类固定尺寸 API

### 破坏性从高到低：

| API | 使用场景 | 影响 |
|-----|---------|------|
| `setMaximumSize(w, h)` | BoxLayout Y_AXIS 子组件 | **最严重** — 组件高度/宽度被硬性封顶，字体增大后内容被截断 |
| `setMinimumSize(w, h)` | 对话框、面板 | 阻止缩小，通常影响较小 |
| `setPreferredSize(w, h)` | 普通面板 | LayoutManager 会优先采用，高度硬编码后内容显示不全 |
| `setSize(w, h)` | JInternalFrame、对话框 | 直接覆盖窗口大小，阻止 `pack()` 生效 |

---

## 二、修复规范

### 2.1 BoxLayout Y_AXIS 子组件（最常见问题）

**问题模式（删除）：**
```java
panel.setPreferredSize(new Dimension(190, 160));
panel.setSize(new Dimension(190, 160));
panel.setMaximumSize(new Dimension(190, 160));   // ← 最毒
panel.setMinimumSize(new Dimension(190, 160));
```

**修复方式：**
```java
// 只设置 max，高度用 Short.MAX_VALUE，宽度用 Integer.MAX_VALUE 允许随面板伸缩
panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Short.MAX_VALUE));
```

- 宽度写 `Integer.MAX_VALUE`：让组件横向填满容器（取消 190 的宽度硬限制）
- 高度写 `Short.MAX_VALUE`：让组件纵向自由伸展
- 不需要 `setPreferredSize` / `setMinimumSize` / `setSize` — 删除

### 2.2 GridBagLayout 面板（RandallPanel）

`RandallPanel` 是对 `GridBagLayout` 的封装，`addToPanel` 的第 5 个参数控制 fill：

| 常量 | 含义 |
|------|------|
| `RandallPanel.NONE` | 不拉伸，组件保持固有大小 |
| `RandallPanel.HORIZONTAL` | 横向填满所在列 |
| `RandallPanel.VERTICAL` | 纵向填满所在行 |
| `RandallPanel.BOTH` | 双向填满 |

**规范：**
- 按钮行通常用 `HORIZONTAL`（让按钮横向撑满面板）
- 树形视图/列表用 `BOTH`（同时纵横扩展）
- 仅当确实需要固定大小时才用 `NONE`

### 2.3 对话框（JDialog / JFrame）

**错误模式：**
```java
setBounds(100, 100, 600, 450);  // 或 setSize(514, 500)
```

**正确做法：**
```java
setMinimumSize(new java.awt.Dimension(600, 450));  // 设置最小尺寸（可选）
pack();                                             // 自动计算尺寸
setLocationRelativeTo(parentComponent);             // 居中于父窗口
```

### 2.4 JSplitPane 两侧面板

JSplitPane 通过 `setDividerLocation(n)` 控制初始分隔位置，两侧面板**不需要**也**不应该**设置固定宽度。

**错误模式：**
```java
lPane.setPreferredSize(new Dimension(257, 100));      // 左侧列表
lItemPanel.setPreferredSize(new Dimension(250, 100)); // 右侧详情
```

直接删除即可，分隔线位置已由 `setDividerLocation` 控制。

### 2.5 图片预览标签（JLabel + ImageIcon）

图片标签高度可以保留（图片有自然比例），但宽度需要放开以随面板伸缩：

```java
// 之前（错误）
iIconLabel.setMaximumSize(new Dimension(190, 112));

// 修复后
iIconLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 112));
iIconLabel.setMinimumSize(new Dimension(0, 112));
```

---

## 三、ScaledPainterPanel 绘制模糊问题

### 原因

`ScaledPainterPanel` 在 `build()` 阶段将内容绘制到 native 分辨率的 `BufferedImage` 缓冲区，  
再在 `paint()` 中调用 `drawScaled()` 放大 `uiScale` 倍（如 1.8×）。  
若文字/图标也在 `build()` 阶段画入缓冲区，放大后会产生模糊。

### 修复：延迟绘制（Deferred Draw）

将需要清晰渲染的文字改为在 `paint()` 阶段直接画到屏幕坐标系：

```java
// build() 阶段：存储数据，不绘制文字
private String goldValueStr = "";
// ... 在 placeItemsInView() 中：
goldValueStr = Long.toString(pane.getGold());

// paint() 阶段：drawScaled() 之后，用屏幕坐标渲染
@Override
public void paint(Graphics g) {
    super.paint(g);  // 内部调用 drawScaled()
    Graphics2D g2 = (Graphics2D) g;
    float s = D2UI.getUiScale();
    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g2.setFont(new Font("Dialog", Font.PLAIN, (int)(11 * s)));
    // 左对齐 "Gold:"，右对齐金额数值
    g2.drawString("Gold:", (int)(25 * s), (int)(yPos * s));
    // ... 右对齐逻辑
}
```

**关键规则：**
- `build()` 中只做布局计算和数据存储
- `paint()` 中在 `drawScaled()` 之后用 `fontSize × uiScale` 和 `TEXT_ANTIALIAS_ON` 绘制文字
- 坐标使用 `nativeCoord × uiScale` 转换到屏幕坐标

---

## 四、窗口分隔线与 ResizeWeight

主窗口用双层 `JSplitPane` 实现三栏布局：

```java
lSplit = new JSplitPane(HORIZONTAL_SPLIT, iLeftPane, iDesktopPane);
rSplit = new JSplitPane(HORIZONTAL_SPLIT, lSplit, iRightPane);
rSplit.setResizeWeight(0.85);  // 窗口拉大时，85% 空间给中间区域，15% 给右侧
```

- `setResizeWeight(1.0)` 表示所有额外空间给左边，右侧不随窗口变化 — **不推荐**
- `setResizeWeight(0.85)` 让右侧面板也能适度随窗口增长

---

## 五、已修改的文件清单

| 文件 | 修改内容 |
|------|---------|
| `gui/D2FileManager.java` | 删除左侧导航栏硬编码高度；右侧 itemControl/charControl 改为 `Integer.MAX_VALUE`；`projControl` 填充模式改为 `HORIZONTAL`；`rSplit.setResizeWeight` 改为 0.85 |
| `gui/D2ViewClipboard.java` | clipboard panel 和 lPane 宽度改为 `Integer.MAX_VALUE`；iIconLabel 宽度放开，高度保留 112 |
| `gui/D2ViewStash.java` | 删除 lPane/lItemPanel 的 setPreferredSize；删除 `setSize(514, 500)` |
| `gui/D2ProjectSettingsDialog.java` | 替换 `setBounds(100,100,600,450)` 为 `setMinimumSize` + `pack()` + `setLocationRelativeTo()` |
| `gui/sharedStash/SharedStashPanel.java` | Gold 标签和数值从 build() 移到 paint() 延迟渲染，解决模糊 |
| `gui/sharedStash/SharedStashGoldTransferPanel.java` | 删除硬编码 `setSize(300, 100)` 和 `setPreferredSize(300, 100)` |
| `gui/desktop/frames/GoMuleFrameDesktop.java` | 删除 Stash 视图的 `setSize(514, 500)` 覆盖 |
