package com.enterprise.module.system.service.registerapply;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.module.system.controller.admin.registerapply.vo.registerapply.RegisterApplyPageReqVO;
import com.enterprise.module.system.dal.dataobject.dept.DeptDO;
import com.enterprise.module.system.dal.dataobject.dept.PostDO;
import com.enterprise.module.system.dal.dataobject.registerapply.RegisterApplyDO;
import com.enterprise.module.system.dal.dataobject.user.AdminUserDO;
import com.enterprise.module.system.dal.mysql.dept.DeptMapper;
import com.enterprise.module.system.dal.mysql.dept.PostMapper;
import com.enterprise.module.system.dal.mysql.registerapply.RegisterApplyMapper;
import com.enterprise.module.system.dal.mysql.user.AdminUserMapper;
import com.enterprise.module.system.service.permission.PermissionService;
import com.enterprise.module.system.service.user.AdminUserService;
import com.enterprise.framework.common.enums.CommonStatusEnum;
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.enterprise.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.enterprise.module.system.enums.ErrorCodeConstants.*;

/**
 * 注册申请 Service 实现类
 *
 * @author 企业管理平台
 */
@Slf4j
@Service
@Validated
public class RegisterApplyServiceImpl implements RegisterApplyService {

    /** 状态：待审批 */
    private static final String STATUS_PENDING = "0";
    /** 状态：已通过 */
    private static final String STATUS_APPROVED = "1";
    /** 状态：已驳回 */
    private static final String STATUS_REJECTED = "2";

    @Resource
    private RegisterApplyMapper registerApplyMapper;
    @Resource
    private AdminUserService userService;
    @Resource
    private AdminUserMapper userMapper;
    @Resource
    private DeptMapper deptMapper;
    @Resource
    private PostMapper postMapper;
    @Resource
    private PermissionService permissionService;
    @Resource
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    @Resource
    private com.enterprise.module.system.service.permission.RoleService roleService;
    @Resource
    private com.enterprise.module.system.dal.mysql.deptrolemap.DeptRoleMapMapper deptRoleMapMapper;

    @Override
    public void createApply(String username, String rawPassword, String nickname, Long deptId, Long postId) {
        // 1.1 用户名未被正式账号占用
        if (userMapper.selectByUsername(username) != null) {
            throw exception(REGISTER_APPLY_DUPLICATE);
        }
        // 1.2 无进行中的同账号申请
        if (registerApplyMapper.selectByUsernameAndPending(username) != null) {
            throw exception(REGISTER_APPLY_DUPLICATE);
        }
        // 1.3 校验申请部门/岗位有效
        DeptDO dept = deptMapper.selectById(deptId);
        if (dept == null || !CommonStatusEnum.ENABLE.getStatus().equals(dept.getStatus())) {
            throw exception(REGISTER_DEPT_INVALID);
        }
        if (postId != null) {
            PostDO post = postMapper.selectById(postId);
            if (post == null || !CommonStatusEnum.ENABLE.getStatus().equals(post.getStatus())) {
                throw exception(REGISTER_POST_INVALID);
            }
        }

        // 2. 创建待审批申请（账号不落 system_users，不可登录）
        RegisterApplyDO apply = new RegisterApplyDO();
        apply.setUsername(username);
        apply.setPassword(passwordEncoder.encode(rawPassword)); // 存摘要，审批通过后沿用
        apply.setNickname(nickname);
        apply.setDeptId(deptId);
        apply.setPostId(postId);
        apply.setStatus(STATUS_PENDING);
        registerApplyMapper.insert(apply);
        log.info("[createApply][注册申请({}) 已提交：账号({}) 昵称({}) 部门({}) 岗位({})]",
                apply.getId(), username, nickname, deptId, postId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveApply(Long id) {
        RegisterApplyDO apply = validateApplyPending(id);
        // 1. 创建正式账号（密码沿用申请时的摘要，不再二次加密）
        AdminUserDO user = new AdminUserDO();
        user.setUsername(apply.getUsername());
        user.setNickname(apply.getNickname());
        user.setPassword(apply.getPassword());
        user.setDeptId(apply.getDeptId());
        if (apply.getPostId() != null) {
            user.setPostIds(Collections.singleton(apply.getPostId()));
        }
        user.setStatus(CommonStatusEnum.ENABLE.getStatus());
        userMapper.insert(user);
        // 2. 分配角色：优先按 部门→默认角色 映射，无映射回退「普通角色」common
        Set<Long> roleIds = resolveRoleIdsByDept(apply.getDeptId());
        if (!roleIds.isEmpty()) {
            permissionService.assignUserRole(user.getId(), roleIds);
        }
        // 3. 标记申请通过并清空密码摘要
        RegisterApplyDO update = new RegisterApplyDO();
        update.setId(id);
        update.setStatus(STATUS_APPROVED);
        update.setAuditTime(LocalDate.now().toString());
        update.setPassword("");
        registerApplyMapper.updateById(update);
        log.info("[approveApply][注册申请({}) 已通过，账号({}) 已加入部门({}) 并分配角色 {}]",
                id, apply.getUsername(), apply.getDeptId(), roleIds);
    }

    @Override
    public void rejectApply(Long id, String reason) {
        RegisterApplyDO apply = validateApplyPending(id);
        RegisterApplyDO update = new RegisterApplyDO();
        update.setId(id);
        update.setStatus(STATUS_REJECTED);
        update.setRejectReason(reason);
        update.setAuditTime(LocalDate.now().toString());
        update.setPassword("");
        registerApplyMapper.updateById(update);
        log.info("[rejectApply][注册申请({}) 已驳回：账号({}) 原因({})]", id, apply.getUsername(), reason);
    }

    @Override
    public boolean hasPendingApply(String username) {
        return registerApplyMapper.selectByUsernameAndPending(username) != null;
    }

    @Override
    public PageResult<RegisterApplyDO> getRegisterApplyPage(RegisterApplyPageReqVO pageReqVO) {
        return registerApplyMapper.selectPage(pageReqVO);
    }

    private RegisterApplyDO validateApplyPending(Long id) {
        RegisterApplyDO apply = registerApplyMapper.selectById(id);
        if (apply == null) {
            throw exception(REGISTER_APPLY_NOT_EXISTS);
        }
        if (!STATUS_PENDING.equals(apply.getStatus())) {
            throw exception(REGISTER_APPLY_ALREADY_AUDITED);
        }
        return apply;
    }

    /**
     * 部门 → 默认角色（预留架构：未来按部门/职位开放模块权限时，在此扩展映射维度）。
     * 优先读 biz_dept_role_map 部门映射；无映射时回退启用状态的「普通角色」common。
     */
    private Set<Long> resolveRoleIdsByDept(Long deptId) {
        Set<Long> roleIds = new HashSet<>();
        deptRoleMapMapper.selectListByDept(deptId)
                .forEach(map -> roleIds.add(map.getRoleId()));
        if (roleIds.isEmpty()) {
            roleService.getRoleList().stream()
                    .filter(role -> "common".equals(role.getCode())
                            && CommonStatusEnum.ENABLE.getStatus().equals(role.getStatus()))
                    .findFirst()
                    .ifPresent(role -> roleIds.add(role.getId()));
        }
        return roleIds;
    }

}
