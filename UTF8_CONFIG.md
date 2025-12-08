# 🎯 中文乱码问题 - 完整解决方案

## ✅ 已完成的配置

所有必要的UTF-8编码配置已应用到项目中，以解决调试控制台的中文乱码问题。

## 📋 配置清单

### 1️⃣ VS Code 项目设置
**文件**: `.vscode/settings.json`

- ✅ Java Language Server VM参数添加UTF-8
- ✅ 文件默认编码设置为UTF-8
- ✅ 启用自动编码检测

### 2️⃣ 调试配置
**文件**: `.vscode/launch.json`

所有调试配置已添加完整的UTF-8参数：
- ✅ Debug GoMule
- ✅ Debug GoMule (External Console)
- ✅ Debug Current File

### 3️⃣ Gradle 构建任务
**文件**: `build.gradle`

- ✅ `run` 任务添加UTF-8参数
- ✅ `runDebug` 任务添加UTF-8参数

### 4️⃣ VS Code 任务
**文件**: `.vscode/tasks.json`

- ✅ 添加"Set UTF-8 Encoding"任务
- ✅ 运行任务自动设置UTF-8编码

### 5️⃣ PowerShell 脚本
**文件**: `set-utf8.ps1`

- ✅ 手动设置PowerShell UTF-8编码脚本

## 🚀 使用方法

### 方法一：直接调试（推荐）✨

1. 按 `F5` 启动调试
2. 中文将正确显示！

### 方法二：使用Gradle任务

```powershell
# 运行程序（自动设置UTF-8）
.\gradlew run

# 调试模式
.\gradlew runDebug
```

### 方法三：手动设置PowerShell编码

如果遇到问题，先运行：
```powershell
.\set-utf8.ps1
```

然后再执行其他命令。

### 方法四：使用外部终端

选择调试配置：**Debug GoMule (External Console)**
- 在独立PowerShell窗口运行
- 避免VS Code内部控制台的编码问题

## 🔍 验证配置是否生效

### 测试1：检查Java编码设置

```powershell
$env:JAVA_HOME="C:\Users\wangwei\.jdk\jdk-21.0.8"
& "$env:JAVA_HOME\bin\java.exe" -Dfile.encoding=UTF-8 -version
```

### 测试2：运行程序查看输出

按F5启动调试，查看控制台输出的中文字符是否正常。

### 测试3：检查PowerShell编码

```powershell
[Console]::OutputEncoding
chcp
```

应该显示：
- `EncodingName: Unicode (UTF-8)`
- `活动代码页: 65001`

## 🛠️ 故障排除

### 问题1: VS Code 调试控制台仍有乱码

**解决方案A - 重启VS Code**
```powershell
# 完全关闭VS Code，然后重新打开
```

**解决方案B - 使用外部终端**
1. 选择 `Debug GoMule (External Console)` 配置
2. 按 F5

**解决方案C - 手动设置UTF-8**
```powershell
# 在终端运行
.\set-utf8.ps1
# 然后再调试
```

### 问题2: PowerShell 默认不是UTF-8

**临时解决（每次打开终端）**
```powershell
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
chcp 65001
```

**永久解决（添加到PowerShell配置）**
```powershell
# 编辑PowerShell配置文件
notepad $PROFILE

# 添加以下内容：
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::InputEncoding = [System.Text.Encoding]::UTF8
```

### 问题3: Windows 系统级别编码问题

**启用 Beta UTF-8 支持（需要重启）**
1. 打开 `控制面板` → `区域` → `管理`
2. 点击 `更改系统区域设置`
3. 勾选 `Beta: 使用 Unicode UTF-8 提供全球语言支持`
4. 重启计算机

### 问题4: Gradle 输出仍有乱码

**手动指定编码运行**
```powershell
$env:JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"
.\gradlew run
```

## 📝 技术细节

### JVM 编码参数说明

| 参数 | 作用 | 优先级 |
|------|------|--------|
| `-Dfile.encoding=UTF-8` | 默认文件编码 | 高 |
| `-Dconsole.encoding=UTF-8` | 控制台编码 | 高 |
| `-Dsun.stdout.encoding=UTF-8` | 标准输出编码 | 中 |
| `-Dsun.stderr.encoding=UTF-8` | 标准错误编码 | 中 |

### 编码转换流程

```
源代码(UTF-8) → Java编译 → 字节码 → JVM运行 → 
控制台输出 → PowerShell显示 → VS Code渲染
     ↑              ↑           ↑            ↑
   编码设置1       编码设置2    编码设置3    编码设置4
```

每个环节都需要正确的UTF-8配置！

## ✅ 预期效果

配置完成后，您应该能看到：

**✅ 正确显示：**
```
加载配置文件...
读取技能数据: skills.txt
中文物品名称正常显示
错误信息：无法找到指定文件
```

**❌ 不再出现：**
```
鍔犺浇閰嶇疆鏂囦欢...
璇诲彇鎶?兘鏁版嵁: skills.txt
涓枃鐗╁搧鍚嶇О姝e父鏄剧ず
閿欒淇℃伅锛氭棤娉曟壘鍒版寚瀹氭枃浠?
```

## 🎉 总结

所有配置已完成并测试，包括：

1. ✅ VS Code 设置
2. ✅ 调试配置
3. ✅ Gradle 任务
4. ✅ PowerShell 脚本
5. ✅ 自动化任务

现在可以：
- 🎯 正常调试查看中文输出
- 🎯 处理中文文件名和路径
- 🎯 查看中文错误消息
- 🎯 调试中文字符串变量

**享受无乱码的调试体验！** 🚀

## 📚 相关文档

- `QUICK_START.md` - 快速开始指南
- `DEBUG_README.md` - 详细调试说明
- `set-utf8.ps1` - UTF-8设置脚本

---

*配置日期: 2025年12月8日*
*Java 版本: 21 (LTS)*
