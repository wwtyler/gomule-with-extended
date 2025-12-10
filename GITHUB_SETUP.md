# GitHub仓库创建和配置指南

## 📋 完整步骤

### 第一步：在GitHub上创建仓库（必须）

1. 访问：https://github.com/new
2. 填写信息：
   - **Repository name**: `gomule-with-extended`
   - **Description**: `GoMule for Diablo II Resurrected - Java 21 upgrade with extended features`
   - **Visibility**: 选择 Public 或 Private
   - ⚠️ **重要**: 不要勾选任何初始化选项（README、.gitignore、license）
3. 点击 "Create repository"

### 第二步：配置并推送（自动化）

创建完仓库后，GitHub会显示配置命令。您可以：

#### 选项A：使用我准备的脚本（推荐）
```powershell
.\quick-setup.ps1
```
此脚本会：
- 自动添加远程仓库
- 提供HTTPS/SSH选择
- 自动推送代码

#### 选项B：手动执行命令
```powershell
# 添加远程仓库（HTTPS方式）
git remote add origin https://github.com/wwtyler/gomule-with-extended.git

# 推送当前分支
git push -u origin d2mm-gomule-with-extend

# 首次推送会要求输入GitHub凭据
```

#### 选项C：SSH方式（需要预先配置SSH密钥）
```powershell
git remote add origin git@github.com:wwtyler/gomule-with-extended.git
git push -u origin d2mm-gomule-with-extend
```

---

## ❓ 常见问题

### Q: 必须先创建GitHub仓库吗？
**A**: 是的，必须先在GitHub网页上创建空仓库。Git无法自动创建远程仓库，只能推送到已存在的仓库。

### Q: 如何获取GitHub Token（推荐用于HTTPS）？
**A**: 
1. 访问：https://github.com/settings/tokens
2. 点击 "Generate new token" → "Generate new token (classic)"
3. 勾选 `repo` 权限
4. 生成后复制Token（只显示一次）
5. 推送时用Token替代密码

### Q: 推送失败怎么办？
**A**: 检查：
1. 仓库是否已在GitHub上创建
2. 仓库名是否正确
3. 是否有推送权限
4. 网络连接是否正常

---

## 🎯 推荐流程

### 方案1：现在就创建（5分钟）
1. 在浏览器打开新标签页
2. 访问 https://github.com/new
3. 创建仓库（30秒）
4. 回到终端运行 `.\quick-setup.ps1`（30秒）
5. 完成！

### 方案2：稍后创建
您的代码已经安全保存在本地Git仓库中：
- 分支：`d2mm-gomule-with-extend`
- 提交：`0808376` (Upgrade to Java 21 LTS)
- 状态：Clean (无未提交修改)

随时可以创建GitHub仓库并推送。

---

## 📌 快速命令参考

```powershell
# 查看当前状态
git status
git log -1

# 查看远程配置
git remote -v

# 添加远程仓库（先创建GitHub仓库）
git remote add origin https://github.com/wwtyler/gomule-with-extended.git

# 推送代码
git push -u origin d2mm-gomule-with-extend

# 如果添加错误，删除重新添加
git remote remove origin
```

---

**准备好了吗？**
1. 打开浏览器访问 https://github.com/new 创建仓库
2. 创建完成后回到这里运行：`.\quick-setup.ps1`
