# D2R / RotW 中 `.d2s` 存档格式关键内容整理

## 1. 结论概览

截至公开资料与常见社区实现来看，**D2R 在最新版本及 RotW 一类模组环境中，并没有把 `.d2s` 改造成全新的编码体系**。其变化更接近于：

- **在经典 D2 的二进制存档结构上继续扩展**
- **保留 bitstream / bit-packed 物品与属性编码方式**
- **加强完整性校验**，但**不是新加密算法**
- **新增或扩展字段**，尤其体现在 header、item flags、属性集合和版本分支处理上

一句话总结：

> D2R 对 `.d2s` 的升级本质上是“字段扩展 + 校验加强 + 版本演进”，而不是“编码体系重做”。

---

## 2. 关于 D2R / RotW 对 `.d2s` 的变化判断

### 2.1 没有发现“全新编码升级”

从公开资料和社区解析器的实现习惯看，`.d2s` 仍然是：

- **little-endian 二进制结构**
- 多个块按顺序排列
- 关键数据区（尤其物品与属性）仍然通过 **bit-level** 方式编码

未见以下特征：

- AES / RSA / 自定义密文封装
- protobuf / json / msgpack 这类全新序列化体系
- 用一种完全不同的容器格式替代旧 d2s 结构

### 2.2 D2R 的主要变化点

更合理的描述是：

1. **Version 字段演进**
   - 存档头里版本字段有变化
   - 解析器需要根据版本做分支处理

2. **Header 中新增字段 / padding / 扩展区**
   - 角色相关元信息和控制标记可能增加
   - 某些区域长度比经典 D2 更大

3. **Item / Stat 数据集合扩展**
   - 增加新的 stat、flag、affix 或与新版内容兼容的字段
   - 某些字段所占 bit 长度可能变化

4. **校验更严格**
   - 仍有 checksum 概念
   - 修改存档后通常需要重算
   - 不属于真正加密，只是完整性验证更严格

### 2.3 RotW 的影响

如果这里的 RotW 是建立在 D2R 之上的模组环境，那么通常：

- **RotW 不会定义一套新的 `.d2s` 文件编码协议**
- 它更常见的是：
  - 改动数据表
  - 扩展 item / affix / skill 逻辑
  - 影响可写入存档的内容范围

因此更准确地说：

> 模组会影响“存进去什么”，但通常不重写“存档文件怎么编码”。

---

## 3. 结合 D2RMM / 社区实现的读取逻辑理解

虽然 D2RMM 的主要职责并不是存档编辑，但社区常见第三方解析逻辑普遍说明：

### 3.1 读取逻辑的总体模式

典型流程是：

1. **读取 header**
2. **验证 checksum / 版本 / 文件长度**
3. **顺序解析各个块**
4. 对属性和物品等区域使用 **BitReader** 按位读取

### 3.2 核心实现特征

常见实现中会有类似逻辑：

- `readUInt32LE(...)` 读取头部字段
- `calcChecksum(...)` 计算校验值
- `readBits(n)` 读取变长 bitstream
- 按 tag 或顺序解析：
  - stats
  - skills
  - items
  - corpse
  - mercenary
  - golem

### 3.3 可以推导出的结论

这些实现共同指向一个结论：

- **D2R 没有换掉经典 d2s 的 bitstream 解析思想**
- 新版主要是：
  - 新字段
  - 新 stat
  - 新 item flag
  - 新版本兼容分支

而不是一套新的编码协议。

---

## 4. `.d2s` 二进制结构图（概览）

下面是一个便于逆向和实现解析器的结构总览。

```text
+---------------------------+
| Header (固定长度 ~0x2FD)  |
+---------------------------+
| Character Stats Block     |
+---------------------------+
| Skills Block              |
+---------------------------+
| Items Block (bitstream)   |
+---------------------------+
| Corpse Items Block        |
+---------------------------+
| Mercenary Block           |
+---------------------------+
| Golem Block (NEC only)    |
+---------------------------+
| Iron Golem Item Data      |
+---------------------------+
```

说明：

- Header 是以固定字段为主的区域
- 后续多个块中，**stats** 和 **items** 是最关键的 bitstream 区域
- Item 数据可递归包含 socketed items

---

## 5. Header 结构（概念图）

```text
Offset  Size   描述
----------------------------------------
0x0000  4      Checksum (uint32 LE)
0x0004  4      Version
0x0008  4      File Size
0x000C  4      Unknown / flags

0x0010  16     Character Name (ASCII)

0x0020  1      Class
0x0021  1      Level
0x0022  1      Status flags
0x0023  1      Progression

0x0024  4      Timestamp

0x0028  ...    Quest flags
0x00A8  ...    Waypoints
0x0120  ...    NPC intro flags

0x0160  ...    Difficulty flags

0x02A0  ...    Merc info

0x02FD  --     Header 结束（典型 D2R）
```

