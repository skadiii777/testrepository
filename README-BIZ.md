# 企业管理系统（基于 RuoYi 4.8.3）

在 RuoYi（SpringBoot + Thymeleaf 前后端不分离版）基础上扩展的企业管理系统，包含三大业务域：

| 业务域 | 功能 |
| --- | --- |
| 客户合同产品 | 客户档案、产品台账、合同管理（草稿/执行中/已完成/已终止） |
| 进销存 | 供应商、采购单（入库自动加库存）、销售单（出库自动扣库存、库存不足拦截）、库存预警 |
| 人事考勤 | 员工档案、每日考勤、请假申请与审批（通过/驳回） |

新增代码位置：

- `ruoyi-business/` — 新 Maven 业务模块（domain / mapper / service / controller）
- `ruoyi-admin/src/main/resources/templates/biz/` — 10 个业务页面（列表/新增/编辑）
- `sql/biz.sql` — 业务建表 + 菜单 + 字典脚本
- `tools/gen_biz.py` — 代码生成脚本（改字段后可重新生成）

## 运行步骤

1. **数据库**：MySQL 中先导入 `sql/ry_20260319.sql`（RuoYi 基础库），再导入 `sql/biz.sql`（业务表/菜单/字典）。
2. **数据源**：修改 `ruoyi-admin/src/main/resources/application-druid.yml` 中 master 的 url/username/password。
3. **JDK**：本工程需要 JDK 17（Spring Boot 4.1）。
4. **启动**：运行 `ruoyi-admin` 下的 `RuoYiApplication`，或命令行：
   ```bash
   mvn clean package -DskipTests
   java -jar ruoyi-admin/target/ruoyi-admin.jar
   ```
5. **访问**：浏览器打开 `http://localhost:8080`，默认账号 `admin / admin123`。
   登录后左侧出现「企业管理」目录：客户合同产品 / 进销存管理 / 人事考勤。

## 业务规则说明

- 采购单保存为「已完成」状态时，自动向「默认仓库」增加该产品库存（无记录则自动创建）。
- 销售单保存为「已完成」状态时，自动扣减库存；库存不足会提示「库存不足，无法出库」。
- 请假单默认「待审批」，列表页对待审批记录提供「通过 / 驳回」一键审批（权限 `biz:leave:audit`）。
- 库存页的「预警下限」字段可用于后续做库存预警（当前仅记录）。

## 权限

每个功能含 `:view/:list/:add/:edit/:remove` 按钮级权限，通过 `sql/biz.sql` 中的菜单自动注册；
新角色在【系统管理-角色管理】里勾选「企业管理」相关菜单即可授权。
