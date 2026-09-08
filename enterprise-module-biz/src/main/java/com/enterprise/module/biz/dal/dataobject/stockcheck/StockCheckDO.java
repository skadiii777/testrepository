package com.enterprise.module.biz.dal.dataobject.stockcheck;

import com.enterprise.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 库存盘点单 DO
 *
 * 创建时快照账面数量；确认时按实盘调整库存并写库存流水（sourceType=stockcheck）。
 *
 * @author 企业管理平台
 */
@TableName("biz_stock_check")
@KeySequence("biz_stock_check_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class StockCheckDO extends TenantBaseDO {

    /**
     * 主键
     */
    private Long id;
    /**
     * 盘点单号（PD+时间戳）
     */
    private String checkNo;
    /**
     * 产品名称
     */
    private String productName;
    /**
     * 仓库
     */
    private String warehouse;
    /**
     * 账面数量（确认时快照）
     */
    private Long bookQuantity;
    /**
     * 实盘数量
     */
    private Long actualQuantity;
    /**
     * 差异（实盘-账面）
     */
    private Long diffQuantity;
    /**
     * 状态（0=待确认 1=已确认）
     */
    private String status;
    /**
     * 盘点日期
     */
    private String checkDate;
    /**
     * 备注
     */
    private String remark;

}
