package com.enterprise.module.biz.dal.dataobject.wms;

import com.enterprise.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * WMS 库位 DO
 */
@TableName("biz_wms_location")
@KeySequence("biz_wms_location_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WmsLocationDO extends TenantBaseDO {

    @TableId
    private Long id;
    /** 仓库ID（biz_warehouse） */
    private Long warehouseId;
    /** 仓库名称（冗余） */
    private String warehouseName;
    /** 库位编码（如 A-01-01） */
    private String code;
    /** 库位名称 */
    private String name;
    /** 库位类型（1存储区 2拣货区 3收货区 4退货区） */
    private Integer type;
    /** 状态（0启用 1停用） */
    private Integer status;
    /** 备注 */
    private String remark;

}
