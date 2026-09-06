# -*- coding: utf-8 -*-
"""enterprise-pro-ui 前端页面生成器

为 14 个业务模块生成 api/biz/{m}/index.ts + views/biz/{m}/index.vue，
另生成 dashboard/approval 页面、portal 4 页、DICT_TYPE 注册。
参考官方样例：src/views/mes/wm/sn/index.vue 三段式（搜索/表格+分页/弹窗表单）。
"""
import os, io

UI = r'E:\AI-Code\enterprise-pro-ui'
SRC = os.path.join(UI, 'src')

# kind: input/number/date/textarea/dict
# search: 进入搜索栏；required: 表单必填；dict: DICT_TYPE 键
F = {}
F['customer'] = dict(cn='客户', vname='Customer', fields=[
    ('customerName', 'input', '客户名称', dict(search=True, required=True)),
    ('contactPerson', 'input', '联系人', dict(required=True)),
    ('phone', 'input', '联系电话', dict()),
    ('email', 'input', '邮箱', dict()),
    ('industry', 'input', '所属行业', dict()),
    ('source', 'input', '客户来源', dict()),
    ('address', 'input', '地址', dict()),
    ('status', 'dict', '状态', dict(d='BIZ_COMMON_STATUS', search=True)),
])
F['followup'] = dict(cn='客户跟进', vname='CustomerFollowup', fields=[
    ('customerName', 'input', '客户名称', dict(search=True, required=True)),
    ('followTime', 'datetime', '跟进时间', dict(required=True)),
    ('method', 'dict', '跟进方式', dict(d='BIZ_FOLLOWUP_METHOD', search=True, required=True)),
    ('content', 'textarea', '跟进内容', dict(required=True)),
    ('nextDate', 'date', '下次跟进日期', dict()),
])
F['product'] = dict(cn='产品', vname='Product', fields=[
    ('productCode', 'input', '产品编号', dict(search=True, required=True)),
    ('productName', 'input', '产品名称', dict(search=True, required=True)),
    ('category', 'input', '产品分类', dict()),
    ('unit', 'input', '单位', dict()),
    ('price', 'number', '销售单价', dict(required=True, prec=2)),
    ('cost', 'number', '成本价', dict(prec=2)),
    ('status', 'dict', '状态', dict(d='BIZ_COMMON_STATUS', search=True)),
])
F['contract'] = dict(cn='合同', vname='Contract', fields=[
    ('contractCode', 'input', '合同编号', dict(search=True, required=True)),
    ('customerName', 'input', '客户名称', dict(search=True, required=True)),
    ('productName', 'input', '产品名称', dict()),
    ('amount', 'number', '合同金额', dict(required=True, prec=2)),
    ('signDate', 'date', '签订日期', dict(search=True, required=True)),
    ('startDate', 'date', '开始日期', dict()),
    ('endDate', 'date', '结束日期', dict()),
    ('owner', 'input', '负责人', dict()),
    ('status', 'dict', '合同状态', dict(d='BIZ_CONTRACT_STATUS', search=True)),
])
F['supplier'] = dict(cn='供应商', vname='Supplier', fields=[
    ('supplierName', 'input', '供应商名称', dict(search=True, required=True)),
    ('contactPerson', 'input', '联系人', dict()),
    ('phone', 'input', '联系电话', dict()),
    ('address', 'input', '地址', dict()),
    ('status', 'dict', '状态', dict(d='BIZ_COMMON_STATUS', search=True)),
])
F['purchase'] = dict(cn='采购单', vname='Purchase', fields=[
    ('purchaseCode', 'input', '采购单号', dict(search=True, required=True)),
    ('supplierName', 'input', '供应商', dict()),
    ('productName', 'input', '产品名称', dict(search=True, required=True)),
    ('quantity', 'number', '采购数量', dict(required=True, prec=0)),
    ('price', 'number', '采购单价', dict(required=True, prec=2)),
    ('totalAmount', 'number', '总金额', dict(prec=2)),
    ('purchaseDate', 'date', '采购日期', dict(search=True, required=True)),
    ('status', 'dict', '入库状态', dict(d='BIZ_INOUT_STATUS', search=True)),
])
F['sales'] = dict(cn='销售单', vname='Sales', fields=[
    ('salesCode', 'input', '销售单号', dict(search=True, required=True)),
    ('customerName', 'input', '客户', dict()),
    ('productName', 'input', '产品名称', dict(search=True, required=True)),
    ('quantity', 'number', '销售数量', dict(required=True, prec=0)),
    ('price', 'number', '销售单价', dict(required=True, prec=2)),
    ('totalAmount', 'number', '总金额', dict(prec=2)),
    ('salesDate', 'date', '销售日期', dict(search=True, required=True)),
    ('status', 'dict', '出库状态', dict(d='BIZ_INOUT_STATUS', search=True)),
])
F['stock'] = dict(cn='库存', vname='Stock', fields=[
    ('productName', 'input', '产品名称', dict(search=True, required=True)),
    ('warehouse', 'input', '仓库', dict()),
    ('quantity', 'number', '库存数量', dict(prec=0)),
    ('minQuantity', 'number', '预警下限', dict(prec=0)),
])
F['stockmove'] = dict(cn='库存流水', vname='StockMove', readonly=True, fields=[
    ('moveType', 'dict', '类型', dict(d='BIZ_STOCK_MOVE_TYPE', search=True)),
    ('productName', 'input', '产品名称', dict(search=True)),
    ('warehouse', 'input', '仓库', dict()),
    ('quantity', 'number', '数量', dict(prec=2)),
    ('balanceAfter', 'number', '结余', dict(prec=2)),
    ('sourceType', 'input', '来源类型', dict()),
    ('sourceCode', 'input', '来源单号', dict(search=True)),
])
F['employee'] = dict(cn='员工', vname='Employee', fields=[
    ('empNo', 'input', '工号', dict(search=True, required=True)),
    ('empName', 'input', '姓名', dict(search=True, required=True)),
    ('deptName', 'input', '部门', dict()),
    ('postName', 'input', '岗位', dict()),
    ('phone', 'input', '联系电话', dict()),
    ('email', 'input', '邮箱', dict()),
    ('entryDate', 'date', '入职日期', dict()),
    ('status', 'dict', '状态', dict(d='BIZ_COMMON_STATUS', search=True)),
])
F['attendance'] = dict(cn='考勤', vname='Attendance', fields=[
    ('empName', 'input', '员工姓名', dict(search=True, required=True)),
    ('workDate', 'date', '考勤日期', dict(search=True, required=True)),
    ('checkIn', 'input', '上班时间', dict()),
    ('checkOut', 'input', '下班时间', dict()),
    ('status', 'dict', '考勤状态', dict(d='BIZ_ATTENDANCE_STATUS', search=True)),
])
F['leave'] = dict(cn='请假', vname='Leave', audit=True, fields=[
    ('empName', 'input', '员工姓名', dict(search=True)),
    ('leaveType', 'dict', '请假类型', dict(d='BIZ_LEAVE_TYPE', search=True, required=True)),
    ('startDate', 'date', '开始日期', dict(required=True)),
    ('endDate', 'date', '结束日期', dict(required=True)),
    ('days', 'number', '请假天数', dict(required=True, prec=1)),
    ('reason', 'textarea', '请假事由', dict()),
    ('status', 'dict', '审批状态', dict(d='BIZ_LEAVE_STATUS', search=True)),
])
F['quota'] = dict(cn='假期余额', vname='LeaveQuota', fields=[
    ('empName', 'input', '员工姓名', dict(search=True, required=True)),
    ('leaveType', 'dict', '假期类型', dict(d='BIZ_LEAVE_TYPE', search=True, required=True)),
    ('year', 'input', '年份', dict(search=True, required=True)),
    ('quotaDays', 'number', '配额天数', dict(required=True, prec=1)),
    ('usedDays', 'number', '已用天数', dict(prec=1)),
])
F['report'] = dict(cn='业务汇报', vname='Report', fields=[
    ('reportType', 'dict', '汇报类型', dict(d='BIZ_REPORT_TYPE', search=True, required=True)),
    ('title', 'input', '标题', dict(search=True, required=True)),
    ('content', 'textarea', '汇报内容', dict()),
    ('reportDate', 'date', '汇报日期', dict(search=True, required=True)),
])
F['correction'] = dict(cn='补卡申请', vname='AttendanceCorrection', audit=True, fields=[
    ('empName', 'input', '员工姓名', dict(search=True)),
    ('workDate', 'date', '补卡日期', dict(search=True, required=True)),
    ('correctType', 'dict', '补卡类型', dict(d='BIZ_CORRECTION_TYPE', search=True, required=True)),
    ('correctTime', 'input', '补卡时间', dict(required=True, placeholder='HH:mm')),
    ('reason', 'textarea', '补卡原因', dict(required=True)),
    ('status', 'dict', '审批状态', dict(d='BIZ_CORRECTION_STATUS', search=True)),
])
F['expense'] = dict(cn='费用报销', vname='Expense', audit=True, fields=[
    ('empName', 'input', '报销人', dict(search=True)),
    ('category', 'dict', '费用类别', dict(d='BIZ_EXPENSE_TYPE', search=True, required=True)),
    ('amount', 'number', '金额', dict(required=True, prec=2)),
    ('expenseDate', 'date', '费用日期', dict(search=True, required=True)),
    ('reason', 'textarea', '费用说明', dict()),
    ('invoiceUrl', 'upload', '发票照片', dict()),
    ('status', 'dict', '审批状态', dict(d='BIZ_EXPENSE_STATUS', search=True)),
])


