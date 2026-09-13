package com.enterprise.module.biz.dal.dataobject.wms;

import com.enterprise.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * WMS 库位库存 DO（产品在库位上的分配量）
 */
@TableName("biz_wms_location_stock")
@KeySequence("biz_wms_location_stock_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsLocationStockDO extends TenantBaseDO {

    @TableId
    private Long id;
    /** 仓库ID */
    private Long warehouseId;
    /** 仓库名称（冗余） */
    private String warehouseName;
    /** 库位ID */
    private Long locationId;
    /** 库位编码（冗余） */
    private String locationCode;
    /** 产品ID */
    private Long productId;
    /** 产品名称（冗余） */
    private String productName;
    /** 库位数量（>=0） */
    private Long quantity;

}
