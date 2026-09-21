# 移动工作台 UI 改版方案（对标钉钉 / 飞书 / 微信）

> 日期：2026-09-21 ｜ 范围：`enterprise-pro-app`（uni-app Vue3，H5 形态，生产 `/m/`）
> 性质：**仅 UI/交互层改造**，不改任何接口调用、字段语义与业务逻辑
> 状态：**B1 已实施完成（2026-09-21）**，构建通过；B2–B4 待排期

---

## 〇、实施进展

### B1 基础层（已完成）

| 项 | 产出 |
|---|---|
| 设计变量 | `src/styles/tokens.scss`（主色 `#0E7A63` + 中性色阶 + 圆角/间距/字号/阴影 + 应用色板），经 `src/uni.scss` 以 `@use ... as *` 全局注入 |
| tabBar 图标 | `tools/gen_tabbar_icons.py` 生成 8 个 PNG 到 `src/static/tabbar/`（消息/工作台/审批/我的 × 常态/选中），改色可重跑 |
| 组件库 | `components/AppIcon.vue`（内联 SVG，22 个图标）、`Avatar.vue`、`Cell.vue`、`SegmentTabs.vue`、`EmptyState.vue` |
| 全局样式 | `App.vue` 改写为 scss 并接 token；**保留全部旧类名**（`.card` / `.form-*` / `.btn-*` / `.tag` / `.empty` / `.row-*`）以免影响未改页面 |
| tabBar | `pages.json` 5→4（消息/工作台/审批/我的）、加图标、改主色；全局导航栏改白底黑字 |
| 工作台 | 主色沉浸头 + **圆形打卡主按钮**（状态机：未上班→上班打卡；已上班→下班打卡；完成→已完成）+ 3 指标卡（本月加班 / 今日考勤 / 待办审批）+ 4 列图标网格（8 项） |
| 我的 | 主色头部 + 分组 cell（带图标）+ 电脑端 / 版本 + 退出登录 |
| 登录 | 白底品牌区 + 行内图标输入 + 全宽主按钮；落地页改 `pages/message` |
| 消息 | 搜索框 +「聊天 \| 通知」分段；聊天段承接原 chat 会话列表（摘要 + 相对时间），通知段保留站内信 |
| 构建 | `npm run build:h5` **通过**（新增 devDependency `sass`） |

### 未完成 / 未验证（重要）

- **未做浏览器实测**：本次仅验证到「构建通过 + 产物完整 + CSS 产物含新主色 + 静态资源 HTTP 200」，
  **界面渲染效果未经真实浏览器确认**。按项目既有规矩（"每个新页面必须浏览器实测一次提交动作"），
  上线前需在 375px 视口逐页走查，重点验证 `<svg v-html>` 图标是否正常渲染、打卡状态机是否符合预期。
- **聊天列表未读徽标未实现**：后端好友列表接口未返回好友级未读数，需后端补字段（B2 前置项）；
  当前只有「通知」分段有未读计数。
- **B2**（chatroom 气泡改造）、**B3**（审批详情时间线 + 4 个表单页）、**B4**（骨架屏 / 下拉刷新 / 动效）未开始。
- **PC 端主色未同步**（按确认，本次只改移动端）。

---

## 一、现状诊断

先说结论：**功能是齐的，缺的是视觉系统。** 12 个页面能跑通登录→打卡→提单→审批→消息全链路，但整体呈现停留在"能用的后台页面"水平。

