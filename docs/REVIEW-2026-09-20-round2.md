# 复核报告落地情况 · 第二轮复核（2026-09-20 晚）

> 复核对象：`docs/REVIEW-2026-09-20.md` 所列问题的修复结果
> 复核方式：三仓库 git 实况核查 + 逐文件读改后内容 + 明文凭据扫描 + 迁移工具语义检查。**只读，未修改任何文件**
> 前序：`docs/REVIEW-2026-09-20.md`（首轮）、`docs/CODE-REVIEW-2026-09-15.md`

---

## 一、总体结论

**本轮修复质量高，首轮报告的 P0 全部闭环，文档体系从"失真"转为"可信"。**

| 维度 | 首轮 | 本轮实测 |
|---|---|---|
| 备份风险 | 移动端零远程、后端 1 commit 未推、前后端 remote-tracking 引用缺失 | 移动端已推 `app` 分支且同步；后端 `origin/main` 左右计数 `0 0`；前端跟踪已恢复 |
| 文档失真 | 3 份核心文档与实现不符 | README 模块表补齐、DEPLOY-ALIYUN 四处修正、前端 README 重写、MIGRATION-STATUS 加归档声明 |
| 凭据卫生 | 生产配置/本地配置多处明文 | 共享 YAML 与 local profile 参数化，原值落 gitignore 的私有 properties；**但仍有 2 处遗漏，见第三节** |
| 工程化 | CI 缺失、测试偏科 | 无变化（CI 受 PAT scope 外部阻塞；测试未动） |

三个仓库当前状态：

| 仓库 | HEAD | 远程 | 同步状态 |
|---|---|---|---|
| enterprise-pro | `118b0e1` | `origin/main` | ✅ 0 / 0 |
| enterprise-pro-ui | `ed200b9` | `github/pro-ui` = `7fa85ab` | ⚠️ **本地领先 1 个提交** |
| enterprise-pro-app | `d62567a` | `github/app` | ✅ 0 / 0 |

---

## 二、已闭环项（首轮建议 → 实测证据）

| # | 首轮建议 | 实测证据 | 状态 |
|---|---|---|---|
| R1 | 移动端建远程并推送 | `refs/remotes/github/app = d62567a` = HEAD；`package.json` 已改 `enterprise-pro-app` / `1.0.0` / 带 description；新增 README（38 行）与 AGENTS（28 行），内容含页面清单、构建/发布流程、业务语义坑 | ✅ |
| R2-a | README 业务模块表补齐 | 新增 FMS / WMS / IM 行，CRM 补线索商机跟进、进销存补盘点退货、协作审批补注册审批、聚合补业绩目标与公告提醒；新增"移动工作台"说明 | ✅ |
| R2-b | 登录凭据说法冲突 | README 新增"登录凭据按场景区分"三条（空库/本地/生产），且明确"生产凭据不写入任何仓库文件"；`AGENTS.md` 同步改为"admin + 本地开发密码（见个人配置）" | ✅ |
| R2-c | `npm run build` 不存在 | `DEPLOY-ALIYUN.md:78` 改 `npm run build:prod` 并注明"package.json 无裸 build 脚本" | ✅ |
| R2-d | `.env.production` 与实际不符 | `:77` 改 `.env.prod`，并说明 `VITE_BASE_URL=''` 即同源 | ✅ |
| R2-e | nginx 段缺 WS / no-cache | `:81-89` 改为"直接使用 `deploy-backup/enterprise.conf` 整文件"，并**显式警告** `nginx-enterprise.conf` 是 09-07 前过期版本、照抄会复发 IM 推送失效与发版白屏 | ✅ 超出建议 |
| R2-f | 明文密码 + 规格不符 | `:32` 改 `mysqldump -uroot -p`（交互式）并加"密码不落盘"注释；`:7` 改 2核8G 起步（现生产 2C8G） | ✅ |
| R2-g | 前端 README 去上游化 | 391 行 → 精简为项目自身说明，版本更正为 Vue 3.5.34 / Vite 8.1.4 / Element Plus 2.13.7 / TS 6.0.3，含发布规矩与关联仓库 | ✅ |
| R2-h | MIGRATION-STATUS 归档 | 文件头加"⚠️ 历史归档（2026-09-04~09-06）"，指向 CHANGELOG 与 AGENTS，并声明环境表已过期 | ✅ |
| R4 | 前端规矩文件入库 | `ed200b9` 提交含 `AGENTS.md` + `CLAUDE.md` + `.github/copilot-instructions.md`；`git status` 已干净 | ✅ 已提交（未推送，见下） |
| R3 | 同仓多历史 | `AGENTS.md:46-55` 新增 testrepository 分支表（main / pro-ui / app / master），标注 master 为历史遗留"勿动勿推"，并声明长期方向是三项目独立仓库 | ✅ 已缓解 |
| N7 | `.env.prod` 残留 | 删除 6 行（`VITE_MALL_H5_DOMAIN` 与 GoView 残留） | ✅ |
| N8 | captcha 键名不一致 | `DEPLOY-ALIYUN.md:46` 统一为 `enterprise.captcha.enable`，并注明 `captcha-enable` 是错误写法 | ✅ |
| P0-3 | 明文凭据收尾 | `application-local.yaml:252` 改 `${ENTERPRISE_BASE_TENCENT_LBS_KEY:}`，原值落 `application-private.properties`（已 gitignore） | ✅ |
| P2-6 | 品牌残留 | 根 `pom.xml:43` `<url>` 改为自有仓库 | ✅ |
| P1-6 | 事务内 RPC | 操作人昵称改 `ConcurrentHashMap` 缓存，事务持锁期最多一次 RPC；docstring 写明取舍 | ⚠️ 部分（见第三节） |
| P1-7 | migrate.py 校验和 | 改 `inspect.getsource(migrate_identity) + migrate_finance` 内容哈希；版本号收敛为 `MIGRATION_VERSION` 常量；`import inspect` 与常量定义均已在位 | ⚠️ 部分（见第三节） |
| — | 报告入库 | `docs/REVIEW-2026-09-20.md` 已入库并推送；`docs/AI-MEMORY-ARCHIVE.md` 已 `git rm --cached` 并加入 `.gitignore`（实测 `ls-files --error-unmatch` 报未跟踪） | ✅ |

