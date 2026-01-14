# GoMule 变更日志

## [Unreleased] - 2025-12-10

### 🔧 修复 (Fix Java Compilation Warnings)

**提交**: `b7ff716` - 2025年12月10日 09:45:07

#### 修改内容
- 为 ArrayList、HashMap 和 Collection 添加了泛型类型参数
- 使用 @SuppressWarnings 注解修复了未检查的类型转换警告
- 改进了资源管理，使用 try-with-resources 语句
- 修复了 Lambda 表达式的类型推断问题
- 删除了冗余的 TODO 注释，清理了代码风格
- 更新了 Comparable 接口实现，添加了正确的类型参数
- 增强了 63 个文件的类型安全性（新增 736 行，删除 185 行）

#### 解决的问题
- 原始类型使用警告
- 未检查的类型转换警告
- 泛型类型推断问题
- 资源泄漏警告

#### 影响范围
- **67 个文件被修改**
- GUI 组件：28 个文件
- 数据模型：15 个文件
- 工具类：12 个文件
- 测试文件：2 个文件

详细信息请参阅: `.github/java-upgrade/20251208112003/fix-compilation-warnings-summary.md`

---

## [Java 21] - 2025-12-08

### 🚀 升级 (Upgrade to Java 21 LTS)

**提交**: `0808376` - 2025年12月08日 20:01:54

#### 主要改进
- 升级到 Java 21 LTS（长期支持版本）
- 配置了完整的开发环境
- 添加了 VS Code 调试配置
- 完善了 UTF-8 编码支持
- 优化了测试配置

#### 新增功能
- VS Code 调试配置（4种模式）
- Gradle 构建任务配置
- UTF-8 编码自动配置脚本
- 开发文档（中英文）

#### 技术细节
- Java 版本：Java 21 (LTS)
- 字节码版本：65
- 构建工具：Gradle 8.x
- IDE 支持：VS Code, IntelliJ IDEA, Eclipse

详细信息请参阅: `COMMIT_DETAILS.md`

---

## [Initial] - 2025-12-08

### 🎯 初始提交

**提交**: `7e27ec4`

- 初始化 Git 仓库
- 导入 GoMule 源代码
- 配置基础项目结构

---

## 版本说明

### 当前版本
- **分支**: `d2rmm-gomule-with-extended`
- **最新提交**: `b7ff716`
- **状态**: 开发中

### 兼容性
- **游戏**: Diablo II Resurrected
- **Java**: 21+ (LTS)
- **操作系统**: Windows (主要), Linux/macOS (未测试)

---

## 贡献指南

如需贡献代码，请：
1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

---

## 许可证

本项目继承原 GoMule 项目的许可证。详见 `LICENSE` 文件。

---

**维护者**: wwtyler <wwtyler@users.noreply.github.com>  
**最后更新**: 2025年12月10日
