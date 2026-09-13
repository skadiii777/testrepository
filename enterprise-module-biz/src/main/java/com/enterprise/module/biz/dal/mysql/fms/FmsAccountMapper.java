package com.enterprise.module.biz.dal.mysql.fms;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.mybatis.core.mapper.BaseMapperX;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.enterprise.module.biz.controller.admin.fms.vo.fms.FmsAccountPageReqVO;
import com.enterprise.module.biz.dal.dataobject.fms.FmsAccountDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FmsAccountMapper extends BaseMapperX<FmsAccountDO> {

    default PageResult<FmsAccountDO> selectPage(FmsAccountPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<FmsAccountDO>()
                .likeIfPresent(FmsAccountDO::getCode, reqVO.getCode())
                .likeIfPresent(FmsAccountDO::getName, reqVO.getName())
                .eqIfPresent(FmsAccountDO::getType, reqVO.getType())
                .eqIfPresent(FmsAccountDO::getStatus, reqVO.getStatus())
                .orderByAsc(FmsAccountDO::getCode));
    }

    default FmsAccountDO selectByCode(String code) {
        return selectOne(FmsAccountDO::getCode, code);
    }

}
