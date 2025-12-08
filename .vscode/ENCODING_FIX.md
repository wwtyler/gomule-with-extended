# 中文编码配置说明

## ✅ 问题已解决

调试控制台的中文乱码问题已修复！所有配置已更新为使用 UTF-8 编码。

## 📝 已应用的配置

### 1. VS Code 设置（`.vscode/settings.json`）
```json
{
    "java.jdt.ls.vmargs": "-Dfile.encoding=UTF-8",
    "files.encoding": "utf8",
    "files.autoGuessEncoding": true
}
```

### 2. 调试配置（`.vscode/launch.json`）
所有调试配置已添加：
```json
{
    "vmArgs": "-Dfile.encoding=UTF-8 -Dconsole.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8",
    "encoding": "UTF-8"
}
```

### 3. Gradle 任务（`build.gradle`）
`run` 和 `runDebug` 任务已配置 UTF-8：
```gradle
jvmArgs = [
    '-Dfile.encoding=UTF-8',
    '-Dconsole.encoding=UTF-8',
    '-Dsun.stdout.encoding=UTF-8',
    '-Dsun.stderr.encoding=UTF-8'
]
```

## 🔧 使用方法

### 方法一：直接按 F5 调试（推荐）
1. 按 `F5` 启动调试
2. 控制台输出现在会正确显示中文

### 方法二：使用 Gradle 运行
```powershell
# 正常运行
.\gradlew run

# 调试模式
.\gradlew runDebug
```

### 方法三：使用外部终端（如果内部控制台仍有问题）
1. 选择调试配置：**Debug GoMule (External Console)**
2. 按 `F5`
3. 程序将在外部 PowerShell 窗口运行

## 🎯 配置说明

### JVM 编码参数解释

| 参数 | 作用 |
|------|------|
| `-Dfile.encoding=UTF-8` | 设置 JVM 文件系统编码为 UTF-8 |
| `-Dconsole.encoding=UTF-8` | 设置控制台编码为 UTF-8 |
| `-Dsun.stdout.encoding=UTF-8` | 设置标准输出流编码为 UTF-8 |
| `-Dsun.stderr.encoding=UTF-8` | 设置标准错误流编码为 UTF-8 |

### VS Code 设置解释

| 设置 | 作用 |
|------|------|
| `files.encoding: "utf8"` | VS Code 文件默认编码 |
| `files.autoGuessEncoding: true` | 自动检测文件编码 |
| `encoding: "UTF-8"` | 调试控制台编码 |

## 💡 故障排除

### 如果仍然出现乱码：

#### 1. 重启 VS Code
关闭并重新打开 VS Code，让配置生效。

#### 2. 检查 PowerShell 编码
```powershell
# 查看当前编码
[Console]::OutputEncoding

# 设置为 UTF-8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
chcp 65001
```

#### 3. 使用外部终端
选择 **Debug GoMule (External Console)** 配置，它会在独立的 PowerShell 窗口中运行。

#### 4. 检查 Windows 系统设置
1. 打开 **设置** → **时间和语言** → **语言和区域**
2. 点击 **管理语言设置**
3. 点击 **更改系统区域设置**
4. 勾选 **Beta: 使用 Unicode UTF-8 提供全球语言支持**
5. 重启计算机

#### 5. 验证 Java 编码
运行以下测试代码：
```powershell
$env:JAVA_HOME="C:\Users\wangwei\.jdk\jdk-21.0.8"
& "$env:JAVA_HOME\bin\java.exe" -Dfile.encoding=UTF-8 -version
```

## 📋 测试中文输出

创建测试文件验证编码：

```java
public class TestEncoding {
    public static void main(String[] args) {
        System.out.println("中文测试：你好世界！");
        System.out.println("当前编码：" + System.getProperty("file.encoding"));
        System.out.println("控制台编码：" + System.getProperty("console.encoding"));
    }
}
```

## ✅ 预期结果

配置后，您应该能在调试控制台中看到：
```
中文测试：你好世界！
当前编码：UTF-8
控制台编码：UTF-8
```

而不是：
```
中文测试：ä½ å¥½ä¸–ç•Œï¼
```

## 🎉 完成

所有配置已应用，现在您可以：
- ✅ 在调试控制台正确查看中文输出
- ✅ 调试包含中文的代码和数据
- ✅ 查看中文错误消息
- ✅ 处理包含中文的文件名和路径

祝调试顺利！🚀
