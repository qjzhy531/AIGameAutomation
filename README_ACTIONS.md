# GitHub Actions 在线编译指南

## 使用步骤

### 1. 创建 GitHub 仓库
1. 访问 https://github.com/new
2. 创建新仓库（例如：AIGameAutomation）
3. 不要初始化 README、.gitignore 或 license

### 2. 推送代码到 GitHub
在项目目录中执行以下命令：

```bash
cd C:\Users\Administrator\Desktop\AIGameAutomation
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/你的用户名/AIGameAutomation.git
git push -u origin main
```

### 3. 触发编译
推送代码后，GitHub Actions 会自动开始编译。

或者手动触发：
1. 访问仓库的 Actions 页面
2. 选择 "Build Android APK" workflow
3. 点击 "Run workflow"

### 4. 下载 APK
编译完成后：
1. 访问仓库的 Actions 页面
2. 点击最新的构建记录
3. 在 "Artifacts" 部分下载 "app-debug" 文件

## 注意事项
- 首次编译可能需要 5-10 分钟
- APK 文件位于：app/build/outputs/apk/debug/app-debug.apk
- 每次推送代码都会自动触发编译