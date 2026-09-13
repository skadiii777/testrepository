package com.enterprise.module.biz.service.fms;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.module.biz.controller.admin.fms.vo.fms.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface FmsVoucherService {
    Long createVoucher(FmsVoucherSaveReqVO reqVO);
    void updateVoucher(FmsVoucherSaveReqVO reqVO);
    void deleteVoucher(Long id);
    void postVoucher(Long id);
    void unpostVoucher(Long id);
    FmsVoucherRespVO getVoucher(Long id);
    PageResult<FmsVoucherRespVO> getVoucherPage(FmsVoucherPageReqVO pageReqVO);
    /** 科目余额汇总（按科目分组：借方合计、贷方合计、余额） */
    List<Map<String, Object>> getAccountBalances();
    /** 当前科目余额总值 */
    BigDecimal getTotalBalance();

    /**
     * 业务单据自动生成已记账凭证（收付款/退货等），同一来源幂等（已有凭证直接返回）。
     * 与调用方同事务：凭证插入失败将回滚业务单据；科目缺失由调用方先行降级。
     */
    Long createAutoPosted(String sourceType, Long sourceId, LocalDate voucherDate,
                          String summary, List<FmsVoucherSaveReqVO.Entry> entries);
}
