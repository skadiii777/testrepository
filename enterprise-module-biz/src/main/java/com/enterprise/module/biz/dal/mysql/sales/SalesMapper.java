package com.enterprise.module.biz.dal.mysql.sales;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.mybatis.core.mapper.BaseMapperX;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.enterprise.module.biz.controller.admin.sales.vo.sales.SalesPageReqVO;
import com.enterprise.module.biz.dal.dataobject.sales.SalesDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
@Mapper
public interface SalesMapper extends BaseMapperX<SalesDO> {

    /**
     * 分页查询
     */
    default PageResult<SalesDO> selectPage(SalesPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<SalesDO>()
                .likeIfPresent(SalesDO::getSalesCode, reqVO.getSalesCode())
                .likeIfPresent(SalesDO::getProductName, reqVO.getProductName())
                .eqIfPresent(SalesDO::getSalesDate, reqVO.getSalesDate())
                .eqIfPresent(SalesDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(SalesDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(SalesDO::getId));
    }

    /**
     * 按日汇总销售金额（>= startDate）
     */
    @Select("SELECT sales_date AS `date`, IFNULL(SUM(total_amount),0) AS total FROM biz_sales "
            + "WHERE deleted = 0 AND sales_date >= #{startDate} GROUP BY sales_date")
    java.util.List<java.util.Map<String, Object>> selectSumByDate(@Param("startDate") String startDate);

    /**
     * 产品销售数量 Top N
     */
    @Select("SELECT product_name AS productName, SUM(quantity) AS totalQty, SUM(total_amount) AS totalAmount "
            + "FROM biz_sales WHERE deleted = 0 GROUP BY product_name ORDER BY totalQty DESC LIMIT #{top}")
    java.util.List<java.util.Map<String, Object>> selectProductTop(@Param("top") int top);

    /**
     * 状态 CAS 流转：仅当当前状态等于 fromStatus 时改为 toStatus，返回影响行数（防并发重复流转）
     */
    default int updateStatusByCas(Long id, String fromStatus, String toStatus) {
        return update(null, new LambdaUpdateWrapper<SalesDO>()
                .eq(SalesDO::getId, id)
                .eq(SalesDO::getStatus, fromStatus)
                .set(SalesDO::getStatus, toStatus));
    }

    /**
     * 审批 CAS：仅待审批(0)可流转（请假用）
     */
    default int updateStatusCas(Long id, String toStatus, String auditRemark) {
        return update(null, new LambdaUpdateWrapper<SalesDO>()
                .eq(SalesDO::getId, id)
                .eq(SalesDO::getStatus, "0")
                .set(SalesDO::getStatus, toStatus));
    }
}
