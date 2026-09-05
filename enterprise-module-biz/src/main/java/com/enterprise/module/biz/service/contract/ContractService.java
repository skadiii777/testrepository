package com.enterprise.module.biz.service.contract;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.module.biz.controller.admin.contract.vo.contract.ContractPageReqVO;
import com.enterprise.module.biz.controller.admin.contract.vo.contract.ContractSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.contract.ContractDO;

/**
 * 合同 Service 接口
 *
 * @author 企业管理平台
 */
public interface ContractService {

    /**
     * 创建合同
     */
    Long createContract(ContractSaveReqVO createReqVO);

    /**
     * 更新合同
     */
    void updateContract(ContractSaveReqVO updateReqVO);

    /**
     * 删除合同
     */
    void deleteContract(Long id);

    /**
     * 获得合同
     */
    ContractDO getContract(Long id);

    /**
     * 获得合同分页
     */
    PageResult<ContractDO> getContractPage(ContractPageReqVO pageReqVO);

}