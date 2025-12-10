# 快速配置GitHub远程仓库
# 前提：您已经在 https://github.com/new 创建了仓库

# 建议的仓库设置：
# 仓库名: gomule-with-extended
# 描述: GoMule for Diablo II Resurrected with extended features - Java 21 upgrade
# 可见性: Public 或 Private
# ⚠️ 不要勾选 "Add a README file"、".gitignore" 或 "license"

Write-Host "=== GitHub 远程仓库快速配置 ===" -ForegroundColor Cyan
Write-Host ""

# 方案1: HTTPS (推荐，无需SSH密钥)
$httpsUrl = "https://github.com/wwtyler/gomule-with-extended.git"

# 方案2: SSH (需要配置SSH密钥)
$sshUrl = "git@github.com:wwtyler/gomule-with-extended.git"

Write-Host "请选择连接方式:" -ForegroundColor Yellow
Write-Host "1. HTTPS (推荐，使用GitHub账号密码或Token)" -ForegroundColor White
Write-Host "2. SSH (需要预先配置SSH密钥)" -ForegroundColor White
Write-Host "3. 输入自定义URL" -ForegroundColor White
Write-Host ""
Write-Host "选择 (1/2/3，默认1): " -ForegroundColor Green -NoNewline
$choice = Read-Host

$repoUrl = switch ($choice) {
    "2" { $sshUrl }
    "3" { 
        Write-Host "请输入仓库URL: " -ForegroundColor Green -NoNewline
        Read-Host 
    }
    default { $httpsUrl }
}

Write-Host ""
Write-Host "使用URL: $repoUrl" -ForegroundColor Cyan
Write-Host ""

# 添加远程仓库
Write-Host "正在添加远程仓库..." -ForegroundColor Cyan
git remote add origin $repoUrl

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ 远程仓库添加成功！" -ForegroundColor Green
} else {
    Write-Host "❌ 添加失败（可能已存在），尝试更新..." -ForegroundColor Yellow
    git remote set-url origin $repoUrl
}

Write-Host ""
Write-Host "当前远程配置:" -ForegroundColor Cyan
git remote -v

Write-Host ""
Write-Host "准备推送到GitHub..." -ForegroundColor Yellow
Write-Host "当前分支: d2mm-gomule-with-extend" -ForegroundColor White
Write-Host ""
Write-Host "是否现在推送? (y/N): " -ForegroundColor Green -NoNewline
$doPush = Read-Host

if ($doPush -eq 'y' -or $doPush -eq 'Y') {
    Write-Host ""
    Write-Host "正在推送到 GitHub..." -ForegroundColor Cyan
    Write-Host "(首次推送可能需要输入GitHub用户名和密码/Token)" -ForegroundColor Yellow
    Write-Host ""
    
    git push -u origin d2mm-gomule-with-extend
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "🎉 推送成功！" -ForegroundColor Green
        Write-Host ""
        Write-Host "访问您的仓库:" -ForegroundColor Cyan
        $webUrl = $repoUrl -replace '\.git$', '' -replace '^git@github\.com:', 'https://github.com/'
        Write-Host $webUrl -ForegroundColor White
        Write-Host ""
        Write-Host "分支查看:" -ForegroundColor Cyan
        Write-Host "$webUrl/tree/d2mm-gomule-with-extend" -ForegroundColor White
    } else {
        Write-Host ""
        Write-Host "❌ 推送失败" -ForegroundColor Red
        Write-Host ""
        Write-Host "可能的原因:" -ForegroundColor Yellow
        Write-Host "1. 仓库尚未在GitHub上创建" -ForegroundColor White
        Write-Host "2. 认证失败（用户名/密码/Token错误）" -ForegroundColor White
        Write-Host "3. 网络连接问题" -ForegroundColor White
        Write-Host ""
        Write-Host "请检查后重试:" -ForegroundColor Cyan
        Write-Host "  git push -u origin d2mm-gomule-with-extend" -ForegroundColor White
    }
} else {
    Write-Host ""
    Write-Host "✅ 远程仓库已配置，稍后可以手动推送" -ForegroundColor Green
    Write-Host ""
    Write-Host "推送命令:" -ForegroundColor Cyan
    Write-Host "  git push -u origin d2mm-gomule-with-extend" -ForegroundColor White
}

Write-Host ""
Write-Host "=== 配置完成 ===" -ForegroundColor Green
