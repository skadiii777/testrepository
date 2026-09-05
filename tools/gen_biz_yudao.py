# -*- coding: utf-8 -*-
"""enterprise-pro 业务代码生成器（yudao 架构规范）

为 14 个业务实体生成：DO / Mapper / VO(3) / Controller / Service
另生成：ErrorCodeConstants、Dashboard/Approval/Portal 控制器、业务 SQL
输出根：enterprise-module-biz/src/main/java/com/enterprise/module/biz/
"""
import os, io

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
import shutil
_biz_src = os.path.join(ROOT, 'enterprise-module-biz', 'src', 'main', 'java', 'com', 'enterprise', 'module', 'biz')
if os.path.exists(_biz_src):
    shutil.rmtree(_biz_src)
    print('cleaned biz src')
BIZ = 'enterprise-module-biz/src/main/java/com/enterprise/module/biz'
PKG = 'com.enterprise.module.biz'

# 查询方式: None / 'like' / 'eq'
QLIKE, QEQ = 'like', 'eq'

# 实体规格: (javaName, javaType, label, sqlType, query, required, dictType)
E = {}
E['customer'] = dict(cn='客户', table='biz_customer', pkg='customer', perm='biz:customer', cn_name='客户', crud=True,
    fields=[('customerName','String','客户名称','varchar(100)','like',True,None),
            ('contactPerson','String','联系人','varchar(50)',None,True,None),
            ('phone','String','联系电话','varchar(30)',None,False,None),
            ('email','String','邮箱','varchar(100)',None,False,None),
            ('industry','String','所属行业','varchar(50)',None,False,None),
            ('source','String','客户来源','varchar(50)',None,False,None),
            ('address','String','地址','varchar(255)',None,False,None),
            ('status','String','状态','char(1)','eq',False,'biz_common_status')])
E['product'] = dict(cn='产品', table='biz_product', pkg='product', perm='biz:product', cn_name='产品', crud=True,
    fields=[('productCode','String','产品编号','varchar(50)','like',True,None),
            ('productName','String','产品名称','varchar(100)','like',True,None),
            ('category','String','产品分类','varchar(50)','eq',False,None),
            ('unit','String','单位','varchar(20)',None,False,None),
            ('price','BigDecimal','销售单价','decimal(12,2)',None,True,None),
            ('cost','BigDecimal','成本价','decimal(12,2)',None,False,None),
            ('status','String','状态','char(1)','eq',False,'biz_common_status')])
E['contract'] = dict(cn='合同', table='biz_contract', pkg='contract', perm='biz:contract', cn_name='合同', crud=True,
    fields=[('contractCode','String','合同编号','varchar(50)','like',True,None),
            ('customerName','String','客户名称','varchar(100)','like',True,None),
            ('productName','String','产品名称','varchar(100)',None,False,None),
            ('amount','BigDecimal','合同金额','decimal(12,2)',None,True,None),
            ('signDate','String','签订日期','varchar(20)','eq',True,None),
            ('startDate','String','开始日期','varchar(20)',None,False,None),
            ('endDate','String','结束日期','varchar(20)',None,False,None),
            ('owner','String','负责人','varchar(50)','eq',False,None),
            ('status','String','合同状态','char(1)','eq',False,'biz_contract_status')])
E['supplier'] = dict(cn='供应商', table='biz_supplier', pkg='supplier', perm='biz:supplier', cn_name='供应商', crud=True,
    fields=[('supplierName','String','供应商名称','varchar(100)','like',True,None),
            ('contactPerson','String','联系人','varchar(50)',None,False,None),
            ('phone','String','联系电话','varchar(30)',None,False,None),
            ('address','String','地址','varchar(255)',None,False,None),
            ('status','String','状态','char(1)','eq',False,'biz_common_status')])
E['purchase'] = dict(cn='采购单', table='biz_purchase', pkg='purchase', perm='biz:purchase', cn_name='采购单', crud=True,
    fields=[('purchaseCode','String','采购单号','varchar(50)','like',True,None),
            ('supplierName','String','供应商','varchar(100)',None,False,None),
            ('productName','String','产品名称','varchar(100)','like',True,None),
            ('quantity','Long','采购数量','int',None,True,None),
            ('price','BigDecimal','采购单价','decimal(12,2)',None,True,None),
            ('totalAmount','BigDecimal','总金额','decimal(12,2)',None,False,None),
            ('purchaseDate','String','采购日期','varchar(20)','eq',True,None),
            ('status','String','入库状态','char(1)','eq',False,'biz_inout_status')],
    extra='stock_link_purchase')
E['sales'] = dict(cn='销售单', table='biz_sales', pkg='sales', perm='biz:sales', cn_name='销售单', crud=True,
    fields=[('salesCode','String','销售单号','varchar(50)','like',True,None),
            ('customerName','String','客户','varchar(100)',None,False,None),
            ('productName','String','产品名称','varchar(100)','like',True,None),
            ('quantity','Long','销售数量','int',None,True,None),
            ('price','BigDecimal','销售单价','decimal(12,2)',None,True,None),
            ('totalAmount','BigDecimal','总金额','decimal(12,2)',None,False,None),
            ('salesDate','String','销售日期','varchar(20)','eq',True,None),
            ('status','String','出库状态','char(1)','eq',False,'biz_inout_status')],
    extra='stock_link_sales')
E['stock'] = dict(cn='库存', table='biz_stock', pkg='stock', perm='biz:stock', cn_name='库存', crud=True,
    fields=[('productName','String','产品名称','varchar(100)','like',True,None),
            ('warehouse','String','仓库','varchar(50)','eq',False,None),
            ('quantity','Long','库存数量','int',None,False,None),
            ('minQuantity','Long','预警下限','int',None,False,None)])
E['stockmove'] = dict(cn='库存流水', table='biz_stock_move', pkg='stockmove', perm='biz:stockmove', cn_name='库存流水', crud=False, cls='StockMove',
    fields=[('productName','String','产品名称','varchar(100)','like',True,None),
            ('warehouse','String','仓库','varchar(50)','eq',False,None),
            ('moveType','String','类型','char(1)','eq',True,None),
            ('quantity','BigDecimal','数量','decimal(12,2)',None,True,None),
            ('balanceAfter','BigDecimal','结余','decimal(12,2)',None,True,None),
            ('sourceType','String','来源类型','varchar(20)','eq',False,None),
            ('sourceCode','String','来源单号','varchar(50)','like',False,None)])
E['employee'] = dict(cn='员工', table='biz_employee', pkg='employee', perm='biz:employee', cn_name='员工', crud=True,
    fields=[('empNo','String','工号','varchar(30)','like',True,None),
            ('empName','String','姓名','varchar(50)','like',True,None),
            ('deptName','String','部门','varchar(50)','eq',False,None),
            ('postName','String','岗位','varchar(50)',None,False,None),
            ('phone','String','联系电话','varchar(30)',None,False,None),
            ('email','String','邮箱','varchar(100)',None,False,None),
            ('entryDate','String','入职日期','varchar(20)',None,False,None),
            ('status','String','状态','char(1)','eq',False,'biz_common_status')])
E['attendance'] = dict(cn='考勤', table='biz_attendance', pkg='attendance', perm='biz:attendance', cn_name='考勤', crud=True,
    fields=[('empName','String','员工姓名','varchar(50)','like',True,None),
            ('workDate','String','考勤日期','varchar(20)','eq',True,None),
            ('checkIn','String','上班时间','varchar(10)',None,False,None),
            ('checkOut','String','下班时间','varchar(10)',None,False,None),
            ('status','String','考勤状态','char(1)','eq',False,'biz_attendance_status'),
            ('overtimeMinutes','Integer','加班时长(分钟)','int',None,False,None)])
E['leave'] = dict(cn='请假', table='biz_leave', pkg='leave', perm='biz:leave', cn_name='请假', crud=True,
    fields=[('empName','String','员工姓名','varchar(50)','like',False,None),
            ('leaveType','String','请假类型','char(1)','eq',True,'biz_leave_type'),
            ('startDate','String','开始日期','varchar(20)','eq',False,None),
            ('endDate','String','结束日期','varchar(20)',None,False,None),
            ('days','BigDecimal','请假天数','decimal(4,1)',None,True,None),
            ('reason','String','请假事由','varchar(500)',None,False,None),
            ('status','String','审批状态','char(1)','eq',False,'biz_leave_status'),
            ('remark','String','审批意见','varchar(500)',None,False,None)],
    extra='leave_audit')
E['quota'] = dict(cn='假期余额', table='biz_leave_quota', pkg='quota', perm='biz:quota', cn_name='假期余额', crud=True, cls='LeaveQuota',
    fields=[('empName','String','员工姓名','varchar(50)','like',True,None),
            ('leaveType','String','假期类型','char(1)','eq',True,'biz_leave_type'),
            ('year','String','年份','varchar(4)','eq',True,None),
            ('quotaDays','BigDecimal','配额天数','decimal(4,1)',None,True,None),
            ('usedDays','BigDecimal','已用天数','decimal(4,1)',None,False,None)],
    extra='quota_logic')
E['report'] = dict(cn='业务汇报', table='biz_report', pkg='report', perm='biz:report', cn_name='业务汇报', crud=True,
    fields=[('reportType','String','汇报类型','char(1)','eq',True,'biz_report_type'),
            ('title','String','标题','varchar(200)','like',True,None),
            ('content','String','汇报内容','text',None,False,None),
            ('reportDate','String','汇报日期','varchar(20)','eq',True,None)])
