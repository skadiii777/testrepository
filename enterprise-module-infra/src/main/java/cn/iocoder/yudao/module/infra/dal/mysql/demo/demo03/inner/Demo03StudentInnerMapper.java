package com.enterprise.module.infra.dal.mysql.demo.demo03.inner;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.mybatis.core.mapper.BaseMapperX;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.enterprise.module.infra.controller.admin.demo.demo03.inner.vo.Demo03StudentInnerPageReqVO;
import com.enterprise.module.infra.dal.dataobject.demo.demo03.Demo03StudentDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生 Mapper
 *
 * @author 企业管理平台源码
 */
@Mapper
public interface Demo03StudentInnerMapper extends BaseMapperX<Demo03StudentDO> {

    default PageResult<Demo03StudentDO> selectPage(Demo03StudentInnerPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<Demo03StudentDO>()
                .likeIfPresent(Demo03StudentDO::getName, reqVO.getName())
                .eqIfPresent(Demo03StudentDO::getSex, reqVO.getSex())
                .eqIfPresent(Demo03StudentDO::getDescription, reqVO.getDescription())
                .betweenIfPresent(Demo03StudentDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(Demo03StudentDO::getId));
    }

}