package com.enterprise.module.biz.service.fms;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.enterprise.module.biz.controller.admin.fms.vo.fms.*;
import com.enterprise.module.biz.dal.dataobject.fms.FmsAccountDO;
import com.enterprise.module.biz.dal.dataobject.fms.FmsVoucherDO;
import com.enterprise.module.biz.dal.dataobject.fms.FmsVoucherEntryDO;
import com.enterprise.module.biz.dal.mysql.fms.FmsAccountMapper;
import com.enterprise.module.biz.dal.mysql.fms.FmsVoucherEntryMapper;
import com.enterprise.module.biz.dal.mysql.fms.FmsVoucherMapper;
import com.enterprise.module.biz.enums.ErrorCodeConstants;
import com.enterprise.module.biz.service.support.BizDocumentNo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.enterprise.framework.common.exception.util.ServiceExceptionUtil.exception;

@Slf4j
@Service
@Validated
public class FmsVoucherServiceImpl implements FmsVoucherService {

    @Resource
    private FmsVoucherMapper voucherMapper;
    @Resource
    private FmsVoucherEntryMapper entryMapper;
    @Resource
    private FmsAccountMapper accountMapper;

    private static final int STATUS_DRAFT = 0;
    private static final int STATUS_POSTED = 1;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createVoucher(FmsVoucherSaveReqVO reqVO) {
        validateBalanced(reqVO.getEntries());
        FmsVoucherDO voucher = buildVoucher(reqVO);
        voucher.setStatus(STATUS_DRAFT);
        voucherMapper.insert(voucher);
        saveEntries(voucher.getId(), reqVO.getEntries());
        return voucher.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateVoucher(FmsVoucherSaveReqVO reqVO) {
        FmsVoucherDO exists = validateVoucherExists(reqVO.getId());
        if (exists.getStatus() == STATUS_POSTED) {
            throw exception(ErrorCodeConstants.FMS_VOUCHER_ALREADY_POSTED);
        }
        validateBalanced(reqVO.getEntries());
        FmsVoucherDO update = buildVoucher(reqVO);
        update.setId(reqVO.getId());
        // 凭证号创建即固定，编辑不重新生成（null 字段 updateById 不更新）
        update.setVoucherNo(null);
        voucherMapper.updateById(update);
        entryMapper.deleteByVoucherId(reqVO.getId());
        saveEntries(reqVO.getId(), reqVO.getEntries());
    }

    @Override
    public void deleteVoucher(Long id) {
        FmsVoucherDO voucher = validateVoucherExists(id);
        if (voucher.getStatus() == STATUS_POSTED) {
            throw exception(ErrorCodeConstants.FMS_VOUCHER_POSTED_NO_DELETE);
        }
        entryMapper.deleteByVoucherId(id);
        voucherMapper.deleteById(id);
    }

    @Override
    public void postVoucher(Long id) {
        FmsVoucherDO voucher = validateVoucherExists(id);
        if (voucher.getStatus() == STATUS_POSTED) {
            throw exception(ErrorCodeConstants.FMS_VOUCHER_ALREADY_POSTED);
        }
        FmsVoucherDO update = new FmsVoucherDO();
        update.setId(id);
        update.setStatus(STATUS_POSTED);
        voucherMapper.updateById(update);
    }

    @Override
    public void unpostVoucher(Long id) {
        FmsVoucherDO voucher = validateVoucherExists(id);
        if (voucher.getStatus() != STATUS_POSTED) {
            throw exception(ErrorCodeConstants.FMS_VOUCHER_NOT_POSTED);
        }
        FmsVoucherDO update = new FmsVoucherDO();
        update.setId(id);
        update.setStatus(STATUS_DRAFT);
        voucherMapper.updateById(update);
    }

    @Override
    public FmsVoucherRespVO getVoucher(Long id) {
        FmsVoucherDO voucher = validateVoucherExists(id);
        FmsVoucherRespVO vo = BeanUtils.toBean(voucher, FmsVoucherRespVO.class);
        vo.setEntries(BeanUtils.toBean(
                entryMapper.selectListByVoucherId(id), FmsVoucherRespVO.Entry.class));
        return vo;
    }

    @Override
    public PageResult<FmsVoucherRespVO> getVoucherPage(FmsVoucherPageReqVO pageReqVO) {
        PageResult<FmsVoucherDO> page = voucherMapper.selectPage(pageReqVO);
        return BeanUtils.toBean(page, FmsVoucherRespVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAutoPosted(String sourceType, Long sourceId, LocalDate voucherDate,
                                 String summary, List<FmsVoucherSaveReqVO.Entry> entries) {
        // 幂等：同一来源单据已有凭证直接返回
        FmsVoucherDO existing = voucherMapper.selectBySource(sourceType, sourceId);
        if (existing != null) {
            return existing.getId();
        }
        validateBalanced(entries);
        FmsVoucherDO voucher = buildVoucherForDate(voucherDate, summary,
                sumSide(entries, true), sumSide(entries, false));
        voucher.setStatus(STATUS_POSTED);
        voucher.setSourceType(sourceType);
        voucher.setSourceId(sourceId);
        try {
            voucherMapper.insert(voucher);
        } catch (DuplicateKeyException e) {
            // 并发下两个请求同时通过上面的先查后插：由 uk_source 唯一键兜底，
            // 后者改为返回已存在的凭证，避免同一来源重复入账或向前端抛 500
            FmsVoucherDO concurrent = voucherMapper.selectBySource(sourceType, sourceId);
            if (concurrent != null) {
                log.info("[createAutoPosted][来源({}/{}) 并发重复生成凭证，返回已存在凭证 {}]",
                        sourceType, sourceId, concurrent.getId());
                return concurrent.getId();
            }
            throw e;
        }
        saveEntries(voucher.getId(), entries);
        return voucher.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSimplePosted(String sourceType, Long sourceId, LocalDate voucherDate, String summary,
                                   String debitAccountCode, String creditAccountCode, BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            return null;
        }
        Long debitId = accountIdByCode(debitAccountCode);
        Long creditId = accountIdByCode(creditAccountCode);
        if (debitId == null || creditId == null) {
            log.warn("[createSimplePosted] 标准科目({}/{})缺失或停用，{} 来源 {} 未自动生成凭证，请手工补录",
                    debitAccountCode, creditAccountCode, sourceType, sourceId);
            return null;
        }
        FmsVoucherSaveReqVO.Entry debit = new FmsVoucherSaveReqVO.Entry();
        debit.setAccountId(debitId);
        debit.setSummary(summary);
        debit.setDebitAmount(amount);
        FmsVoucherSaveReqVO.Entry credit = new FmsVoucherSaveReqVO.Entry();
        credit.setAccountId(creditId);
        credit.setSummary(summary);
        credit.setCreditAmount(amount);
        return createAutoPosted(sourceType, sourceId, voucherDate, summary, List.of(debit, credit));
    }

    private Long accountIdByCode(String code) {
        FmsAccountDO account = accountMapper.selectOne(
                new LambdaQueryWrapperX<FmsAccountDO>()
                        .eq(FmsAccountDO::getCode, code)
                        .eq(FmsAccountDO::getStatus, 0)
                        .last("LIMIT 1"));
        return account != null ? account.getId() : null;
    }

    private BigDecimal sumSide(List<FmsVoucherSaveReqVO.Entry> entries, boolean debit) {
        return entries.stream()
                .map(e -> debit ? e.getDebitAmount() : e.getCreditAmount())
                .map(a -> a != null ? a : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public List<Map<String, Object>> getAccountBalances() {
        // 科目余额只统计已记账凭证的分录（草稿不进账）
        List<Long> postedIds = voucherMapper.selectList(
                        new LambdaQueryWrapperX<FmsVoucherDO>().eq(FmsVoucherDO::getStatus, STATUS_POSTED))
                .stream().map(FmsVoucherDO::getId).collect(Collectors.toList());
        if (postedIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<FmsVoucherEntryDO> allPosted = entryMapper.selectList(
                new LambdaQueryWrapperX<FmsVoucherEntryDO>().in(FmsVoucherEntryDO::getVoucherId, postedIds));
        // 按 account_id 分组汇总
        Map<Long, BigDecimal> debitMap = new LinkedHashMap<>();
        Map<Long, BigDecimal> creditMap = new LinkedHashMap<>();
        Map<Long, FmsVoucherEntryDO> sampleMap = new LinkedHashMap<>();
        for (FmsVoucherEntryDO e : allPosted) {
            debitMap.merge(e.getAccountId(), e.getDebitAmount(), BigDecimal::add);
            creditMap.merge(e.getAccountId(), e.getCreditAmount(), BigDecimal::add);
            sampleMap.putIfAbsent(e.getAccountId(), e);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (var entry : debitMap.entrySet()) {
            Long accId = entry.getKey();
            FmsVoucherEntryDO sample = sampleMap.get(accId);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("accountId", accId);
            row.put("accountCode", sample.getAccountCode());
            row.put("accountName", sample.getAccountName());
            row.put("debitTotal", entry.getValue());
            row.put("creditTotal", creditMap.getOrDefault(accId, BigDecimal.ZERO));
            row.put("balance", entry.getValue().subtract(creditMap.getOrDefault(accId, BigDecimal.ZERO)));
            result.add(row);
        }
        result.sort(Comparator.comparing(r -> String.valueOf(r.get("accountCode"))));
        return result;
    }

    @Override
    public BigDecimal getTotalBalance() {
        // 期初为 0 的账套，总资产余额 = 已记账分录借方合计 - 贷方合计
        List<Map<String, Object>> balances = getAccountBalances();
        return balances.stream()
                .map(r -> (BigDecimal) r.get("balance"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public Map<String, Object> getFinancialReport(LocalDate beginDate, LocalDate endDate) {
        // 已记账凭证（按日期过滤）的分录
        List<FmsVoucherDO> vouchers = voucherMapper.selectList(
                new LambdaQueryWrapperX<FmsVoucherDO>().eq(FmsVoucherDO::getStatus, STATUS_POSTED));
        List<Long> voucherIds = vouchers.stream()
                .filter(v -> (beginDate == null || !v.getVoucherDate().isBefore(beginDate))
                        && (endDate == null || !v.getVoucherDate().isAfter(endDate)))
                .map(FmsVoucherDO::getId).collect(Collectors.toList());
        // 科目类型映射
        Map<Long, FmsAccountDO> accountMap = accountMapper.selectList(
                        new LambdaQueryWrapperX<FmsAccountDO>()).stream()
                .collect(Collectors.toMap(FmsAccountDO::getId, a -> a, (a, b) -> a));
        // 按科目聚合（借方/贷方发生额）
        Map<Long, BigDecimal> debit = new LinkedHashMap<>();
        Map<Long, BigDecimal> credit = new LinkedHashMap<>();
        if (!voucherIds.isEmpty()) {
            for (FmsVoucherEntryDO e : entryMapper.selectList(
                    new LambdaQueryWrapperX<FmsVoucherEntryDO>().in(FmsVoucherEntryDO::getVoucherId, voucherIds))) {
                debit.merge(e.getAccountId(), e.getDebitAmount(), BigDecimal::add);
                credit.merge(e.getAccountId(), e.getCreditAmount(), BigDecimal::add);
            }
        }
        // 五大类的科目行：balance 按科目余额方向取自然余额
        Map<Integer, List<Map<String, Object>>> byType = new LinkedHashMap<>();
        for (FmsAccountDO a : accountMap.values()) {
            BigDecimal d = debit.getOrDefault(a.getId(), BigDecimal.ZERO);
            BigDecimal c = credit.getOrDefault(a.getId(), BigDecimal.ZERO);
            if (d.signum() == 0 && c.signum() == 0) continue;
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("accountId", a.getId());
            row.put("code", a.getCode());
            row.put("name", a.getName());
            row.put("debit", d);
            row.put("credit", c);
            row.put("balance", a.getDirection() != null && a.getDirection() == 2 ? c.subtract(d) : d.subtract(c));
            byType.computeIfAbsent(a.getType(), k -> new ArrayList<>()).add(row);
        }
        byType.values().forEach(rows -> rows.sort(Comparator.comparing(r -> String.valueOf(r.get("code")))));
        BigDecimal revenue = sumType(byType.get(5), r -> (BigDecimal) r.get("credit"));
        BigDecimal expense = sumType(byType.get(5), r -> (BigDecimal) r.get("debit"));
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("assets", byType.getOrDefault(1, List.of()));
        report.put("liabilities", byType.getOrDefault(2, List.of()));
        report.put("equity", byType.getOrDefault(3, List.of()));
        report.put("profitItems", byType.getOrDefault(5, List.of()));
        report.put("totalRevenue", revenue);
        report.put("totalExpense", expense);
        report.put("netProfit", revenue.subtract(expense));
        // 资产负债表平衡校验：资产 = 负债 + 权益 + 净利润
        report.put("totalAssets", sumType(byType.get(1), r -> (BigDecimal) r.get("balance")));
        report.put("totalLiabilities", sumType(byType.get(2), r -> (BigDecimal) r.get("balance")));
        report.put("totalEquity", sumType(byType.get(3), r -> (BigDecimal) r.get("balance")));
        return report;
    }

    private BigDecimal sumType(List<Map<String, Object>> rows, java.util.function.Function<Map<String, Object>, BigDecimal> f) {
        return rows == null ? BigDecimal.ZERO
                : rows.stream().map(f).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private FmsVoucherDO buildVoucher(FmsVoucherSaveReqVO reqVO) {
        return buildVoucherForDate(reqVO.getVoucherDate(), reqVO.getSummary(),
                sumSide(reqVO.getEntries(), true), sumSide(reqVO.getEntries(), false));
    }

    private FmsVoucherDO buildVoucherForDate(LocalDate voucherDate, String summary,
                                             BigDecimal debitTotal, BigDecimal creditTotal) {
        FmsVoucherDO voucher = new FmsVoucherDO();
        // 凭证号：前缀 + yyMMddHHmmss + 2 位随机字母数字（与销售/采购单号同一生成器）。
        // 旧实现用 System.currentTimeMillis() % 10000，取值空间仅 1 万且每 10 秒循环一次，
        // 同日撞号后由 uk_no 唯一键拦下并抛错；改用随机单号后撞号概率可忽略，唯一键仍作最终兜底。
        voucher.setVoucherNo(BizDocumentNo.nextShort("JZ"));
        voucher.setVoucherDate(voucherDate);
        voucher.setSummary(summary);
        voucher.setDebitTotal(debitTotal);
        voucher.setCreditTotal(creditTotal);
        return voucher;
    }

    private void saveEntries(Long voucherId, List<FmsVoucherSaveReqVO.Entry> entries) {
        int sort = 0;
        for (var e : entries) {
            FmsAccountDO account = accountMapper.selectById(e.getAccountId());
            FmsVoucherEntryDO entry = FmsVoucherEntryDO.builder()
                    .voucherId(voucherId)
                    .accountId(e.getAccountId())
                    .accountCode(account != null ? account.getCode() : "")
                    .accountName(account != null ? account.getName() : "")
                    .summary(e.getSummary() != null ? e.getSummary() : "")
                    .debitAmount(e.getDebitAmount() != null ? e.getDebitAmount() : BigDecimal.ZERO)
                    .creditAmount(e.getCreditAmount() != null ? e.getCreditAmount() : BigDecimal.ZERO)
                    .sort(sort++)
                    .build();
            entryMapper.insert(entry);
        }
    }

    private void validateBalanced(List<FmsVoucherSaveReqVO.Entry> entries) {
        BigDecimal debit = BigDecimal.ZERO;
        BigDecimal credit = BigDecimal.ZERO;
        for (var e : entries) {
            debit = debit.add(e.getDebitAmount() != null ? e.getDebitAmount() : BigDecimal.ZERO);
            credit = credit.add(e.getCreditAmount() != null ? e.getCreditAmount() : BigDecimal.ZERO);
        }
        if (debit.compareTo(credit) != 0) {
            throw exception(ErrorCodeConstants.FMS_VOUCHER_NOT_BALANCED);
        }
    }

    private FmsVoucherDO validateVoucherExists(Long id) {
        FmsVoucherDO voucher = voucherMapper.selectById(id);
        if (voucher == null) {
            throw exception(ErrorCodeConstants.FMS_VOUCHER_NOT_EXISTS);
        }
        return voucher;
    }
}