E['expense'] = dict(cn='费用报销', table='biz_expense', pkg='expense', perm='biz:expense', cn_name='费用报销', crud=False,
    fields=[('empName','String','报销人','varchar(50)','like',False,None),
            ('category','String','费用类别','char(1)','eq',True,'biz_expense_type'),
            ('amount','BigDecimal','金额','decimal(12,2)',None,True,None),
            ('expenseDate','String','费用发生日期','varchar(20)','eq',True,None),
            ('reason','String','费用说明','varchar(500)',None,False,None),
            ('status','String','审批状态','char(1)','eq',False,'biz_expense_status'),
            ('auditRemark','String','审批意见','varchar(500)',None,False,None),
            ('auditBy','String','审批人','varchar(64)',None,False,None),
            ('auditTime','String','审批时间','varchar(20)',None,False,None)],
    extra='expense_audit')

def cap(s): return s[0].upper() + s[1:]
def cls_of(key, e): return e.get('cls', cap(s=key))
def w(path, content):
    full = os.path.join(ROOT, path)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    with io.open(full, 'w', encoding='utf-8', newline='\n') as f:
        f.write(content)
    print('  write', path.replace(ROOT + os.sep, '').replace(ROOT + '/', ''))

JTYPE = {'String':'String','Long':'Long','Integer':'Integer','BigDecimal':'BigDecimal'}
SQLT = {'String':'{}','Long':'{}','Integer':'{}','BigDecimal':'{}'}

def w_file(path, content):
    full = os.path.join(ROOT, path)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    with io.open(full, 'w', encoding='utf-8', newline='\n') as f:
        f.write(content)
    print('  write', path.replace(ROOT + '/', ''))

# ============ DO ============
def gen_do(key, e):
    fields = '\n'.join('''    /**%s*/
    private %s %s;''' % (f[2], f[1], f[0]) for f in e['fields'])
    return f'''package {PKG}.dal.dataobject.{e['pkg']};

import com.enterprise.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * {e['cn']} DO
 *
 * @author 企业管理平台
 */
@TableName("{e['table']}")
@KeySequence("{e['table']}_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class {cls_of(key, e)}DO extends TenantBaseDO {{

    /**
     * 主键
     */
    @TableId
    private Long id;
{fields}
{EXTRA_DO.get(key, '')}
}}'''

# ============ VO ============
def gen_vos(key, e):
    pkg = e['pkg']; Cls = cls_of(key, e)
    # PageReqVO
    qf = []
    for (n, t, label, sq, q, r, d) in e['fields']:
        if q == 'like':
            qf.append('''    @Schema(description = "%s")
    private String %s;''' % (label, n))
        elif q == 'eq':
            qf.append('''    @Schema(description = "%s")
    private String %s;''' % (label, n))
    vo = f'''package {PKG}.controller.admin.{pkg}.vo.{pkg};

import com.enterprise.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.enterprise.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - {e['cn']}分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class {Cls}PageReqVO extends PageParam {{

{chr(10).join(qf)}

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}}'''
    w(f'{BIZ}/controller/admin/{pkg}/vo/{pkg}/{Cls}PageReqVO.java', vo)
    # SaveReqVO
    sf = []
    for (n, t, label, sq, q, r, d) in e['fields']:
        if n == 'status' and key in ('purchase', 'sales'):
            continue  # 状态由业务控制（采购/销售由管理员设置）
        if key == 'expense' and n in ('auditRemark','auditBy','auditTime'):
            continue
        ann = ''
        if r:
            ann = '@NotBlank(message="%s不能为空")' % label if t == 'String' else '@NotNull(message="%s不能为空")' % label
        sf.append('''    @Schema(description = "%s"%s)
    %s
    private %s %s;''' % (label, (', requiredMode = Schema.RequiredMode.REQUIRED' if r else ''), ann, t, n))
    if key in ('purchase','sales'):
        sf.append('''    @Schema(description = "状态（0待处理 1已完成）")
    private String status;''')
    vo = f'''package {PKG}.controller.admin.{pkg}.vo.{pkg};

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "管理后台 - {e['cn']}新增/修改 Request VO")
@Data
public class {Cls}SaveReqVO {{

    @Schema(description = "主键，更新时必填")
    private Long id;

{chr(10).join(sf)}
}}'''
    w(f'{BIZ}/controller/admin/{pkg}/vo/{pkg}/{Cls}SaveReqVO.java', vo)
    # RespVO
    rf = []
    for (n, t, label, sq, q, r, d) in e['fields']:
        rf.append('''    @Schema(description = "%s")
    private %s %s;''' % (label, t, n))
    vo = f'''package {PKG}.controller.admin.{pkg}.vo.{pkg};

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - {e['cn']} Response VO")
@Data
public class {Cls}RespVO {{

    @Schema(description = "主键")
    private Long id;

{chr(10).join(rf)}

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}}'''
    w(f'{BIZ}/controller/admin/{pkg}/vo/{pkg}/{Cls}RespVO.java', vo)
EXTRA_DO = {'quota': '''
    /** 剩余额度（配额-已用） */
    public java.math.BigDecimal getRemainDays() {{
        if (quotaDays == null) {{
            return java.math.BigDecimal.ZERO;
        }}
        return quotaDays.subtract(usedDays == null ? java.math.BigDecimal.ZERO : usedDays);
 }}'''}

for key, e in E.items():
    print('DO+VO:', key)
    w(f'{BIZ}/dal/dataobject/{e["pkg"]}/{cls_of(key, e)}DO.java', gen_do(key, e))
    gen_vos(key, e)

print('VO done')

# ============ Mapper ============
EXTRA_IMPORTS = {
    'stock': 'import org.apache.ibatis.annotations.Param;\nimport org.apache.ibatis.annotations.Update;',
    'quota': 'import org.apache.ibatis.annotations.Param;\nimport org.apache.ibatis.annotations.Update;',
    'sales': 'import org.apache.ibatis.annotations.Param;\nimport org.apache.ibatis.annotations.Select;',
    'purchase': 'import org.apache.ibatis.annotations.Param;\nimport org.apache.ibatis.annotations.Select;',
    'expense': 'import org.apache.ibatis.annotations.Update;',
}
EXTRA_METHODS = {
'expense': '''
    /**
     * 审批报销（SQL 层防重复审批：仅待审批可流转）
     */
    @Update("UPDATE biz_expense SET status = #{status}, audit_remark = #{auditRemark}, "
            + "audit_by = #{auditBy}, audit_time = #{auditTime}, update_time = NOW() "
            + "WHERE id = #{id} AND status = '0' AND deleted = 0")
    int auditExpense(ExpenseDO expense);''',
'stock': '''
    /**
     * 原子增减库存（delta 正数入库/负数出库），数量不足时更新 0 行
     */
    @Update("UPDATE biz_stock SET quantity = quantity + #{delta} "
            + "WHERE id = #{id} AND quantity + #{delta} >= 0 AND deleted = 0")
    int adjustQuantity(@Param("id") Long id, @Param("delta") Long delta);

    /**
     * 按产品+仓库精确查询
     */
    default StockDO selectByProductAndWarehouse(String productName, String warehouse) {
        return selectOne(new LambdaQueryWrapperX<StockDO>()
                .eq(StockDO::getProductName, productName)
                .eq(StockDO::getWarehouse, warehouse)
                .last("LIMIT 1"));
    }''',
'quota': '''
    /**
     * 按员工+类型+年份精确查询（唯一键）
     */
    default LeaveQuotaDO selectUnique(String empName, String leaveType, String year) {
        return selectOne(new LambdaQueryWrapperX<LeaveQuotaDO>()
                .eq(LeaveQuotaDO::getEmpName, empName)
                .eq(LeaveQuotaDO::getLeaveType, leaveType)
                .eq(LeaveQuotaDO::getYear, year)
                .last("LIMIT 1"));
    }

    /**
     * 原子增减已用天数（delta 正数扣减/负数返还）
     * 带 quotaDays 时做余额兜底：used_days + delta <= quota_days
     */
    @Update("<script>UPDATE biz_leave_quota SET used_days = used_days + #{delta}, update_time = NOW() "
            + "WHERE id = #{id} AND deleted = 0 "
            + "<if test='quotaDays != null'>AND used_days + #{delta} &lt;= #{quotaDays}</if></script>")
    int adjustUsedDays(@Param("id") Long id, @Param("delta") java.math.BigDecimal delta,
                       @Param("quotaDays") java.math.BigDecimal quotaDays);''',
'attendance': '''
    /**
     * 查询员工某天的考勤记录
     */
    default AttendanceDO selectByEmpAndDate(String empName, String workDate) {
        return selectOne(new LambdaQueryWrapperX<AttendanceDO>()
                .eq(AttendanceDO::getEmpName, empName)
                .eq(AttendanceDO::getWorkDate, workDate)
                .last("LIMIT 1"));
    }''',
'sales': '''
    /**
     * 按日汇总销售金额（>= startDate）
     */
    @Select("SELECT sales_date AS `date`, IFNULL(SUM(total_amount),0) AS total FROM biz_sales "
            + "WHERE deleted = 0 AND sales_date >= #{startDate} GROUP BY sales_date")
    java.util.List<java.util.Map<String, Object>> selectSumByDate(@Param("startDate") String startDate);

    /**
     * 产品销售数量 Top N
     */
    @Select("SELECT product_name AS productName, SUM(quantity) AS totalQty, SUM(total_amount) AS totalAmount "
            + "FROM biz_sales WHERE deleted = 0 GROUP BY product_name ORDER BY totalQty DESC LIMIT #{top}")
    java.util.List<java.util.Map<String, Object>> selectProductTop(@Param("top") int top);''',
'purchase': '''
    /**
     * 按日汇总采购金额（>= startDate）
     */
    @Select("SELECT purchase_date AS `date`, IFNULL(SUM(total_amount),0) AS total FROM biz_purchase "
            + "WHERE deleted = 0 AND purchase_date >= #{startDate} GROUP BY purchase_date")
    java.util.List<java.util.Map<String, Object>> selectSumByDate(@Param("startDate") String startDate);''',
'leave': '''
    /**
     * 各审批状态数量统计
     */
    default java.util.List<java.util.Map<String, Object>> selectStatusCount() {
        return selectMaps(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<LeaveDO>()
                .select("status", "count(*) AS cnt").groupBy("status"));
    }''',
'contract': '''
    /**
     * 各合同状态数量统计
     */
    default java.util.List<java.util.Map<String, Object>> selectStatusCount() {
        return selectMaps(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ContractDO>()
                .select("status", "count(*) AS cnt").groupBy("status"));
    }''',
}