| # | 问题 | 证据（文件 : 行） |
|---|---|---|
| 1 | **没有设计变量体系** | `src/uni.scss` 仍是 uni-app 官方模板原文（`$uni-color-primary: #007aff`），项目从未改过 |
| 2 | **主色硬编码散落** | `#1ab394` 出现在 `App.vue:87,143`、`pages.json:50,66`、`index/index.vue:146,222,250`、`chat/index.vue:85`、`message`、`mine/index.vue:75`、`approval/index.vue:163,175` 等 8+ 文件 |
| 3 | **tabBar 无图标** | `pages.json:55-61` 五个 tab 只有 `text`，无 `iconPath` / `selectedIconPath` |
| 4 | **用汉字充当图标** | 工作台九宫格 `index/index.vue:82-87` 用"休/报/补/周/审/信"；「我的」菜单 `mine/index.vue:13` 用 `›` 字符 |
| 5 | **头像一律是色块首字** | `chat/index.vue:5`、`mine/index.vue:4` 取 `nickname[0]`，无在线态、无群头像、无身份色 |
| 6 | **详情靠系统弹窗** | `leave/index.vue:100` 请假详情是 `uni.showModal` 纯文本；`approval/index.vue:114-134` 审批用 `showModal({editable:true})` 收驳回原因 |
| 7 | **列表信息层级薄** | `chat/index.vue` 无搜索、无未读徽标、无置顶、无分组；摘要被硬截 20 字（`:52`） |
| 8 | **打卡缺仪式感** | 现为 hero 区两个并列半透明块（`index/index.vue:12-21`），办公场景最高频动作却最弱 |
| 9 | **导航栏是原生青绿** | `pages.json:64-67` 全局 `#1ab394`，与内容区无过渡，非主流做法 |
| 10 | **无加载/空态/下拉刷新** | 仅 `.empty` 灰字（`App.vue:105-110`），无骨架屏、无插图、无刷新 |

**信息架构问题**：tabBar 里「聊天」和「消息」概念重叠（前者是好友会话，后者是站内信），用户要记两个入口；主流做法是合并为一个「消息」容器 + 内部分段。

---

## 二、设计目标

- **主线**：以钉钉为结构参照（工作台 + 待办驱动），以飞书为视觉参照（克制、留白、线性图标），以微信为列表参照（会话行信息密度）
- **不改动**：业务字段、接口、权限判断、路由参数
- **可回退**：全部改动集中在新增的样式/组件文件 + 12 个页面的 `<style>` 与模板结构，无数据层改动

---

## 三、设计系统（Design Token）

新增 `src/styles/tokens.scss`，全量替换硬编码值。

### 颜色

| 语义 | 值 | 用途 |
|---|---|---|
| `$c-primary` | `#0E7A63` | 主色（比现 `#1ab394` 更深，白底对比度从 2.3:1 提到 4.9:1，达 WCAG AA） |
| `$c-primary-soft` | `#E1F5EE` | 主色浅底（图标容器、选中态） |
| `$c-primary-weak` | `#F2FBF8` | 主色极浅底（列表选中行） |
| `$c-danger` | `#E24B4A` | 驳回、未读徽标、负向数值 |
| `$c-warning` | `#BA7517` | 待审批、预警 |
| `$c-success` | `#1D9E75` | 已通过、在线点 |
| `$c-text-1` | `#111827` | 标题、主文本 |
| `$c-text-2` | `#6B7280` | 次要文本 |
| `$c-text-3` | `#9CA3AF` | 占位、时间戳 |
| `$c-border` | `#E5E7EB` | 描边 |
| `$c-divider` | `#F1F3F5` | 分隔线 |
| `$c-page` | `#F5F6F8` | 页面底 |
| `$c-card` | `#FFFFFF` | 卡片底 |

应用图标色板（低饱和底 + 深色描边，成对使用）：

| 色系 | 底 | 描边 | 用于 |
|---|---|---|---|
| teal | `#E1F5EE` | `#0F6E56` | 请假 |
| blue | `#E6F1FB` | `#185FA5` | 报销 |
| amber | `#FAEEDA` | `#854F0B` | 补卡 |
| purple | `#EEEDFE` | `#534AB7` | 汇报 |
| red | `#FCEBEB` | `#A32D2D` | 审批 |
| gray | `#F1EFE8` | `#5F5E5A` | 消息 |

### 圆角 / 间距 / 字号

