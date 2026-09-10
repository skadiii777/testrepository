package com.enterprise.module.biz.service.contract;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.module.biz.controller.admin.contract.vo.contract.ContractPageReqVO;
import com.enterprise.module.biz.controller.admin.contract.vo.contract.ContractSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.contract.ContractDO;
import com.enterprise.module.biz.dal.dataobject.payment.PaymentDO;
import com.enterprise.module.biz.dal.mysql.contract.ContractMapper;
import com.enterprise.module.biz.dal.mysql.payment.PaymentMapper;
import com.enterprise.module.biz.enums.ErrorCodeConstants;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;


import static com.enterprise.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.enterprise.module.biz.enums.ErrorCodeConstants.*;

/**
 * 合同 Service 实现类
 *
 * @author 企业管理平台
 */
@Service
@Validated
public class ContractServiceImpl implements ContractService {

    @Resource
    private ContractMapper contractMapper;
    @Resource
    private PaymentMapper paymentMapper;

    @Override
    public Long createContract(ContractSaveReqVO createReqVO) {
        ContractDO contract = BeanUtils.toBean(createReqVO, ContractDO.class);
        contractMapper.insert(contract);
        return contract.getId();
    }


    @Override
    public void updateContract(ContractSaveReqVO updateReqVO) {
        validateContractExists(updateReqVO.getId());
        ContractDO updateObj = BeanUtils.toBean(updateReqVO, ContractDO.class);
        contractMapper.updateById(updateObj);
    }


    @Override
    public void deleteContract(Long id) {
        validateContractExists(id);
        contractMapper.deleteById(id);
    }

    private void validateContractExists(Long id) {
        if (contractMapper.selectById(id) == null) {
            throw exception(CONTRACT_NOT_EXISTS);
        }
    }

    @Override
    public ContractDO getContract(Long id) {
        return fillReceivedAmount(contractMapper.selectById(id));
    }

    @Override
    public PageResult<ContractDO> getContractPage(ContractPageReqVO pageReqVO) {
        PageResult<ContractDO> pageResult = contractMapper.selectPage(pageReqVO);
        pageResult.getList().forEach(this::fillReceivedAmount);
        return pageResult;
    }

    /**
     * 回款进度：汇总挂到该合同的收款金额（收付款可选挂合同）
     */
    private ContractDO fillReceivedAmount(ContractDO contract) {
        if (contract == null || contract.getId() == null) {
            return contract;
        }
        contract.setReceivedAmount(paymentMapper.selectListByContract(contract.getId()).stream()
                .map(PaymentDO::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
        return contract;
    }
}