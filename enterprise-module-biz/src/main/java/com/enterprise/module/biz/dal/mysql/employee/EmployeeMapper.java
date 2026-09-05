package com.enterprise.module.biz.dal.mysql.employee;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.mybatis.core.mapper.BaseMapperX;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.enterprise.module.biz.controller.admin.employee.vo.employee.EmployeePageReqVO;
import com.enterprise.module.biz.dal.dataobject.employee.EmployeeDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EmployeeMapper extends BaseMapperX<EmployeeDO> {

    /**
     * 分页查询
     */
    default PageResult<EmployeeDO> selectPage(EmployeePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<EmployeeDO>()
                .likeIfPresent(EmployeeDO::getEmpNo, reqVO.getEmpNo())
                .likeIfPresent(EmployeeDO::getEmpName, reqVO.getEmpName())
                .eqIfPresent(EmployeeDO::getDeptName, reqVO.getDeptName())
                .eqIfPresent(EmployeeDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(EmployeeDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(EmployeeDO::getId));
    }

}