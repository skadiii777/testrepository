#!/usr/bin/env bash
# 阶段一：把后端 main 与前端 pro-ui 推到自有远程仓库。
# 本机到 GitHub 的代理不稳定（502 / schannel / Empty reply 交替），故带多次重试。
#
# 成功判定：本地分支 SHA == 远程分支 SHA（不能只看分支是否存在——main 本就在远程存在，
# 只看存在性会误判成功；也不能用 `git push | tail` 的 $?，那是 tail 的退出码）。
set -u

GIT_OPTS=(-c http.version=HTTP/1.1 -c http.postBuffer=524288000
          -c http.lowSpeedLimit=1000 -c http.lowSpeedTime=90)

# push_repo <目录> <远程名> <分支> <标签> <尝试次数>
push_repo() {
  local dir="$1" remote="$2" branch="$3" label="$4" tries="${5:-12}"
  local attempt rc local_sha remote_sha

  cd "$dir" || { echo "[$label] 目录不存在: $dir"; return 1; }

  for attempt in $(seq 1 "$tries"); do
    echo "=== [$label] 尝试 $attempt/$tries $(date '+%H:%M:%S') ==="
    timeout 240 git "${GIT_OPTS[@]}" push -u --progress "$remote" "$branch" 2>&1 | tail -5
    rc=${PIPESTATUS[0]}

    if [ "$rc" -eq 0 ]; then
      local_sha=$(git rev-parse "$branch" 2>/dev/null)
      remote_sha=$(timeout 45 git ls-remote "$remote" "refs/heads/$branch" 2>/dev/null | cut -f1)
      if [ -n "$local_sha" ] && [ "$local_sha" = "$remote_sha" ]; then
        echo "=== [$label] ✅ 成功，SHA 一致: $local_sha ==="
        return 0
      fi
      echo "[$label] 推送返回 0 但 SHA 不一致（本地 $local_sha / 远程 $remote_sha），继续重试"
    else
      echo "[$label] 推送失败 rc=$rc，等待后重试"
    fi
    sleep 20
  done

  echo "=== [$label] ❌ $tries 次尝试均失败 ==="
  return 1
}

rc_all=0
push_repo "/e/AI-Code/enterprise-pro-ui" github pro-ui "frontend-pro-ui" 14 || rc_all=1
push_repo "/e/AI-Code/enterprise-pro"    origin main   "backend-main"    4  || rc_all=1

echo "ALL_DONE rc=$rc_all"
