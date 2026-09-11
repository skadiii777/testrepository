package com.enterprise.module.biz;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.spring.MybatisSqlSessionFactoryBean;
import com.enterprise.framework.common.exception.ServiceException;
import com.enterprise.framework.tenant.core.context.TenantContextHolder;
import com.enterprise.module.biz.controller.admin.payment.vo.payment.PaymentSaveReqVO;
import com.enterprise.module.biz.dal.mysql.payment.PaymentMapper;
import com.enterprise.module.biz.dal.mysql.sales.SalesMapper;
import com.enterprise.module.biz.service.payment.*;
import com.enterprise.module.biz.service.stock.*;
import com.enterprise.module.biz.service.sales.*;
import com.enterprise.module.biz.service.purchase.*;
import com.enterprise.module.biz.service.quota.*;
import com.enterprise.module.biz.service.leave.*;
import com.enterprise.module.biz.service.returnorder.*;
import com.enterprise.module.biz.service.stockcheck.*;
import com.enterprise.module.biz.service.support.*;
import com.enterprise.module.bpm.api.task.BpmProcessInstanceApi;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.*;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.*;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import static org.junit.jupiter.api.Assertions.*;

/** Real service proxies, MyBatis, tenant interceptor and InnoDB transactions; never mock SQL locks. */
@SpringJUnitConfig(BizConcurrencyTest.Config.class)
@org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable(named="BIZ_TEST_JDBC_URL", matches=".*enterprise_pro_qa_.*")
class BizConcurrencyTest {
    @Configuration
    @EnableTransactionManagement
    @MapperScan("com.enterprise.module.biz.dal.mysql")
    static class Config {
        @Bean DataSource dataSource() {
            String url = System.getenv("BIZ_TEST_JDBC_URL");
            if (url == null || !url.matches("jdbc:mysql://[^/]+/enterprise_pro_qa_[A-Za-z0-9_]+(?:\\?.*)?"))
                throw new IllegalStateException("Only an isolated enterprise_pro_qa_ database is allowed");
            return new DriverManagerDataSource(url,System.getenv("BIZ_TEST_DB_USER"),System.getenv("BIZ_TEST_DB_PASSWORD"));
        }
        @Bean JdbcTemplate jdbcTemplate(DataSource ds) { return new JdbcTemplate(ds); }
        @Bean PlatformTransactionManager transactionManager(DataSource ds) { return new DataSourceTransactionManager(ds); }
        @Bean SqlSessionFactory sqlSessionFactory(DataSource ds) throws Exception {
            var factory = new MybatisSqlSessionFactoryBean();
            factory.setDataSource(ds);
            var config = new MybatisConfiguration();
            config.setMapUnderscoreToCamelCase(true);
            factory.setConfiguration(config);
            factory.setGlobalConfig(new GlobalConfig().setMetaObjectHandler(new com.enterprise.framework.mybatis.core.handler.DefaultDBFieldHandler()).setDbConfig(new GlobalConfig.DbConfig().setIdType(IdType.AUTO)));
            var interceptor = new MybatisPlusInterceptor();
            interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new TenantLineHandler() {
                public Expression getTenantId() { return new LongValue(TenantContextHolder.getRequiredTenantId()); }
            }));
            factory.setPlugins(interceptor);
            return factory.getObject();
        }
        @Bean PaymentService paymentService() { return new PaymentServiceImpl(); }
        @Bean StockService stockService() { return new StockServiceImpl(); }
        @Bean SalesService salesService() { return new SalesServiceImpl(); }
        @Bean PurchaseService purchaseService() { return new PurchaseServiceImpl(); }
        @Bean LeaveQuotaService quotaService() { return new LeaveQuotaServiceImpl(); }
        @Bean LeaveService leaveService() { return new LeaveServiceImpl(); }
        @Bean ReturnService returnService() { return new ReturnServiceImpl(); }
        @Bean StockCheckService stockCheckService() { return new StockCheckServiceImpl(); }
        @Bean BizReferenceService references() { return new BizReferenceService(); }
        @Bean BpmProcessInstanceApi bpm() { return org.mockito.Mockito.mock(BpmProcessInstanceApi.class); }
    }

    @Autowired JdbcTemplate jdbc;
    @Autowired PaymentService payments;
    @Autowired SalesService sales;
    @Autowired LeaveService leaves;
    @Autowired ReturnService returns;
    @Autowired StockService stock;
    @Autowired BizReferenceService references;
    @Autowired PaymentMapper paymentMapper;
    @Autowired SalesMapper salesMapper;
    @Autowired LeaveQuotaService quotas;
    @Autowired com.enterprise.module.biz.dal.mysql.quota.LeaveQuotaMapper quotaMapper;
    @Autowired PlatformTransactionManager transactions;
    static final long TENANT=91001;

    @BeforeEach void seed() {
        TenantContextHolder.setTenantId(TENANT);
        for (String table : List.of("biz_payment","biz_return","biz_stock_move","biz_stock","biz_sales","biz_purchase",
                "biz_leave","biz_leave_quota","biz_stock_check","biz_product","biz_employee","biz_warehouse"))
            jdbc.update("DELETE FROM "+table+" WHERE tenant_id IN (?,?)",TENANT,TENANT+1);
        jdbc.update("INSERT INTO biz_product(id,tenant_id,product_code,product_name,status) VALUES(91001,?,'QA','Shared name','0')",TENANT);
        jdbc.update("INSERT INTO biz_warehouse(id,tenant_id,name) VALUES(91001,?,'QA warehouse')",TENANT);
        jdbc.update("INSERT INTO biz_stock(id,tenant_id,product_id,warehouse_id,product_name,warehouse,quantity,min_quantity) VALUES(91001,?,91001,91001,'Shared name','QA warehouse',100,0)",TENANT);
        jdbc.update("INSERT INTO biz_sales(id,tenant_id,sales_code,product_id,warehouse_id,product_name,warehouse,quantity,price,total_amount,sales_date,status) VALUES(91001,?,'QA-S',91001,91001,'Shared name','QA warehouse',10,10,100,'2026-09-11','2')",TENANT);
        jdbc.update("INSERT INTO biz_employee(id,tenant_id,emp_no,emp_name) VALUES(91001,?,'QA-E','Same employee')",TENANT);
        jdbc.update("INSERT INTO biz_leave_quota(id,tenant_id,employee_id,emp_name,leave_type,year,quota_days,used_days) VALUES(91001,?,91001,'Same employee','3','2026',5,0)",TENANT);
        jdbc.update("INSERT INTO biz_leave(id,tenant_id,employee_id,emp_name,leave_type,start_date,end_date,days,status,creator) VALUES(91001,?,91001,'Same employee','3','2026-09-11','2026-09-13',3,'0','91001')",TENANT);
    }
    @AfterEach void clearContext() { TenantContextHolder.clear(); }

    PaymentSaveReqVO request(String id, int amount) {
        var req=new PaymentSaveReqVO();req.setPaymentType("1");req.setBizType("1");req.setOrderId(91001L);
        req.setAmount(BigDecimal.valueOf(amount));req.setPaymentDate("2026-09-11");req.setRequestId(id);return req;
    }
    <T> List<T> race(int count,Supplier<T> action) throws Exception {
        ExecutorService pool=Executors.newFixedThreadPool(count);
        CountDownLatch ready=new CountDownLatch(count),start=new CountDownLatch(1);
        try {
            List<Future<T>> futures=new ArrayList<>();
            for(int i=0;i<count;i++) futures.add(pool.submit(() -> {
                TenantContextHolder.setTenantId(TENANT);ready.countDown();
                try { if(!start.await(10,TimeUnit.SECONDS)) throw new AssertionError("start timeout");return action.get(); }
                finally { TenantContextHolder.clear(); }
            }));
            assertTrue(ready.await(10,TimeUnit.SECONDS));start.countDown();
            List<T> results=new ArrayList<>();for(var f:futures)results.add(f.get(30,TimeUnit.SECONDS));return results;
        } finally { pool.shutdownNow(); }
    }
    boolean accepted(Runnable work) { try { work.run();return true; } catch(ServiceException expected) { return false; } }
    long count(String table) { return jdbc.queryForObject("SELECT COUNT(*) FROM "+table+" WHERE tenant_id=?",Long.class,TENANT); }
    BigDecimal paid() { return payments.getPaidSumByOrder("1",91001L); }

    @Test void concurrentPaymentsCannotExceedOrder() throws Exception {
        var outcomes=race(8,() -> accepted(() -> payments.createPayment(request(UUID.randomUUID().toString(),60))));
        assertEquals(1,outcomes.stream().filter(Boolean::booleanValue).count());assertEquals(0,paid().compareTo(new BigDecimal("60")));
    }
    @Test void repeatedRequestReturnsSameRow() throws Exception {
        assertEquals(1,new HashSet<>(race(8,() -> payments.createPayment(request("same-request",60)))).size());assertEquals(1,count("biz_payment"));
        assertThrows(ServiceException.class,() -> payments.createPayment(request("same-request",50)));
    }
    @Test void reversalIsAppendOnlyAndExactlyOnce() throws Exception {
        Long id=payments.createPayment(request("original",60));
        assertEquals(1,new HashSet<>(race(8,() -> payments.reversePayment(id,"Wrong amount"))).size());
        assertEquals(2,count("biz_payment"));assertEquals(0,paid().signum());assertNotNull(paymentMapper.selectById(id));
        assertThrows(ServiceException.class,() -> payments.deletePayment(id));
    }
    @Test void concurrentCompletionChangesStockOnce() throws Exception {
        jdbc.update("UPDATE biz_sales SET status='1' WHERE id=91001");
        var outcomes=race(8,() -> accepted(() -> sales.completeSales(91001L)));
        assertEquals(1,outcomes.stream().filter(Boolean::booleanValue).count());assertEquals(90,stock.findQuantity(91001L,91001L));assertEquals(1,count("biz_stock_move"));
    }
    @Test void insufficientStockRollsBackClaimedStatus() {
        jdbc.update("UPDATE biz_sales SET status='1',quantity=101 WHERE id=91001");
        assertThrows(ServiceException.class,() -> sales.completeSales(91001L));
        assertEquals("1",salesMapper.selectById(91001L).getStatus());assertEquals(100,stock.findQuantity(91001L,91001L));assertEquals(0,count("biz_stock_move"));
    }
    @Test void concurrentApprovalsAndCancellationAdjustQuotaOnce() throws Exception {
        var accepted=race(8,() -> accepted(() -> leaves.auditLeave(91001L,"1","ok")));
        assertEquals(1,accepted.stream().filter(Boolean::booleanValue).count());
        assertEquals(0,jdbc.queryForObject("SELECT used_days FROM biz_leave_quota WHERE id=91001",BigDecimal.class).compareTo(new BigDecimal("3")));
        var canceled=race(8,() -> accepted(() -> leaves.cancelLeave(91001L,91001L)));
        assertEquals(1,canceled.stream().filter(Boolean::booleanValue).count());assertEquals(0,jdbc.queryForObject("SELECT used_days FROM biz_leave_quota WHERE id=91001",BigDecimal.class).signum());
    }
    @Test void insufficientQuotaRollsBackApprovalAndBpmReplayIsHarmless() {
        jdbc.update("UPDATE biz_leave_quota SET quota_days=2 WHERE id=91001");
        assertThrows(ServiceException.class,() -> leaves.auditLeave(91001L,"1","ok"));
        assertEquals("0",jdbc.queryForObject("SELECT status FROM biz_leave WHERE id=91001",String.class));
        jdbc.update("UPDATE biz_leave_quota SET quota_days=5 WHERE id=91001");
        leaves.updateLeaveStatusFromBpm(91001L,2);leaves.updateLeaveStatusFromBpm(91001L,2);
        assertEquals(0,jdbc.queryForObject("SELECT used_days FROM biz_leave_quota WHERE id=91001",BigDecimal.class).compareTo(new BigDecimal("3")));
    }
    @Test void namesCanChangeWithoutMovingStockAndCrossTenantIdsAreRejected() {
        jdbc.update("UPDATE biz_product SET product_name='Renamed product' WHERE id=91001");
        assertTrue(stock.changeStock(91001L,91001L,-1L,"test","rename"));assertEquals(99,stock.findQuantity(91001L,91001L));
        TenantContextHolder.setTenantId(TENANT+1);
        assertNull(salesMapper.selectById(91001L));assertThrows(ServiceException.class,() -> references.product(91001L,null));
        assertThrows(ServiceException.class,() -> payments.createPayment(request("cross-tenant",10)));
    }
    @Test void duplicateNamesRequireIds() {
        jdbc.update("INSERT INTO biz_product(tenant_id,product_code,product_name,status) VALUES(?,'QA2','Shared name','0')",TENANT);
        assertThrows(ServiceException.class,() -> references.product(null,"Shared name"));assertEquals(91001L,references.product(91001L,null).getId());
    }
    @Test void concurrentReturnChangesInventoryAndMoneyOnce() throws Exception {
        payments.createPayment(request("before-return",100));
        jdbc.update("INSERT INTO biz_return(id,tenant_id,return_no,return_type,order_id,order_code,product_id,warehouse_id,product_name,warehouse,quantity,price,total_amount,return_date,status,reason) VALUES(91001,?,'QA-R','1',91001,'QA-S',91001,91001,'Shared name','QA warehouse',2,10,20,'2026-09-11','0','QA return')",TENANT);
        var outcomes=race(8,() -> accepted(() -> returns.executeReturn(91001L)));
        assertEquals(1,outcomes.stream().filter(Boolean::booleanValue).count());assertEquals(102,stock.findQuantity(91001L,91001L));
        assertEquals(0,paid().compareTo(new BigDecimal("80")));assertEquals(1,count("biz_stock_move"));assertEquals(2,count("biz_payment"));
    }
    @Test void twoLeavesCompeteForTheSameQuotaWithoutPartialApproval() throws Exception {
        jdbc.update("INSERT INTO biz_leave(id,tenant_id,employee_id,emp_name,leave_type,start_date,end_date,days,status,creator) VALUES(91002,?,91001,'Same employee','3','2026-09-15','2026-09-17',3,'0','91001')",TENANT);
        var counter=new java.util.concurrent.atomic.AtomicInteger();
        var outcomes=race(2,() -> accepted(() -> leaves.auditLeave(91001L+counter.getAndIncrement(),"1","ok")));
        assertEquals(1,outcomes.stream().filter(Boolean::booleanValue).count());
        assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM biz_leave WHERE tenant_id=? AND status='0'",Integer.class,TENANT));
        assertEquals(0,jdbc.queryForObject("SELECT used_days FROM biz_leave_quota WHERE id=91001",BigDecimal.class).compareTo(new BigDecimal("3")));
    }
    @Test void requestValidationAcceptsIdsAndRejectsNegativeOrders() {
        try (var factory=jakarta.validation.Validation.buildDefaultValidatorFactory()) {
            var validator=factory.getValidator();
            var employee=new com.enterprise.module.biz.controller.admin.employee.vo.employee.EmployeeSaveReqVO();
            employee.setEmpNo("QA");employee.setEmpName("QA");employee.setUserId(1L);
            assertTrue(validator.validate(employee).isEmpty());
            var check=new com.enterprise.module.biz.controller.admin.stockcheck.vo.stockcheck.StockCheckCreateReqVO();
            check.setProductId(1L);check.setWarehouseId(1L);check.setActualQuantity(1L);check.setCheckDate("2026-09-11");
            assertTrue(validator.validate(check).isEmpty());
            var order=new com.enterprise.module.biz.controller.admin.sales.vo.sales.SalesSaveReqVO();
            order.setSalesCode("QA");order.setProductId(1L);order.setQuantity(-5L);order.setPrice(BigDecimal.ONE);order.setSalesDate("2026-09-11");
            assertFalse(validator.validate(order).isEmpty());
        }
    }
    @Test void quotaReductionCannotBeBypassedByAnOlderTransactionSnapshot() {
        new org.springframework.transaction.support.TransactionTemplate(transactions).executeWithoutResult(tx -> {
            assertEquals(0,quotaMapper.selectUnique(91001L,"3","2026").getQuotaDays().compareTo(new BigDecimal("5")));
            CompletableFuture.runAsync(() -> jdbc.update("UPDATE biz_leave_quota SET quota_days=2 WHERE id=91001")).join();
            assertThrows(ServiceException.class,() -> quotas.deductUsedDays(91001L,"3","2026",new BigDecimal("3")));
        });
        assertEquals(0,jdbc.queryForObject("SELECT used_days FROM biz_leave_quota WHERE id=91001",BigDecimal.class).signum());
    }
    @Test void editingAndApprovalChargeThePersistedLeaveDays() throws Exception {
        var counter=new java.util.concurrent.atomic.AtomicInteger();
        race(2,() -> accepted(() -> {
            if (counter.getAndIncrement()==0) {
                var req=new com.enterprise.module.biz.controller.admin.leave.vo.leave.LeaveSaveReqVO();
                req.setId(91001L);req.setDays(new BigDecimal("4"));leaves.updateLeave(req);
            } else leaves.auditLeave(91001L,"1","ok");
        }));
        assertEquals("1",jdbc.queryForObject("SELECT status FROM biz_leave WHERE id=91001",String.class));
        assertEquals(0,jdbc.queryForObject("SELECT used_days FROM biz_leave_quota WHERE id=91001",BigDecimal.class)
                .compareTo(jdbc.queryForObject("SELECT days FROM biz_leave WHERE id=91001",BigDecimal.class)));
        assertThrows(ServiceException.class,() -> leaves.deleteLeave(91001L));
        assertThrows(ServiceException.class,() -> quotas.deleteLeaveQuota(91001L));
    }
    @Test void negativeLeaveDaysCannotCreditTheQuota() {
        assertThrows(ServiceException.class,() -> quotas.deductUsedDays(91001L,"3","2026",BigDecimal.ONE.negate()));
        var req=new com.enterprise.module.biz.controller.admin.leave.vo.leave.LeaveSaveReqVO();
        req.setEmployeeId(91001L);req.setDays(BigDecimal.ONE.negate());
        assertThrows(ServiceException.class,() -> leaves.createLeave(req));
        assertEquals(0,jdbc.queryForObject("SELECT used_days FROM biz_leave_quota WHERE id=91001",BigDecimal.class).signum());
    }
    @Test void concurrentDocumentNumbersAreDistinct() throws Exception {
        Set<String> all=ConcurrentHashMap.newKeySet();
        race(8,() -> { for(int i=0;i<1000;i++)assertTrue(all.add(BizDocumentNo.next("SK")));return true; });
        assertEquals(8000,all.size());
    }
}
