# 企业管理系统 · Web 前端（enterprise-pro-ui）

`enterprise-pro` 后端的管理台前端，基于 yudao-ui-admin-vue3 脚手架深度定制（Vue3 + Element Plus + Vite + TypeScript），覆盖 CRM、进销存、人事考勤、财务 FMS、仓储 WMS、审批中心、数据看板等业务模块。

## 技术栈

| 项 | 版本 |
| --- | --- |
| Vue | 3.5.34 |
| Vite | 8.1.4 |
| Element Plus | 2.13.7 |
| TypeScript | 6.0.3 |

## 快速开始

```bash
npm install --legacy-peer-deps
npm run dev            # 本地开发（默认 80 端口，代理 /admin-api → 后端 48080）
```

## 构建与发布

```bash
npm run build:prod     # 生产构建 → dist-prod/（注意：无裸 build 脚本）
```

发布规矩（详见后端仓库 DEPLOY-ALIYUN.md 阶段 5.1）：
- **必须先 `npm run build:prod` 再打包 dist-prod**，禁止复用上次构建产物（曾因此把旧 bundle 发上生产导致页面缺功能）
- 产物上传 `/data/enterprise/front`，index.html 由 nginx 配 no-cache、/assets/ immutable
- 本地仓库是唯一事实源，严禁在服务器上直接改代码

## 目录约定

- `src/views/biz/*` 业务模块页面；`src/views/portal/*` 员工自助工作台
- `src/api/*` 接口封装（与后端 `/admin-api` 一一对应）
- 新增 views 文件后需重启 vite dev（路由 glob 在启动时扫描）

## 关联仓库

| 仓库 | 说明 |
| --- | --- |
| `enterprise-pro` | 后端（Spring Boot 3.5 多模块），本仓库接口契约的事实源 |
| `enterprise-pro-app` | 移动工作台（uni-app Vue3），生产部署于 `/m/` 子路径 |

## 开发注意

- 项目约定、环境要点见仓库根 `AGENTS.md`（含 git 事故教训与前端坑清单）
- 类型检查：`npm run ts:check`（存量错误清零前以 CI 告警处理）
- Element Plus 组件在浏览器自动化下需真实事件链，`evaluate el.click()` 可能不更新 v-model