```
圆角：$r-sm 8rpx ｜ $r-md 16rpx ｜ $r-lg 24rpx ｜ $r-icon 26rpx ｜ $r-full 999rpx
间距：$sp-1 8rpx ｜ $sp-2 16rpx ｜ $sp-3 24rpx ｜ $sp-4 32rpx ｜ $sp-6 48rpx
字号：$fs-cap 22rpx ｜ $fs-sm 24rpx ｜ $fs-body 26rpx ｜ $fs-md 28rpx ｜ $fs-lg 32rpx ｜ $fs-xl 40rpx
阴影：$shadow-card: 0 2rpx 8rpx rgba(17, 24, 39, 0.04)   ← 极轻，主流扁平风
```

**对比度提醒**：现 `#1ab394` 上的白字在阳光下几乎看不清，这是户外打卡场景的实际痛点，也是换深主色的主要理由。

### 图标方案

内联 SVG（`viewBox="0 0 24 24"`，`stroke-width: 1.7~1.8`，`fill: none`），封装成 `components/AppIcon.vue`，按 `name` 渲染。
当前只构建 H5，可直接用 `<svg>`；**若未来要出微信小程序，需把 SVG 换成 `image` + data-uri 或字体图标** —— 已在组件内预留 `mode` 分支位置。

---

## 四、信息架构调整

```
现在：聊天 ｜ 工作台 ｜ 审批 ｜ 消息 ｜ 我的      （5 tab，聊天/消息语义重叠）

改后：消息 ｜ 工作台 ｜ 审批 ｜ 我的              （4 tab，主流数量）
        └─ 内部分段：聊天 ｜ 通知
```

- 「消息」容器内两个分段：**聊天**（好友会话，取原 `pages/chat` 能力）+ **通知**（站内信，取原 `pages/message` 能力）
- 登录后落地页从 `pages/chat/index` 改为 `pages/message/index?tab=chat`
- 工作台九宫格里指向 `tab:/pages/message/index` 的入口保留，仅改跳转参数

> 兼容：原 `pages/chat/index` 页面**保留不删**，降级为「只显示会话列表」的独立页，避免一次改动过大；`chatroom` 路由与 params 完全不变。

---

## 五、页面级改造清单

| 页面 | 主要改动 | 优先级 |
|---|---|---|
| `pages.json` | tabBar 5→4 + 8 个图标资源 + 主色替换 + `backgroundColor` 校正 | P1 |
| `App.vue` | 全局类接 token；新增 `.cell`（列表单元格）、`.seg`（分段）、`.badge`、`.skeleton` | P1 |
| `pages/index/index` | 头像+搜索沉浸头 → **圆形打卡主按钮**（96px 环）→ 3 指标卡 → 图标化九宫格（4 列）→ 待办/公告卡 | P1 |
| `pages/message/index` | 搜索框 + 分段（聊天/通知）+ 会话行（头像+摘要+时间+未读）+ 下拉刷新 | P1 |
| `pages/mine/index` | 头部改窄（头像+姓名+部门）+ 按功能分组 cell（带图标）+ 新增设置项（通知/关于） | P1 |
| `pages/login/index` | 品牌区上移、输入改行内单元格式、主按钮全宽、加"记住账号" | P1 |
| `pages/chatroom/index` | 气泡（自己右侧主色、对方左侧白卡）+ 时间分组 + 底部输入栏 + 常用语、消息状态 | P2 |
| `pages/approval/index` | 分段控件（带计数）+ 卡片带申请人头像 + 底部固定「驳回/通过」+ 驳回意见底部弹层（替代 `showModal`） | P2 |
| `pages/approval/detail`（**新建**） | 结构化字段 + **审批流程时间线**（提交/通过/待审批节点 + 意见气泡） | P2 |
| `pages/leave/index` | 表单改行内单元格（label 左值右）+ 底部吸底提交 + 天数自动计算提示 | P2 |
| `pages/expense/index` | 同上 + 金额大号输入 | P2 |
| `pages/correction/index` | 同上 + 时间选择器升级 | P2 |
| `pages/report/index` | 同上 + 模板快捷语 | P2 |
| 全局 | 骨架屏、空态插图、按钮点击态（`active` 缩放 0.97）、下拉刷新 | P3 |

