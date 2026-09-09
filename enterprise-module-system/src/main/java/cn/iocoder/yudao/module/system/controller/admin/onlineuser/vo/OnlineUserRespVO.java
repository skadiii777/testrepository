package com.enterprise.module.system.controller.admin.onlineuser.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 在线用户 Response VO")
@Data
public class OnlineUserRespVO {

    @Schema(description = "用户编号")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "部门")
    private String deptName;

    @Schema(description = "用户类型（2=管理端）")
    private Integer userType;

    @Schema(description = "客户端编号")
    private String clientId;

    @Schema(description = "访问令牌（脱敏）")
    private String accessTokenMask;

    @Schema(description = "登录时间")
    private LocalDateTime createTime;

    @Schema(description = "过期时间")
    private LocalDateTime expiresTime;

}
