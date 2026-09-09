package com.enterprise.module.biz.controller.admin.clue;

import com.enterprise.framework.apilog.core.annotation.ApiAccessLog;
import com.enterprise.framework.common.pojo.CommonResult;
import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.module.biz.controller.admin.clue.vo.clue.ClueConvertReqVO;
import com.enterprise.module.biz.controller.admin.clue.vo.clue.CluePageReqVO;
import com.enterprise.module.biz.controller.admin.clue.vo.clue.ClueRespVO;
import com.enterprise.module.biz.controller.admin.clue.vo.clue.ClueSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.clue.ClueDO;
import com.enterprise.module.biz.service.clue.ClueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.enterprise.framework.apilog.core.enums.OperateTypeEnum.DELETE;
import static com.enterprise.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.enterprise.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 销售线索")
@RestController
@RequestMapping("/biz/clue")
@Validated
public class ClueController {

    @Resource
    private ClueService clueService;

    @PostMapping("/create")
    @Operation(summary = "创建线索")
    @PreAuthorize("@ss.hasPermission('biz:clue:create')")
    public CommonResult<Long> createClue(@Valid @RequestBody ClueSaveReqVO createReqVO) {
        return success(clueService.createClue(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新线索（已转化/已无效不可更新）")
    @PreAuthorize("@ss.hasPermission('biz:clue:update')")
    public CommonResult<Boolean> updateClue(@Valid @RequestBody ClueSaveReqVO updateReqVO) {
        clueService.updateClue(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除线索")
    @Parameter(name = "id", description = "线索编号", required = true)
    @PreAuthorize("@ss.hasPermission('biz:clue:delete')")
    public CommonResult<Boolean> deleteClue(@RequestParam("id") Long id) {
        clueService.deleteClue(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得线索")
    @Parameter(name = "id", description = "线索编号", required = true)
    @PreAuthorize("@ss.hasPermission('biz:clue:query')")
    public CommonResult<ClueRespVO> getClue(@RequestParam("id") Long id) {
        return success(BeanUtils.toBean(clueService.getClue(id), ClueRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得线索分页")
    @PreAuthorize("@ss.hasPermission('biz:clue:query')")
    public CommonResult<PageResult<ClueRespVO>> getCluePage(@Valid CluePageReqVO pageReqVO) {
        PageResult<ClueDO> pageResult = clueService.getCluePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ClueRespVO.class));
    }

    @PutMapping("/convert")
    @Operation(summary = "线索转商机（创建客户+商机，线索置为已转化）")
    @Parameter(name = "id", description = "线索编号", required = true)
    @PreAuthorize("@ss.hasPermission('biz:clue:update')")
    public CommonResult<Long> convertClue(@RequestParam("id") Long id,
                                          @Valid @RequestBody ClueConvertReqVO convertReqVO) {
        return success(clueService.convertClue(id, convertReqVO));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出线索 Excel")
    @ApiAccessLog(operateType = EXPORT)
    @PreAuthorize("@ss.hasPermission('biz:clue:query')")
    public void exportClueExcel(@Valid CluePageReqVO pageReqVO,
                                jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        pageReqVO.setPageSize(com.enterprise.framework.common.pojo.PageParam.PAGE_SIZE_NONE);
        List<ClueRespVO> list = BeanUtils.toBean(
                clueService.getCluePage(pageReqVO).getList(), ClueRespVO.class);
        com.enterprise.framework.excel.core.util.ExcelUtils.write(response, "销售线索.xls", "数据",
                ClueRespVO.class, list);
    }

}
