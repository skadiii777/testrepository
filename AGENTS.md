# enterprise-pro-app（移动工作台 · uni-app Vue3）

> 本文件随 git 提交，**禁止写入密码/密钥**。共享规则见上级 `..\AGENTS.md` 与后端仓库 `enterprise-pro/AGENTS.md`。

## 工程要点

- uni-app Vue3 + Vite（@dcloudio 3.x），当前只构建 H5；`manifest.json` h5.router.base=`/m/` + history 模式
- `src/utils/request.ts`：uni.request 封装——自动 Bearer + `tenant-id: 1`、CommonResult 解包、401 统一 reLaunch 登录页；`del()` 是真 DELETE（撤回类接口用），`put()` 用于站内信已读
- `src/utils/dict.ts`：内置兜底字典（与生产 system_dict_data 一致）+ 登录后 simple-list 刷新
- 登录：`/system/auth/login`（生产 captcha 关闭），token 存 uni storage；登录后 reLaunch 到聊天页（tabBar 首位）

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