def gen_mapper(key, e):
    pkg = e['pkg']; Cls = cls_of(key, e)
    conds = []
    for (n, t, label, sq, q, r, d) in e['fields']:
        if q == 'like':
            conds.append('.likeIfPresent(%sDO::get%s, reqVO.get%s())' % (Cls, cap(n), cap(n)))
        elif q == 'eq':
            conds.append('.eqIfPresent(%sDO::get%s, reqVO.get%s())' % (Cls, cap(n), cap(n)))
    conds.append('.betweenIfPresent(%sDO::getCreateTime, reqVO.getCreateTime())' % Cls)
    body = '\n                '.join(conds)
    return f'''package {PKG}.dal.mysql.{pkg};

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.mybatis.core.mapper.BaseMapperX;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import {PKG}.controller.admin.{pkg}.vo.{pkg}.{Cls}PageReqVO;
import {PKG}.dal.dataobject.{pkg}.{Cls}DO;
import org.apache.ibatis.annotations.Mapper;
{EXTRA_IMPORTS.get(key, '')}
@Mapper
public interface {Cls}Mapper extends BaseMapperX<{Cls}DO> {{

    /**
     * 分页查询
     */
    default PageResult<{Cls}DO> selectPage({Cls}PageReqVO reqVO) {{
        return selectPage(reqVO, new LambdaQueryWrapperX<{Cls}DO>()
                {body}
                .orderByDesc({Cls}DO::getId));
    }}
{EXTRA_METHODS.get(key, '')}
}}'''

for key, e in E.items():
    print('Mapper:', key)
    w(f'{BIZ}/dal/mysql/{e["pkg"]}/{cls_of(key, e)}Mapper.java', gen_mapper(key, e))

# ============ Service 接口 ============
EXTRA_SVC = {
'purchase': '',
'sales': '''
    /**
     * 校验库存是否满足出库
     */
    void validateStockEnough(String productName, Long quantity);''',
'leave': '''
    /**
     * 仅本人分页（员工工作台）
     */
    PageResult<LeaveDO> getLeavePageSelf(LeavePageReqVO pageReqVO, Long userId);

    /**
     * 审批请假（status: 1=通过 2=驳回），通过时扣减假期余额
     */
    void auditLeave(Long id, String status, String auditRemark);

    /**
     * 销假（已通过 -> 已销假），返还假期余额
     */
    void cancelLeave(Long id, Long loginUserId);''',
'report': '''
    /**
     * 仅本人分页（员工工作台）
     */
    PageResult<ReportDO> getReportPageSelf(ReportPageReqVO pageReqVO, Long userId);''',
'attendance': '''
    /**
     * 查询员工某天的考勤记录
     */
    AttendanceDO getTodayAttendance(String empName, String workDate);''',
'quota': '''
    /**
     * 查询剩余天数，无配额记录返回足够大值（不限额）
     */
    java.math.BigDecimal findRemainDays(String empName, String leaveType, String year);

    /**
     * 原子扣减已用天数（余额不足抛异常）
     */
    void deductUsedDays(String empName, String leaveType, String year, java.math.BigDecimal days);

    /**
     * 原子返还已用天数
     */
    void refundUsedDays(String empName, String leaveType, String year, java.math.BigDecimal days);''',
'expense': '''
    /**
     * 仅本人分页（员工工作台）
     */
    PageResult<ExpenseDO> getExpensePageSelf(ExpensePageReqVO pageReqVO, Long userId);

    /**
     * 审批报销（1=通过 2=驳回），SQL 层防重复审批
     */
    void auditExpense(Long id, String status, String auditRemark, Long auditorUserId);''',
'stock': '''
    /**
     * 库存变更（delta 正数入库/负数出库），自动落流水；数量不足返回 false
     */
    boolean changeStock(String productName, String warehouse, Long delta, String sourceType, String sourceCode);

    /**
     * 查询库存数量（无记录返回 0）
     */
    Long findQuantity(String productName, String warehouse);

    /**
     * 查询库存低于预警下限的记录
     */
    java.util.List<StockDO> getLowStockList();''',
}

def gen_service_iface(key, e):
    pkg = e['pkg']; Cls = cls_of(key, e)
    return f'''package {PKG}.service.{pkg};

import com.enterprise.framework.common.pojo.PageResult;
import {PKG}.controller.admin.{pkg}.vo.{pkg}.{Cls}PageReqVO;
import {PKG}.controller.admin.{pkg}.vo.{pkg}.{Cls}SaveReqVO;
import {PKG}.dal.dataobject.{pkg}.{Cls}DO;

/**
 * {e['cn']} Service 接口
 *
 * @author 企业管理平台
 */
public interface {Cls}Service {{

    /**
     * 创建{e['cn']}
     */
    Long create{Cls}({Cls}SaveReqVO createReqVO);

    /**
     * 更新{e['cn']}
     */
    void update{Cls}({Cls}SaveReqVO updateReqVO);

    /**
     * 删除{e['cn']}
     */
    void delete{Cls}(Long id);

    /**
     * 获得{e['cn']}
     */
    {Cls}DO get{Cls}(Long id);

    /**
     * 获得{e['cn']}分页
     */
    PageResult<{Cls}DO> get{Cls}Page({Cls}PageReqVO pageReqVO);
{EXTRA_SVC.get(key, '')}
}}'''
for key, e in E.items():
    if key == 'stockmove':
        continue
    w(f'{BIZ}/service/{e["pkg"]}/{cls_of(key, e)}Service.java', gen_service_iface(key, e))

# ============ ServiceImpl ============
EXTRA_IMPL_IMPORTS = {
'purchase': 'import com.enterprise.module.biz.service.stock.StockService;',
'sales': 'import com.enterprise.module.biz.service.stock.StockService;',
'leave': 'import com.enterprise.module.biz.service.quota.LeaveQuotaService;',
'stock': 'import com.enterprise.module.biz.dal.mysql.stockmove.StockMoveMapper; import com.enterprise.module.biz.dal.dataobject.stockmove.StockMoveDO;',
}

