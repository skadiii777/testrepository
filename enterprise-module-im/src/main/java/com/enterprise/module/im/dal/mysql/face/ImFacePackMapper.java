package com.enterprise.module.im.dal.mysql.face;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.mybatis.core.mapper.BaseMapperX;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.enterprise.module.im.controller.admin.manager.face.vo.pack.ImFacePackPageReqVO;
import com.enterprise.module.im.dal.dataobject.face.ImFacePackDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IM 表情包 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface ImFacePackMapper extends BaseMapperX<ImFacePackDO> {

    default List<ImFacePackDO> selectListByStatusOrderBySort(Integer status) {
        return selectList(new LambdaQueryWrapperX<ImFacePackDO>()
                .eq(ImFacePackDO::getStatus, status)
                .orderByAsc(ImFacePackDO::getSort)
                .orderByAsc(ImFacePackDO::getId));
    }

    default PageResult<ImFacePackDO> selectPage(ImFacePackPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ImFacePackDO>()
                .likeIfPresent(ImFacePackDO::getName, reqVO.getName())
                .eqIfPresent(ImFacePackDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ImFacePackDO::getCreateTime, reqVO.getCreateTime())
                .orderByAsc(ImFacePackDO::getSort)
                .orderByDesc(ImFacePackDO::getId));
    }

}
