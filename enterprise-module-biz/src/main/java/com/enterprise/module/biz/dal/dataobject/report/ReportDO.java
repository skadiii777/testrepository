package com.enterprise.module.biz.dal.dataobject.report;

import com.enterprise.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 业务汇报 DO
 *
 * @author 企业管理平台
 */
@TableName("biz_report")
@KeySequence("biz_report_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**汇报类型*/
    private String reportType;
    /**标题*/
    private String title;
    /**汇报内容*/
    private String content;
    /**汇报日期*/
    private String reportDate;

}