def w(path, content):
    full = os.path.join(SRC, path)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    with io.open(full, 'w', encoding='utf-8', newline='\n') as f:
        f.write(content)
    print('  write', path)


def api_ts(mod, e):
    v = e['vname']
    lines = ['import request from \'@/config/axios\'', '',
             'export interface %sVO {' % v, '  id?: number']
    for (n, kind, label, opt) in e['fields']:
        ts = 'number' if kind in ('number',) else ('string' if kind != 'dict' else 'string')
        lines.append('  %s?: %s' % (n, ts))
    lines.append('  createTime?: Date')
    lines.append('}')
    audit_extra = ''
    body = []
    for fn, verb, url in [
        ('get%sPage' % v, 'get', '/biz/%s/page' % mod),
        ('get%s' % v, 'get', '/biz/%s/get' % mod),
        ('create%s' % v, 'post', '/biz/%s/create' % mod),
        ('update%s' % v, 'put', '/biz/%s/update' % mod),
        ('delete%s' % v, 'delete', '/biz/%s/delete' % mod),
        ('export%s' % v, 'download', '/biz/%s/export-excel' % mod),
    ]:
        if e.get('readonly') and fn.startswith(('create', 'update', 'delete')):
            continue
        if fn.startswith('get%s' % v) and not fn.endswith('Page'):
            body.append("export const %s = async (id: number) => {" % fn)
            body.append("  return await request.get({ url: '%s', params: { id } })" % url)
            body.append("}")
        elif fn.startswith('delete'):
            body.append("export const %s = async (id: number) => {" % fn)
            body.append("  return await request.delete({ url: '%s', params: { id } })" % url)
            body.append("}")
        else:
            arg = 'params: any' if fn.startswith(('get%sPage' % v, 'export%s' % v)) else 'data: %sVO' % v
            method = 'request.download' if verb == 'download' else 'request.%s' % verb
            kw = 'params' if fn.startswith(('get%sPage' % v, 'export%s' % v)) else 'data'
            body.append("export const %s = async (%s) => {" % (fn, arg))
            body.append("  return await %s({ url: '%s', %s })" % (method, url, kw))
            body.append("}")
    if e.get('audit'):
        body.append("// 审批（status: 1=通过 2=驳回）")
        body.append("export const audit%s = async (id: number, status: string, auditRemark?: string) => {" % v)
        body.append("  return await request.post({ url: '/biz/%s/audit', params: { id, status, auditRemark } })" % mod)
        body.append("}")
    content = '\n'.join(lines) + '\n\n' + '\n\n'.join(body) + '\n'
    w('api/biz/%s/index.ts' % mod, content)


