package com.enterprise.module.system.dal.mysql.registerapply;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.enterprise.framework.mybatis.core.mapper.BaseMapperX;
import com.enterprise.module.system.controller.admin.registerapply.vo.registerapply.RegisterApplyPageReqVO;
import com.enterprise.module.system.dal.dataobject.registerapply.RegisterApplyDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 注册申请 Mapper
 *
 * @author 企业管理平台
 */
@Mapper
public interface RegisterApplyMapper extends BaseMapperX<RegisterApplyDO> {

    default RegisterApplyDO selectByUsernameAndPending(String username) {
        return selectOne(new LambdaQueryWrapperX<RegisterApplyDO>()
                .eq(RegisterApplyDO::getUsername, username)
                .eq(RegisterApplyDO::getStatus, "0")
                .last("LIMIT 1"));
    }

    default PageResult<RegisterApplyDO> selectPage(RegisterApplyPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<RegisterApplyDO>()
                .likeIfPresent(RegisterApplyDO::getUsername, reqVO.getUsername())
                .likeIfPresent(RegisterApplyDO::getNickname, reqVO.getNickname())
                .eqIfPresent(RegisterApplyDO::getStatus, reqVO.getStatus())
                .orderByDesc(RegisterApplyDO::getId));
    }

}