EXTRA_IMPL_HEAD = {
'purchase': '''    @Resource
    private StockService stockService;
''',
'sales': '''    @Resource
    private StockService stockService;
''',
'leave': '''    @Resource
    private LeaveQuotaService leaveQuotaService;
''',
'quota': '',
'expense': '',
'stock': '''    @Resource
    private StockMoveMapper stockMoveMapper;
''',
}
EXTRA_IMPL_BODY = {
'purchase': '''
    @Override
    public Long createPurchase(PurchaseSaveReqVO createReqVO) {
        PurchaseDO purchase = BeanUtils.toBean(createReqVO, PurchaseDO.class);
        purchaseMapper.insert(purchase);
        // 已完成状态自动入库
        if ("1".equals(purchase.getStatus())) {
            stockService.changeStock(purchase.getProductName(), "默认仓库", purchase.getQuantity(),
                    "purchase", purchase.getPurchaseCode());
        }
        return purchase.getId();
    }''',
'sales': '''
    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public Long createSales(SalesSaveReqVO createReqVO) {
        SalesDO sales = BeanUtils.toBean(createReqVO, SalesDO.class);
        if ("1".equals(sales.getStatus())) {
            validateStockEnough(sales.getProductName(), sales.getQuantity());
        }
        salesMapper.insert(sales);
        if ("1".equals(sales.getStatus())) {
            boolean ok = stockService.changeStock(sales.getProductName(), "默认仓库", -sales.getQuantity(),
                    "sales", sales.getSalesCode());
            if (!ok) {
                throw exception(STOCK_NOT_ENOUGH);
            }
        }
        return sales.getId();
    }

    @Override
    public void validateStockEnough(String productName, Long quantity) {
        Long qty = stockService.findQuantity(productName, "默认仓库");
        if (quantity == null || qty == null || qty < quantity) {
            throw exception(STOCK_NOT_ENOUGH);
        }
    }''',
'stock': '''
    /**
     * 库存变更并落流水（synchronized 单机串行 + SQL 条件兜底）
     */
    @Override
    public synchronized boolean changeStock(String productName, String warehouse, Long delta,
                                            String sourceType, String sourceCode) {
        String wh = warehouse == null ? "默认仓库" : warehouse;
        StockDO stock = stockMapper.selectByProductAndWarehouse(productName, wh);
        if (stock == null) {
            stock = StockDO.builder().productName(productName).warehouse(wh)
                    .quantity(0L).minQuantity(0L).build();
            stockMapper.insert(stock);
        }
        int rows = stockMapper.adjustQuantity(stock.getId(), delta);
        if (rows == 0) {
            return false;
        }
        Long balanceAfter = stockMapper.selectById(stock.getId()).getQuantity();
        StockMoveDO move = StockMoveDO.builder()
                .productName(productName).warehouse(wh)
                .moveType(delta >= 0 ? "1" : "2")
                .quantity(java.math.BigDecimal.valueOf(Math.abs(delta)))
                .balanceAfter(java.math.BigDecimal.valueOf(balanceAfter))
                .sourceType(sourceType == null ? "manual" : sourceType)
                .sourceCode(sourceCode == null ? "" : sourceCode)
                .build();
        stockMoveMapper.insert(move);
        return true;
    }

    @Override
    public Long findQuantity(String productName, String warehouse) {
        StockDO stock = stockMapper.selectByProductAndWarehouse(productName, warehouse);
        return stock == null || stock.getQuantity() == null ? 0L : stock.getQuantity();
    }

    @Override
    public java.util.List<StockDO> getLowStockList() {
        return stockMapper.selectList(new LambdaQueryWrapperX<StockDO>()
                .apply("quantity <= min_quantity")
                .orderByAsc(StockDO::getQuantity));
    }''',
'report': '''
    @Override
    public PageResult<ReportDO> getReportPageSelf(ReportPageReqVO pageReqVO, Long userId) {
        return reportMapper.selectPage(pageReqVO, new LambdaQueryWrapperX<ReportDO>()
                .eq(ReportDO::getCreator, String.valueOf(userId))
                .orderByDesc(ReportDO::getId));
    }''',
'attendance': '''
    @Override
    public AttendanceDO getTodayAttendance(String empName, String workDate) {
        return attendanceMapper.selectByEmpAndDate(empName, workDate);
    }''',
'quota': '''
    @Override
    public Long createLeaveQuota(LeaveQuotaSaveReqVO createReqVO) {
        // 唯一键防重：员工+类型+年份
        if (quotaMapper.selectUnique(createReqVO.getEmpName(), createReqVO.getLeaveType(), createReqVO.getYear()) != null) {
            throw exception(QUOTA_DUPLICATE);
        }
        LeaveQuotaDO quota = BeanUtils.toBean(createReqVO, LeaveQuotaDO.class);
        if (quota.getUsedDays() == null) {
            quota.setUsedDays(java.math.BigDecimal.ZERO);
        }
        if (quota.getUsedDays().compareTo(quota.getQuotaDays()) > 0) {
            throw exception(QUOTA_USED_OVER);
        }
        quotaMapper.insert(quota);
        return quota.getId();
    }

    @Override
    public void updateLeaveQuota(LeaveQuotaSaveReqVO updateReqVO) {
        validateLeaveQuotaExists(updateReqVO.getId());
        LeaveQuotaDO exist = quotaMapper.selectUnique(updateReqVO.getEmpName(), updateReqVO.getLeaveType(), updateReqVO.getYear());
        if (exist != null && !exist.getId().equals(updateReqVO.getId())) {
            throw exception(QUOTA_DUPLICATE);
        }
        LeaveQuotaDO updateObj = BeanUtils.toBean(updateReqVO, LeaveQuotaDO.class);
        if (updateObj.getUsedDays() != null && updateObj.getQuotaDays() != null
                && updateObj.getUsedDays().compareTo(updateObj.getQuotaDays()) > 0) {
            throw exception(QUOTA_USED_OVER);
        }
        quotaMapper.updateById(updateObj);
    }

    @Override
    public java.math.BigDecimal findRemainDays(String empName, String leaveType, String year) {
        LeaveQuotaDO quota = quotaMapper.selectUnique(empName, leaveType, year);
        // 无配额记录 = 该类型未配置限额
        return quota == null ? new java.math.BigDecimal("99999") : quota.getRemainDays();
    }

    @Override
    public void deductUsedDays(String empName, String leaveType, String year, java.math.BigDecimal days) {
        LeaveQuotaDO quota = quotaMapper.selectUnique(empName, leaveType, year);
        if (quota == null) {
            return; // 未配置配额的类型不限额
        }
        if (quota.getRemainDays().compareTo(days) < 0) {
            throw exception(LEAVE_QUOTA_NOT_ENOUGH);
        }
        quotaMapper.adjustUsedDays(quota.getId(), days, quota.getQuotaDays());
    }

    @Override
    public void refundUsedDays(String empName, String leaveType, String year, java.math.BigDecimal days) {
        LeaveQuotaDO quota = quotaMapper.selectUnique(empName, leaveType, year);
        if (quota != null) {
            quotaMapper.adjustUsedDays(quota.getId(), days.negate(), null);
        }
    }''',
'leave': '''
    @Override
    public Long createLeave(LeaveSaveReqVO createReqVO) {
        LeaveDO leave = BeanUtils.toBean(createReqVO, LeaveDO.class);
        leave.setStatus("0"); // 强制初始状态，防止客户端篡改
        leaveMapper.insert(leave);
        return leave.getId();
    }

    @Override
    public PageResult<LeaveDO> getLeavePageSelf(LeavePageReqVO pageReqVO, Long userId) {
        return leaveMapper.selectPage(pageReqVO, new LambdaQueryWrapperX<LeaveDO>()
                .eq(LeaveDO::getCreator, String.valueOf(userId))
                .orderByDesc(LeaveDO::getId));
    }

    @Override
    public void auditLeave(Long id, String status, String auditRemark) {
        LeaveDO leave = leaveMapper.selectById(id);
        if (leave == null) {
            throw exception(LEAVE_NOT_EXISTS);
        }
        if ("1".equals(status)) {
            leaveQuotaService.deductUsedDays(leave.getEmpName(), leave.getLeaveType(),
                    yearOf(leave.getStartDate()), leave.getDays());
        }
        LeaveDO update = new LeaveDO();
        update.setId(id);
        update.setStatus(status);
        update.setRemark(auditRemark);
        leaveMapper.updateById(update);
    }

    @Override
    public void cancelLeave(Long id, Long loginUserId) {
        LeaveDO leave = leaveMapper.selectById(id);
        if (leave == null) {
            throw exception(LEAVE_NOT_EXISTS);
        }
        if (!String.valueOf(loginUserId).equals(leave.getCreator())) {
            throw exception(PORTAL_NOT_OWNER);
        }
        if (!"1".equals(leave.getStatus())) {
            throw exception(LEAVE_CANCEL_ONLY_APPROVED);
        }
        leaveQuotaService.refundUsedDays(leave.getEmpName(), leave.getLeaveType(),
                yearOf(leave.getStartDate()), leave.getDays());
        LeaveDO update = new LeaveDO();
        update.setId(id);
        update.setStatus("3");
        update.setRemark("员工申请销假（提前返岗）");
        leaveMapper.updateById(update);
    }

    private String yearOf(String date) {
        return date != null && date.length() >= 4 ? date.substring(0, 4)
                : String.valueOf(java.time.Year.now().getValue());
    }''',
'expense': '''
    @Override
    public Long createExpense(ExpenseSaveReqVO createReqVO) {
        ExpenseDO expense = BeanUtils.toBean(createReqVO, ExpenseDO.class);
        expense.setStatus("0"); // 强制初始状态，防止客户端篡改
        expenseMapper.insert(expense);
        return expense.getId();
    }

    @Override
    public PageResult<ExpenseDO> getExpensePageSelf(ExpensePageReqVO pageReqVO, Long userId) {
        return expenseMapper.selectPage(pageReqVO, new LambdaQueryWrapperX<ExpenseDO>()
                .eq(ExpenseDO::getCreator, String.valueOf(userId))
                .orderByDesc(ExpenseDO::getId));
    }

    @Override
    public void auditExpense(Long id, String status, String auditRemark, Long auditorUserId) {
        if (!"1".equals(status) && !"2".equals(status)) {
            throw exception(EXPENSE_AUDIT_STATUS_INVALID);
        }
        ExpenseDO update = ExpenseDO.builder()
                .id(id).status(status).auditRemark(auditRemark)
                .auditBy(String.valueOf(auditorUserId))
                .auditTime(java.time.LocalDate.now().toString())
                .build();
        if (expenseMapper.auditExpense(update) == 0) {
            throw exception(EXPENSE_ALREADY_AUDITED);
        }
    }''',
}
# leave 额外注入 quotaService
EXTRA_IMPL_HEAD['leave'] = '''    @Resource
    private LeaveQuotaService leaveQuotaService;
'''

def gen_service_impl(key, e):
    pkg = e['pkg']; Cls = cls_of(key, e); cls = key
    svc_cls = f'{Cls}Service'
    return f'''package {PKG}.service.{pkg};

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import {PKG}.controller.admin.{pkg}.vo.{pkg}.{Cls}PageReqVO;
import {PKG}.controller.admin.{pkg}.vo.{pkg}.{Cls}SaveReqVO;
import {PKG}.dal.dataobject.{pkg}.{Cls}DO;
import {PKG}.dal.mysql.{pkg}.{Cls}Mapper;
import {PKG}.enums.ErrorCodeConstants;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
{EXTRA_IMPL_IMPORTS.get(key, '')}

import static com.enterprise.framework.common.exception.util.ServiceExceptionUtil.exception;
import static {PKG}.enums.ErrorCodeConstants.*;

/**
 * {e['cn']} Service 实现类
 *
 * @author 企业管理平台
 */
@Service
@Validated
public class {Cls}ServiceImpl implements {svc_cls} {{

    @Resource
    private {Cls}Mapper {cls}Mapper;
{EXTRA_IMPL_HEAD.get(key, '')}
{'' if key in ('purchase', 'sales', 'quota', 'leave', 'expense') else f'''    @Override
    public Long create{Cls}({Cls}SaveReqVO createReqVO) {{
        {Cls}DO {cls} = BeanUtils.toBean(createReqVO, {Cls}DO.class);
        {cls}Mapper.insert({cls});
        return {cls}.getId();
    }}
