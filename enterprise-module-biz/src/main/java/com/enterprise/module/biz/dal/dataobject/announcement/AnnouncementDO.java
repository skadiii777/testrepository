package com.enterprise.module.biz.dal.dataobject.announcement;

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
 * 公司公告 DO
 *
 * 管理员发布，全员在工作台可见；置顶优先展示，下架后不再对员工展示。
 *
 * @author 企业管理平台
 */
@TableName("biz_announcement")
@KeySequence("biz_announcement_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 标题
     */
    private String title;
    /**
     * 类型（biz_announcement_type：1通知 2公告 3制度）
     */
    private String type;
    /**
     * 正文
     */
    private String content;
    /**
     * 是否置顶（0=否 1=是）
     */
    private String pinned;
    /**
     * 状态（0=已发布 1=已下架）
     */
    private String status;
    /**
     * 发布日期（创建时服务端填充）
     */
    private String publishDate;

}
