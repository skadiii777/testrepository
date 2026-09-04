# -*- coding: utf-8 -*-
"""RuoYi 业务模块代码生成器：按实体规格生成 domain/mapper/xml/service/controller/html"""
import os, io

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

# 字段: (javaName, javaType, label, sqlType, query(是否列表查询项), required, dictType(None或字典key))
E = {}

E['customer'] = dict(cn='客户', table='biz_customer', path='customer', perm='biz:customer',
    fields=[('customerName','String','客户名称','varchar(100)',1,1,None),
            ('contactPerson','String','联系人','varchar(50)',1,1,None),
            ('phone','String','联系电话','varchar(30)',0,0,None),
            ('email','String','邮箱','varchar(100)',0,0,None),
            ('industry','String','所属行业','varchar(50)',0,0,None),
            ('source','String','客户来源','varchar(50)',0,0,None),
            ('address','String','地址','varchar(255)',0,0,None),
            ('status','String','状态','char(1)',1,0,'sys_normal_disable')])
E['product'] = dict(cn='产品', table='biz_product', path='product', perm='biz:product',
    fields=[('productCode','String','产品编号','varchar(50)',1,1,None),
            ('productName','String','产品名称','varchar(100)',1,1,None),
            ('category','String','产品分类','varchar(50)',1,0,None),
            ('unit','String','单位','varchar(20)',0,0,None),
            ('price','java.math.BigDecimal','销售单价','decimal(12,2)',0,1,None),
            ('cost','java.math.BigDecimal','成本价','decimal(12,2)',0,0,None),
            ('status','String','状态','char(1)',1,0,'sys_normal_disable')])
E['contract'] = dict(cn='合同', table='biz_contract', path='contract', perm='biz:contract',
    fields=[('contractCode','String','合同编号','varchar(50)',1,1,None),
            ('customerName','String','客户名称','varchar(100)',1,1,None),
            ('productName','String','产品名称','varchar(100)',0,0,None),
            ('amount','java.math.BigDecimal','合同金额','decimal(12,2)',0,1,None),
            ('signDate','String','签订日期','varchar(20)',0,1,None),
            ('startDate','String','开始日期','varchar(20)',0,0,None),
            ('endDate','String','结束日期','varchar(20)',0,0,None),
            ('owner','String','负责人','varchar(50)',1,0,None),
            ('status','String','合同状态','char(1)',1,0,'biz_contract_status')])
E['supplier'] = dict(cn='供应商', table='biz_supplier', path='supplier', perm='biz:supplier',
    fields=[('supplierName','String','供应商名称','varchar(100)',1,1,None),
            ('contactPerson','String','联系人','varchar(50)',1,0,None),
            ('phone','String','联系电话','varchar(30)',0,0,None),
            ('address','String','地址','varchar(255)',0,0,None),
            ('status','String','状态','char(1)',1,0,'sys_normal_disable')])
E['purchase'] = dict(cn='采购单', table='biz_purchase', path='purchase', perm='biz:purchase',
    fields=[('purchaseCode','String','采购单号','varchar(50)',1,1,None),
            ('supplierName','String','供应商','varchar(100)',1,0,None),
            ('productName','String','产品名称','varchar(100)',1,0,None),
            ('quantity','Long','采购数量','int(11)',0,1,None),
            ('price','java.math.BigDecimal','采购单价','decimal(12,2)',0,1,None),
            ('totalAmount','java.math.BigDecimal','总金额','decimal(12,2)',0,0,None),
            ('purchaseDate','String','采购日期','varchar(20)',1,0,None),
            ('status','String','入库状态','char(1)',1,0,'biz_inout_status')])
E['sales'] = dict(cn='销售单', table='biz_sales', path='sales', perm='biz:sales',
    fields=[('salesCode','String','销售单号','varchar(50)',1,1,None),
            ('customerName','String','客户','varchar(100)',1,0,None),
            ('productName','String','产品名称','varchar(100)',1,0,None),
            ('quantity','Long','销售数量','int(11)',0,1,None),
            ('price','java.math.BigDecimal','销售单价','decimal(12,2)',0,1,None),
            ('totalAmount','java.math.BigDecimal','总金额','decimal(12,2)',0,0,None),
            ('salesDate','String','销售日期','varchar(20)',1,0,None),
            ('status','String','出库状态','char(1)',1,0,'biz_inout_status')])