**附带确认**：`git ls-files` 扫描 `application-private.properties` / `server.log` / `*.jar` 均无命中，说明敏感文件确未入库。

**额外动作（首轮未要求）**：CHANGELOG 记录已在生产做了部署冒烟（新 jar 上线、采购 0.01 → 凭证 `JZ260920160414NZ`、WMS 建库位→putaway→remove 全链路、operator_name 缓存路径两次操作均生效）。代码改动经过真机验证，这是本轮最有力的质量证据。

---

## 三、遗留与待讨论（6 项）

### L1 · 前端 1 个提交未推送 ⚠️

- 实测：`github/pro-ui = 7fa85ab`，本地 HEAD = `ed200b9`，`rev-list` 左右计数 `0 1`
- CHANGELOG 记为"UI 按约定暂不推送远程"，但**该提交正是本轮新增的前端 AGENTS.md（含 09-15 两次 git 事故教训）+ 重写后的 README**
- 风险：这些知识目前仍只存在本机磁盘 —— 与首轮 R4 的诉求（防止知识丢失）相同，只是状态从"未提交"变成"未推送"
- 建议：若无明确技术阻碍，`git -c http.version=HTTP/1.1 push github pro-ui`；如确有意暂缓，建议在 CHANGELOG 写明理由与计划推送时间

### L2 · 明文凭据仍有 2 处遗漏 🟠

本轮脱敏按 CHANGELOG 记了 3 处（`admin<弱口令>` / `-p<弱口令>` / 地图 key），但扫描发现：

| 文件 | 残留 | 说明 |
|---|---|---|
| `MIGRATION-STATUS.md:20/23/63/93/95` | `root/<弱口令>`、`admin/<弱口令>` 共 5 处 | 该文件**随 git 提交**，且本次修改正好又提交了一次；文件头已加归档声明，但密码未脱敏 |
| `application-dev.yaml:179` | 腾讯地图 key 明文（与 `application-local.yaml` 已参数化的写法不一致） | dev profile 虽本地用，但文件入库即明文进 git 历史 |

- 说明：`docs/AI-MEMORY-ARCHIVE.md`（含同类明文）已通过 gitignore + `rm --cached` 处理干净，说明处理手法已有，此处属遗漏
- 另有大量上游自带的示例值（`@Schema(example="123456")`、`.http` 文件、`sql/tools/*`、`ruoyi-vue-pro.sql` 种子初始密码）—— 属上游遗留、非项目凭据，可不处理，但建议在 CI 的 secret-scan 里显式列入白名单，避免告警疲劳
- 建议：脱敏上述 2 处；历史中已存在的地图 key 按原 CHANGELOG 待办做"真实性确认 + 轮换"

### L3 · migrate.py 校验和由硬失败降级为软提示 🟠（需决策）

