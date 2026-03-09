#!/usr/bin/env bash

# ==================== WJFakeLocation 自动化发布脚本 ====================
# 功能：自动版本号、打标签、推送到 GitHub

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 打印带颜色的消息
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查 Git 状态
check_git_status() {
    print_info "检查 Git 状态..."
    
    if ! git diff-index --quiet HEAD --; then
        print_warning "存在未提交的更改，请先提交或暂存"
        git status
        exit 1
    fi
    
    print_success "工作区干净"
}

# 获取当前版本号
get_current_version() {
    local version=$(grep "^versionName = " app/build.gradle.kts | sed 's/.*"\(.*\)".*/\1/')
    echo "$version"
}

# 获取下一个版本号
get_next_version() {
    local current=$1
    local type=$2
    
    IFS='.' read -r major minor patch <<< "$current"
    
    case $type in
        major)
            echo "$((major + 1)).0.0"
            ;;
        minor)
            echo "$major.$((minor + 1)).0"
            ;;
        patch)
            echo "$major.$minor.$((patch + 1))"
            ;;
        *)
            echo "$current"
            ;;
    esac
}

# 更新 build.gradle.kts 版本号
update_version() {
    local new_version=$1
    
    print_info "更新版本号为: $new_version"
    
    # 更新 versionCode (使用时间戳)
    local version_code=$(date +%s | cut -c6-10)
    
    # macOS 兼容的 sed 命令
    if [[ "$OSTYPE" == "darwin"* ]]; then
        sed -i '' "s/versionCode = .*/versionCode = $version_code/" app/build.gradle.kts
        sed -i '' "s/versionName = \".*\"/versionName = \"$new_version\"/" app/build.gradle.kts
    else
        sed -i "s/versionCode = .*/versionCode = $version_code/" app/build.gradle.kts
        sed -i "s/versionName = \".*\"/versionName = \"$new_version\"/" app/build.gradle.kts
    fi
    
    print_success "版本号已更新"
}

# 创建 Git 标签
create_tag() {
    local version=$1
    local tag="v$version"
    
    print_info "创建 Git 标签：$tag"
    
    if git rev-parse "$tag" >/dev/null 2>&1; then
        print_error "标签 $tag 已存在！"
        exit 1
    fi
    
    git tag -a "$tag" -m "Release version $tag"
    
    print_success "Git 标签创建成功"
}

# 推送到远程
push_to_remote() {
    local branch=$(git rev-parse --abbrev-ref HEAD)
    local tag="v$1"
    
    print_info "推送到远程仓库..."
    print_warning "这将推送代码和标签到 GitHub"
    
    read -p "确认继续？(y/n): " confirm
    if [[ $confirm != [yY] ]]; then
        print_warning "操作已取消"
        exit 0
    fi
    
    git push origin "$branch"
    git push origin "$tag"
    
    print_success "推送成功！"
    print_info "GitHub Actions 将自动开始构建和发布"
}

# 主函数
main() {
    echo "========================================"
    echo "   WJFakeLocation 自动化发布工具"
    echo "========================================"
    echo ""
    
    check_git_status
    
    local current_version=$(get_current_version)
    print_info "当前版本：$current_version"
    
    echo ""
    print_info "请选择版本类型："
    echo "1) Patch (补丁版本) - $(get_next_version $current_version patch)"
    echo "2) Minor (次要版本) - $(get_next_version $current_version minor)"
    echo "3) Major (主要版本) - $(get_next_version $current_version major)"
    echo ""
    
    read -p "请输入选项 (1/2/3): " choice
    
    local new_version
    case $choice in
        1)
            new_version=$(get_next_version $current_version patch)
            ;;
        2)
            new_version=$(get_next_version $current_version minor)
            ;;
        3)
            new_version=$(get_next_version $current_version major)
            ;;
        *)
            print_error "无效选项"
            exit 1
            ;;
    esac
    
    echo ""
    print_info "即将发布新版本：$new_version"
    echo ""
    
    update_version "$new_version"
    
    # 提交版本变更
    git add app/build.gradle.kts
    git commit -m "chore: 版本升级到 v$new_version"
    
    create_tag "$new_version"
    push_to_remote "$new_version"
    
    echo ""
    echo "========================================"
    print_success "发布流程完成！"
    echo "========================================"
    print_info "请访问 GitHub Actions 查看构建进度："
    echo "https://github.com/yourusername/WJFakeLocation/actions"
    echo ""
}

# 执行主函数
main
