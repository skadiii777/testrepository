package com.enterprise.module.biz.service.clue;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.module.biz.controller.admin.clue.vo.clue.ClueConvertReqVO;
import com.enterprise.module.biz.controller.admin.clue.vo.clue.CluePageReqVO;
import com.enterprise.module.biz.controller.admin.clue.vo.clue.ClueSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.business.BusinessDO;
import com.enterprise.module.biz.dal.dataobject.clue.ClueDO;
import com.enterprise.module.biz.dal.dataobject.customer.CustomerDO;
import com.enterprise.module.biz.dal.mysql.business.BusinessMapper;
import com.enterprise.module.biz.dal.mysql.clue.ClueMapper;
import com.enterprise.module.biz.dal.mysql.customer.CustomerMapper;
import com.enterprise.module.system.api.user.AdminUserApi;
import com.enterprise.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import static com.enterprise.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.enterprise.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static com.enterprise.module.biz.enums.ErrorCodeConstants.*;

/**
 * 销售线索 Service 实现类
 *
 * @author 企业管理平台
 */
@Slf4j
@Service
@Validated
public class ClueServiceImpl implements ClueService {

    /** 线索状态：待跟进/跟进中/已转化/已无效 */
    private static final String STATUS_PENDING = "0";
    private static final String STATUS_FOLLOWING = "1";
    private static final String STATUS_CONVERTED = "2";
    private static final String STATUS_INVALID = "3";

    @Resource
    private ClueMapper clueMapper;
    @Resource
    private CustomerMapper customerMapper;
    @Resource
    private BusinessMapper businessMapper;
    @Resource
    private com.enterprise.module.biz.dal.mysql.contact.ContactMapper contactMapper;
    @Resource
    private AdminUserApi adminUserApi;

    @Override
    public Long createClue(ClueSaveReqVO createReqVO) {
        ClueDO clue = BeanUtils.toBean(createReqVO, ClueDO.class);
        clue.setId(null);
        clue.setStatus(STATUS_PENDING);
        clue.setOwnerName(currentNickname());
        clue.setCustomerId(null);
        clueMapper.insert(clue);
        return clue.getId();
    }

    @Override
    public void updateClue(ClueSaveReqVO updateReqVO) {
        ClueDO exists = validateClueExists(updateReqVO.getId());
        if (STATUS_CONVERTED.equals(exists.getStatus()) || STATUS_INVALID.equals(exists.getStatus())) {
            throw exception(CLUE_STATUS_INVALID);
        }
        ClueDO updateObj = BeanUtils.toBean(updateReqVO, ClueDO.class);
        // 负责人与转化状态服务端管理；状态仅允许 0/1/3 之间变更
        updateObj.setOwnerName(null);
        updateObj.setCustomerId(null);
        if (updateObj.getStatus() != null
                && !STATUS_PENDING.equals(updateObj.getStatus())
                && !STATUS_FOLLOWING.equals(updateObj.getStatus())
                && !STATUS_INVALID.equals(updateObj.getStatus())) {
            throw exception(CLUE_STATUS_INVALID);
        }
        clueMapper.updateById(updateObj);
    }

    @Override
    public void deleteClue(Long id) {
        validateClueExists(id);
        clueMapper.deleteById(id);
    }

    @Override
    public ClueDO getClue(Long id) {
        return clueMapper.selectById(id);
    }

    @Override
    public PageResult<ClueDO> getCluePage(CluePageReqVO pageReqVO) {
        return clueMapper.selectPage(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long convertClue(Long id, ClueConvertReqVO convertReqVO) {
        ClueDO clue = validateClueExists(id);
        if (!STATUS_PENDING.equals(clue.getStatus()) && !STATUS_FOLLOWING.equals(clue.getStatus())) {
            throw exception(CLUE_ALREADY_CONVERTED);
        }
        // 1. 客户：按线索名称查找，不存在则新建（带出联系人/电话/来源）
        CustomerDO customer = customerMapper.selectByName(clue.getName());
        if (customer == null) {
            customer = new CustomerDO();
            customer.setCustomerName(clue.getName());
            customer.setContactPerson(clue.getContactName());
            customer.setPhone(clue.getContactMobile());
            customer.setSource(clue.getSource());
            customer.setStatus("0");
            customerMapper.insert(customer);
        }
        // 2. 联系人：线索联系人落入客户联系人（决策人档案起点）
        com.enterprise.module.biz.dal.dataobject.contact.ContactDO contact =
                com.enterprise.module.biz.dal.dataobject.contact.ContactDO.builder()
                        .customerId(customer.getId())
                        .customerName(customer.getCustomerName())
                        .name(clue.getContactName())
                        .mobile(clue.getContactMobile())
                        .remark("由线索「" + clue.getName() + "」转化")
                        .build();
        contactMapper.insert(contact);
        // 3. 商机：初始阶段默认 1（初步接触）
        BusinessDO business = new BusinessDO();
        business.setName(convertReqVO.getBusinessName());
        business.setCustomerName(clue.getName());
        business.setStage(convertReqVO.getStage() == null || convertReqVO.getStage().isEmpty()
                ? "1" : convertReqVO.getStage());
        business.setAmount(convertReqVO.getAmount());
        business.setExpectedDate(convertReqVO.getExpectedDate());
        business.setOwnerName(clue.getOwnerName());
        business.setRemark("由线索「" + clue.getName() + "」转化");
        businessMapper.insert(business);
        // 4. 线索置为已转化并记录客户
        ClueDO update = new ClueDO();
        update.setId(id);
        update.setStatus(STATUS_CONVERTED);
        update.setCustomerId(customer.getId());
        clueMapper.updateById(update);
        return business.getId();
    }

    private ClueDO validateClueExists(Long id) {
        ClueDO clue = clueMapper.selectById(id);
        if (clue == null) {
            throw exception(CLUE_NOT_EXISTS);
        }
        return clue;
    }

    private String currentNickname() {
        AdminUserRespDTO user = adminUserApi.getUser(getLoginUserId());
        return user == null ? "未知" : user.getNickname();
    }

}
