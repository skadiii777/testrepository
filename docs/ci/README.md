# CI 配置（待启用）

`github-actions-ci.yml` 是后端仓库的 GitHub Actions 工作流，**当前处于未启用状态**。

## 为什么放在这里而不是 `.github/workflows/`

推送时 GitHub 返回：

```
! [remote rejected] main -> main
(refusing to allow a Personal Access Token to create or update workflow
 `.github/workflows/ci.yml` without `workflow` scope)
```

本机保存的 PAT 权限为 `X-OAuth-Scopes: repo`，**缺少 `workflow` scope**。
GitHub 规定 classic PAT 没有该 scope 就不允许创建或修改 `.github/workflows/` 下的文件，
因此工作流无法通过该凭据推送。放在 `docs/ci/` 下不受此限制，可以先纳入版本控制与备份。

## 启用步骤

二选一：

**方式 A：给 PAT 补 `workflow` scope**

1. GitHub → Settings → Developer settings → Personal access tokens → 编辑该 token
2. 勾选 **`workflow`**，保存
3. 执行：

```bash
cd E:/AI-Code/enterprise-pro
mkdir -p .github/workflows
git mv docs/ci/github-actions-ci.yml .github/workflows/ci.yml
git commit -m "ci: 启用 GitHub Actions"
git push origin main
```

**方式 B：改用 SSH（推荐）**

SSH 密钥没有 scope 概念，可绕开该限制；同时能规避本机到 GitHub 的 HTTP 代理不稳定问题。

1. 把公钥 `~/.ssh/id_github_enterprise.pub` 添加到 GitHub（Settings → SSH and GPG keys）
2. 切换远程地址：

```bash
git remote set-url origin git@github.com:skadiii777/testrepository.git
```

3. 再执行方式 A 的第 3 步

## 工作流内容

| job | 作用 |
|---|---|
| `build` | JDK 17 + Maven 缓存 → `mvn -B -DskipTests compile` → `mvn -B -DskipTests package` |
| `secret-scan` | 扫描受版本控制的配置文件中是否残留明文密钥 |

两个 job 的命令与扫描规则均已在本地实测通过：全项目 `compile`（3:55）与 `package`（3:45）
各 BUILD SUCCESS；扫描规则当前 0 命中，并用含明文密码的样例文件反向验证确认规则有效。

工作流文件末尾注释列出了**尚未纳入 CI 的检查**（集成测试需真实 MySQL、SpotBugs/Spotless/JaCoCo
存量问题未清理、前端为独立仓库），避免被误读为"已全覆盖"。
