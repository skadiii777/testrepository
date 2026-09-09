package com.enterprise.module.biz.dal.mysql.business;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.mybatis.core.mapper.BaseMapperX;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.enterprise.module.biz.controller.admin.business.vo.business.BusinessPageReqVO;
import com.enterprise.module.biz.dal.dataobject.business.BusinessDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商机 Mapper
 *
 * @author 企业管理平台
 */
@Mapper
public interface BusinessMapper extends BaseMapperX<BusinessDO> {

    default PageResult<BusinessDO> selectPage(BusinessPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<BusinessDO>()
                .likeIfPresent(BusinessDO::getName, reqVO.getName())
                .likeIfPresent(BusinessDO::getCustomerName, reqVO.getCustomerName())
                .eqIfPresent(BusinessDO::getStage, reqVO.getStage())
                .orderByDesc(BusinessDO::getId));
    }

    /**
     * 统计各阶段商机数量与预期金额（漏斗）
     */
    default java.util.List<BusinessDO> selectListAll() {
        return selectList(new LambdaQueryWrapperX<BusinessDO>().orderByDesc(BusinessDO::getId));
    }

}
