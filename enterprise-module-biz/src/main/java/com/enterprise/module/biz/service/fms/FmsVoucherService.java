package com.enterprise.module.biz.service.fms;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.module.biz.controller.admin.fms.vo.fms.*;

import java.math.BigDecimal;
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
}