'''}
{EXTRA_IMPL_BODY.get(key, '')}
{'' if key == 'quota' else f'''    @Override
    public void update{Cls}({Cls}SaveReqVO updateReqVO) {{
        validate{Cls}Exists(updateReqVO.getId());
        {Cls}DO updateObj = BeanUtils.toBean(updateReqVO, {Cls}DO.class);
        {cls}Mapper.updateById(updateObj);
    }}
'''}

    @Override
    public void delete{Cls}(Long id) {{
        validate{Cls}Exists(id);
        {cls}Mapper.deleteById(id);
    }}

    private void validate{Cls}Exists(Long id) {{
        if ({cls}Mapper.selectById(id) == null) {{
            throw exception({Cls.upper()}_NOT_EXISTS);
        }}
    }}

    @Override
    public {Cls}DO get{Cls}(Long id) {{
        return {cls}Mapper.selectById(id);
    }}

    @Override
    public PageResult<{Cls}DO> get{Cls}Page({Cls}PageReqVO pageReqVO) {{
        return {cls}Mapper.selectPage(pageReqVO);
    }}
}}'''
for key, e in E.items():
    if key == 'stockmove':
        continue
    print('ServiceImpl:', key)
    w(f'{BIZ}/service/{e["pkg"]}/{cls_of(key, e)}ServiceImpl.java', gen_service_impl(key, e))

# ============ ErrorCodeConstants ============
w(f'{BIZ}/enums/ErrorCodeConstants.java', f'''package {PKG}.enums;

import com.enterprise.framework.common.exception.ErrorCode;

/**
 * Biz 模块错误码区间 [1_050_000_000, 1_060_000_000)
 */
public interface ErrorCodeConstants {{

    // ========== 实体不存在 1_050_000_001 ==========
    ErrorCode CUSTOMER_NOT_EXISTS = new ErrorCode(1_050_000_001, "客户不存在");
    ErrorCode PRODUCT_NOT_EXISTS = new ErrorCode(1_050_000_002, "产品不存在");
    ErrorCode CONTRACT_NOT_EXISTS = new ErrorCode(1_050_000_003, "合同不存在");
    ErrorCode SUPPLIER_NOT_EXISTS = new ErrorCode(1_050_000_004, "供应商不存在");
    ErrorCode PURCHASE_NOT_EXISTS = new ErrorCode(1_050_000_005, "采购单不存在");
    ErrorCode SALES_NOT_EXISTS = new ErrorCode(1_050_000_006, "销售单不存在");
    ErrorCode STOCK_NOT_EXISTS = new ErrorCode(1_050_000_007, "库存记录不存在");
    ErrorCode EMPLOYEE_NOT_EXISTS = new ErrorCode(1_050_000_008, "员工不存在");
    ErrorCode ATTENDANCE_NOT_EXISTS = new ErrorCode(1_050_000_009, "考勤记录不存在");
    ErrorCode LEAVE_NOT_EXISTS = new ErrorCode(1_050_000_010, "请假单不存在");
    ErrorCode QUOTA_NOT_EXISTS = new ErrorCode(1_050_000_011, "假期配额不存在");
    ErrorCode LEAVEQUOTA_NOT_EXISTS = QUOTA_NOT_EXISTS; // 生成器命名别名
    ErrorCode REPORT_NOT_EXISTS = new ErrorCode(1_050_000_012, "汇报不存在");
    ErrorCode EXPENSE_NOT_EXISTS = new ErrorCode(1_050_000_013, "报销单不存在");

    // ========== 业务规则 1_050_001_XXX ==========
    ErrorCode STOCK_NOT_ENOUGH = new ErrorCode(1_050_001_001, "库存不足，无法出库，请先采购入库");
    ErrorCode LEAVE_QUOTA_NOT_ENOUGH = new ErrorCode(1_050_001_002, "假期余额不足");
    ErrorCode QUOTA_DUPLICATE = new ErrorCode(1_050_001_003, "该员工此假期类型在该年度已存在配额");
    ErrorCode QUOTA_USED_OVER = new ErrorCode(1_050_001_004, "已用天数不能大于配额天数");
    ErrorCode EXPENSE_ALREADY_AUDITED = new ErrorCode(1_050_001_005, "该报销已审批，不能重复操作");
    ErrorCode EXPENSE_AUDIT_STATUS_INVALID = new ErrorCode(1_050_001_006, "审批状态不合法");
    ErrorCode PUNCH_DUPLICATE = new ErrorCode(1_050_001_007, "今日已完成该打卡，无需重复操作");
    ErrorCode PUNCH_TYPE_INVALID = new ErrorCode(1_050_001_008, "打卡类型不合法");
    ErrorCode LEAVE_CANCEL_ONLY_APPROVED = new ErrorCode(1_050_001_009, "仅已通过的请假可以销假");
    ErrorCode PORTAL_NOT_OWNER = new ErrorCode(1_050_001_010, "仅能操作本人提交的记录");
    ErrorCode EXPENSE_AMOUNT_INVALID = new ErrorCode(1_050_001_011, "报销金额必须大于0");
}}''')

# ============ Controller ============
def gen_controller(key, e):
    pkg = e['pkg']; Cls = cls_of(key, e); cls = key
    perm = e['perm']
    methods = []
    if e.get('crud', True):
        methods.append(f'''
    @PostMapping("/create")
    @Operation(summary = "创建{e['cn']}")
    @PreAuthorize("@ss.hasPermission('{perm}:create')")
    public CommonResult<Long> create{Cls}(@Valid @RequestBody {Cls}SaveReqVO createReqVO) {{
        return success({cls}Service.create{Cls}(createReqVO));
    }}

    @PutMapping("/update")
    @Operation(summary = "更新{e['cn']}")
    @PreAuthorize("@ss.hasPermission('{perm}:update')")
    public CommonResult<Boolean> update{Cls}(@Valid @RequestBody {Cls}SaveReqVO updateReqVO) {{
        {cls}Service.update{Cls}(updateReqVO);
        return success(true);
    }}

    @DeleteMapping("/delete")
    @Operation(summary = "删除{e['cn']}")
    @Parameter(name = "id", description = "主键", required = true)
    @PreAuthorize("@ss.hasPermission('{perm}:delete')")
    public CommonResult<Boolean> delete{Cls}(@RequestParam("id") Long id) {{
        {cls}Service.delete{Cls}(id);
        return success(true);
    }}''')
    if key == 'leave':
        methods.append(f'''
    @PostMapping("/audit")
    @Operation(summary = "审批请假")
    @PreAuthorize("@ss.hasPermission('{perm}:audit')")
    public CommonResult<Boolean> auditLeave(@RequestParam("id") Long id,
                                            @RequestParam("status") String status,
                                            @RequestParam(value = "auditRemark", required = false) String auditRemark) {{
        leaveService.auditLeave(id, status, auditRemark);
        return success(true);
    }}''')
    if key == 'expense':
        methods.append(f'''
    @PostMapping("/audit")
    @Operation(summary = "审批报销")
    @PreAuthorize("@ss.hasPermission('{perm}:audit')")
    public CommonResult<Boolean> auditExpense(@RequestParam("id") Long id,
                                              @RequestParam("status") String status,
                                              @RequestParam(value = "auditRemark", required = false) String auditRemark) {{
        expenseService.auditExpense(id, status, auditRemark, getLoginUserId());
        return success(true);
    }}''')
    service_fields = ''
    if key == 'stockmove':
        # 流水只读，无写端点；service 为只读专用
        pass
    return f'''package {PKG}.controller.admin.{pkg};

import com.enterprise.framework.common.pojo.CommonResult;
import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.framework.excel.core.util.ExcelUtils;
import com.enterprise.framework.apilog.core.annotation.ApiAccessLog;
import {PKG}.controller.admin.{pkg}.vo.{pkg}.*;
import {PKG}.dal.dataobject.{pkg}.{Cls}DO;
import {PKG}.service.{pkg}.{Cls}Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static com.enterprise.framework.common.pojo.CommonResult.success;
import static com.enterprise.framework.common.pojo.PageParam.PAGE_SIZE_NONE;
import static com.enterprise.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.enterprise.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - {e['cn']}")
@RestController
@RequestMapping("/biz/{pkg}")
@Validated
public class {Cls}Controller {{

    @Resource
    private {Cls}Service {cls}Service;{service_fields}
{chr(10).join(methods)}
    @GetMapping("/get")
    @Operation(summary = "获得{e['cn']}")
    @Parameter(name = "id", description = "主键", required = true)
    @PreAuthorize("@ss.hasPermission('{perm}:query')")
    public CommonResult<{Cls}RespVO> get{Cls}(@RequestParam("id") Long id) {{
        {Cls}DO {cls} = {cls}Service.get{Cls}(id);
        return success(BeanUtils.toBean({cls}, {Cls}RespVO.class));
    }}

    @GetMapping("/page")
    @Operation(summary = "获得{e['cn']}分页")
    @PreAuthorize("@ss.hasPermission('{perm}:query')")
    public CommonResult<PageResult<{Cls}RespVO>> get{Cls}Page(@Valid {Cls}PageReqVO pageReqVO) {{
        PageResult<{Cls}DO> pageResult = {cls}Service.get{Cls}Page(pageReqVO);
        return success(BeanUtils.toBean(pageResult, {Cls}RespVO.class));
    }}

    @GetMapping("/export-excel")
    @Operation(summary = "导出{e['cn']} Excel")
    @PreAuthorize("@ss.hasPermission('{perm}:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void export{Cls}Excel(@Valid {Cls}PageReqVO pageReqVO, HttpServletResponse response) throws IOException {{
        pageReqVO.setPageSize(PAGE_SIZE_NONE);
        List<{Cls}RespVO> list = BeanUtils.toBean({cls}Service.get{Cls}Page(pageReqVO).getList(), {Cls}RespVO.class);
        ExcelUtils.write(response, "{e['cn']}.xls", "数据", {Cls}RespVO.class, list);
    }}
}}'''
for key, e in E.items():
    print('Controller:', key)
    w(f'{BIZ}/controller/admin/{e["pkg"]}/{cls_of(key, e)}Controller.java', gen_controller(key, e))

