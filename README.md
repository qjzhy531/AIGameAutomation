# AI游戏自动化工具

基于原生Android开发的智能游戏自动化工具，支持AI屏幕识别和智能决策。

## 功能特性

- ✅ **屏幕捕获**：实时捕获游戏屏幕
- ✅ **AI图像识别**：使用ML Kit识别游戏元素
- ✅ **智能决策**：根据屏幕内容自动决策
- ✅ **UI自动化**：自动点击、滑动等操作
- ✅ **自定义策略**：支持自定义游戏策略

## 技术栈

- **语言**：Kotlin
- **框架**：Android原生
- **AI引擎**：Google ML Kit
- **图像处理**：OpenCV

## 项目结构

```
AIGameAutomation/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/ai/gameautomation/
│   │   │   │   ├── MainActivity.kt          # 主界面
│   │   │   │   ├── ai/
│   │   │   │   │   └── AIEngine.kt          # AI引擎
│   │   │   │   └── service/
│   │   │   │       ├── AutomationService.kt # 自动化服务
│   │   │   │       └── GameAccessibilityService.kt # 无障碍服务
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   │   ├── activity_main.xml
│   │   │   │   │   └── overlay_control.xml
│   │   │   │   ├── values/
│   │   │   │   │   ├── strings.xml
│   │   │   │   │   ├── colors.xml
│   │   │   │   │   └── themes.xml
│   │   │   │   └── xml/
│   │   │   │       └── accessibility_service_config.xml
│   │   │   └── AndroidManifest.xml
│   │   └── build.gradle
│   └── proguard-rules.pro
├── build.gradle
└── settings.gradle
```

## 使用说明

### 1. 编译安装

```bash
# 使用Android Studio打开项目
# 或者使用命令行编译
./gradlew assembleDebug

# 安装到设备
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 2. 权限配置

首次运行需要授予以下权限：

1. **悬浮窗权限**：用于显示控制面板
2. **无障碍服务权限**：用于执行UI操作

### 3. 启动自动化

1. 打开应用
2. 开启"启用自动化"开关
3. 授予悬浮窗权限
4. 开启无障碍服务
5. 点击"开始游戏自动化"

### 4. 自定义策略

在 `AIEngine.kt` 中可以自定义游戏策略：

```kotlin
// 示例：自定义策略
class MyGameStrategy : GameStrategy {
    override fun makeDecision(context: GameContext): Decision {
        // 根据屏幕内容返回决策
        return Decision(
            action = "click",
            x = 100,
            y = 200,
            confidence = 0.9f
        )
    }
}

// 设置策略
AIEngine.setStrategy(MyGameStrategy())
```

## 内置策略

### 1. AutoClickStrategy
自动点击指定元素

```kotlin
val strategy = AutoClickStrategy("button")
AIEngine.setStrategy(strategy)
```

### 2. AutoSwipeStrategy
自动滑动

```kotlin
val strategy = AutoSwipeStrategy(540, 1500, 540, 1000)
AIEngine.setStrategy(strategy)
```

### 3. SmartGameStrategy
智能游戏策略（根据血量、敌人等决策）

```kotlin
AIEngine.setStrategy(SmartGameStrategy())
```

## API说明

### AIEngine

```kotlin
// 分析屏幕
val decision = AIEngine.analyzeScreen(screenshot)

// 查找元素位置
val position = AIEngine.findElementPosition(screenshot, "button")

// 设置策略
AIEngine.setStrategy(strategy)
```

### GameAccessibilityService

```kotlin
// 执行点击
GameAccessibilityService.performClick(x, y)

// 执行滑动
GameAccessibilityService.performSwipe(x1, y1, x2, y2)

// 获取屏幕信息
val info = GameAccessibilityService.getCurrentScreenInfo()
```

## 注意事项

1. **性能**：屏幕捕获和AI分析会消耗较多资源
2. **兼容性**：需要Android 7.0 (API 24) 或更高版本
3. **权限**：必须授予悬浮窗和无障碍服务权限
4. **游戏限制**：部分游戏可能检测自动化行为

## 开发计划

- [ ] 支持更多AI模型
- [ ] 添加脚本录制功能
- [ ] 支持多任务并行
- [ ] 添加性能监控
- [ ] 支持云端AI模型

## 许可证

MIT License

## 联系方式

如有问题或建议，欢迎提Issue。