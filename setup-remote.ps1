# GitHub远程仓库初始化脚本
# 使用说明: 在GitHub上创建仓库后，运行此脚本连接远程仓库

Write-Host "=== GitHub 远程仓库配置 ===" -ForegroundColor Cyan

# 提示用户输入仓库URL
Write-Host "`n请按以下步骤操作：" -ForegroundColor Yellow
Write-Host "1. 访问 https://github.com/new" -ForegroundColor White
Write-Host "2. 创建新仓库，建议名称: gomule-with-extended" -ForegroundColor White
Write-Host "3. 不要初始化README、.gitignore或license（使用现有代码）" -ForegroundColor White
Write-Host "4. 复制仓库URL（SSH或HTTPS）" -ForegroundColor White

Write-Host "`n请输入仓库URL (例如: https://github.com/wwtyler/gomule-with-extended.git): " -ForegroundColor Green -NoNewline
$repoUrl = Read-Host

if ([string]::IsNullOrWhiteSpace($repoUrl)) {
    Write-Host "`n❌ 未输入仓库URL，脚本退出" -ForegroundColor Red
    exit 1
}

Write-Host "`n正在配置远程仓库..." -ForegroundColor Cyan

# 检查是否已存在remote
$existingRemote = git remote get-url origin 2>$null
if ($existingRemote) {
    Write-Host "⚠️  检测到已存在的远程仓库: $existingRemote" -ForegroundColor Yellow
    Write-Host "是否要替换? (y/N): " -ForegroundColor Yellow -NoNewline
    $replace = Read-Host
    if ($replace -eq 'y' -or $replace -eq 'Y') {
        git remote remove origin
        Write-Host "✅ 已删除旧的远程配置" -ForegroundColor Green
    } else {
        Write-Host "❌ 操作取消" -ForegroundColor Red
        exit 1
    }
}

# 添加远程仓库
git remote add origin $repoUrl
Write-Host "✅ 远程仓库已添加: $repoUrl" -ForegroundColor Green

# 验证远程配置
Write-Host "`n远程仓库配置:" -ForegroundColor Cyan
git remote -v

# 推送选项
Write-Host "`n推送选项:" -ForegroundColor Yellow
Write-Host "1. 推送当前分支 (d2mm-gomule-with-extend)" -ForegroundColor White
Write-Host "2. 推送所有分支" -ForegroundColor White
Write-Host "3. 稍后手动推送" -ForegroundColor White
Write-Host "请选择 (1/2/3): " -ForegroundColor Green -NoNewline
$choice = Read-Host

switch ($choice) {
    "1" {
        Write-Host "`n正在推送当前分支..." -ForegroundColor Cyan
        git push -u origin d2mm-gomule-with-extend
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ 分支 d2mm-gomule-with-extend 已推送成功！" -ForegroundColor Green
        } else {
            Write-Host "❌ 推送失败，请检查权限或网络连接" -ForegroundColor Red
        }
    }
    "2" {
        Write-Host "`n正在推送所有分支..." -ForegroundColor Cyan
        git push -u origin --all
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ 所有分支已推送成功！" -ForegroundColor Green
        } else {
            Write-Host "❌ 推送失败，请检查权限或网络连接" -ForegroundColor Red
        }
    }
    "3" {
        Write-Host "`n✅ 远程仓库已配置，您可以稍后手动推送" -ForegroundColor Green
        Write-Host "推送命令:" -ForegroundColor Cyan
        Write-Host "  git push -u origin d2mm-gomule-with-extend" -ForegroundColor White
    }
    default {
        Write-Host "`n⚠️  无效选择，已跳过推送" -ForegroundColor Yellow
    }
}

Write-Host "`n=== 配置完成 ===" -ForegroundColor Green
Write-Host "`n常用命令:" -ForegroundColor Cyan
Write-Host "  git push                    # 推送当前分支" -ForegroundColor White
Write-Host "  git pull                    # 拉取最新更改" -ForegroundColor White
Write-Host "  git fetch origin            # 获取远程更新" -ForegroundColor White
Write-Host "  git remote -v               # 查看远程配置" -ForegroundColor White
