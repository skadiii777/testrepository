# 企业管理系统

基于 Spring Boot + Shiro + MyBatis + Thymeleaf 的企业管理系统，包含三大业务域：

- **客户合同产品**：客户档案、产品台账、合同管理
- **进销存**：供应商、采购入库、销售出库、库存管理（出入库自动联动库存）
- **人事考勤**：员工档案、考勤记录、请假审批

## 快速开始

1. MySQL 导入 `sql/ry_20260319.sql`（基础库）和 `sql/biz.sql`（业务库）
2. 修改 `ruoyi-admin/src/main/resources/application-druid.yml` 数据源
3. 启动 `ruoyi-admin` 的 `RuoYiApplication`（需 JDK 17）
4. 访问 `http://localhost:8090`，默认账号 `admin / admin123`

详见 `README-BIZ.md`。
