package com.enterprise.module.biz.service.announcement;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.module.biz.controller.admin.announcement.vo.announcement.AnnouncementPageReqVO;
import com.enterprise.module.biz.controller.admin.announcement.vo.announcement.AnnouncementSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.announcement.AnnouncementDO;

import java.util.List;

/**
 * 公司公告 Service 接口
 *
 * @author 企业管理平台
 */
public interface AnnouncementService {

    /**
     * 创建公告（状态=已发布，发布日期=今天）
     */
    Long createAnnouncement(AnnouncementSaveReqVO createReqVO);

    /**
     * 更新公告（可改标题/正文/类型/置顶/上下架）
     */
    void updateAnnouncement(AnnouncementSaveReqVO updateReqVO);

    /**
     * 删除公告
     */
    void deleteAnnouncement(Long id);

    /**
     * 获得公告
     */
    AnnouncementDO getAnnouncement(Long id);

    /**
     * 获得公告分页
     */
    PageResult<AnnouncementDO> getAnnouncementPage(AnnouncementPageReqVO pageReqVO);

    /**
     * 工作台最新公告（已发布，置顶优先）
     */
    List<AnnouncementDO> getLatestAnnouncementList(int limit);

}
