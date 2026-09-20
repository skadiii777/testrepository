# 企业管理系统 · 移动工作台（enterprise-pro-app）

基于 uni-app（Vue3 + Vite）的员工移动端，当前以 **H5** 形态发布，生产部署于 `http://8.155.128.225/m/`（与 PC 端同源，免 CORS）。同一套代码后续可加微信小程序 / App 构建目标（需备案域名 + HTTPS）。

## 功能页面

- **聊天**（tabBar 首位，登录直达）：好友会话列表 + 私聊对话（文本消息，8s 增量轮询）
- **工作台**：上下班打卡、假期余额、快捷入口、审批角标
- **四件套**：我的请假（销假）/ 报销（撤回）/ 补卡（撤回）/ 业务汇报（删除）
- **审批中心**：请假/报销/补卡三页签，通过/驳回（无权限显示提示）
- **消息**：站内信（未读点、全部已读）；**我的**：登出、入口导航

## 开发与构建

```bash
npm install
npm run dev:h5          # 本地开发（vite 代理 /admin-api → 生产后端）
npm run build:h5        # 生产构建 → dist/build/h5（manifest 已配 router.base=/m/ + history）
```

## 发布流程

```bash
npm run build:h5
tar czf /tmp/app-m.tgz -C dist/build/h5 .
scp /tmp/app-m.tgz root@<ECS>:/tmp/app-m.tgz
ssh root@<ECS> 'rm -rf /data/enterprise/mobile/* && tar xzf /tmp/app-m.tgz -C /data/enterprise/mobile'
```

- nginx 的 `/m/` location 已配（alias `/data/enterprise/mobile/` + history 回退），见后端仓库 `deploy-backup/enterprise.conf`
- **必须先 `build:h5` 再打包**，禁止复用旧 dist（同 Web 端发版规矩）

## 约定

- 接口复用后端 `/admin-api`（`src/api/index.ts`），鉴权 Bearer + `tenant-id: 1`
- 登录/租户/字典等约定见 `AGENTS.md`
- 关联仓库：`enterprise-pro`（后端）、`enterprise-pro-ui`（Web 前端）
