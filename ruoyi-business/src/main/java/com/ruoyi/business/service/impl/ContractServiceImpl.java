package com.ruoyi.business.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.business.domain.Contract;
import com.ruoyi.business.mapper.ContractMapper;
import com.ruoyi.business.service.IContractService;

/**
 * 合同 服务层实现
 * 
 * @author biz
 */
@Service
public class ContractServiceImpl implements IContractService
{
    @Autowired
    private ContractMapper contractMapper;

    @Override
    public Contract selectContractById(Long id)
    {
        return contractMapper.selectContractById(id);
    }

    @Override
    public List<Contract> selectContractList(Contract contract)
    {
        return contractMapper.selectContractList(contract);
    }

    @Override
    public int insertContract(Contract contract)
    {
        return contractMapper.insertContract(contract);
    }

    @Override
    public int updateContract(Contract contract)
    {
        return contractMapper.updateContract(contract);
    }

    @Override
    public int deleteContractByIds(String ids)
    {
        return contractMapper.deleteContractByIds(Convert.toStrArray(ids));
    }

}
