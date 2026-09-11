package com.enterprise.module.biz.service.business;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.module.biz.controller.admin.business.vo.business.BusinessFunnelRespVO;
import com.enterprise.module.biz.controller.admin.business.vo.business.BusinessPageReqVO;
import com.enterprise.module.biz.controller.admin.business.vo.business.BusinessSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.business.BusinessDO;
import com.enterprise.module.biz.dal.dataobject.customer.CustomerDO;
import com.enterprise.module.biz.dal.mysql.business.BusinessMapper;
import com.enterprise.module.biz.dal.mysql.contract.ContractMapper;
import com.enterprise.module.biz.dal.mysql.customer.CustomerMapper;
import com.enterprise.module.system.api.user.AdminUserApi;
import com.enterprise.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.enterprise.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.enterprise.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static com.enterprise.module.biz.enums.ErrorCodeConstants.*;

/**
 * 商机 Service 实现类
 *
 * @author 企业管理平台
 */
@Slf4j
@Service
@Validated
public class BusinessServiceImpl implements BusinessService {

    /** 阶段：1-4 推进中，5=赢单 6=输单（终局） */
    private static final List<String> VALID_STAGES = List.of("1", "2", "3", "4", "5", "6");
    private static final String STAGE_WIN = "5";
    private static final String STAGE_LOSE = "6";
    /** 漏斗固定展示的阶段与名称 */
    private static final Map<String, String> STAGE_NAMES = Map.of(
            "1", "初步接触", "2", "需求确认", "3", "方案报价", "4", "谈判协商", "5", "赢单", "6", "输单");

    @Resource
    private BusinessMapper businessMapper;
    @Resource
    private CustomerMapper customerMapper;
    @Resource
    private ContractMapper contractMapper;
    @Resource
    private AdminUserApi adminUserApi;

    @Override
    public Long createBusiness(BusinessSaveReqVO createReqVO) {
        validateStage(createReqVO.getStage());
        validateCustomerExists(createReqVO.getCustomerName());
        BusinessDO business = BeanUtils.toBean(createReqVO, BusinessDO.class);
        business.setId(null);
        business.setOwnerName(currentNickname());
        businessMapper.insert(business);
        return business.getId();
    }

    @Override
    public void updateBusiness(BusinessSaveReqVO updateReqVO) {
        BusinessDO exists = validateBusinessExists(updateReqVO.getId());
        if (STAGE_WIN.equals(exists.getStage()) || STAGE_LOSE.equals(exists.getStage())) {
            throw exception(BUSINESS_STAGE_TERMINAL);
        }
        validateStage(updateReqVO.getStage());
        BusinessDO updateObj = BeanUtils.toBean(updateReqVO, BusinessDO.class);
        // 负责人服务端管理；客户名不可变更（换客户应删了重开）
        updateObj.setOwnerName(null);
        updateObj.setCustomerName(null);
        businessMapper.updateById(updateObj);
    }

    @Override
    public void deleteBusiness(Long id) {
        validateBusinessExists(id);
        businessMapper.deleteById(id);
    }

    @Override
    public BusinessDO getBusiness(Long id) {
        return businessMapper.selectById(id);
    }

    @Override
    public PageResult<BusinessDO> getBusinessPage(BusinessPageReqVO pageReqVO) {
        return businessMapper.selectPage(pageReqVO);
    }

    @Override
    public List<BusinessFunnelRespVO> getFunnelStats() {
        Map<String, List<BusinessDO>> byStage = businessMapper.selectListAll().stream()
                .collect(Collectors.groupingBy(BusinessDO::getStage));
        List<BusinessFunnelRespVO> result = new ArrayList<>(STAGE_NAMES.size());
        for (String stage : List.of("1", "2", "3", "4", "5", "6")) {
            BusinessFunnelRespVO vo = new BusinessFunnelRespVO();
            vo.setStage(stage);
            vo.setStageName(STAGE_NAMES.get(stage));
            List<BusinessDO> list = byStage.get(stage);
            vo.setCount(list == null ? 0L : (long) list.size());
            vo.setTotalAmount(list == null ? BigDecimal.ZERO : list.stream()
                    .map(BusinessDO::getAmount)
                    .filter(a -> a != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            result.add(vo);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long convertToContract(Long id,
                                  com.enterprise.module.biz.controller.admin.business.vo.business.BusinessContractConvertReqVO convertReqVO) {
        BusinessDO business = validateBusinessExists(id);
        if (!STAGE_WIN.equals(business.getStage())) {
            throw exception(BUSINESS_NOT_WIN);
        }
        // 组装合同：客户/金额/负责人带入商机；编号自动生成避免重号
        java.time.LocalDate today = java.time.LocalDate.now();
        com.enterprise.module.biz.dal.dataobject.contract.ContractDO contract =
                com.enterprise.module.biz.dal.dataobject.contract.ContractDO.builder()
                        .contractCode(com.enterprise.module.biz.service.support.BizDocumentNo.next("HT"))
                        .customerName(business.getCustomerName())
                        .productName(convertReqVO.getProductName())
                        .amount(business.getAmount())
                        .signDate(today.toString())
                        .startDate(convertReqVO.getStartDate() != null ? convertReqVO.getStartDate() : today.toString())
                        .endDate(convertReqVO.getEndDate() != null ? convertReqVO.getEndDate() : today.plusYears(1).toString())
                        .owner(business.getOwnerName())
                        .status("1") // 执行中
                        .remark("由商机「" + business.getName() + "」转化")
                        .build();
        contractMapper.insert(contract);
        return contract.getId();
    }

    private void validateStage(String stage) {
        if (stage == null || !VALID_STAGES.contains(stage)) {
            throw exception(BUSINESS_STAGE_INVALID);
        }
    }

    private void validateCustomerExists(String customerName) {
        CustomerDO customer = customerMapper.selectByName(customerName);
        if (customer == null) {
            throw exception(BUSINESS_CUSTOMER_NOT_EXISTS);
        }
    }

    private BusinessDO validateBusinessExists(Long id) {
        BusinessDO business = businessMapper.selectById(id);
        if (business == null) {
            throw exception(BUSINESS_NOT_EXISTS);
        }
        return business;
    }

    private String currentNickname() {
        AdminUserRespDTO user = adminUserApi.getUser(getLoginUserId());
        return user == null ? "未知" : user.getNickname();
    }

}
