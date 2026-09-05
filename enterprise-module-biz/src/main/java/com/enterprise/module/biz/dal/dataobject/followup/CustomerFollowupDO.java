package com.enterprise.module.biz.dal.dataobject.followup;

import com.enterprise.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * 客户跟进记录 DO
 *
 * @author 企业管理平台
 */
@TableName("biz_customer_followup")
@KeySequence("biz_customer_followup_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerFollowupDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**客户名称*/
    private String customerName;
    /**跟进时间*/
    private String followTime;
    /**跟进方式（1=电话 2=上门 3=微信 4=邮件 5=其他）*/
    private String method;
    /**跟进内容*/
    private String content;
    /**下次跟进日期*/
    private String nextDate;
}