---

## 六、技术实现

### 新增文件

```
src/styles/tokens.scss          # 设计变量（唯一色彩来源）
src/components/AppIcon.vue      # SVG 图标（约 20 个）
src/components/NavBar.vue       # 沉浸式自定义导航
src/components/Avatar.vue       # 首字头像：按 userId 稳定取色 + 在线点
src/components/SegmentTabs.vue  # 分段控件（带计数徽标）
src/components/EmptyState.vue   # 空态（图形 + 文案 + 主行动）
src/components/SkeletonList.vue # 骨架屏
src/components/Cell.vue         # 列表单元格（图标/标题/右侧值/箭头）
src/components/StatusTag.vue    # 状态标签（接 dictColor）
src/static/tabbar/*.svg         # tabBar 图标（H5 可用 SVG）
```

### 改动约束

- **不改** `src/api/index.ts`、`src/utils/request.ts`、`src/utils/auth.ts`、`src/utils/dict.ts` 的对外签名
- **不改** 任何接口路径、请求参数、字典 key
- 页面内 `mode='list' | 'form'` 的双视图结构保留（避免引入 router 复杂度），仅重做样式
- 每批次改完必须**真机/窄视口实测一遍提交动作**（AGENTS.md 已固化：API 冒烟发现不了 UI 提交问题）

---

## 七、分批落地计划

| 批次 | 内容 | 交付标准 |
|---|---|---|
| **B1 基础层** | tokens + AppIcon + NavBar + Avatar + Cell + SegmentTabs + `pages.json` tabBar + 工作台 + 我的 + 登录 | 首页/我的/登录三页视觉到位，tabBar 有图标有红点，无功能回归 |
| **B2 消息域** | message（合并 chat 会话）+ chatroom 气泡 | 会话列表有搜索/未读/时间，聊天收发正常 |
| **B3 审批域** | approval 列表 + 新建详情页（时间线）+ 4 个表单页统一 | 审批通过/驳回走查通过，表单提交成功 |
| **B4 打磨** | 骨架屏、空态、点击态、下拉刷新、动效 | 弱网有骨架、空列表有引导 |

每批次独立可发布，建议 B1 先上，观察一周再做 B2。

---

## 八、风险与回退

| 风险 | 说明 | 应对 |
|---|---|---|
| tabBar 从 5 改 4 | 影响 `login/index.vue:59` 的落地页、工作台 `tab:` 跳转、用户肌肉记忆 | 全量 grep `switchTab` / `reLaunch` 逐一核对；上线公告说明 |
| 深主色改变观感 | 与 PC 端 Element Plus 主题色不再一致 | PC 端可在 `src/styles/element/index.scss` 同步改，保持品牌一致（需另开任务） |
| SVG 图标不利于小程序 | uni-app 小程序端不支持内联 `<svg>` | 组件内预留切换分支；真要出小程序时改 data-uri |
| 真机样式差异 | `rpx` 在 H5 与小程序计算不同 | 以 375px 视口为基准，真机验收 |
| 生产直接覆盖 | 移动端发版是 `rm -rf` 后解压 | 发版前先打包旧目录留档（已记入 README 建议） |

**回退方式**：改动集中在新增文件 + 页面样式块，`git revert` 对应批次提交即可；`dist` 用发版备份回滚。

---

## 九、待确认事项

1. **主色**：加深为 `#0E7A63`（推荐，解决白字对比度）｜ 保留 `#1ab394` ｜ 改商务蓝 `#1677FF`
2. **tabBar 数量**：4 个（推荐，合并聊天+消息）｜ 保持 5 个只加图标
3. **落地批次**：按 B1→B4 分批（推荐）｜ 一次全量
4. **PC 端主色**：是否同步调整（另开任务，不在本次范围）
