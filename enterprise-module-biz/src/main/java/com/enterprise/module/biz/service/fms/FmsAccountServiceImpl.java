package com.enterprise.module.biz.service.fms;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.module.biz.controller.admin.fms.vo.fms.*;
import com.enterprise.module.biz.dal.dataobject.fms.FmsAccountDO;
import com.enterprise.module.biz.dal.dataobject.fms.FmsVoucherEntryDO;
import com.enterprise.module.biz.dal.mysql.fms.FmsAccountMapper;
import com.enterprise.module.biz.dal.mysql.fms.FmsVoucherEntryMapper;
import com.enterprise.module.biz.enums.ErrorCodeConstants;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static com.enterprise.framework.common.exception.util.ServiceExceptionUtil.exception;

@Service
@Validated
public class FmsAccountServiceImpl implements FmsAccountService {

    @Resource
    private FmsAccountMapper accountMapper;
    @Resource
    private FmsVoucherEntryMapper entryMapper;

    @Override
    public Long createAccount(FmsAccountSaveReqVO reqVO) {
        validateCodeUnique(reqVO.getCode(), null);
        FmsAccountDO account = BeanUtils.toBean(reqVO, FmsAccountDO.class);
        account.setId(null);
        accountMapper.insert(account);
        return account.getId();
    }

    @Override
    public void updateAccount(FmsAccountSaveReqVO reqVO) {
        validateExists(reqVO.getId());
        validateCodeUnique(reqVO.getCode(), reqVO.getId());
        FmsAccountDO account = BeanUtils.toBean(reqVO, FmsAccountDO.class);
        accountMapper.updateById(account);
    }

    @Override
    public void deleteAccount(Long id) {
        validateExists(id);
        if (entryMapper.selectCount(new LambdaQueryWrapper<FmsVoucherEntryDO>()
                .eq(FmsVoucherEntryDO::getAccountId, id)) > 0) {
            throw exception(ErrorCodeConstants.FMS_ACCOUNT_HAS_VOUCHER);
        }
        if (accountMapper.selectCount(new LambdaQueryWrapper<FmsAccountDO>()
                .eq(FmsAccountDO::getParentId, id)) > 0) {
            throw exception(ErrorCodeConstants.FMS_ACCOUNT_HAS_CHILD);
        }
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

    @Override
    public List<FmsAccountRespVO> getSimpleAccountList() {
        // 仅启用状态，凭证分录下拉用
        List<FmsAccountDO> list = accountMapper.selectList(new LambdaQueryWrapper<FmsAccountDO>()
                .eq(FmsAccountDO::getStatus, 0)
                .orderByAsc(FmsAccountDO::getCode));
        return BeanUtils.toBean(list, FmsAccountRespVO.class);
    }

    @Override
    public Long getAccountIdByCode(String code) {
        FmsAccountDO account = accountMapper.selectOne(new LambdaQueryWrapper<FmsAccountDO>()
                .eq(FmsAccountDO::getCode, code)
                .eq(FmsAccountDO::getStatus, 0)
                .last("LIMIT 1"));
        return account != null ? account.getId() : null;
    }

    private void validateExists(Long id) {
        if (accountMapper.selectById(id) == null) {
            throw exception(ErrorCodeConstants.FMS_ACCOUNT_NOT_EXISTS);
        }
    }

    private void validateCodeUnique(String code, Long excludeId) {
        FmsAccountDO exists = accountMapper.selectOne(new LambdaQueryWrapper<FmsAccountDO>()
                .eq(FmsAccountDO::getCode, code));
        if (exists != null && (excludeId == null || !exists.getId().equals(excludeId))) {
            throw exception(ErrorCodeConstants.FMS_ACCOUNT_CODE_DUPLICATE);
        }
    }
}