def search_field(n, kind, label, opt):
    if kind == 'dict':
        return ('''      <el-form-item label="%s" prop="%s">
        <el-select v-model="queryParams.%s" placeholder="请选择%s" clearable class="!w-240px">
          <el-option
            v-for="d in getDictOptions(DICT_TYPE.%s)"
            :key="d.value"
            :label="d.label"
            :value="d.value"
          />
        </el-select>
      </el-form-item>''' % (label, n, n, label, opt['d']))
    if kind == 'date':
        return ('''      <el-form-item label="%s" prop="%s">
        <el-date-picker v-model="queryParams.%s" value-format="YYYY-MM-DD" type="date"
          placeholder="请选择%s" clearable class="!w-240px" />
      </el-form-item>''' % (label, n, n, label))
    return ('''      <el-form-item label="%s" prop="%s">
        <el-input v-model="queryParams.%s" placeholder="请输入%s" clearable
          @keyup.enter="handleQuery" class="!w-240px" />
      </el-form-item>''' % (label, n, n, label))


def table_col(n, kind, label, opt):
    if kind == 'dict':
        return ('''      <el-table-column label="%s" align="center" prop="%s">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.%s" :value="scope.row.%s" />
        </template>
      </el-table-column>''' % (label, n, opt['d'], n))
    if kind == 'number':
        return '      <el-table-column label="%s" align="right" prop="%s" min-width="100" />' % (label, n)
    return '      <el-table-column label="%s" align="center" prop="%s" min-width="110" />' % (label, n)