注意：

- 这是用于理解的结构图，不应被视为绝对逐字节标准
- D2R 相对经典 D2，常见表现是：
  - padding 更多
  - 某些区段更长
  - 版本分支更多

---

## 6. Stats Block

Stats Block 常以 `gf` 开头：

```text
+--------+----------------------+
| "gf"   | stats bitstream      |
+--------+----------------------+
```

其内容通常是：

```text
[stat_id][value]
[stat_id][value]
...
[0x1FF 终止]
```

关键点：

- `stat_id` 通常按 **9 bits** 读取
- `value` 的 bit 长度取决于该 stat 的定义
- 结束标志通常是：
  - `0x1FF`（即 511）

---

## 7. Skills Block

Skills Block 常以 `if` 开头：

```text
+--------+------------------+
| "if"   | 30 bytes skills  |
+--------+------------------+
```

特点：

- 每个技能通常占 1 byte
- 顺序固定，和职业技能顺序有关

---

## 8. Items Block（核心区）

Items Block 常以 `JM` 开头：

```text
+--------+----------------------+
| "JM"   | item bitstream       |
+--------+----------------------+
```

每个 item 可以抽象为：

```text
[Item Header Bits]
[Item Core Data]
[Extended Data (if exists)]
[Socketed Items...]
```

### 8.1 Item Header 的典型标志位

常见逻辑包括：

```text
1 bit   Identified
1 bit   Socketed
1 bit   New
1 bit   IsEar
1 bit   StarterItem

1 bit   SimpleItem
1 bit   Ethereal
1 bit   Personalized
1 bit   Gamble

...（D2R 可能增加新 flag 或保留位）
```

### 8.2 Item 基础字段

可概念化为：

```text
[version?]
[location]
[position x/y]
[container]
[item code] (4 chars → 32 bits)
```

### 8.3 Quality 分支

不同品质会触发不同读取逻辑：

```text
if quality == magic:
    prefix + suffix

if rare:
    rare name + affixes

if unique:
    unique id

if set:
    set id
```

### 8.4 Item 属性列表

物品扩展属性本身也是一个 bitstream：

```text
[stat_id][value]
...
[0x1FF end]
```

### 8.5 镶嵌物品（递归）

```text
[socket_count]

for each socket:
    parse item（递归）
```

这也是 item 区最容易错位的部分之一。

---

## 9. Corpse / Mercenary / Golem 相关块

### 9.1 Corpse Items

```text
+--------+----------------------+
| "JM"   | corpse item stream   |
+--------+----------------------+
```

- 结构与普通 item block 类似
- 可能存在多个尸体数据段

### 9.2 Mercenary Block

```text
+--------+------------------+
| "jf"   | merc data        |
+--------+------------------+
```

通常包含：

```text
[merc id]
[merc name]
[merc type]
[merc exp]

+ merc items ("JM" block)
```

### 9.3 Iron Golem

```text
+--------+------------------+
| "kf"   | golem flag       |
+--------+------------------+
```

如存在傀儡物品：

```text
+--------+------------------+
| "JM"   | golem item       |
+--------+------------------+
```

---

## 10. `stat_id` 编码规则

这一部分对写解析器最关键。

### 10.1 基本规则

在 stats 或 item 属性流中，通常采用：

```text
stat_id  -> 9 bits
value    -> 变长，由 stat 定义
```

### 10.2 value 的来源

`value` 的位宽不固定，通常由数据定义控制。社区实现通常会参考类似 `ItemStatCost.txt` 中的信息，例如：

- `Save Bits`
- `Save Add`

### 10.3 解码公式

```text
real_value = raw_value - SaveAdd
```

因此解析时不能只知道 `stat_id` 名称，还要知道：

- 该 stat 需要读多少 bit
- 是否有偏移量要减回去

### 10.4 结束标志

```text
stat_id = 0x1FF (511)
```

表示当前属性列表结束。

---

## 11. 常用 `stat_id` 映射表（核心子集）

完整表会非常长，这里只保留逆向和工具开发中最常碰到的部分。

### 11.1 基础属性

| stat_id | 名称 |
|---|---|
| 0 | strength |
| 1 | energy |
| 2 | dexterity |
| 3 | vitality |
| 6 | life |
| 7 | max_life |
| 8 | mana |
| 9 | max_mana |
| 10 | stamina |
| 11 | max_stamina |
| 12 | level |