E['stock'] = dict(cn='库存', table='biz_stock', path='stock', perm='biz:stock',
    fields=[('productName','String','产品名称','varchar(100)',1,1,None),
            ('warehouse','String','仓库','varchar(50)',1,0,None),
            ('quantity','Long','库存数量','int(11)',0,0,None),
            ('minQuantity','Long','预警下限','int(11)',0,0,None)])
E['employee'] = dict(cn='员工', table='biz_employee', path='employee', perm='biz:employee',
    fields=[('empNo','String','工号','varchar(30)',1,1,None),
            ('empName','String','姓名','varchar(50)',1,1,None),
            ('deptName','String','部门','varchar(50)',1,0,None),
            ('postName','String','岗位','varchar(50)',0,0,None),
            ('phone','String','联系电话','varchar(30)',0,0,None),
            ('email','String','邮箱','varchar(100)',0,0,None),
            ('entryDate','String','入职日期','varchar(20)',0,0,None),
            ('status','String','状态','char(1)',1,0,'sys_normal_disable')])
E['attendance'] = dict(cn='考勤', table='biz_attendance', path='attendance', perm='biz:attendance',
    fields=[('empName','String','员工姓名','varchar(50)',1,1,None),
            ('workDate','String','考勤日期','varchar(20)',1,1,None),
            ('checkIn','String','上班时间','varchar(10)',0,0,None),
            ('checkOut','String','下班时间','varchar(10)',0,0,None),
            ('status','String','考勤状态','char(1)',1,0,'biz_attendance_status')])
E['leave'] = dict(cn='请假', table='biz_leave', path='leave', perm='biz:leave',
    fields=[('empName','String','员工姓名','varchar(50)',1,1,None),
            ('leaveType','String','请假类型','char(1)',1,0,'biz_leave_type'),
            ('startDate','String','开始日期','varchar(20)',1,0,None),
            ('endDate','String','结束日期','varchar(20)',1,0,None),
            ('days','java.math.BigDecimal','请假天数','decimal(4,1)',0,1,None),
            ('reason','String','请假事由','varchar(500)',0,0,None),
            ('status','String','审批状态','char(1)',1,0,'biz_leave_status')])

def cap(s): return s[0].upper() + s[1:]
def snake(s):
    return ''.join('_'+c.lower() if c.isupper() else c for c in s).lstrip('_')
def type_short(t): return t.split('.')[-1]
def default_val(t):
    return {'String':'""','Long':'0L','java.math.BigDecimal':'new BigDecimal("0")'}.get(t,'null')

def w(path, content):
    full = os.path.join(ROOT, path)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    with io.open(full, 'w', encoding='utf-8', newline='\n') as f:
        f.write(content)
    print('write', path)

# ============ Java: domain ============
DOMAIN_T = '''package com.ruoyi.business.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * {cn}对象 {table}
 * 
 * @author biz
 */
public class {Cls} extends BaseEntity
{{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    public void setId(Long id)
    {{
        this.id = id;
    }}

    public Long getId()
    {{
        return id;
    }}
{FIELDS}
    public {Cls}() {{
    }}
{METHODS}
    @Override
    public String toString() {{
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE){TOSTR}
            .toString();
    }}
}}
'''

def gen_domain(key, e):
    fields, methods, tostr = [], [], []
    fields.append('    /** %s */\n' % e['cn'])
    for (n, t, label, sq, q, r, d) in e['fields']:
        ann = '@Excel(name = "%s")' % label
        if d: ann = '@Excel(name = "%s", readConverterExp = "%s")' % (label, DICT_READ.get(d, ''))
        fields.append('    /** %s */\n    %s\n    private %s %s;\n' % (label, ann, type_short(t), n))
        T = type_short(t)
        methods.append('''
    public void set%s(%s %s)
    {{
        this.%s = %s;
    }}

    public %s get%s()
    {{
        return %s;
    }}
''' % (cap(n), T, n, n, n, T, cap(n), n))
        tostr.append('\n            .append("%s", get%s())' % (n, cap(n)))
    return DOMAIN_T.format(cn=e['cn'], table=e['table'], Cls=cap(key), Fields=''.join(fields),
                           FIELDS=''.join(fields), METHODS=''.join(methods), TOSTR=''.join(tostr))

