package com.enterprise.module.biz.dal.dataobject.stockmove;

import com.enterprise.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 库存流水 DO
 *
 * @author 企业管理平台
 */
@TableName("biz_stock_move")
@KeySequence("biz_stock_move_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMoveDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**产品名称*/
    private String productName;
    /**仓库*/
    private String warehouse;
    /**类型*/
    private String moveType;
    /**数量*/
    private BigDecimal quantity;
    /**结余*/
    private BigDecimal balanceAfter;
    /**来源类型*/
    private String sourceType;
    /**来源单号*/
    private String sourceCode;

}