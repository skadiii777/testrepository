package com.enterprise.module.biz.service.followup;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.module.biz.controller.admin.followup.vo.followup.CustomerFollowupPageReqVO;
import com.enterprise.module.biz.controller.admin.followup.vo.followup.CustomerFollowupSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.followup.CustomerFollowupDO;
import com.enterprise.module.biz.dal.mysql.followup.CustomerFollowupMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static com.enterprise.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.enterprise.module.biz.enums.ErrorCodeConstants.FOLLOWUP_NOT_EXISTS;

/**
 * 客户跟进 Service 实现类
 *
 * @author 企业管理平台
 */
@Service
@Validated
public class CustomerFollowupServiceImpl implements CustomerFollowupService {

    @Resource
    private CustomerFollowupMapper followupMapper;

    @Override
    public Long createFollowup(CustomerFollowupSaveReqVO createReqVO) {
        CustomerFollowupDO followup = BeanUtils.toBean(createReqVO, CustomerFollowupDO.class);
        followupMapper.insert(followup);
        return followup.getId();
    }

    @Override
    public void deleteFollowup(Long id) {
        if (followupMapper.selectById(id) == null) {
            throw exception(FOLLOWUP_NOT_EXISTS);
        }
        followupMapper.deleteById(id);
    }

    @Override
    public CustomerFollowupDO getFollowup(Long id) {
        return followupMapper.selectById(id);
    }

    @Override
    public PageResult<CustomerFollowupDO> getFollowupPage(CustomerFollowupPageReqVO pageReqVO) {
        return followupMapper.selectPage(pageReqVO);
    }

    @Override
    public List<CustomerFollowupDO> getFollowupListByCustomer(String customerName) {
        return followupMapper.selectListByCustomer(customerName);
    }
}
