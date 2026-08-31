package com.ruoyi.business.service;

import java.util.List;
import com.ruoyi.business.domain.Contract;

/**
 * 合同 服务层
 * 
 * @author biz
 */
public interface IContractService 
{
    public Contract selectContractById(Long id);

    public List<Contract> selectContractList(Contract contract);

    public int insertContract(Contract contract);

    public int updateContract(Contract contract);

    public int deleteContractByIds(String ids);
}