DICT_READ = {
  'sys_normal_disable': '0=正常,1=停用',
  'biz_contract_status': '0=草稿,1=执行中,2=已完成,3=已终止',
  'biz_inout_status': '0=待处理,1=已完成',
  'biz_attendance_status': '0=正常,1=迟到,2=早退,3=缺勤',
  'biz_leave_type': '1=事假,2=病假,3=年假,4=调休',
  'biz_leave_status': '0=待审批,1=已通过,2=已驳回',
}

# ============ Java: mapper interface ============
MAPPER_T = '''package com.ruoyi.business.mapper;

import java.util.List;
import com.ruoyi.business.domain.{Cls};

/**
 * {cn} 数据层
 * 
 * @author biz
 */
public interface {Cls}Mapper
{{
    public {Cls} select{Cls}ById(Long id);

    public List<{Cls}> select{Cls}List({Cls} {key});

    public int insert{Cls}({Cls} {key});

    public int update{Cls}({Cls} {key});

    public int delete{Cls}ByIds(String[] ids);
}}
'''

# ============ mapper xml ============
XML_T = '''<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
"http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.ruoyi.business.mapper.{Cls}Mapper">
    
    <resultMap type="{Cls}" id="{Cls}Result">
        <id     property="id"           column="{key}_id"       />
{RESULTMAP}
        <result property="remark"    column="remark"    />
        <result property="createBy"    column="create_by"    />
        <result property="createTime"    column="create_time"    />
        <result property="updateBy"    column="update_by"    />
        <result property="updateTime"    column="update_time"    />
    </resultMap>

    <sql id="select{Cls}Vo">
        select {key}_id,{COLS}, create_by, create_time, update_by, update_time, remark from {table}
    </sql>

    <select id="select{Cls}List" parameterType="{Cls}" resultMap="{Cls}Result">
        <include refid="select{Cls}Vo"/>
        <where>  
{WHERES}
            <if test="remark != null  and remark != ''">
                and remark like concat('%', #{{remark}}, '%')
            </if>
        </where>
        order by {key}_id desc
    </select>
    
    <select id="select{Cls}ById" parameterType="Long" resultMap="{Cls}Result">
        <include refid="select{Cls}Vo"/>
        where {key}_id = #{{id}}
    </select>
        
    <insert id="insert{Cls}" parameterType="{Cls}" useGeneratedKeys="true" keyProperty="id">
        insert into {table}(
{INSCOLS}
            <if test="remark != null and remark != ''">remark,</if>
            <if test="createBy != null and createBy != ''">create_by,</if>
            create_time
        )values(
{INSVALS}
            <if test="remark != null and remark != ''">#{{remark}},</if>
            <if test="createBy != null and createBy != ''">#{{createBy}},</if>
            sysdate()
        )
    </insert>
        
    <update id="update{Cls}" parameterType="{Cls}">
        update {table}
        <set>
{UPDATES}
            <if test="remark != null">remark = #{{remark}},</if>
            <if test="updateBy != null and updateBy != ''">update_by = #{{updateBy}},</if>
            update_time = sysdate()
        </set>
        where {key}_id = #{{id}}
    </update>

    <delete id="delete{Cls}ByIds" parameterType="String">
        delete from {table} where {key}_id in 
        <foreach item="id" collection="array" open="(" separator="," close=")">
            #{{id}}
        </foreach>
    </delete>
    
</mapper>
'''

def gen_xml(key, e):
    rm, cols, wheres, insc, insv, ups = [], [snake(key)+'_id'], [], [], [], []
    for (n, t, label, sq, q, r, d) in e['fields']:
        col = snake(n)
        rm.append('        <result property="%s"    column="%s"    />' % (n, col))
        cols.append(col)
        if q:
            if t == 'String' and n not in ('status',):
                wheres.append('            <if test="%s != null  and %s != \'\'">\n                and %s like concat(\'%%\', #{%s}, \'%%\')\n            </if>' % (n, n, col, n))
            else:
                wheres.append('            <if test="%s != null  and %s != \'\'">\n                and %s = #{%s}\n            </if>' % (n, n, col, n))
        insc.append('            <if test="%s != null and %s != \'\'">%s,</if>' % (n, n, col))
        jt = {'String': None, 'Long': None, 'java.math.BigDecimal': None}[t]
        insv.append('            <if test="%s != null and %s != \'\'">#{%s},</if>' % (n, n, n))
        ups.append('            <if test="%s != null and %s != \'\'">%s = #{%s},</if>' % (n, n, col, n))
    return XML_T.format(Cls=cap(key), key=key, table=e['table'],
        RESULTMAP='\n'.join(rm), COLS=', '.join(cols[1:]), WHERES='\n'.join(wheres),
        INSCOLS='\n'.join(insc), INSVALS='\n'.join(insv), UPDATES='\n'.join(ups))

