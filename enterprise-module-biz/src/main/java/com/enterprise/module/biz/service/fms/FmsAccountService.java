package com.enterprise.module.biz.service.fms;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.module.biz.controller.admin.fms.vo.fms.*;

import java.util.List;

public interface FmsAccountService {
    Long createAccount(FmsAccountSaveReqVO reqVO);
    void updateAccount(FmsAccountSaveReqVO reqVO);
    void deleteAccount(Long id);
    FmsAccountRespVO getAccount(Long id);
    PageResult<FmsAccountRespVO> getAccountPage(FmsAccountPageReqVO pageReqVO);
    List<FmsAccountRespVO> getSimpleAccountList();
    /** 按编码取科目 ID（自动凭证映射用），不存在或停用返回 null */
    Long getAccountIdByCode(String code);
}
