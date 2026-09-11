package com.enterprise.module.biz.dal.dataobject.stock;

import com.enterprise.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 库存 DO
 *
 * @author 企业管理平台
 */
@TableName("biz_stock")
@KeySequence("biz_stock_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**产品名称*/
    private Long productId;
    private Long warehouseId;
    private String productName;
    /**仓库*/
    private String warehouse;
    /**库存数量*/
    private Long quantity;
    /**预警下限*/
    private Long minQuantity;

}