# ============ service interface / impl ============
SVC_T = '''package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.{Cls};

/**
 * {cn} 服务层
 * 
 * @author biz
 */
public interface I{Cls}Service 
{{
    public {Cls} select{Cls}ById(Long id);

    public List<{Cls}> select{Cls}List({Cls} {key});

    public int insert{Cls}({Cls} {key});

    public int update{Cls}({Cls} {key});

    public int delete{Cls}ByIds(String ids);
}}
'''

SVCIMPL_T = '''package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.business.domain.{Cls};
import com.ruoyi.business.mapper.{Cls}Mapper;
import com.ruoyi.business.service.I{Cls}Service;

/**
 * {cn} 服务层实现
 * 
 * @author biz
 */
@Service
public class {Cls}ServiceImpl implements I{Cls}Service
{{
    @Autowired
    private {Cls}Mapper {key}Mapper;

    @Override
    public {Cls} select{Cls}ById(Long id)
    {{
        return {key}Mapper.select{Cls}ById(id);
    }}

    @Override
    public List<{Cls}> select{Cls}List({Cls} {key})
    {{
        return {key}Mapper.select{Cls}List({key});
    }}

    @Override
    public int insert{Cls}({Cls} {key})
    {{
        return {key}Mapper.insert{Cls}({key});
    }}

    @Override
    public int update{Cls}({Cls} {key})
    {{
        return {key}Mapper.update{Cls}({key});
    }}

    @Override
    public int delete{Cls}ByIds(String ids)
    {{
        return {key}Mapper.delete{Cls}ByIds(Convert.toStrArray(ids));
    }}
{EXTRA}
}}
'''

# ============ controller ============
CTRL_T = '''package com.ruoyi.business.controller;

import java.util.List;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.business.domain.{Cls};
import com.ruoyi.business.service.I{Cls}Service;

/**
 * {cn} 信息操作处理
 * 
 * @author biz
 */
@Controller
@RequestMapping("/biz/{key}")
public class {Cls}Controller extends BaseController
{{
    private String prefix = "biz/{key}";

    @Autowired
    private I{Cls}Service {key}Service;

    @RequiresPermissions("{perm}:view")
    @GetMapping()
    public String {key}()
    {{
        return prefix + "/{key}";
    }}

    @RequiresPermissions("{perm}:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list({Cls} {key})
    {{
        startPage();
        List<{Cls}> list = {key}Service.select{Cls}List({key});
        return getDataTable(list);
    }}

    @RequiresPermissions("{perm}:add")
    @GetMapping("/add")
    public String add()
    {{
        return prefix + "/add";
    }}

    @RequiresPermissions("{perm}:add")
    @Log(title = "{cn}", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    @ResponseBody
    public AjaxResult addSave({Cls} {key})
    {{
        return toAjax({key}Service.insert{Cls}({key}));
    }}

    @RequiresPermissions("{perm}:edit")
    @GetMapping("/edit/{{id}}")
    public String edit(@PathVariable("id") Long id, ModelMap mmap)
    {{
        mmap.put("{key}", {key}Service.select{Cls}ById(id));
        return prefix + "/edit";
    }}

    @RequiresPermissions("{perm}:edit")
    @Log(title = "{cn}", businessType = BusinessType.UPDATE)
    @PostMapping("/edit")
    @ResponseBody
    public AjaxResult editSave({Cls} {key})
    {{
        return toAjax({key}Service.update{Cls}({key}));
    }}

    @RequiresPermissions("{perm}:remove")
    @Log(title = "{cn}", businessType = BusinessType.DELETE)
    @PostMapping("/remove")
    @ResponseBody
    public AjaxResult remove(String ids)
    {{
        return toAjax({key}Service.delete{Cls}ByIds(ids));
    }}
}}
'''

