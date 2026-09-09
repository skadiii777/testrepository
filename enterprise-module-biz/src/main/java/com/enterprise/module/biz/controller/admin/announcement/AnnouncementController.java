package com.enterprise.module.biz.controller.admin.announcement;

import com.enterprise.framework.common.pojo.CommonResult;
import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.module.biz.controller.admin.announcement.vo.announcement.AnnouncementPageReqVO;
import com.enterprise.module.biz.controller.admin.announcement.vo.announcement.AnnouncementRespVO;
import com.enterprise.module.biz.controller.admin.announcement.vo.announcement.AnnouncementSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.announcement.AnnouncementDO;
import com.enterprise.module.biz.service.announcement.AnnouncementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.enterprise.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 公司公告")
@RestController
@RequestMapping("/biz/announcement")
@Validated
public class AnnouncementController {

    @Resource
    private AnnouncementService announcementService;

    @PostMapping("/create")
    @Operation(summary = "创建公告（发布）")
    @PreAuthorize("@ss.hasPermission('biz:announcement:create')")
    public CommonResult<Long> createAnnouncement(@Valid @RequestBody AnnouncementSaveReqVO createReqVO) {
        return success(announcementService.createAnnouncement(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新公告（可下架/重新发布）")
    @PreAuthorize("@ss.hasPermission('biz:announcement:update')")
    public CommonResult<Boolean> updateAnnouncement(@Valid @RequestBody AnnouncementSaveReqVO updateReqVO) {
        announcementService.updateAnnouncement(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除公告")
    @Parameter(name = "id", description = "公告编号", required = true)
    @PreAuthorize("@ss.hasPermission('biz:announcement:delete')")
    public CommonResult<Boolean> deleteAnnouncement(@RequestParam("id") Long id) {
        announcementService.deleteAnnouncement(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得公告详情")
    @Parameter(name = "id", description = "公告编号", required = true)
    public CommonResult<AnnouncementRespVO> getAnnouncement(@RequestParam("id") Long id) {
        return success(BeanUtils.toBean(announcementService.getAnnouncement(id), AnnouncementRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得公告分页（管理端）")
    @PreAuthorize("@ss.hasPermission('biz:announcement:query')")
    public CommonResult<PageResult<AnnouncementRespVO>> getAnnouncementPage(@Valid AnnouncementPageReqVO pageReqVO) {
        PageResult<AnnouncementDO> pageResult = announcementService.getAnnouncementPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, AnnouncementRespVO.class));
    }

    @GetMapping("/list-latest")
    @Operation(summary = "工作台最新公告（已发布，置顶优先，登录即可）")
    public CommonResult<List<AnnouncementRespVO>> getLatestAnnouncementList(
            @RequestParam(value = "limit", defaultValue = "5") Integer limit) {
        return success(BeanUtils.toBean(announcementService.getLatestAnnouncementList(limit),
                AnnouncementRespVO.class));
    }

}