### 11.2 战斗属性

| stat_id | 名称 |
|---|---|
| 16 | defense |
| 17 | chance_to_block |
| 19 | min_damage |
| 20 | max_damage |
| 21 | second_min_damage |
| 22 | second_max_damage |
| 23 | damage_percent |
| 24 | attack_rating |
| 25 | attack_rating_percent |
| 26 | block_rate |
| 27 | faster_run_walk |
| 28 | faster_attack_rate |
| 29 | faster_hit_recovery |
| 30 | faster_block_rate |

### 11.3 抗性

| stat_id | 名称 |
|---|---|
| 39 | fire_resist |
| 40 | max_fire_resist |
| 41 | lightning_resist |
| 42 | max_lightning_resist |
| 43 | cold_resist |
| 44 | max_cold_resist |
| 45 | poison_resist |
| 46 | max_poison_resist |

### 11.4 元素伤害

| stat_id | 名称 |
|---|---|
| 48 | fire_min_damage |
| 49 | fire_max_damage |
| 50 | lightning_min_damage |
| 51 | lightning_max_damage |
| 52 | magic_min_damage |
| 53 | magic_max_damage |
| 54 | cold_min_damage |
| 55 | cold_max_damage |
| 56 | cold_length |
| 57 | poison_min_damage |
| 58 | poison_max_damage |
| 59 | poison_length |

### 11.5 吸取与恢复

| stat_id | 名称 |
|---|---|
| 60 | life_leech |
| 62 | mana_leech |
| 74 | mana_recovery |
| 75 | mana_recovery_bonus |
| 76 | stamina_recovery_bonus |

### 11.6 掉宝与经济类

| stat_id | 名称 |
|---|---|
| 79 | gold_find |
| 80 | magic_find |
| 81 | reduced_vendor_prices |

### 11.7 减伤与抗性补充

| stat_id | 名称 |
|---|---|
| 34 | damage_reduction |
| 35 | magic_damage_reduction |
| 36 | damage_reduction_percent |
| 37 | magic_resist_percent |

### 11.8 特殊效果

| stat_id | 名称 |
|---|---|
| 83 | knockback |
| 84 | fire_absorb |
| 85 | lightning_absorb |
| 86 | magic_absorb |
| 87 | cold_absorb |

### 11.9 技能相关

| stat_id | 名称 |
|---|---|
| 97 | all_skills |
| 107 | skill_on_attack |
| 108 | skill_on_kill |
| 109 | skill_on_death |
| 110 | skill_on_hit |
| 111 | skill_on_level_up |
| 112 | skill_on_get_hit |
| 127 | class_skills |
| 188 | skill_tab |
| 195 | single_skill |

### 11.10 物品耐久 / 数量 / 回复

| stat_id | 名称 |
|---|---|
| 140 | extra_charges |
| 141 | quantity |
| 142 | durability |
| 143 | max_durability |
| 144 | replenish_life |
| 145 | replenish_quantity |

### 11.11 常见终局属性

| stat_id | 名称 |
|---|---|
| 153 | cannot_be_frozen |
| 154 | pierce |
| 155 | open_wounds |
| 156 | crushing_blow |
| 157 | deadly_strike |

---

## 12. 典型 Save Bits 示例

实际 bit 长度要以具体版本定义为准，但以下是常见近似示例：

| 名称 | 典型位宽 |
|---|---|
| strength | 10 bits |
| dexterity | 10 bits |
| vitality | 10 bits |
| life | 21 bits |
| mana | 21 bits |
| fire_resist | 8 bits |
| lightning_resist | 8 bits |
| enhanced_damage | 9 bits |
| attack_rating | 10 bits |
| magic_find | 9 bits |
| gold_find | 9 bits |

说明：

- 这部分适合用来理解解析模型
- 真正写程序时应尽量由数据表驱动，而不是把所有位宽硬编码死

---

## 13. 特殊 stat 的结构化读取

并非所有 stat 都是单一 `value`。

### 13.1 技能触发类

例如 `skill_on_attack` 一类，往往不是简单的一个数值，而是组合结构：

```text
[level]
[skill_id]
[chance]
```

示意：

```text
[level]   (6 bits)
[skill_id](10 bits)
[chance]  (7 bits)
```

### 13.2 Charges

充能类属性通常包含：

```text
[skill_id]
[level]
[current charges]
[max charges]
```

### 13.3 Poison / Cold 等持续类属性

这类往往不是单值，而是多个值组合：

