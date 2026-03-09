# ==================== CI/CD 修复验证脚本 ====================
# 用途：验证修复是否正确应用

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   CI/CD 配置验证工具" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检查 1: 验证 app/build.gradle.kts
Write-Host "[检查 1] 验证 app/build.gradle.kts ..." -ForegroundColor Yellow

$appBuildContent = Get-Content "app\build.gradle.kts" -Raw

if ($appBuildContent -match 'kotlin\(\"kapt\"\)') {
    Write-Host "  ✅ Kapt 插件已添加" -ForegroundColor Green
} else {
    Write-Host "  ❌ Kapt 插件缺失" -ForegroundColor Red
    Write-Host "     请确保第 9 行包含：kotlin(`"kapt`")" -ForegroundColor Yellow
}

if ($appBuildContent -match 'layout\.buildDirectory') {
    Write-Host "  ✅ layout.buildDirectory 已使用（Gradle 8.9+ 兼容）" -ForegroundColor Green
} elseif ($appBuildContent -match '\$buildDir') {
    Write-Host "  ❌ 仍在使用废弃的 \$buildDir" -ForegroundColor Red
    Write-Host "     请替换为：layout.buildDirectory" -ForegroundColor Yellow
} else {
    Write-Host "  ⚠️  未找到 buildDirectory 引用" -ForegroundColor Yellow
}

if ($appBuildContent -match 'tasks\.register<JacocoReport>\(\"jacocoTestReport\"\)') {
    Write-Host "  ✅ Jacoco 任务已正确注册" -ForegroundColor Green
} else {
    Write-Host "  ❌ Jacoco 任务未找到" -ForegroundColor Red
}

Write-Host ""

# 检查 2: 验证根 build.gradle.kts
Write-Host "[检查 2] 验证 build.gradle.kts (根目录) ..." -ForegroundColor Yellow

$rootBuildContent = Get-Content "build.gradle.kts" -Raw

if ($rootBuildContent -match 'kotlin\(\"kapt\"\)') {
    Write-Host "  ✅ Kapt 插件已在根项目配置" -ForegroundColor Green
} else {
    Write-Host "  ❌ 根项目缺少 Kapt 插件" -ForegroundColor Red
    Write-Host "     请添加：kotlin(`"kapt`") version `"2.0.0`" apply false" -ForegroundColor Yellow
}

Write-Host ""

# 检查 3: 验证 GitHub Actions 工作流
Write-Host "[检查 3] 验证 .github/workflows/android-ci.yml ..." -ForegroundColor Yellow

if (Test-Path ".github\workflows\android-ci.yml") {
    $workflowContent = Get-Content ".github\workflows\android-ci.yml" -Raw
    
    if ($workflowContent -match 'Clean Gradle Cache') {
        Write-Host "  ✅ Gradle 缓存清理步骤已添加" -ForegroundColor Green
    } else {
        Write-Host "  ⚠️  建议添加 Gradle 缓存清理步骤" -ForegroundColor Yellow
    }
    
    Write-Host "  ✅ GitHub Actions 工作流文件存在" -ForegroundColor Green
} else {
    Write-Host "  ❌ GitHub Actions 工作流文件不存在" -ForegroundColor Red
}

Write-Host ""

# 检查 4: 总结
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   验证完成！" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$allGood = ($appBuildContent -match 'kotlin\("kapt"') -and 
           ($appBuildContent -match 'layout\.buildDirectory') -and 
           ($rootBuildContent -match 'kotlin\("kapt"')

if ($allGood) {
    Write-Host "🎉 所有检查通过！可以推送到 GitHub 了" -ForegroundColor Green
    Write-Host ""
    Write-Host "下一步操作:" -ForegroundColor Cyan
    Write-Host "  1. 提交更改到 Git" -ForegroundColor White
    Write-Host "  2. 推送到 GitHub（触发 CI/CD）" -ForegroundColor White
    Write-Host "  3. 在 GitHub Actions 页面查看构建进度" -ForegroundColor White
} else {
    Write-Host "⚠️  发现问题，请先修复上述错误" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "修复建议:" -ForegroundColor Cyan
    Write-Host "  - 检查 app/build.gradle.kts 的 plugins 块" -ForegroundColor White
    Write-Host "  - 检查 build.gradle.kts (根目录) 的 plugins 块" -ForegroundColor White
    Write-Host "  - 确保使用 layout.buildDirectory 替代 \$buildDir" -ForegroundColor White
}

Write-Host ""