# ============ StockMove 只读 Service ============
w(f'{BIZ}/service/stockmove/StockMoveService.java', f'''package {PKG}.service.stockmove;

import com.enterprise.framework.common.pojo.PageResult;
import {PKG}.controller.admin.stockmove.vo.stockmove.StockMovePageReqVO;
import {PKG}.dal.dataobject.stockmove.StockMoveDO;

/**
 * 库存流水 Service 接口（只增不改）
 *
 * @author 企业管理平台
 */
public interface StockMoveService {{

    /**
     * 记录一条库存流水
     */
    void record(StockMoveDO move);

    /**
     * 获得库存流水
     */
    StockMoveDO getStockMove(Long id);

    /**
     * 获得库存流水分页
     */
    PageResult<StockMoveDO> getStockMovePage(StockMovePageReqVO pageReqVO);
}}''')
w(f'{BIZ}/service/stockmove/StockMoveServiceImpl.java', f'''package {PKG}.service.stockmove;

import com.enterprise.framework.common.pojo.PageResult;
import {PKG}.controller.admin.stockmove.vo.stockmove.StockMovePageReqVO;
import {PKG}.dal.dataobject.stockmove.StockMoveDO;
import {PKG}.dal.mysql.stockmove.StockMoveMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 库存流水 Service 实现类（只增不改）
 *
 * @author 企业管理平台
 */
@Service
@Validated
public class StockMoveServiceImpl implements StockMoveService {{

    @Resource
    private StockMoveMapper stockMoveMapper;

    @Override
    public void record(StockMoveDO move) {{
        stockMoveMapper.insert(move);
    }}

    @Override
    public StockMoveDO getStockMove(Long id) {{
        return stockMoveMapper.selectById(id);
    }}

    @Override
    public PageResult<StockMoveDO> getStockMovePage(StockMovePageReqVO pageReqVO) {{
        return stockMoveMapper.selectPage(pageReqVO);
    }}
}}''')

# ============ DashboardController ============
w(f'{BIZ}/controller/admin/dashboard/DashboardController.java', f'''package {PKG}.controller.admin.dashboard;

import com.enterprise.framework.common.pojo.CommonResult;
import {PKG}.dal.mysql.contract.ContractMapper;
import {PKG}.dal.mysql.leave.LeaveMapper;
import {PKG}.dal.mysql.purchase.PurchaseMapper;
import {PKG}.dal.mysql.sales.SalesMapper;
import {PKG}.dal.mysql.stock.StockMapper;
import {PKG}.dal.mysql.customer.CustomerMapper;
import {PKG}.dal.mysql.product.ProductMapper;
import {PKG}.dal.mysql.employee.EmployeeMapper;
import {PKG}.dal.mysql.expense.ExpenseMapper;
import {PKG}.dal.dataobject.leave.LeaveDO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

import static com.enterprise.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 数据看板")
@RestController
@RequestMapping("/biz/dashboard")
@Validated
public class DashboardController {{

    @Resource
    private CustomerMapper customerMapper;
    @Resource
    private ProductMapper productMapper;
    @Resource
    private EmployeeMapper employeeMapper;
    @Resource
    private SalesMapper salesMapper;
    @Resource
    private PurchaseMapper purchaseMapper;
    @Resource
    private StockMapper stockMapper;
    @Resource
    private LeaveMapper leaveMapper;
    @Resource
    private ExpenseMapper expenseMapper;
    @Resource
    private ContractMapper contractMapper;

    @PostMapping("/panel")
    @Operation(summary = "核心指标卡")
    @PreAuthorize("@ss.hasPermission('biz:dashboard:query')")
    public CommonResult<Map<String, Object>> panel() {{
        Map<String, Object> data = new HashMap<>();
        data.put("customerCount", customerMapper.selectCount());
        data.put("productCount", productMapper.selectCount());
        data.put("employeeCount", employeeMapper.selectCount());
        data.put("salesCount", salesMapper.selectCount());
        data.put("purchaseCount", purchaseMapper.selectCount());
        LeaveDO pending = new LeaveDO();
        pending.setStatus("0");
        data.put("leavePending", leaveMapper.selectCount(new QueryWrapper<LeaveDO>().eq("status", "0")));
        data.put("expensePending", expenseMapper.selectCount(new QueryWrapper<com.enterprise.module.biz.dal.dataobject.expense.ExpenseDO>().eq("status", "0")));
        // 库存预警数
        data.put("lowStockCount", stockMapper.selectList(new QueryWrapper<com.enterprise.module.biz.dal.dataobject.stock.StockDO>()
                .apply("quantity <= min_quantity")).size());
        return success(data);
    }}

    @PostMapping("/trend")
    @Operation(summary = "近7日销售/采购金额趋势")
    @PreAuthorize("@ss.hasPermission('biz:dashboard:query')")
    public CommonResult<Map<String, Object>> trend() {{
        LocalDate start = LocalDate.now().minusDays(6);
        Map<String, Object> salesMap = new HashMap<>();
        for (Map<String, Object> row : salesMapper.selectSumByDate(start.toString())) {{
            salesMap.put(String.valueOf(row.get("date")), row.get("total"));
        }}
        Map<String, Object> purchaseMap = new HashMap<>();
        for (Map<String, Object> row : purchaseMapper.selectSumByDate(start.toString())) {{
            purchaseMap.put(String.valueOf(row.get("date")), row.get("total"));
        }}
        List<String> dates = new ArrayList<>();
        List<Object> sales = new ArrayList<>();
        List<Object> purchase = new ArrayList<>();
        for (int i = 0; i < 7; i++) {{
            String d = start.plusDays(i).toString();
            dates.add(d.substring(5));
            sales.add(salesMap.getOrDefault(d, 0));
            purchase.add(purchaseMap.getOrDefault(d, 0));
        }}
        Map<String, Object> data = new HashMap<>();
        data.put("dates", dates);
        data.put("sales", sales);
        data.put("purchase", purchase);
        return success(data);
    }}

    @PostMapping("/productTop")
    @Operation(summary = "产品销售Top5")
    @PreAuthorize("@ss.hasPermission('biz:dashboard:query')")
    public CommonResult<List<Map<String, Object>>> productTop() {{
        return success(salesMapper.selectProductTop(5));
    }}

    @PostMapping("/status")
    @Operation(summary = "状态分布（合同/请假）")
    @PreAuthorize("@ss.hasPermission('biz:dashboard:query')")
    public CommonResult<Map<String, Object>> status() {{
        Map<String, Object> data = new HashMap<>();
        data.put("contract", contractMapper.selectStatusCount());
        data.put("leave", leaveMapper.selectStatusCount());
        return success(data);
    }}
}}''')

# ============ ApprovalController ============
w(f'{BIZ}/controller/admin/approval/ApprovalController.java', f'''package {PKG}.controller.admin.approval;

import com.enterprise.framework.common.pojo.CommonResult;
import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import {PKG}.controller.admin.leave.vo.leave.LeavePageReqVO;
import {PKG}.controller.admin.leave.vo.leave.LeaveRespVO;
import {PKG}.controller.admin.attendance.vo.attendance.AttendanceSaveReqVO;
import {PKG}.controller.admin.expense.vo.expense.ExpensePageReqVO;
import {PKG}.controller.admin.expense.vo.expense.ExpenseRespVO;
import {PKG}.dal.dataobject.expense.ExpenseDO;
import {PKG}.dal.dataobject.leave.LeaveDO;
import {PKG}.dal.mysql.expense.ExpenseMapper;
import {PKG}.dal.mysql.leave.LeaveMapper;
import {PKG}.service.expense.ExpenseService;
import {PKG}.service.leave.LeaveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static com.enterprise.framework.common.pojo.CommonResult.success;
import static com.enterprise.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 审批中心")
@RestController
@RequestMapping("/biz/approval")
@Validated
public class ApprovalController {{

    @Resource
    private LeaveMapper leaveMapper;
    @Resource
    private ExpenseMapper expenseMapper;
    @Resource
    private LeaveService leaveService;
    @Resource
    private ExpenseService expenseService;

    @PostMapping("/pending")
    @Operation(summary = "待办数量角标")
    @PreAuthorize("@ss.hasPermission('biz:approval:query')")
    public CommonResult<Map<String, Object>> pending() {{
        Map<String, Object> data = new HashMap<>();
        data.put("leaveCount", leaveMapper.selectCount(new QueryWrapper<LeaveDO>().eq("status", "0")));
        data.put("expenseCount", expenseMapper.selectCount(new QueryWrapper<ExpenseDO>().eq("status", "0")));
        return success(data);
    }}

    @GetMapping("/leave-page")
    @Operation(summary = "待审批请假分页")
    @PreAuthorize("@ss.hasPermission('biz:approval:query')")
    public CommonResult<PageResult<LeaveRespVO>> getLeavePage(LeavePageReqVO pageReqVO) {{
        pageReqVO.setStatus("0");
        PageResult<LeaveDO> pageResult = leaveService.getLeavePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, LeaveRespVO.class));
    }}

    @PostMapping("/leave-audit")
    @Operation(summary = "审批请假")
    @PreAuthorize("@ss.hasPermission('biz:approval:audit')")
    public CommonResult<Boolean> auditLeave(@RequestParam("id") Long id,
                                            @RequestParam("status") String status,
                                            @RequestParam(value = "auditRemark", required = false) String auditRemark) {{
        leaveService.auditLeave(id, status, auditRemark);
        return success(true);
    }}

    @GetMapping("/expense-page")
    @Operation(summary = "待审批报销分页")
    @PreAuthorize("@ss.hasPermission('biz:approval:query')")
    public CommonResult<PageResult<ExpenseRespVO>> getExpensePage(ExpensePageReqVO pageReqVO) {{
        pageReqVO.setStatus("0");
        PageResult<ExpenseDO> pageResult = expenseService.getExpensePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ExpenseRespVO.class));
    }}

    @PostMapping("/expense-audit")
    @Operation(summary = "审批报销")
    @PreAuthorize("@ss.hasPermission('biz:approval:audit')")
    public CommonResult<Boolean> auditExpense(@RequestParam("id") Long id,
                                              @RequestParam("status") String status,
                                              @RequestParam(value = "auditRemark", required = false) String auditRemark) {{
        expenseService.auditExpense(id, status, auditRemark, getLoginUserId());
        return success(true);
    }}
}}''')

