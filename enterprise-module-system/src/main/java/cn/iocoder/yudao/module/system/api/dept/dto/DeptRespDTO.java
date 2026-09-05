package com.enterprise.module.system.api.dept.dto;

import com.enterprise.framework.common.enums.CommonStatusEnum;
import lombok.Data;

/**
 * 部门 Response DTO
 *
 * @author 企业管理平台源码
 */
@Data
public class DeptRespDTO {

    /**
     * 部门编号
     */
    private Long id;
    /**
     * 部门名称
     */
    private String name;
    /**
     * 父部门编号
     */
    private Long parentId;
    /**
     * 负责人的用户编号
     */
    private Long leaderUserId;
    /**
     * 部门状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

}
