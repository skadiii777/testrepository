package com.ruoyi.business.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 首页看板 数据层
 *
 * @author biz
 */
public interface BizDashboardMapper
{
    /** 指定起始日期后的销售金额按日汇总 */
    @Select("select sales_date as date, ifnull(sum(total_amount),0) as total "
            + " from biz_sales where sales_date >= #{startDate} group by sales_date")
    List<Map<String, Object>> selectSalesSum(String startDate);

    /** 指定起始日期后的采购金额按日汇总 */
    @Select("select purchase_date as date, ifnull(sum(total_amount),0) as total "
            + " from biz_purchase where purchase_date >= #{startDate} group by purchase_date")
    List<Map<String, Object>> selectPurchaseSum(String startDate);

    /** 销售数量Top N产品 */
    @Select("select product_name as productName, sum(quantity) as totalQty, sum(total_amount) as totalAmount "
            + " from biz_sales group by product_name order by totalQty desc limit ${top}")
    List<Map<String, Object>> selectProductTop(@Param("top") int top);

    /** 合同状态分布 */
    @Select("select status as status, count(*) as cnt from biz_contract group by status")
    List<Map<String, Object>> selectContractStatus();

    /** 请假审批状态分布 */
    @Select("select status as status, count(*) as cnt from biz_leave group by status")
    List<Map<String, Object>> selectLeaveStatus();
}