# ============ PortalController（员工工作台） ============
w(f'{BIZ}/controller/admin/portal/PortalController.java', f'''package {PKG}.controller.admin.portal;

import com.enterprise.framework.common.pojo.CommonResult;
import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.framework.security.core.util.SecurityFrameworkUtils;
import com.enterprise.module.system.api.user.AdminUserApi;
import com.enterprise.module.system.api.user.dto.AdminUserRespDTO;
import {PKG}.controller.admin.attendance.vo.attendance.AttendanceSaveReqVO;
import {PKG}.controller.admin.expense.vo.expense.ExpensePageReqVO;
import {PKG}.controller.admin.expense.vo.expense.ExpenseRespVO;
import {PKG}.controller.admin.expense.vo.expense.ExpenseSaveReqVO;
import {PKG}.controller.admin.leave.vo.leave.LeavePageReqVO;
import {PKG}.controller.admin.leave.vo.leave.LeaveRespVO;
import {PKG}.controller.admin.leave.vo.leave.LeaveSaveReqVO;
import {PKG}.controller.admin.report.vo.report.ReportPageReqVO;
import {PKG}.controller.admin.report.vo.report.ReportRespVO;
import {PKG}.controller.admin.report.vo.report.ReportSaveReqVO;
import {PKG}.dal.dataobject.attendance.AttendanceDO;
import {PKG}.dal.dataobject.expense.ExpenseDO;
import {PKG}.dal.dataobject.leave.LeaveDO;
import {PKG}.dal.dataobject.report.ReportDO;
import {PKG}.service.attendance.AttendanceService;
import {PKG}.service.expense.ExpenseService;
import {PKG}.service.leave.LeaveService;
import {PKG}.service.quota.LeaveQuotaService;
import {PKG}.service.report.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static com.enterprise.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.enterprise.framework.common.pojo.CommonResult.success;
import static {PKG}.enums.ErrorCodeConstants.*;

@Tag(name = "管理后台 - 员工工作台")
@RestController
@RequestMapping("/portal")
@Validated
public class PortalController {{

    private static final String WORK_START = "09:00";
    private static final String WORK_END = "18:00";

    @Resource
    private AttendanceService attendanceService;
    @Resource
    private LeaveService leaveService;
    @Resource
    private LeaveQuotaService quotaService;
    @Resource
    private ReportService reportService;
    @Resource
    private ExpenseService expenseService;
    @Resource
    private AdminUserApi adminUserApi;

    private String displayName() {{
        AdminUserRespDTO user = adminUserApi.getUser(SecurityFrameworkUtils.getLoginUserId());
        return user == null ? "未知" : user.getNickname();
    }}

    private Long loginUserId() {{
        return SecurityFrameworkUtils.getLoginUserId();
    }}

    @GetMapping("/index-data")
    @Operation(summary = "工作台首页数据：今日打卡 + 假期余额")
    @PreAuthorize("@ss.hasPermission('portal:index:query')")
    public CommonResult<Map<String, Object>> indexData() {{
        Map<String, Object> data = new HashMap<>();
        data.put("today", LocalDate.now().toString());
        data.put("workStart", WORK_START);
        data.put("workEnd", WORK_END);
        AttendanceDO today = attendanceService.getTodayAttendance(displayName(), LocalDate.now().toString());
        data.put("checkIn", today == null ? null : today.getCheckIn());
        data.put("checkOut", today == null ? null : today.getCheckOut());
        data.put("status", today == null ? null : today.getStatus());
        Map<String, Object> quotaMap = new LinkedHashMap<>();
        for (String type : Arrays.asList("1", "2", "3", "4")) {{
            BigDecimal remain = quotaService.findRemainDays(displayName(), type, String.valueOf(LocalDate.now().getYear()));
            if (remain.compareTo(new BigDecimal("99999")) < 0) {{
                quotaMap.put(type, remain);
            }}
        }}
        data.put("quotas", quotaMap);
        return success(data);
    }}

    @PostMapping("/punch")
    @Operation(summary = "打卡（type: in 上班 / out 下班）")
    @PreAuthorize("@ss.hasPermission('portal:punch:add')")
    public CommonResult<String> punch(@RequestParam("type") String type) {{
        String now = LocalTime.now().withNano(0).toString().substring(0, 5);
        String today = LocalDate.now().toString();
        String name = displayName();
        AttendanceDO record = attendanceService.getTodayAttendance(name, today);
        if ("in".equals(type)) {{
            if (record != null && record.getCheckIn() != null) {{
                throw exception(PUNCH_DUPLICATE);
            }}
            AttendanceSaveReqVO save = new AttendanceSaveReqVO();
            save.setEmpName(name);
            save.setWorkDate(today);
            save.setCheckIn(now);
            save.setStatus(now.compareTo(WORK_START) > 0 ? "1" : "0");
            if (record == null) {{
                attendanceService.createAttendance(save);
            }} else {{
                save.setId(record.getId());
                attendanceService.updateAttendance(save);
            }}
            return success("上班打卡成功（" + now + "）" + (now.compareTo(WORK_START) > 0 ? "，迟到" : ""));
        }} else if ("out".equals(type)) {{
            if (record != null && record.getCheckOut() != null) {{
                throw exception(PUNCH_DUPLICATE);
            }}
            AttendanceSaveReqVO save = new AttendanceSaveReqVO();
            save.setEmpName(name);
            save.setWorkDate(today);
            save.setCheckOut(now);
            if (record == null) {{
                save.setStatus("3");
                attendanceService.createAttendance(save);
                return success("下班打卡成功（" + now + "），注意：今日无上班打卡记录");
            }}
            // 已有记录：必定更新该记录（迟到状态保持为迟到，其余按下班时间判定早退）
            save.setId(record.getId());
            if (!"1".equals(record.getStatus())) {{
                save.setStatus(now.compareTo(WORK_END) < 0 ? "2" : "0");
            }}
            attendanceService.updateAttendance(save);
            return success("下班打卡成功（" + now + "）" + (now.compareTo(WORK_END) < 0 ? "，早退" : ""));
        }}
        throw exception(PUNCH_TYPE_INVALID);
    }}

    @GetMapping("/leave-page")
    @Operation(summary = "我的请假分页")
    @PreAuthorize("@ss.hasPermission('portal:leave:query')")
    public CommonResult<PageResult<LeaveRespVO>> getLeavePage(LeavePageReqVO pageReqVO) {{
        return success(BeanUtils.toBean(leaveService.getLeavePageSelf(pageReqVO, loginUserId()), LeaveRespVO.class));
    }}

    @PostMapping("/leave-submit")
    @Operation(summary = "提交请假")
    @PreAuthorize("@ss.hasPermission('portal:leave:add')")
    public CommonResult<Long> submitLeave(@Valid @RequestBody LeaveSaveReqVO createReqVO) {{
        String year = createReqVO.getStartDate() != null && createReqVO.getStartDate().length() >= 4
                ? createReqVO.getStartDate().substring(0, 4) : String.valueOf(LocalDate.now().getYear());
        BigDecimal days = createReqVO.getDays() == null ? BigDecimal.ZERO : createReqVO.getDays();
        BigDecimal remain = quotaService.findRemainDays(displayName(), createReqVO.getLeaveType(), year);
        if (days.compareTo(BigDecimal.ZERO) > 0 && remain.compareTo(days) < 0) {{
            throw exception(LEAVE_QUOTA_NOT_ENOUGH);
        }}
        createReqVO.setEmpName(displayName());
        createReqVO.setStatus("0");
        return success(leaveService.createLeave(createReqVO));
    }}

    @PostMapping("/leave-cancel")
    @Operation(summary = "销假")
    @PreAuthorize("@ss.hasPermission('portal:leave:add')")
    public CommonResult<Boolean> cancelLeave(@RequestParam("id") Long id) {{
        leaveService.cancelLeave(id, loginUserId());
        return success(true);
    }}

    @GetMapping("/report-page")
    @Operation(summary = "我的汇报分页")
    @PreAuthorize("@ss.hasPermission('portal:report:query')")
    public CommonResult<PageResult<ReportRespVO>> getReportPage(ReportPageReqVO pageReqVO) {{
        return success(BeanUtils.toBean(reportService.getReportPageSelf(pageReqVO, loginUserId()), ReportRespVO.class));
    }}

    @PostMapping("/report-submit")
    @Operation(summary = "提交汇报")
    @PreAuthorize("@ss.hasPermission('portal:report:add')")
    public CommonResult<Long> submitReport(@Valid @RequestBody ReportSaveReqVO createReqVO) {{
        return success(reportService.createReport(createReqVO));
    }}

    @DeleteMapping("/report-delete")
    @Operation(summary = "删除本人汇报")
    @PreAuthorize("@ss.hasPermission('portal:report:add')")
    public CommonResult<Boolean> deleteReport(@RequestParam("id") Long id) {{
        ReportDO report = reportService.getReport(id);
        if (report == null || !String.valueOf(loginUserId()).equals(report.getCreator())) {{
            throw exception(PORTAL_NOT_OWNER);
        }}
        reportService.deleteReport(id);
        return success(true);
    }}

    @GetMapping("/expense-page")
    @Operation(summary = "我的报销分页")
    @PreAuthorize("@ss.hasPermission('portal:expense:query')")
    public CommonResult<PageResult<ExpenseRespVO>> getExpensePage(ExpensePageReqVO pageReqVO) {{
        return success(BeanUtils.toBean(expenseService.getExpensePageSelf(pageReqVO, loginUserId()), ExpenseRespVO.class));
    }}

    @PostMapping("/expense-submit")
    @Operation(summary = "提交报销")
    @PreAuthorize("@ss.hasPermission('portal:expense:add')")
    public CommonResult<Long> submitExpense(@Valid @RequestBody ExpenseSaveReqVO createReqVO) {{
        if (createReqVO.getAmount() == null || createReqVO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {{
            throw exception(EXPENSE_AMOUNT_INVALID);
        }}
        createReqVO.setEmpName(displayName());
        createReqVO.setStatus("0");
        return success(expenseService.createExpense(createReqVO));
    }}

    @DeleteMapping("/expense-withdraw")
    @Operation(summary = "撤回待审批报销")
    @PreAuthorize("@ss.hasPermission('portal:expense:add')")
    public CommonResult<Boolean> withdrawExpense(@RequestParam("id") Long id) {{
        ExpenseDO expense = expenseService.getExpense(id);
        if (expense == null || !String.valueOf(loginUserId()).equals(expense.getCreator())) {{
            throw exception(PORTAL_NOT_OWNER);
        }}
        if (!"0".equals(expense.getStatus())) {{
            throw exception(EXPENSE_ALREADY_AUDITED);
        }}
        expenseService.deleteExpense(id);
        return success(true);
    }}
}}''')

