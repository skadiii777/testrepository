package com.enterprise.module.biz.dal.mysql.announcement;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.mybatis.core.mapper.BaseMapperX;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.enterprise.module.biz.controller.admin.announcement.vo.announcement.AnnouncementPageReqVO;
import com.enterprise.module.biz.dal.dataobject.announcement.AnnouncementDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 公司公告 Mapper
 *
 * @author 企业管理平台
 */
@Mapper
public interface AnnouncementMapper extends BaseMapperX<AnnouncementDO> {

    default PageResult<AnnouncementDO> selectPage(AnnouncementPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AnnouncementDO>()
                .likeIfPresent(AnnouncementDO::getTitle, reqVO.getTitle())
                .eqIfPresent(AnnouncementDO::getType, reqVO.getType())
                .eqIfPresent(AnnouncementDO::getStatus, reqVO.getStatus())
                .orderByDesc(AnnouncementDO::getPinned)
                .orderByDesc(AnnouncementDO::getId));
    }

    /**
     * 工作台最新公告：已发布，置顶优先，最多 limit 条
     */
    default List<AnnouncementDO> selectLatestList(int limit) {
        return selectList(new LambdaQueryWrapperX<AnnouncementDO>()
                .eq(AnnouncementDO::getStatus, "0")
                .orderByDesc(AnnouncementDO::getPinned)
                .orderByDesc(AnnouncementDO::getId)
                .last("LIMIT " + limit));
    }

}