- 现状：`_run_locked` 中校验和不匹配时，由原先的 `raise RuntimeError` 改为 `print` 一行提示后 `return []`
- 合理性：历史记录的校验和出自旧算法（整脚本哈希），无法与新算法比对，不降级会让所有老库无法运行 —— 这个判断正确
- 但副作用：`HARDENING-2026-09-11.md:23` 明确写的规矩"**已应用版本的脚本不可再编辑**"失去技术强制，退化为口头约定；改动迁移逻辑后重跑不会再被拦截
- 建议二选一：
  1. 过渡方案：加一次性迁移，把历史 checksum 重写为新算法的值并打标记（如 `algo='content-v1'`），之后恢复硬校验
  2. 折中方案：保留提示，但在 `--apply` 时对不匹配项要求显式 `--ack-checksum-change` 才继续

### L4 · P1-6 的修法仍是缓解而非根除 🟡

- 现状：`ConcurrentHashMap<Long,String> operatorCache` 让"事务持锁期内最多一次 RPC"
- 残留：**首次**调用仍在 `@Transactional` 内、仍持有产品行锁；缓存无失效与容量上限（用户量小可接受，但与"无界缓存"的通用风险一致）
- 建议：把 `resolveOperatorName()` 提到事务方法入口（锁之前）执行并传参；或直接取 SecurityContext 中的登录用户昵称（yudao 上下文已带，可不发 RPC）。缓存加注释说明预期规模上限

### L5 · 首轮 P1 中仍未处理的三项 ❌

| 编号 | 项 | 现状 |
|---|---|---|
| P1-3 | CI 启用 | `.github/` 下只有 `copilot-instructions.md`，**无 `workflows/`**；工作流仍在 `docs/ci/`（受 PAT `workflow` scope 阻塞，属外部约束） |
| P1-4 | 测试覆盖 | `enterprise-module-biz/src/test` 仍只有 `BizConcurrencyTest.java` 1 个文件；FMS/WMS/CRM/考勤/门户零测试 |
| P1-5 | 全表捞 + N+1 | `FmsVoucherServiceImpl.getAccountBalances()` 仍是"取全部已记账凭证 → 取全部分录 → 内存聚合" |
| P1-9 | AI 模块死代码 | 根 `pom.xml` 中 `enterprise-module-ai` 仍为注释态，`docs/RAG-LEARNING-GUIDE.md` 未标注该状态 |

### L6 · 后端 `AGENTS.md` 状态节未同步 🟡

- `AGENTS.md:39-44`「当前状态（2026-09-15）」仍写"**后端 14+ commit 未推 GitHub**"，实测已是 `0 0` 完全同步；同段"前端 pro-ui 已推远程（单 commit 7fa85ab）"也已过时（现 `ed200b9`，领先 1）
- 建议：把该节改为「当前状态（2026-09-20）」，或改为指向 CHANGELOG 的指针，避免同类过期（这正是首轮指出的"文档失真"模式的复发点）

---

## 四、本轮改动引入的新观察（4 项，均为轻微）

| # | 观察 | 位置 | 建议 |
|---|---|---|---|
| O1 | 移动端 README 写 `npm run dev:h5`「代理 /admin-api → **生产后端**」 | `enterprise-pro-app/README.md:17` | 本地开发直连生产后端存在误写生产数据的风险，建议默认指向本地 48080，并在 AGENTS 标注"连生产仅限只读验证" |
| O2 | 移动端 README 发布脚本含 `rm -rf /data/enterprise/mobile/*` | `:27` | AGENTS.md 已写"先备份"，README 脚本未体现；建议脚本改为"先 tar 备份旧目录再解压" |
| O3 | 前端 README 称 `npm run ts:check`「存量错误清零前以 CI 告警处理」 | `enterprise-pro-ui/README.md:48` | 当前 `node_modules` 中未解析到 `vue-tsc`，该命令在本机跑不起来；且 CI 尚未启用。建议标注"待依赖对齐后生效" |
| O4 | 后端 AGENTS 写"生产验证 = 16 项 JUnit（tools/test_biz.py 自建 QA 库）" | `enterprise-pro/AGENTS.md:22` | `tools/test_biz.py` 是 Python 脚本，非 JUnit；表述易误导，建议改为"16 项集成用例" |

---

## 五、建议下一步（按优先级）

1. **P0**：推送前端 `pro-ui` 的 `ed200b9`（或写明暂缓理由）
2. **P0**：脱敏 `MIGRATION-STATUS.md` 5 处与 `application-dev.yaml` 1 处
3. **P1**：决策 migrate.py 校验和策略（L3 二选一）
4. **P1**：更新后端 `AGENTS.md` 的「当前状态」节
5. **P2**：P1-6 根除（操作人昵称移出事务）、P1-5 查询下推、P1-4 补 FMS/WMS 测试
6. **P3**：CI 启用（需先补 PAT `workflow` scope 或改 SSH）

---

*本轮复核基于 2026-09-20 晚的仓库与文件实况；所有结论附文件路径/行号或命令计数证据。*