# ============ SQL 生成 ============
def gen_sql():
    parts = ['''-- ----------------------------
-- 企业业务模块 SQL（enterprise-pro / yudao 架构）
-- 依赖：先导入 ruoyi-vue-pro.sql 基础库
-- ----------------------------
''']
    for key, e in E.items():
        cols = []
        for (n, t, label, sq, q, r, d) in e['fields']:
            nn = 'DEFAULT NULL'
            cols.append("  `%s`  %s  %s  COMMENT '%s'" % (sql_col(n), sq, nn, label))
        parts.append(f'''-- ----------------------------
-- {e['cn']}表 {e['table']}
-- ----------------------------
DROP TABLE IF EXISTS {e['table']};
CREATE TABLE {e['table']} (
  `id`  bigint  NOT NULL AUTO_INCREMENT  COMMENT '主键',
{',\n'.join(cols)},
  `creator`    varchar(64)  DEFAULT ''  COMMENT '创建者',
  `create_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater`    varchar(64)  DEFAULT ''  COMMENT '更新者',
  `update_time` datetime    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted`    bit(1)       NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id`  bigint       NOT NULL DEFAULT 0  COMMENT '租户编号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='{e['cn']}表';
''')
    dicts = {
        'biz_common_status': ('通用状态', [('正常','0'),('停用','1')]),
        'biz_contract_status': ('合同状态', [('草稿','0'),('执行中','1'),('已完成','2'),('已终止','3')]),
        'biz_inout_status': ('出入库状态', [('待处理','0'),('已完成','1')]),
        'biz_attendance_status': ('考勤状态', [('正常','0'),('迟到','1'),('早退','2'),('缺勤','3')]),
        'biz_leave_type': ('请假类型', [('事假','1'),('病假','2'),('年假','3'),('调休','4')]),
        'biz_leave_status': ('请假审批状态', [('待审批','0'),('已通过','1'),('已驳回','2'),('已销假','3')]),
        'biz_report_type': ('汇报类型', [('日报','1'),('周报','2'),('月报','3')]),
        'biz_expense_type': ('报销类别', [('差旅','1'),('餐费','2'),('办公','3'),('其他','4')]),
        'biz_expense_status': ('报销审批状态', [('待审批','0'),('已通过','1'),('已驳回','2')]),
        'biz_stock_move_type': ('库存流水类型', [('入库','1'),('出库','2')]),
    }
    for dtype, (dname, items) in dicts.items():
        parts.append(f"INSERT INTO system_dict_type (name, type, status, remark, creator, create_time, updater, update_time, deleted) "
                     f"VALUES ('{dname}', '{dtype}', 0, '业务字典', '1', NOW(), '1', NOW(), b'0');")
        for sort, (label, value) in enumerate(items, 1):
            parts.append(f"INSERT INTO system_dict_data (sort, label, value, dict_type, status, creator, create_time, updater, update_time, deleted) "
                         f"VALUES ({sort}, '{label}', '{value}', '{dtype}', 0, '1', NOW(), '1', NOW(), b'0');")
    parts.append(menu_sql())
    return '\n'.join(parts)

def sql_col(n):
    import re
    return re.sub(r'([A-Z])', lambda mm: '_' + mm.group(1).lower(), n)

def menu_sql():
    def m(name, perm, mtype, sort, parent, path='', icon='#', component='', cname=''):
        return (f"INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, component_name, "
                f"status, creator, create_time, updater, update_time, deleted) "
                f"VALUES ('{name}', '{perm}', {mtype}, {sort}, {parent}, '{path}', '{icon}', '{component}', '{cname}', "
                f"0, '1', NOW(), '1', NOW(), b'0');")
    out = []
    out.append(m('企业管理', '', 1, 10, 0, '/biz', 'ep:suitcase'))
    out.append("SET @dirBiz = LAST_INSERT_ID();")
    out.append(m('员工工作台', '', 1, 5, 0, '/portal', 'ep:user'))
    out.append("SET @dirPortal = LAST_INSERT_ID();")
    groups = {
        '客户合同产品': ('@dirBiz', 'crm', ['customer', 'product', 'contract', 'supplier']),
        '进销存管理': ('@dirBiz', 'inventory', ['purchase', 'sales', 'stock', 'stockmove']),
        '人事考勤': ('@dirBiz', 'hr', ['employee', 'attendance', 'leave', 'quota']),
        '协作审批': ('@dirBiz', 'collab', ['report', 'expense']),
    }
    btns = ['query:查询', 'create:新增', 'update:修改', 'delete:删除', 'export:导出']
    for gname, (parent, gpath, mods) in groups.items():
        out.append(m(gname, '', 1, 1, parent, gpath, '#'))
        out.append("SET @dir = LAST_INSERT_ID();")
        for mod in mods:
            e = E[mod]
            out.append(m(e['cn'], f"{e['perm']}:query", 2, 1, '@dir', e['pkg'], '#', f'biz/{e["pkg"]}/index', cap(mod)))
            out.append("SET @m = LAST_INSERT_ID();")
            for idx, b in enumerate(btns, 1):
                perm, label = b.split(':')
                out.append(m(f"{e['cn']}{label}", f"{e['perm']}:{perm}", 3, idx, '@m', '', '#'))
            if mod in ('leave', 'expense'):
                out.append(m(f"{e['cn']}审批", f"{e['perm']}:audit", 3, 0, '@m', '', '#'))
    out.append(m('审批中心', 'biz:approval:query', 2, 2, '@dirBiz', 'approval', 'ep:finished', 'biz/approval/index', 'Approval'))
    out.append(m('数据看板', 'biz:dashboard:query', 2, 0, '@dirBiz', 'dashboard', 'ep:data-line', 'biz/dashboard/index', 'Dashboard'))
    out.append(m('我的打卡', 'portal:index:query', 2, 1, '@dirPortal', 'index', '#', 'portal/index', 'PortalIndex'))
    out.append("SET @m = LAST_INSERT_ID();")
    out.append(m('打卡操作', 'portal:punch:add', 3, 1, '@m', '', '#'))
    out.append(m('我的请假', 'portal:leave:query', 2, 2, '@dirPortal', 'leave', '#', 'portal/leave/index', 'PortalLeave'))
    out.append("SET @m = LAST_INSERT_ID();")
    out.append(m('请假提交', 'portal:leave:add', 3, 1, '@m', '', '#'))
    out.append(m('业务汇报', 'portal:report:query', 2, 3, '@dirPortal', 'report', '#', 'portal/report/index', 'PortalReport'))
    out.append("SET @m = LAST_INSERT_ID();")
    out.append(m('汇报提交', 'portal:report:add', 3, 1, '@m', '', '#'))
    out.append(m('我的报销', 'portal:expense:query', 2, 4, '@dirPortal', 'expense', '#', 'portal/expense/index', 'PortalExpense'))
    out.append("SET @m = LAST_INSERT_ID();")
    out.append(m('报销提交', 'portal:expense:add', 3, 1, '@m', '', '#'))
    out.append("INSERT INTO system_role_menu (role_id, menu_id, creator, create_time, updater, update_time, deleted) "
               "SELECT 2, id, '1', NOW(), '1', NOW(), b'0' FROM system_menu WHERE permission LIKE 'portal:%';")
    return '\n'.join(out)

sql = gen_sql()
os.makedirs(os.path.join(ROOT, 'sql', 'mysql'), exist_ok=True)
with io.open(os.path.join(ROOT, 'sql', 'mysql', 'enterprise-biz.sql'), 'w', encoding='utf-8', newline='\n') as f:
    f.write(sql)
print('SQL written: sql/mysql/enterprise-biz.sql')
print('DONE')
