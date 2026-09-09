package com.enterprise.module.biz.dal.dataobject.contact;

import com.enterprise.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 客户联系人 DO
 *
 * 对齐 yudao CRM 联系人概念（简化版）：一个客户可挂多个联系人（决策人/经办人）；
 * 线索转商机时自动把线索联系人落入该表。
 *
 * @author 企业管理平台
 */
@TableName("biz_contact")
@KeySequence("biz_contact_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 关联客户 id
     */
    private Long customerId;
    /**
     * 关联客户名称（冗余，便于列表展示）
     */
    private String customerName;
    /**
     * 联系人姓名
     */
    private String name;
    /**
     * 职位
     */
    private String position;
    /**
     * 手机号
     */
    private String mobile;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 微信
     */
    private String wechat;
    /**
     * 备注
     */
    private String remark;

}
