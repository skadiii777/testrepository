#!/bin/bash
# 每日备份：DB 全量（保留7天）+ jar 备份轮转（保留2份）+ 磁盘水位记录
# 服务器路径 /data/backup/db.sh，cron: 30 2 * * *
set -e
DBPW=$(grep MYSQL_PW /root/enterprise-credentials.txt | cut -d= -f2)
mkdir -p /data/backup/db /data/backup/jar

mysqldump -uenterprise -p$DBPW -h127.0.0.1 --single-transaction --default-character-set=utf8mb4 --no-tablespaces "enterprise-pro" | gzip > /data/backup/db/enterprise-pro-$(date +%F).sql.gz
find /data/backup/db -name "*.sql.gz" -mtime +7 -delete

# jar 发版备份轮转：只保留最新 2 份（上次 40G 盘被多份 185MB jar 备份吃满的教训）
ls -t /data/backup/jar/enterprise-server-*.jar 2>/dev/null | tail -n +3 | xargs -r rm -f

# 磁盘水位：超 80% 记告警日志
df -h / | tail -1 > /data/backup/disk.log
usage=$(df / | tail -1 | awk '{print int($5)}')
if [ "$usage" -ge 80 ]; then
  echo "[$(date '+%F %T')] 磁盘使用率已达 ${usage}%，请清理" >> /data/backup/disk-alert.log
fi
