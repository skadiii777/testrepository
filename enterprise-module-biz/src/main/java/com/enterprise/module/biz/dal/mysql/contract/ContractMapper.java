package com.enterprise.module.biz.dal.mysql.contract;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.mybatis.core.mapper.BaseMapperX;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.enterprise.module.biz.controller.admin.contract.vo.contract.ContractPageReqVO;
import com.enterprise.module.biz.dal.dataobject.contract.ContractDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ContractMapper extends BaseMapperX<ContractDO> {

    /**
     * 分页查询
     */
    default PageResult<ContractDO> selectPage(ContractPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ContractDO>()
                .likeIfPresent(ContractDO::getContractCode, reqVO.getContractCode())
                .likeIfPresent(ContractDO::getCustomerName, reqVO.getCustomerName())
                .eqIfPresent(ContractDO::getSignDate, reqVO.getSignDate())
                .eqIfPresent(ContractDO::getOwner, reqVO.getOwner())
                .eqIfPresent(ContractDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ContractDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ContractDO::getId));
    }

    /**
     * 各合同状态数量统计
     */
    default java.util.List<java.util.Map<String, Object>> selectStatusCount() {
        return selectMaps(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ContractDO>()
                .select("status", "count(*) AS cnt").groupBy("status"));
    }
}