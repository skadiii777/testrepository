package com.enterprise.module.biz.controller.admin.contact;

import com.enterprise.framework.apilog.core.annotation.ApiAccessLog;
import com.enterprise.framework.common.pojo.CommonResult;
import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.module.biz.controller.admin.contact.vo.contact.ContactPageReqVO;
import com.enterprise.module.biz.controller.admin.contact.vo.contact.ContactRespVO;
import com.enterprise.module.biz.controller.admin.contact.vo.contact.ContactSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.contact.ContactDO;
import com.enterprise.module.biz.service.contact.ContactService;
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

@Tag(name = "管理后台 - 客户联系人")
@RestController
@RequestMapping("/biz/contact")
@Validated
public class ContactController {

    @Resource
    private ContactService contactService;

    @PostMapping("/create")
    @Operation(summary = "创建联系人")
    @PreAuthorize("@ss.hasPermission('biz:contact:create')")
    public CommonResult<Long> createContact(@Valid @RequestBody ContactSaveReqVO createReqVO) {
        return success(contactService.createContact(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新联系人")
    @PreAuthorize("@ss.hasPermission('biz:contact:update')")
    public CommonResult<Boolean> updateContact(@Valid @RequestBody ContactSaveReqVO updateReqVO) {
        contactService.updateContact(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除联系人")
    @Parameter(name = "id", description = "联系人编号", required = true)
    @PreAuthorize("@ss.hasPermission('biz:contact:delete')")
    public CommonResult<Boolean> deleteContact(@RequestParam("id") Long id) {
        contactService.deleteContact(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得联系人")
    @Parameter(name = "id", description = "联系人编号", required = true)
    @PreAuthorize("@ss.hasPermission('biz:contact:query')")
    public CommonResult<ContactRespVO> getContact(@RequestParam("id") Long id) {
        return success(BeanUtils.toBean(contactService.getContact(id), ContactRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得联系人分页")
    @PreAuthorize("@ss.hasPermission('biz:contact:query')")
    public CommonResult<PageResult<ContactRespVO>> getContactPage(@Valid ContactPageReqVO pageReqVO) {
        PageResult<ContactDO> pageResult = contactService.getContactPage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, ContactRespVO.class));
    }

    @GetMapping("/list-by-customer")
    @Operation(summary = "获得指定客户的联系人列表")
    @Parameter(name = "customerId", description = "客户编号", required = true)
    @PreAuthorize("@ss.hasPermission('biz:contact:query')")
    public CommonResult<List<ContactRespVO>> getContactListByCustomer(
            @RequestParam("customerId") Long customerId) {
        return success(BeanUtils.toBean(contactService.getContactListByCustomer(customerId),
                ContactRespVO.class));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出联系人 Excel")
    @ApiAccessLog(operateType = EXPORT)
    @PreAuthorize("@ss.hasPermission('biz:contact:query')")
    public void exportContactExcel(@Valid ContactPageReqVO pageReqVO,
                                   jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        pageReqVO.setPageSize(com.enterprise.framework.common.pojo.PageParam.PAGE_SIZE_NONE);
        List<ContactRespVO> list = BeanUtils.toBean(
                contactService.getContactPage(pageReqVO).getList(), ContactRespVO.class);
        com.enterprise.framework.excel.core.util.ExcelUtils.write(response, "客户联系人.xls", "数据",
                ContactRespVO.class, list);
    }

}
