package com.enterprise.module.system.controller.admin.onlineuser;

import com.enterprise.framework.common.pojo.CommonResult;
import com.enterprise.framework.common.pojo.PageParam;
import com.enterprise.module.system.controller.admin.onlineuser.vo.OnlineUserRespVO;
import com.enterprise.module.system.dal.dataobject.dept.DeptDO;
import com.enterprise.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import com.enterprise.module.system.dal.dataobject.user.AdminUserDO;
import com.enterprise.module.system.service.dept.DeptService;
import com.enterprise.module.system.service.oauth2.OAuth2TokenService;
import com.enterprise.module.system.service.user.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static com.enterprise.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 在线用户")
@RestController
@RequestMapping("/system/online-user")
@Validated
public class OnlineUserController {

    @Resource
    private OAuth2TokenService oauth2TokenService;
    @Resource
    private AdminUserService adminUserService;
    @Resource
    private DeptService deptService;

    @GetMapping("/list")
    @Operation(summary = "在线用户列表（有效期内令牌 = 在线会话）")
    @PreAuthorize("@ss.hasPermission('system:online-user:query')")
    public CommonResult<List<OnlineUserRespVO>> getOnlineUserList() {
        // 复用令牌分页（只返回有效期内的），一次拉满
        List<OAuth2AccessTokenDO> tokens = oauth2TokenService.getAccessTokenPage(
                new com.enterprise.module.system.controller.admin.oauth2.vo.token.OAuth2AccessTokenPageReqVO() {{
                    setPageNo(1);
                    setPageSize(PageParam.PAGE_SIZE_NONE);
                }}).getList();
        List<OnlineUserRespVO> result = new ArrayList<>();
        for (OAuth2AccessTokenDO token : tokens) {
            OnlineUserRespVO vo = new OnlineUserRespVO();
            vo.setUserId(token.getUserId());
            vo.setUserType(token.getUserType());
            vo.setClientId(token.getClientId());
            vo.setAccessTokenMask(mask(token.getAccessToken()));
            vo.setCreateTime(token.getCreateTime());
            vo.setExpiresTime(token.getExpiresTime());
            if (token.getUserId() != null) {
                AdminUserDO user = adminUserService.getUser(token.getUserId());
                if (user != null) {
                    vo.setUsername(user.getUsername());
                    vo.setNickname(user.getNickname());
                    DeptDO dept = user.getDeptId() != null ? deptService.getDept(user.getDeptId()) : null;
                    vo.setDeptName(dept != null ? dept.getName() : null);
                }
            }
            result.add(vo);
        }
        return success(result);
    }

    @DeleteMapping("/kick")
    @Operation(summary = "强制下线（按用户踢出其全部会话，单设备模式下即当前唯一会话）")
    @Parameter(name = "userId", description = "用户编号", required = true)
    @Parameter(name = "userType", description = "用户类型", required = true)
    @PreAuthorize("@ss.hasPermission('system:online-user:delete')")
    public CommonResult<Boolean> kickOnlineUser(@RequestParam("userId") Long userId,
                                                @RequestParam("userType") Integer userType) {
        oauth2TokenService.removeAccessToken(userId, userType);
        return success(true);
    }

    /** 令牌脱敏：仅展示前 8 位 */
    private String mask(String accessToken) {
        return accessToken == null || accessToken.length() <= 8
                ? "****" : accessToken.substring(0, 8) + "****";
    }

}
