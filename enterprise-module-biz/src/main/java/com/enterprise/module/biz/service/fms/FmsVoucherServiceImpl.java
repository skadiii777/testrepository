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
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.enterprise.framework.common.exception.util.ServiceExceptionUtil.exception;

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

    private FmsVoucherDO buildVoucher(FmsVoucherSaveReqVO reqVO) {
        BigDecimal debitTotal = reqVO.getEntries().stream()
                .map(e -> e.getDebitAmount() != null ? e.getDebitAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal creditTotal = reqVO.getEntries().stream()
                .map(e -> e.getCreditAmount() != null ? e.getCreditAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        FmsVoucherDO voucher = new FmsVoucherDO();
        voucher.setVoucherNo("JZ" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                + System.currentTimeMillis() % 10000);
        voucher.setVoucherDate(reqVO.getVoucherDate());
        voucher.setSummary(reqVO.getSummary());
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
