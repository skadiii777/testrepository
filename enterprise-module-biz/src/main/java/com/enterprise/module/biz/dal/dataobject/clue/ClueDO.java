package com.enterprise.module.biz.dal.dataobject.clue;

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
 * 销售线索 DO
 *
 * 对齐 yudao CRM 线索概念（简化版）：线索跟进后可一键转化为客户 + 商机；
 * 已转化（2）/ 已无效（3）为终态，不可再编辑。
 *
 * @author 企业管理平台
 */
@TableName("biz_clue")
@KeySequence("biz_clue_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClueDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 线索名称（客户公司名称）
     */
    private String name;
    /**
     * 联系人
     */
    private String contactName;
    /**
     * 联系电话
     */
    private String contactMobile;
    /**
     * 线索来源（biz_clue_source：1广告投放 2客户推荐 3官网咨询 4电话营销 5其他渠道）
     */
    private String source;
    /**
     * 状态（biz_clue_status：0待跟进 1跟进中 2已转化 3已无效）
     */
    private String status;
    /**
     * 负责人（创建时自动取当前登录人昵称）
     */
    private String ownerName;
    /**
     * 转化后的客户 id（未转化为空）
     */
    private Long customerId;
    /**
     * 备注
     */
    private String remark;

}
