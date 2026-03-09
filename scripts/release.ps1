# ==================== WJFakeLocation 自动化发布脚本 (PowerShell) ====================
# 功能：自动版本号、打标签、推送到 GitHub

param(
    [switch]$Help,
    [string]$VersionType = "patch"
)

# 显示帮助信息
if ($Help) {
    Write-Host @"
WJFakeLocation 自动化发布工具

用法:
  .\scripts\release.ps1 [-VersionType <major|minor|patch>]

示例:
  .\scripts\release.ps1 -VersionType patch   # 补丁版本 (1.0.0 -> 1.0.1)
  .\scripts\release.ps1 -VersionType minor   # 次要版本 (1.0.0 -> 1.1.0)
  .\scripts\release.ps1 -VersionType major   # 主要版本 (1.0.0 -> 2.0.0)

"@
    exit 0
}

# 颜色定义
function Write-Info { Write-Host "[INFO] $args" -ForegroundColor Cyan }
function Write-Success { Write-Host "[SUCCESS] $args" -ForegroundColor Green }
function Write-Warning { Write-Host "[WARNING] $args" -ForegroundColor Yellow }
function Write-Error { Write-Host "[ERROR] $args" -ForegroundColor Red }

# 检查 Git 状态
function Check-GitStatus {
    Write-Info "检查 Git 状态..."
    
    $status = git status --porcelain
    if ($status) {
        Write-Warning "存在未提交的更改："
        git status
        Write-Warning "请先提交或暂存更改"
        exit 1
    }
    
    Write-Success "工作区干净"
}

# 获取当前版本号
function Get-CurrentVersion {
    $content = Get-Content "app\build.gradle.kts" -Raw
    if ($content -match 'versionName\s*=\s*"([^"]+)"') {
        return $matches[1]
    }
    return "2.0.0"
}

# 获取下一个版本号
function Get-NextVersion {
    param(
        [string]$Current,
        [ValidateSet("major", "minor", "patch")]
        [string]$Type
    )
    
    $parts = $Current.Split('.')
    $major = [int]$parts[0]
    $minor = [int]$parts[1]
    $patch = [int]$parts[2]
    
    switch ($Type) {
        "major" { return "$($major + 1).0.0" }
        "minor" { return "$major.$($minor + 1).0" }
        "patch" { return "$major.$minor.$($patch + 1)" }
    }
}

# 更新 build.gradle.kts
function Update-Version {
    param([string]$NewVersion)
    
    Write-Info "更新版本号为：$NewVersion"
    
    # 生成 versionCode (使用时间戳)
    $versionCode = [int](Get-Date -UFormat %s).ToString().Substring(5)
    
    $content = Get-Content "app\build.gradle.kts" -Raw
    $content = $content -replace 'versionCode\s*=\s*\d+', "versionCode = $versionCode"
    $content = $content -replace 'versionName\s*=\s*"[^"]+"', "versionName = `"$NewVersion`""
    
    Set-Content "app\build.gradle.kts" -Value $content -NoNewline
    
    Write-Success "版本号已更新"
}

# 创建 Git 标签
function Create-Tag {
    param([string]$Version)
    
    $tag = "v$Version"
    Write-Info "创建 Git 标签：$tag"
    
    # 检查标签是否存在
    $existingTags = git tag -l $tag
    if ($existingTags) {
        Write-Error "标签 $tag 已存在！"
        exit 1
    }
    
    git tag -a $tag -m "Release version $tag"
    Write-Success "Git 标签创建成功"
}

# 推送到远程
function Push-ToRemote {
    param([string]$Version)
    
    $branch = git rev-parse --abbrev-ref HEAD
    $tag = "v$Version"
    
    Write-Info "推送到远程仓库..."
    Write-Warning "这将推送代码和标签到 GitHub"
    
    $confirm = Read-Host "确认继续？(y/n)"
    if ($confirm -ne "y" -and $confirm -ne "Y") {
        Write-Warning "操作已取消"
        exit 0
    }
    
    git push origin $branch
    git push origin $tag
    
    Write-Success "推送成功！"
    Write-Info "GitHub Actions 将自动开始构建和发布"
    Write-Info "查看进度：https://github.com/yourusername/WJFakeLocation/actions"
}

# 主函数
function Main {
    Write-Host "========================================"
    Write-Host "   WJFakeLocation 自动化发布工具"
    Write-Host "========================================"
    Write-Host ""
    
    Check-GitStatus
    
    $currentVersion = Get-CurrentVersion
    Write-Info "当前版本：$currentVersion"
    Write-Host ""
    
    Write-Info "即将发布新版本：$(Get-NextVersion -Current $currentVersion -Type $VersionType)"
    Write-Host ""
    
    $newVersion = Get-NextVersion -Current $currentVersion -Type $VersionType
    Update-Version -NewVersion $newVersion
    
    # 提交版本变更
    git add app/build.gradle.kts
    git commit -m "chore: 版本升级到 v$newVersion"
    
    Create-Tag -Version $newVersion
    Push-ToRemote -Version $newVersion
    
    Write-Host ""
    Write-Host "========================================"
    Write-Success "发布流程完成！"
    Write-Host "========================================"
}

# 执行主函数
Main