```text
[min]
[max]
[length]
```

因此解析器若把所有 stat 都当成 `readBits(bits)` 一次读取，就会在这些地方错位。

---

## 14. D2R 相比旧版的关键差异点

### 14.1 `stat_id` 范围可能扩大

旧版认知常停留在较小范围内，但 D2R 的内容扩展意味着：

- `stat_id` 可超出旧工具的预设范围
- 旧解析器若表不完整，容易把后续 bitstream 读歪

### 14.2 Save Bits / Save Add 可能变化

某些 stat 的位宽或解释方式可能调整，导致：

- 旧工具读取值异常
- 解析器在某个字段后整体错位

### 14.3 Item flag / 新属性 / 版本分支

D2R 更需要：

- 按版本处理 header
- 按版本处理 item 扩展结构
- 对未知 flag 保持谨慎，不要简单假设其不存在

---

## 15. 校验与写回的注意事项

修改 `.d2s` 后，通常要：

1. 正确回写所有 bitstream
2. 保持字段对齐关系正确
3. 重新计算 checksum
4. 修正文件长度或相关头部字段（若实现要求）

如果只改动某个值而不重算校验，常见结果是：

- 游戏拒绝读取
- 存档损坏
- 编辑器能读，游戏不能读

---

## 16. 实用解析流程（伪代码）

### 16.1 解析属性流

```text
while true:
    stat_id = readBits(9)
    if stat_id == 0x1FF:
        break

    meta = lookupStatMeta(stat_id)
    value = readBits(meta.saveBits)
    real_value = value - meta.saveAdd
```

### 16.2 更稳妥的版本

```text
while true:
    stat_id = readBits(9)
    if stat_id == 0x1FF:
        break

    meta = lookupStatMeta(stat_id)

    if meta.encodingType == "simple":
        raw = readBits(meta.saveBits)
        emit(raw - meta.saveAdd)
    else:
        emit(parseComplexStat(meta))
```

### 16.3 解析 item 的简化流程

```text
read item flags
read location / position / container
read item code

if not simple item:
    read quality-specific fields
    read magical properties until stat_id == 0x1FF

if socketed:
    recursively parse child items
```

---

## 17. 对工具开发的建议

如果要自己做读取器/编辑器，推荐路线是：

### 17.1 用“数据驱动”代替“纯硬编码”

应尽量根据类似 `ItemStatCost` 的元数据来决定：

- `stat_id` 名称
- `saveBits`
- `saveAdd`
- 是否是复杂结构属性

### 17.2 把 item 解析器设计成递归结构

因为 socketed items、corpse items、merc items、golem items 都会复用同类逻辑。

### 17.3 对 D2R 做版本分支

建议至少把这三层分开：

- header 解析版本分支
- item flags 版本分支
- stat 表版本分支

### 17.4 修改写回后一定做回读验证

流程最好是：

1. 读取原始文件
2. 写回修改
3. 再次用同一个解析器读回
4. 比较对象结构是否一致
5. 最后再交给游戏测试

---

## 18. 最终总结

### 18.1 关于“编码升级”的判断

更准确的说法是：

- **不是彻底更换编码体系**
- **而是在旧 d2s 结构上不断扩展字段、扩展 stat、扩展 item 逻辑并加强校验**

### 18.2 对逆向最重要的认识

`.d2s` 可以被理解为：

```text
[固定头]
+ 多个 tag / block
+ 若干 bitstream（stats / items）
+ 递归 item 树
```

### 18.3 对实现最重要的要求

要正确解析 D2R 存档，至少需要：

- 稳定的 BitReader
- 正确的 `stat_id -> 元数据` 映射
- 对复杂 stat 的专门解析逻辑
- 对 item/socket/merc/corpse/golem 的递归处理
- 正确的 checksum 重算

---

## 19. 后续可扩展方向

如果还要继续深入，下一步最有价值的是：

1. **整理完整 `stat_id -> SaveBits -> SaveAdd` 表**
2. **对照版本差异补全 D2R 新增 stat**
3. **按社区实现写一个最小可用解析器（Python / JS）**
4. **补充 checksum 精确算法与样例代码**
5. **补 item header 每一位 flag 的逐位说明**

---

## 20. 备注

本文档基于前面对话中的整理结果，定位是：

- 帮你快速把握 D2R `.d2s` 的结构模型
- 方便后续继续做逆向、解析器、编辑器或数据核对

如果后续需要，可以在此基础上继续扩展成：

- 完整 stat 表文档
- 二进制字段参考手册
- Python / JS 解析器示例工程说明
