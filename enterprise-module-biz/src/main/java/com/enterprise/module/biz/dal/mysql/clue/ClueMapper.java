package com.enterprise.module.biz.dal.mysql.clue;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.mybatis.core.mapper.BaseMapperX;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.enterprise.module.biz.controller.admin.clue.vo.clue.CluePageReqVO;
import com.enterprise.module.biz.dal.dataobject.clue.ClueDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 销售线索 Mapper
 *
 * @author 企业管理平台
 */
@Mapper
public interface ClueMapper extends BaseMapperX<ClueDO> {

    default PageResult<ClueDO> selectPage(CluePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ClueDO>()
                .likeIfPresent(ClueDO::getName, reqVO.getName())
                .likeIfPresent(ClueDO::getContactName, reqVO.getContactName())
                .eqIfPresent(ClueDO::getSource, reqVO.getSource())
                .eqIfPresent(ClueDO::getStatus, reqVO.getStatus())
                .orderByDesc(ClueDO::getId));
    }

}
