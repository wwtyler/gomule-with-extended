# GoMule × D2RMM 集成指南

> 本文档沉淀 GoMule 与 D2RMM mod 生态集成的设计、实现与坑点。
> 后续修改 GoMule 布局/Sprite/材料 tab 相关功能前必读。

---

## 1. 工作空间角色（必须先理解）

| 目录 | 路径 | 角色 |
|---|---|---|
| **mods/** | `D:\260305\D2RMM 1.9.0\mods` | D2RMM 管理的 mod 源代码（数十个 mod.js / config.json）。**所有 mod 编辑工作的真实源** |
| **d2rmm/** | `D:\260305\d2rmm` | D2RMM 应用源码（Electron + React mod 管理器） |
| **GoMule/** | `D:\260305\gomule-with-extended-stash-size\GoMule` | 本项目，Java Swing 角色/物品存档管理工具 |
| **D2RMMMDKV3.mpq/** | `D:\BlizGames\Diablo II Resurrected\mods\D2RMMMDKV3\D2RMMMDKV3.mpq` | **D2RMM 编译部署目标**：所有 mods 经 D2RMM 处理后的产物，游戏从这里加载 |

### 仅作参考的目录（不要默认使用）
- `MDK.mpq/`、`YTE.mpq/` — 独立 mod，仅资源/实现参考。**任何任务都不应优先把它们当成模板或第一参考**
- `data/`（Ladiks Casc Viewer）— 原版游戏数据快照，仅用于对比/查阅原版字段

### 数据流
```
mods/(*)/mod.js  ──D2RMM运行──►  D2RMMMDKV3.mpq/data/...  ──游戏加载──► 玩家
                                          │
                                          └──GoMule读取──► 渲染 sprite / tooltip
```

GoMule 读取的资源（sprite、翻译、布局参数）应优先指向 `D2RMMMDKV3.mpq/data/`，与玩家实际游戏体验保持一致。

---

## 2. LayoutProfile — 多 mod 布局适配

[src/main/java/gomule/gui/LayoutProfile.java](src/main/java/gomule/gui/LayoutProfile.java)

### 设计目的
不同的 D2RMM mod 组合会改变背包/魔盒/仓库尺寸。`LayoutProfile` 用枚举把"网格尺寸 + 像素坐标 + 背景图"打包成可切换的预设，避免散落在 GUI 代码里的硬编码常量。

### 现有 Profile
| Profile | 背包 | 魔盒 | 仓库 | 对应 mod |
|---|---|---|---|---|
| `NATIVE` | 10×4 | 3×4 | 10×10 | 原生（无 mod） |
| `BIG_STASH` | 10×4 | 6×4 | 16×13 | (1.1)BigStash + (1.2)BigCube |
| `BIG_STASH_INV_10x8` | 10×8 | 6×4 | 16×13 | + (1.3)BigInventory |
| `BIG_STASH_INV_XL_13x8` | 13×8 | 6×4 | 16×13 | + (1.4)BigInventoryXL |

### 关键字段（全部 1x 基准像素，渲染时乘 `scale`）
- 网格尺寸：`invSizeX/Y`、`cubeSizeX/Y`、`stashSizeX/Y`
- 像素坐标：`invCoordX/Y`、`cubeCoordX/Y`、`stashCoordX/Y`、`beltCoordX/Y`
- 面板尺寸：`bgWidth/bgHeight`
- 背景图基础名：`bgImageBase` → `background-{base}.png` / `background2-{base}.png`

### 全局缩放与渲染模式
```java
public static float scale = 2.0f;            // 1.0 = 1080p, 2.0 = 4K
public static boolean proceduralBackground = true;  // true=Java2D绘制, false=加载PNG
public static int s(int basePx);             // 将基准像素 × scale
```

- `scale` 在启动时设置一次，全局生效
- `proceduralBackground=true` 时无需 PNG 资源，任意缩放下均无失真
- `proceduralBackground=false` 时尝试加载 `resources/background-{base}.png`，找不到自动降级到程序绘制

### 持久化
- properties key：`LayoutProfile.PROPERTY_NAME = "layout.profile"`
- 反序列化：`LayoutProfile.fromName(name)`（找不到默认 `BIG_STASH`）

### 添加新 Profile 步骤
1. 在 enum 末尾新增条目，填全 12+ 个坐标/尺寸字段
2. 若用 PNG 背景：在 `resources/` 放 `background-{base}.png`（武器槽1）和 `background2-{base}.png`（武器槽2）
3. 若用程序绘制：保持 `proceduralBackground=true`，无需图
4. 注意：左列（背包/设备）X<326，右列（仓库/魔盒）X≥326，避免重叠；背包延伸不会撞到魔盒

---

## 3. SpriteParser — D2R .sprite 文件解析

[src/main/java/gomule/gui/SpriteParser.java](src/main/java/gomule/gui/SpriteParser.java)

### 当前能力
- **仅支持 version=31（raw RGBA 无压缩）** 的 .sprite 文件
- 直接读为 `BufferedImage(TYPE_INT_ARGB)`

### 文件格式（已实现部分）
| Offset | 类型 | 含义 |
|---|---|---|
| 0x04 | uint16 LE | version（必须 = 31） |
| 0x08 | int32 LE | width |
| 0x0C | int32 LE | height |
| 0x28 | RGBA[w*h] | 像素数据（每像素 R,G,B,A 各 1 字节） |

### 限制
- DXT5 压缩（version=61）等其他格式 → 返回 `null`
- 任何错误（文件不存在、长度不足、宽高非法）均静默返回 `null`，由调用方决定回退策略

### 后续扩展点
若需要 DXT5 解码，需要实现块解压（4×4 像素块、64-bit color block + 64-bit alpha block）。可参考 D2RMM 工具链或开源 DXT 解码实现。

---

## 4. D2SpriteCache — Sprite 查找与缓存

[src/main/java/gomule/gui/D2SpriteCache.java](src/main/java/gomule/gui/D2SpriteCache.java)

### 职责
按 `itemCode` 查找并缓存 D2R 物品 sprite 图像，供材料 tab、tooltip 等地方使用。

### 查找链路
```
itemCode ──► items.json (assetMap) ──► asset string ──► resolveCategory
                                                            │
                                                            ▼
            hd/global/ui/items/{category}/{asset}.lowend.sprite
                                                            │
                                                            ▼
                                  在 dataDirs 列表中按顺序找文件
                                                            │
                                                            ▼
                                              SpriteParser.parse()
```

- `assetMap` 来自 `hd/items/items.json`（D2R 标准索引文件）
- `category` 由 `randall.d2files.D2TxtFile`（misc/armor/weapon TSV）反推
- 缓存：`itemCode → BufferedImage` 或 `SENTINEL_NOT_FOUND`（避免重复磁盘 IO）

### 数据目录配置
- properties key：`sprite.data.dirs`（分号分隔，按顺序尝试）
- 默认：
  1. `D:\BlizGames\Diablo II Resurrected\mods\D2RMMMDKV3\D2RMMMDKV3.mpq\data`
  2. `D:\260305\D2RM_Ladiks Casc Viewer\Work\data\data`

### 公共 API
| 方法 | 用途 |
|---|---|
| `getInstance()` | 单例（首次访问读配置） |
| `reset()` | 配置变更后强制重建（清缓存） |
| `getImage(itemCode)` | 取图（带缓存，找不到返回 null） |
| `getDataDirs()` | 当前生效的数据目录列表 |
| `setDataDirs(List<String>)` | 修改并持久化 |

### 渲染回退策略（`drawMatSlot` 等）
1. 优先 `D2SpriteCache.getImage(code)` → D2R .sprite
2. 回退到 GoMule 旧的 DC6 资源（兼容老存档/无 D2R 安装的场景）

---

## 5. 材料 Tab Tooltip（已完成）

- `src/main/java/gomule/gui/sharedStash/SharedStashPanel.java`
  - 新增 `getMatItemAt(int px, int py)`：根据像素定位材料 tab 中的物品
- `src/main/java/gomule/gui/sharedStash/SharedStashMouseMotionListener.java`
  - `mouseMoved()` 增加材料 tab 分支，触发 tooltip 显示

---

## 6. 编译 / 运行

```powershell
# 增量编译
.\gradlew.bat compileJava -x test

# 完整构建分发版
.\gradlew.bat createDistribution copyResourcesToDistribution copyJarToDistribution -x test

# 运行
cd build\tmp\distribution\GoMule
D:\Code\jdk-21.0.9\bin\java.exe -jar GoMule.jar
```

---

## 7. 已知待办（暂搁置）

- **词缀 tooltip 颜色集成** — 已确认 D2R `ÿcX` 颜色由 D2RMM `(3.1)ColorMod` 在 install 时**运行时注入** lng 文件，外部 `D2RMMMDKV3.mpq` 的物理 `item-modifiers.json` 可能无颜色（取决于用户是否启用并 install 过 ColorMod）。该问题涉及多源翻译合并、颜色覆盖优先级等复杂决策，**暂时搁置后续再完善**。

---

## 8. 修改注意事项

1. **不要把 MDK 当成第一参考**——MDK/YTE 是独立 mod，不是当前 mod 集合的一部分
2. **新增 LayoutProfile 时坐标必须避开重叠**——左列 X<326、右列 X≥326 是经验分割线
3. **`D2SpriteCache.reset()` 必须在切换数据目录后调用**，否则旧缓存遮蔽新文件
4. **SpriteParser 仅支持 RGBA 格式**——遇到 DXT5 文件不要静默忽略，需要时再实现
5. **`LayoutProfile.scale` 启动后不要中途修改**——已有面板的坐标已按当时 scale 计算
