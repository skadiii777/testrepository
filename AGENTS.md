# enterprise-pro-app（移动工作台 · uni-app Vue3）

> 本文件随 git 提交，**禁止写入密码/密钥**。共享规则见上级 `..\AGENTS.md` 与后端仓库 `enterprise-pro/AGENTS.md`。

## 工程要点

- uni-app Vue3 + Vite（@dcloudio 3.x），当前只构建 H5；`manifest.json` h5.router.base=`/m/` + history 模式
- `src/utils/request.ts`：uni.request 封装——自动 Bearer + `tenant-id: 1`、CommonResult 解包、401 统一 reLaunch 登录页；`del()` 是真 DELETE（撤回类接口用），`put()` 用于站内信已读
- `src/utils/dict.ts`：内置兜底字典（与生产 system_dict_data 一致）+ 登录后 simple-list 刷新
- 登录：`/system/auth/login`（生产 captcha 关闭），token 存 uni storage；登录后 reLaunch 到聊天页（tabBar 首位）

## UI 设计系统（2026-09-21 改版）

- **主色 `#0E7A63`**（原 `#1ab394`，白底对比度 2.3:1 → 4.9:1）。**唯一来源** `src/styles/tokens.scss`，
  经 `src/uni.scss` 全局注入，页面 `<style lang="scss" scoped>` 直接用 `$c-primary` / `$sp-3` / `$r-md`。
  改主色只需改这一处；tabBar 图标同源色见 `tools/gen_tabbar_icons.py` 的 `ON` 常量。
- **⚠️ uni.scss 陷阱**：uni.scss 内容会被**内联注入到每个组件的 style 块**，所以引用必须写
  `@use '@/styles/tokens.scss' as *;`，**不能用相对路径 `./`** —— 相对路径会按各组件所在目录解析，
  在 `pages/*/` 下会报 `Can't find stylesheet to import`。用 `@use`（非 `@import`）可避免
  Dart Sass 的弃用警告刷屏。
- **新依赖 `sass`**（devDependency）：项目原本是纯 CSS，2026-09-21 引入 scss 变量体系，缺它构建直接失败。
- **图标统一走 `components/AppIcon.vue`**（内联 SVG，`name` + `size` + `color`）。内联 `<svg>` **仅 H5 有效**，
  要出微信小程序需改走 image + data-uri 或字体图标。
- **tabBar 图标是位图**（uni-app 的 iconPath 不支持 svg/base64），由 `tools/gen_tabbar_icons.py`
  程序化生成 8 个 PNG 到 `src/static/tabbar/`，改色/改形后重跑该脚本即可。
- **tabBar 5→4**：消息 / 工作台 / 审批 / 我的。原 `pages/chat`（好友会话列表）**保留但不再是 tab**，
  会话列表已并入 `pages/message` 的「聊天 | 通知」分段；登录落地页从 chat 改为 message。
  改 tabBar 数量时务必全量 grep `switchTab` / `reLaunch` 核对跳转目标。
- **组件库**（`src/components/`，需显式 import，未配 easycom）：`AppIcon` / `Avatar`（首字头像+稳定取色+在线点）/
  `Cell`（列表单元格）/ `SegmentTabs`（分段+计数）/ `EmptyState`（空态+导引）。
- **设计变量速查**：主色 `$c-primary`、危险 `$c-danger`、文字三级 `$c-text-1/2/3`、
  圆角 `$r-sm/md/lg`、间距 `$sp-1..6`（8 栅格）、字号 `$fs-cap..xl`、阴影 `$shadow-card`。

## 业务语义坑

- 请假「销假」按钮仅 status=1（已通过）可用；报销/补卡「撤回」= DELETE 且仅 status=0
- IM 消息 content 是 JSON 字符串（TEXT type=101 → `{"content":"..."}`）；sendTime 兼容 epoch 毫秒与 LocalDateTime 字符串
- uni H5 路由中文参数会二次编码——会话标题不传 name，由 chatroom 拉好友列表解析
- uni-input 内层真实 input 依赖外层显式高度（`.form-input` 全局给了 88rpx，勿删）
- yudao 对未知路径返回 HTTP 200 + code:404 壳，判错看响应体别看状态码

## 发版

- `npm run build:h5` → tar `dist/build/h5` → scp → 解压 `/data/enterprise/mobile`（先备份）
- **必须先 build 再打包，禁复用旧 dist**（Web 端同规矩，曾因此发过旧包）
- 验证：手机/窄视口走一遍登录→打卡→提单→审批→消息

## 远程备份

- 推到后端仓库 testrepository 的 `app` 分支（分支约定见后端 AGENTS.md，勿推 master）