def form_field(n, kind, label, opt):
    req = ':rules'  # 统一用 rules 对象校验，模板内简化
    if kind == 'dict':
        return ('''        <el-form-item label="%s" prop="%s">
          <el-select v-model="formData.%s" placeholder="请选择%s" class="!w-1/1">
            <el-option
              v-for="d in getDictOptions(DICT_TYPE.%s)"
              :key="d.value"
              :label="d.label"
              :value="d.value"
            />
          </el-select>
        </el-form-item>''' % (label, n, n, label, opt['d']))
    if kind == 'date':
        return ('''        <el-form-item label="%s" prop="%s">
          <el-date-picker v-model="formData.%s" value-format="YYYY-MM-DD" type="date"
            placeholder="请选择%s" class="!w-1/1" />
        </el-form-item>''' % (label, n, n, label))
    if kind == 'datetime':
        return ('''        <el-form-item label="%s" prop="%s">
          <el-date-picker v-model="formData.%s" value-format="YYYY-MM-DD HH:mm:ss" type="datetime"
            placeholder="请选择%s" class="!w-1/1" />
        </el-form-item>''' % (label, n, n, label))
    if kind == 'upload':
        return ('''        <el-form-item label="%s" prop="%s">
          <UploadImg v-model="formData.%s" :limit="1" />
        </el-form-item>''' % (label, n, n))
    if kind == 'number':
        prec = opt.get('prec', 2)
        return ('''        <el-form-item label="%s" prop="%s">
          <el-input-number v-model="formData.%s" :precision="%d" :min="0" class="!w-1/1" />
        </el-form-item>''' % (label, n, n, prec))
    if kind == 'textarea':
        return ('''        <el-form-item label="%s" prop="%s">
          <el-input v-model="formData.%s" type="textarea" :rows="4" placeholder="请输入%s" />
        </el-form-item>''' % (label, n, n, label))
    return ('''        <el-form-item label="%s" prop="%s">
          <el-input v-model="formData.%s" placeholder="请输入%s" />
        </el-form-item>''' % (label, n, n, label))


