# enterprise-pro-ui（企业平台前端 · Vue3 + Element Plus + vite）

> 共享规则（大动作先征求同意等）见 `..\AGENTS.md`。本文件随 git 提交，**禁止写入密码/密钥**。

## 技术栈与运行

- yudao-ui-admin-vue3 底座；dev 跑 80 端口（VITE_BASE_URL=http://localhost:48080）；构建 `npm run build:prod` → dist-prod（**发版必须重新构建，禁复用旧产物**）
- npm install 须 `--legacy-peer-deps`（npmmirror 源）；vite.config.ts watch 已排除 .mimosa/node_modules（EBUSY 坑）
- 上游 15 个未用模块目录已裁剪；裁剪/新增模块必须同步 `remaining.ts` 静态路由段 + 顶部 import（KEEP 白名单勿漏 /im）
- 组件体系：src/components/** 由 unplugin-vue-components 自动注册；store 不在 auto-import 需显式 import；Echart 用 `import { Echart } from '@/components/Echart'`
- 路由 = 菜单父链拼接（如 `/biz/inventory/wms-location`），直接拼短路径会 404
- **transition 勿包 keep-alive 路由出口**（多根节点页面白屏，已回退过一次 5bc77f9）

## 事故教训（2026-09-15 两次 · 最高警惕）

1. `git rebase` 超时被强杀 → 中断自动 gc → refs/loose objects 被清，19 个本地提交历史不可恢复（工作区源码无损，已重建为单 commit 7fa85ab 推送）。**可能超时的 git 操作必须 `-c gc.auto=0` 或放后台给足时间**
2. `git rm -r` 越界删除：本意删 93 个文件，实际清空 330 个（src/components 整树 + src/api 91 个）。**沙箱删除类操作（rm -rf/mv/git rm -r）不可靠，删除前列表二次确认**
- 恢复利器：`git restore --source=HEAD --staged --worktree .`
- 安全副本 `E:\AI-Code\enterprise-pro-ui-SAFE-20260915` **务必保留**
- GitHub 推送：`-c http.version=HTTP/1.1` + 后台重试；.github/workflows 需 PAT workflow scope（CI 工作流暂在 docs/ci/）

## 新页面/改页面注意

- 提交表单必须传 `formData.value`（直接 stringify RefImpl 会循环引用报错——2026-09-09 全站 38 处教训）；**每个新页面必须浏览器实测一次提交动作**（API 冒烟发现不了）
- el-select 在自动化里需真实鼠标事件链（cua 坐标点击 option 可靠）；凭证页分录行下拉要按"可见面板过滤"选
- 新增 views 文件后必须重启 vite dev（import.meta.glob 不热感知）
- 权限按钮 `v-hasPermi` 的权限串必须先落菜单表，否则界面按钮被移除而 API 冒烟全绿（2026-09-08 教训）
