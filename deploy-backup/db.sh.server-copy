#!/bin/bash
# 每日备份 enterprise-pro 库，保留 14 天（服务器路径 /data/backup/db.sh，cron: 30 2 * * *）
set -e
PW=$(grep MYSQL_PW /root/enterprise-credentials.txt | cut -d= -f2)
F=/data/backup/enterprise-pro-$(date +%F).sql.gz
mysqldump -uenterprise -p$PW --single-transaction --default-character-set=utf8mb4 --no-tablespaces enterprise-pro | gzip > $F
find /data/backup -name 'enterprise-pro-*.sql.gz' -mtime +14 -delete