# ============ html pages ============
LIST_T = '''<!DOCTYPE html>
<html lang="zh" xmlns:th="http://www.thymeleaf.org" xmlns:shiro="http://www.pollix.at/thymeleaf/shiro">
<head>
    <th:block th:include="include :: header('{cn}列表')" />
</head>
<body class="gray-bg">
    <div class="container-div">
        <div class="row">
            <div class="col-sm-12 search-collapse">
                <form id="{key}-form">
                    <div class="select-list">
                        <ul>
{SEARCH}
                            <li>
                                <a class="btn btn-primary btn-rounded btn-sm" onclick="$.table.search()"><i class="fa fa-search"></i>&nbsp;搜索</a>
                                <a class="btn btn-warning btn-rounded btn-sm" onclick="$.form.reset()"><i class="fa fa-refresh"></i>&nbsp;重置</a>
                            </li>
                        </ul>
                    </div>
                </form>
            </div>

            <div class="btn-group-sm" id="toolbar" role="group">
                <a class="btn btn-success" onclick="$.operate.add()" shiro:hasPermission="{perm}:add">
                    <i class="fa fa-plus"></i> 新增
                </a>
                <a class="btn btn-primary single disabled" onclick="$.operate.edit()" shiro:hasPermission="{perm}:edit">
                    <i class="fa fa-edit"></i> 修改
                </a>
                <a class="btn btn-danger multiple disabled" onclick="$.operate.removeAll()" shiro:hasPermission="{perm}:remove">
                    <i class="fa fa-remove"></i> 删除
                </a>
            </div>

            <div class="col-sm-12 select-table table-striped">
                <table id="bootstrap-table"></table>
            </div>
        </div>
    </div>
    <th:block th:include="include :: footer" />
    <script th:inline="javascript">
        var editFlag = [[${{ @permission.hasPermi('{perm}:edit') }}]];
        var removeFlag = [[${{ @permission.hasPermi('{perm}:remove') }}]];
{DICTVARS}
        var prefix = ctx + "biz/{key}";

        $(function() {{
            var options = {{
                url: prefix + "/list",
                createUrl: prefix + "/add",
                updateUrl: prefix + "/edit/{{id}}",
                removeUrl: prefix + "/remove",
                modalName: "{cn}",
                columns: [{{
                    checkbox: true
                }},
                {{
                    field: 'id',
                    title: '编号'
                }},
{COLUMNS}
                {{
                    title: '操作',
                    align: 'center',
                    formatter: function(value, row, index) {{
                        var actions = [];
                        actions.push('<a class="btn btn-success btn-xs ' + editFlag + '" href="javascript:void(0)" onclick="$.operate.edit(\\'' + row.id + '\\')"><i class="fa fa-edit"></i>编辑</a> ');
                        actions.push('<a class="btn btn-danger btn-xs ' + removeFlag + '" href="javascript:void(0)" onclick="$.operate.remove(\\'' + row.id + '\\')"><i class="fa fa-remove"></i>删除</a>');
                        return actions.join('');
                    }}
                }}]
            }};
            $.table.init(options);
        }});
    </script>
</body>
</html>
'''

FORM_T = '''<!DOCTYPE html>
<html lang="zh" xmlns:th="http://www.thymeleaf.org">
<head>
    <th:block th:include="include :: header('{op}{cn}')" />
</head>
<body class="white-bg">
    <div class="wrapper wrapper-content animated fadeInRight ibox-content">
        <form class="form-horizontal m" id="form-{key}-{op}">
{FORMFIELDS}
        </form>
    </div>
    <th:block th:include="include :: footer" />
    <script type="text/javascript">
        var prefix = ctx + "biz/{key}";

        $("#form-{key}-{op}").validate({{
            focusCleanup: true
        }});

        function submitHandler() {{
            if ($.validate.form()) {{
                $.operate.save(prefix + "{SAVEURL}", $('#form-{key}-{op}').serialize());
            }}
        }}
    </script>
</body>
</html>
'''

