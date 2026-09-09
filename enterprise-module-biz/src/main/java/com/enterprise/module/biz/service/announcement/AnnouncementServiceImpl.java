package com.enterprise.module.biz.service.announcement;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.module.biz.controller.admin.announcement.vo.announcement.AnnouncementPageReqVO;
import com.enterprise.module.biz.controller.admin.announcement.vo.announcement.AnnouncementSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.announcement.AnnouncementDO;
import com.enterprise.module.biz.dal.mysql.announcement.AnnouncementMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.List;

import static com.enterprise.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.enterprise.module.biz.enums.ErrorCodeConstants.*;

/**
 * 公司公告 Service 实现类
 *
 * @author 企业管理平台
 */
@Slf4j
@Service
@Validated
public class AnnouncementServiceImpl implements AnnouncementService {

    /** 状态：已发布/已下架 */
    private static final String STATUS_PUBLISHED = "0";
    private static final String STATUS_OFFLINE = "1";

    @Resource
    private AnnouncementMapper announcementMapper;

    @Override
    public Long createAnnouncement(AnnouncementSaveReqVO createReqVO) {
        AnnouncementDO announcement = BeanUtils.toBean(createReqVO, AnnouncementDO.class);
        announcement.setId(null);
        announcement.setStatus(STATUS_PUBLISHED);
        announcement.setPublishDate(LocalDate.now().toString());
        if (announcement.getPinned() == null || announcement.getPinned().isEmpty()) {
            announcement.setPinned("0");
        }
        announcementMapper.insert(announcement);
        return announcement.getId();
    }

    @Override
    public void updateAnnouncement(AnnouncementSaveReqVO updateReqVO) {
        AnnouncementDO exists = validateAnnouncementExists(updateReqVO.getId());
        AnnouncementDO updateObj = BeanUtils.toBean(updateReqVO, AnnouncementDO.class);
        // 发布日期与创建状态服务端管理；下架/重新发布通过 status 字段更新
        updateObj.setPublishDate(null);
        if (updateObj.getStatus() != null
                && !STATUS_PUBLISHED.equals(updateObj.getStatus())
                && !STATUS_OFFLINE.equals(updateObj.getStatus())) {
            throw exception(ANNOUNCEMENT_STATUS_INVALID);
        }
        if (updateObj.getStatus() == null) {
            updateObj.setStatus(exists.getStatus());
        }
        announcementMapper.updateById(updateObj);
    }

    @Override
    public void deleteAnnouncement(Long id) {
        validateAnnouncementExists(id);
        announcementMapper.deleteById(id);
    }

    @Override
    public AnnouncementDO getAnnouncement(Long id) {
        return announcementMapper.selectById(id);
    }

    @Override
    public PageResult<AnnouncementDO> getAnnouncementPage(AnnouncementPageReqVO pageReqVO) {
        return announcementMapper.selectPage(pageReqVO);
    }

    @Override
    public List<AnnouncementDO> getLatestAnnouncementList(int limit) {
        return announcementMapper.selectLatestList(limit);
    }

    private AnnouncementDO validateAnnouncementExists(Long id) {
        AnnouncementDO announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw exception(ANNOUNCEMENT_NOT_EXISTS);
        }
        return announcement;
    }

}
