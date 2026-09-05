package com.enterprise.module.system.service.logger;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.biz.system.logger.dto.OperateLogCreateReqDTO;
import com.enterprise.module.system.api.logger.dto.OperateLogPageReqDTO;
import com.enterprise.module.system.controller.admin.logger.vo.operatelog.OperateLogPageReqVO;
import com.enterprise.module.system.dal.dataobject.logger.OperateLogDO;

/**
 * 操作日志 Service 接口
 *
 * @author 企业管理平台源码
 */
public interface OperateLogService {

    /**
     * 记录操作日志
     *
     * @param createReqDTO 创建请求
     */
    void createOperateLog(OperateLogCreateReqDTO createReqDTO);

    /**
     * 获得操作日志
     *
     * @param id 编号
     * @return 操作日志
     */
    OperateLogDO getOperateLog(Long id);

    /**
     * 获得操作日志分页列表
     *
     * @param pageReqVO 分页条件
     * @return 操作日志分页列表
     */
    PageResult<OperateLogDO> getOperateLogPage(OperateLogPageReqVO pageReqVO);

    /**
     * 获得操作日志分页列表
     *
     * @param pageReqVO 分页条件
     * @return 操作日志分页列表
     */
    PageResult<OperateLogDO> getOperateLogPage(OperateLogPageReqDTO pageReqVO);

}
