package com.enterprise.module.biz.service.fms;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.module.biz.controller.admin.fms.vo.fms.*;
import com.enterprise.module.biz.dal.dataobject.fms.FmsAccountDO;
import com.enterprise.module.biz.dal.mysql.fms.FmsAccountMapper;
import com.enterprise.module.biz.enums.ErrorCodeConstants;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static com.enterprise.framework.common.exception.util.ServiceExceptionUtil.exception;

@Service
@Validated
public class FmsAccountServiceImpl implements FmsAccountService {

    @Resource
    private FmsAccountMapper accountMapper;

    @Override
    public Long createAccount(FmsAccountSaveReqVO reqVO) {
        FmsAccountDO account = BeanUtils.toBean(reqVO, FmsAccountDO.class);
        account.setId(null);
        accountMapper.insert(account);
        return account.getId();
    }

    @Override
    public void updateAccount(FmsAccountSaveReqVO reqVO) {
        validateExists(reqVO.getId());
        FmsAccountDO account = BeanUtils.toBean(reqVO, FmsAccountDO.class);
        accountMapper.updateById(account);
    }

    @Override
    public void deleteAccount(Long id) {
        validateExists(id);
        accountMapper.deleteById(id);
    }

    @Override
    public FmsAccountRespVO getAccount(Long id) {
        return BeanUtils.toBean(accountMapper.selectById(id), FmsAccountRespVO.class);
    }

    @Override
    public PageResult<FmsAccountRespVO> getAccountPage(FmsAccountPageReqVO pageReqVO) {
        PageResult<FmsAccountDO> page = accountMapper.selectPage(pageReqVO);
        return BeanUtils.toBean(page, FmsAccountRespVO.class);
    }

    private void validateExists(Long id) {
        if (accountMapper.selectById(id) == null) {
            throw exception(ErrorCodeConstants.FMS_ACCOUNT_NOT_EXISTS);
        }
    }
}
