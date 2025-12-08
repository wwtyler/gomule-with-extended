# 设置 PowerShell 为 UTF-8 编码
# 此脚本将 PowerShell 的输出编码设置为 UTF-8

Write-Host "正在设置 PowerShell 编码为 UTF-8..." -ForegroundColor Green

# 设置控制台输出编码为 UTF-8
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::InputEncoding = [System.Text.Encoding]::UTF8

# 设置代码页为 UTF-8 (65001)
chcp 65001 | Out-Null

# 验证设置
Write-Host "`n当前编码设置：" -ForegroundColor Cyan
Write-Host "输出编码: $([Console]::OutputEncoding.EncodingName)" -ForegroundColor Yellow
Write-Host "输入编码: $([Console]::InputEncoding.EncodingName)" -ForegroundColor Yellow
Write-Host "代码页: $(chcp)" -ForegroundColor Yellow

Write-Host "`n✅ UTF-8 编码已设置！" -ForegroundColor Green
Write-Host "现在可以正常显示中文了：你好世界！🎉" -ForegroundColor Magenta
