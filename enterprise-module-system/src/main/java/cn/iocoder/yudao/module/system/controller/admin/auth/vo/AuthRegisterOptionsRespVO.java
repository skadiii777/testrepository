package com.enterprise.module.system.controller.admin.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "管理后台 - 注册页下拉选项 Response VO（启用部门 + 启用岗位，仅含 id/名称）")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthRegisterOptionsRespVO {

    @Schema(description = "启用部门列表")
    private List<Dept> depts;

    @Schema(description = "启用岗位列表")
    private List<Post> posts;

    @Schema(description = "部门精简信息")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Dept {

        @Schema(description = "部门编号")
        private Long id;

        @Schema(description = "部门名称")
        private String name;

    }

    @Schema(description = "岗位精简信息")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Post {

        @Schema(description = "岗位编号")
        private Long id;

        @Schema(description = "岗位名称")
        private String name;

    }

}
