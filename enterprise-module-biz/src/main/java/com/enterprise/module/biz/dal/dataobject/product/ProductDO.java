package com.enterprise.module.biz.dal.dataobject.product;

import com.enterprise.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 产品 DO
 *
 * @author 企业管理平台
 */
@TableName("biz_product")
@KeySequence("biz_product_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**产品编号*/
    private String productCode;
    /**产品名称*/
    private String productName;
    /**产品分类*/
    private String category;
    /**单位*/
    private String unit;
    /**销售单价*/
    private BigDecimal price;
    /**成本价*/
    private BigDecimal cost;
    /**状态*/
    private String status;

}