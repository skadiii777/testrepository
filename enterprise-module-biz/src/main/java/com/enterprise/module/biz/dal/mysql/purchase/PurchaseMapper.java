package com.enterprise.module.biz.dal.mysql.purchase;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.mybatis.core.mapper.BaseMapperX;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.enterprise.module.biz.controller.admin.purchase.vo.purchase.PurchasePageReqVO;
import com.enterprise.module.biz.dal.dataobject.purchase.PurchaseDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
@Mapper
public interface PurchaseMapper extends BaseMapperX<PurchaseDO> {

    /**
     * 分页查询
     */
    default PageResult<PurchaseDO> selectPage(PurchasePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PurchaseDO>()
                .likeIfPresent(PurchaseDO::getPurchaseCode, reqVO.getPurchaseCode())
                .likeIfPresent(PurchaseDO::getProductName, reqVO.getProductName())
                .eqIfPresent(PurchaseDO::getPurchaseDate, reqVO.getPurchaseDate())
                .eqIfPresent(PurchaseDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(PurchaseDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(PurchaseDO::getId));
    }

    /**
     * 按日汇总采购金额（>= startDate）
     */
    @Select("SELECT purchase_date AS `date`, IFNULL(SUM(total_amount),0) AS total FROM biz_purchase "
            + "WHERE deleted = 0 AND purchase_date >= #{startDate} GROUP BY purchase_date")
    java.util.List<java.util.Map<String, Object>> selectSumByDate(@Param("startDate") String startDate);

    /**
     * 状态 CAS 流转：仅当当前状态等于 fromStatus 时改为 toStatus，返回影响行数（防并发重复流转）
     */
    default int updateStatusByCas(Long id, String fromStatus, String toStatus) {
        return update(null, new LambdaUpdateWrapper<PurchaseDO>()
                .eq(PurchaseDO::getId, id)
                .eq(PurchaseDO::getStatus, fromStatus)
                .set(PurchaseDO::getStatus, toStatus));
    }

    /**
     * 审批 CAS：仅待审批(0)可流转（请假用）
     */
    default int updateStatusCas(Long id, String toStatus, String auditRemark) {
        return update(null, new LambdaUpdateWrapper<PurchaseDO>()
                .eq(PurchaseDO::getId, id)
                .eq(PurchaseDO::getStatus, "0")
                .set(PurchaseDO::getStatus, toStatus));
    }
}
