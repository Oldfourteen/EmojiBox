# EmojiBox

内置微软 Fluent 彩色Emoji表情的 Minecraft 聊天表情模组，**完全离线、不联网**，为国内网络环境做了适配，支持中文界面与中文搜索。

> 本项目 fork 自 [HRZN Studio](https://github.com/HRZNStudio) 的 **Emojiful**，并基于 Minecraft 26.2 / Fabric 重写。
> 原项目使用 MIT 协议，本项目继续沿用 MIT 协议，原版权声明保留在 [LICENSE](LICENSE) 中。

## 为什么要做这个分支

原版 Emojiful 从 GitHub raw、emojidex、Twemoji、Discord CDN、Google Sheets 等境外服务在线拉取表情与屏蔽词。由于网络原因，这些地址在国内基本无法访问，导致表情加载失败、界面空白甚至长时间卡顿。

EmojiBox 把表情资源直接打包进 mod 本体，运行时不发起任何网络请求，开箱即用。

![Uploading 图片.png…]()

## 主要改动

- **移除全部境外在线数据源**：GitHub raw、emojidex、Twemoji、Discord CDN、Google Sheets 屏蔽词列表等全部删除，不再有任何 HTTP 请求。
- **表情内置**：微软 Fluent Emoji（Color 彩色风格）打包为一张位图字体图集，随 mod 一起分发。
- **重写渲染方案**：放弃原版的动态贴图加载，改为数据驱动的 bitmap 字体；聊天短代码在发送时转换为字形字符，由内置字体直接渲染。
- **不依赖 Mixin**：界面按钮与菜单改用 Fabric API 官方事件（`ScreenEvents` / `ScreenMouseEvents` / `ClientSendMessageEvents`），减少与其他模组的冲突。
- **重做表情菜单**：复刻原版的内嵌式小菜单（顶部搜索 + 左侧分类 + 表情网格 + 底部信息栏），不再弹出独立全屏界面。
- **完整汉化**：界面文案、8 个分类名称、525 个表情的中文名与中文关键词全部本地化，可直接用中文搜索（例如"笑""心""猫"）。
- **只支持 Fabric**：删除 Forge 与多加载器工程，仅保留 Fabric 客户端实现。
- **目标版本**：Minecraft 26.2、Java 25。
- **无需 Gradle 构建**：提供 `build.ps1`，直接使用本机客户端 jar 编译打包（方便离线环境）。

## 表情来源

- 图形：**Microsoft Fluent Emoji**（Color 风格），由 Microsoft 以 MIT 协议开源，本项目选取常用表情生成本地位图图集。
- 中文名与关键词：参考 Emojibase 的中文标签数据整理。
- 英文短代码：参考 GitHub `gemoji` 的短代码习惯，兼容常见写法（如 `:smile:`、`:heart:`、`:thumbsup:`）。

## 使用

1. 安装 Fabric Loader 与 Fabric API。
2. 把 `emojibox-1.0.0.jar` 放入 `mods` 文件夹。
3. 打开聊天栏，点击输入框上方的「表情」按钮，选择表情即可插入。
4. 也可以直接输入短代码，例如 `:smile:`、`:heart:`、`:fire:`，发送后显示为彩色表情。

配置文件：`config/emojiful.json`

```json
{
  "enabled": true,
  "showChatButton": true
}
```

## 构建

需要 JDK 25 与本机安装的 Minecraft 26.2 客户端（脚本默认路径见文件顶部，可按需修改）：

```powershell
.\build.ps1
```

产物输出到 `build/libs/emojibox-1.0.0.jar`。

## 已知限制

- **纯客户端模组**：服务端无需安装，也不会影响服务器逻辑。未安装本模组的玩家看到的是字形字符而不是彩色表情。
- 内部 mod id 仍为 `emojiful`（涉及资源命名空间与配置文件路径），因此**不能与原版 Emojiful 同时安装**。
- 会向游戏默认字体追加表情字形，正常情况下与原版及其他资源包兼容；若安装了大幅修改字体的资源包，个别情况下可能需要调整加载顺序。

## 致谢

- [HRZN Studio](https://github.com/HRZNStudio) — 原版 Emojiful 的作者与设计
- [Microsoft Fluent Emoji](https://github.com/microsoft/fluentui-emoji) — 表情图形素材
- [Emojibase](https://emojibase.dev/) — 表情标签数据参考

## 许可证

[MIT](LICENSE)
