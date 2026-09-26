-- 竞赛分析菜单（race 分支 · 年报智能解析与同业对比）
-- 依赖：race-api（Python :49000）提供服务；本 SQL 只挂菜单，幂等。

INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`deleted`)
SELECT 12910,'竞赛分析','',1,500,0,'race','ep:data-analysis','','',0,'','','','\0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE id=12910 AND deleted=0);

INSERT INTO `system_menu` (`id`,`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`component_name`,`status`,`visible`,`keep_alive`,`always_show`,`deleted`)
SELECT 12911,'同业对比','race:compare:query',2,1,12910,'compare','ep:histogram','race/compare/index','RaceCompare',0,'','','','\0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE id=12911 AND deleted=0);
