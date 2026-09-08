package com.enterprise.module.system.service.registerapply;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.module.system.controller.admin.registerapply.vo.registerapply.RegisterApplyPageReqVO;
import com.enterprise.module.system.dal.dataobject.registerapply.RegisterApplyDO;

/**
 * 注册申请 Service 接口
 *
 * 注册与审批的业务规则：
 * 1. 注册：校验用户名未占用（system_users + 待审批申请），创建待审批申请（不创建账号、不可登录）
 * 2. 审批通过：创建正式账号（启用、入申请部门/岗位），按 部门默认角色映射 分配角色（无映射则
 *    回退「普通角色」common），该映射表是未来"按部门/职位开放模块权限"的挂载点
 * 3. 驳回：仅标记申请记录，不创建账号
 *
 * @author 企业管理平台
 */
public interface RegisterApplyService {

    /**
     * 创建注册申请
     *
     * @param username      用户账号
     * @param passwordHash  已加密的密码摘要
     * @param nickname      用户昵称
     * @param deptId        申请部门
     * @param postId        申请岗位（可为空）
     */
    void createApply(String username, String passwordHash, String nickname, Long deptId, Long postId);

    /**
     * 审批通过：创建正式账号并分配部门/岗位/角色
     *
     * @param id 申请编号
     */
    void approveApply(Long id);

    /**
     * 驳回申请
     *
     * @param id     申请编号
     * @param reason 驳回原因
     */
    void rejectApply(Long id, String reason);

    /**
     * 判断用户名是否存在待审批申请（登录时用于给出"待审批"提示）
     *
     * @param username 用户账号
     * @return 是否存在
     */
    boolean hasPendingApply(String username);

    /**
     * 获得注册申请分页
     *
     * @param pageReqVO 分页查询
     * @return 分页结果
     */
    PageResult<RegisterApplyDO> getRegisterApplyPage(RegisterApplyPageReqVO pageReqVO);

}