def form_field_row(n, label, required, dict_type, val_expr):
    req = ' is-required' if required else ''
    if dict_type:
        field_attr = ' th:field="${{{v}}}"'.format(v=val_expr) if val_expr else ''
        ctrl = '''<select name="{n}" class="form-control m-b" th:with="type=${{@dict.getType('{d}')}}">
                    <option th:each="dict : ${{type}}" th:text="${{dict.dictLabel}}" th:value="${{dict.dictValue}}"{f}></option>
                </select>'''.format(n=n, d=dict_type, f=field_attr)
    else:
        val_attr = '' if val_expr == '' else ' th:value="${%s}"' % val_expr
        ctrl = '<input name="%s" class="form-control" type="text"%s>' % (n, val_attr)
    return '''            <div class="form-group">
                <label class="col-sm-3 control-label%s">%s：</label>
                <div class="col-sm-8">
                    %s
                </div>
            </div>
''' % (req, label, ctrl)

def gen_htmls(key, e):
    # list page
    search, columns, dictvars = [], [], []
    for (n, t, label, sq, q, r, d) in e['fields']:
        if d:
            search.append('                            <li>\n                                %s：<select name="%s" th:with="type=${@dict.getType(\'%s\')}">\n                                    <option value="">所有</option>\n                                    <option th:each="dict : ${type}" th:text="${dict.dictLabel}" th:value="${dict.dictValue}"></option>\n                                </select>\n                            </li>' % (label, n, d))
            dictvars.append("        var %sDict = [[${@dict.getType('%s')}]];" % (n, d))
            columns.append('''                {
                    field: '%s',
                    title: '%s',
                    align: 'center',
                    formatter: function(value, row, index) {
                        return $.table.selectDictLabel(%sDict, value);
                    }
                },''' % (n, label, n))
        elif q:
            search.append('                            <li>\n                                %s：<input type="text" name="%s"/>\n                            </li>' % (label, n))
        if not d:
            columns.append("                {\n                    field: '%s',\n                    title: '%s'\n                }," % (n, label))
    list_html = LIST_T.format(cn=e['cn'], key=key, perm=e['perm'],
        SEARCH='\n'.join(search), COLUMNS='\n'.join(columns),
        DICTVARS='\n'.join(dictvars))
    # add / edit
    add_fields, edit_fields = [], []
    for (n, t, label, sq, q, r, d) in e['fields']:
        add_fields.append(form_field_row(n, label, r, d, ''))
        edit_fields.append(form_field_row(n, label, r, d, '{key}.%s' % n))
    add_html = FORM_T.format(cn=e['cn'], key=key, op='add', SAVEURL='/add',
        FORMFIELDS=''.join(add_fields))
    hidden_id = '            <input type="hidden" name="id" th:value="${' + key + '.id}">
'
    edit_html = FORM_T.format(cn=e['cn'], key=key, op='edit', SAVEURL='/edit',
        FORMFIELDS=hidden_id + ''.join(edit_fields).replace('{key}.', key + '.'))
    return list_html, add_html, edit_html

# ============ run ============
for key, e in E.items():
    w('ruoyi-business/src/main/java/com/ruoyi/business/domain/%s.java' % cap(key), gen_domain(key, e))
    w('ruoyi-business/src/main/java/com/ruoyi/business/mapper/%sMapper.java' % cap(key), MAPPER_T.format(Cls=cap(key), key=key, cn=e['cn']))
    w('ruoyi-business/src/main/resources/mapper/business/%sMapper.xml' % cap(key), gen_xml(key, e))
    w('ruoyi-business/src/main/java/com/ruoyi/business/service/I%sService.java' % cap(key), SVC_T.format(Cls=cap(key), key=key, cn=e['cn']))
    w('ruoyi-business/src/main/java/com/ruoyi/business/service/impl/%sServiceImpl.java' % cap(key), SVCIMPL_T.format(Cls=cap(key), key=key, cn=e['cn'], EXTRA=''))
    w('ruoyi-business/src/main/java/com/ruoyi/business/controller/%sController.java' % cap(key), CTRL_T.format(Cls=cap(key), key=key, cn=e['cn'], perm=e['perm']))
    lst, add, edit = gen_htmls(key, e)
    w('ruoyi-admin/src/main/resources/templates/biz/%s/%s.html' % (key, key), lst)
    w('ruoyi-admin/src/main/resources/templates/biz/%s/add.html' % key, add)
    w('ruoyi-admin/src/main/resources/templates/biz/%s/edit.html' % key, edit)
print('DONE')
