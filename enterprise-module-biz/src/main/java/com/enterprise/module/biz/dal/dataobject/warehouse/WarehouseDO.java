package com.enterprise.module.biz.dal.dataobject.warehouse;
import com.enterprise.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
@Data @EqualsAndHashCode(callSuper = true) @TableName("biz_warehouse")
public class WarehouseDO extends TenantBaseDO {
    @TableId private Long id;
    private String name;
}