def index_vue(mod, e):
    v = e['vname']
    Q1 = chr(39)
    NL = chr(10)
    search_items = NL.join(search_field(n, k, l, o) for (n, k, l, o) in e['fields'] if o.get('search'))
    cols = NL.join(table_col(n, k, l, o) for (n, k, l, o) in e['fields'])
    form_items = (NL.join(form_field(n, k, l, o) for (n, k, l, o) in e['fields']) if not e.get('readonly') else '')
    ops_audit = ''
    if e.get('audit'):
        ops_audit = ('          <el-button link type="primary" @click="handleAudit(scope.row.id, ' + Q1 + '1' + Q1 + ')" v-hasPermi="[' + Q1 + 'biz:' + mod + ':audit' + Q1 + ']">通过</el-button>'
                      + NL + '          <el-button link type="warning" @click="handleAudit(scope.row.id, ' + Q1 + '2' + Q1 + ')" v-hasPermi="[' + Q1 + 'biz:' + mod + ':audit' + Q1 + ']">驳回</el-button>')
    audit_fn = ''
    if e.get('audit'):
        audit_fn = (NL + '/** 审批操作 */'
            + NL + 'const handleAudit = async (id: number, status: string) => {'
            + NL + "  const tip = status === '" + '1' + "' ? '" + '确认通过该' + e['cn'] + '申请吗？' + "' : '" + '确认驳回该' + e['cn'] + '申请吗？' + "'"
            + NL + '  await message.confirm(tip)'
            + NL + '  await Api.audit' + v + '(id, status, status === ' + Q1 + '1' + Q1 + ' ? ' + Q1 + '审批通过' + Q1 + ' : ' + Q1 + '审批驳回' + Q1 + ')'
            + NL + "  message.success('审批成功')"
            + NL + '  await getList()'
            + NL + '}')
    create_call = ''
    if not e.get('readonly'):
        create_call = ('        <el-button type="primary" plain @click="openForm(' + Q1 + 'create' + Q1 + ')" v-hasPermi="[' + Q1 + 'biz:' + mod + ':create' + Q1 + ']">'
                       + NL + '          <Icon icon="ep:plus" class="mr-5px" /> 新增'
                       + NL + '        </el-button>')
    update_btn = ''
    if not e.get('readonly'):
        update_btn = ('          <el-button link type="primary" @click="openForm(' + Q1 + 'update' + Q1 + ', scope.row.id)" v-hasPermi="[' + Q1 + 'biz:' + mod + ':update' + Q1 + ']">修改</el-button>')
    del_btn = ('          <el-button link type="danger" @click="handleDelete(scope.row.id)" v-hasPermi="[' + Q1 + 'biz:' + mod + ':delete' + Q1 + ']">删除</el-button>')
    form_dialog = ''
    if not e.get('readonly'):
        form_dialog = (NL + '  <!-- 表单弹窗 -->'
            + NL + '  <el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px">'
            + NL + '    <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">'
            + NL + form_items
            + NL + '    </el-form>'
            + NL + '    <template #footer>'
            + NL + '      <el-button @click="dialogVisible = false">取 消</el-button>'
            + NL + '      <el-button type="primary" @click="submitForm">确 定</el-button>'
            + NL + '    </template>'
            + NL + '  </el-dialog>')
    form_script = ''
    crud_calls = ''
    if not e.get('readonly'):
        rules = NL.join('  ' + n + ': [{ required: true, message: "' + l + '不能为空", trigger: "blur" }],' for (n, k, l, o) in e['fields'] if o.get('required')) or '  dummy: []'
        form_script = (NL + '/** 表单弹窗逻辑 */'
            + NL + 'const dialogVisible = ref(false)'
            + NL + "const dialogTitle = ref('')"
            + NL + "const formType = ref('')"
            + NL + 'const formLoading = ref(false)'
            + NL + 'const formRef = ref()'
            + NL + 'const formData = ref<Api.' + v + 'VO>({} as Api.' + v + 'VO)'
            + NL + 'const formRules = reactive({'
            + NL + rules
            + NL + '})'
            + NL + 'const openForm = (type: string, id?: number) => {'
            + NL + '  dialogVisible.value = true'
            + NL + '  dialogTitle.value = type === ' + Q1 + 'create' + Q1 + ' ? ' + Q1 + '新增' + e['cn'] + Q1 + ' : ' + Q1 + '修改' + e['cn'] + Q1
            + NL + '  formType.value = type'
            + NL + '  if (id) {'
            + NL + '    formLoading.value = true'
            + NL + '    Api.get' + v + '(id).then((data) => {'
            + NL + '      formData.value = data'
            + NL + '    }).finally(() => { formLoading.value = false })'
            + NL + '  } else {'
            + NL + '    formData.value = {} as Api.' + v + 'VO'
            + NL + '  }'
            + NL + '}')
        crud_calls = (NL + '/** 删除按钮操作 */'
            + NL + 'const handleDelete = async (id: number) => {'
            + NL + '  try {'
            + NL + '    await message.delConfirm()'
            + NL + '    await Api.delete' + v + '(id)'
            + NL + "    message.success('删除成功')"
            + NL + '    await getList()'
            + NL + '  } catch {}'
            + NL + '}'
            + NL + '/** 提交表单 */'
            + NL + "const emit = defineEmits(['success'])"
            + NL + 'const submitForm = async () => {'
            + NL + '  await formRef.value.validate()'
            + NL + '  formLoading.value = true'
            + NL + '  try {'
            + NL + '    if (formType.value === ' + Q1 + 'create' + Q1 + ') {'
            + NL + '      await Api.create' + v + '(formData as unknown as Api.' + v + 'VO)'
            + NL + "      message.success('新增成功')"
            + NL + '    } else {'
            + NL + '      await Api.update' + v + '(formData as unknown as Api.' + v + 'VO)'
            + NL + "      message.success('修改成功')"
            + NL + '    }'
            + NL + '    dialogVisible.value = false'
            + NL + "    emit('success')"
            + NL + '  } finally {'
            + NL + '    formLoading.value = false'
            + NL + '  }'
            + NL + '}')
    export_fn = (NL + '/** 导出按钮操作 */'
        + NL + 'const handleExport = async () => {'
        + NL + '  try {'
        + NL + '    await message.exportConfirm()'
        + NL + '    exportLoading.value = true'
        + NL + '    const data = await Api.export' + v + '(queryParams)'
        + NL + '    download.excel(data, ' + Q1 + e['cn'] + '.xls' + Q1 + ')'
        + NL + '  } catch {'
        + NL + '  } finally {'
        + NL + '    exportLoading.value = false'
        + NL + '  }'
        + NL + '}')
    qfields = ',\n'.join('  ' + n + ': undefined' for (n, k, l, o) in e['fields'] if o.get('search')) + ','
    tpl = open(os.path.join(os.path.dirname(os.path.abspath(__file__)), 'front_index_template.vue.tpl'), encoding='utf-8').read()
    vue = (tpl.replace('__SEARCH__', search_items)
              .replace('__CREATE__', create_call)
              .replace('__PERM__', mod)
              .replace('__COLS__', cols)
              .replace('__UPDATE__', update_btn)
              .replace('__AUDIT__', ops_audit)
              .replace('__DEL__', del_btn)
              .replace('__FORM__', form_dialog)
              .replace('__API__', 'biz/' + mod)
              .replace('__VO__', v)
              .replace('__VNAME__', v)
              .replace('__QFIELDS__', qfields)
              .replace('__AUDITFN__', audit_fn)
              .replace('__FORMSCRIPT__', form_script)
              .replace('__CRUD__', crud_calls)
              .replace('__EXPORT__', export_fn))
    w('views/biz/' + mod + '/index.vue', vue)


# ============ 执行 ============# ============ 执行 ============
for mod, e in F.items():
    print('module:', mod)
    api_ts(mod, e)
    index_vue(mod, e)
print('DONE')
