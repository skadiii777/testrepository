package com.enterprise.module.biz.dal.dataobject.fms;

import com.enterprise.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 记账凭证 DO（头）
 */
@TableName("biz_fms_voucher")
@KeySequence("biz_fms_voucher_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FmsVoucherDO extends TenantBaseDO {

    @TableId
    private Long id;
    /** 凭证号（系统生成，如 JZ20260912001） */
    private String voucherNo;
    /** 凭证日期 */
    private LocalDate voucherDate;
    /** 摘要 */
    private String summary;
    /** 状态（0草稿 1已记账） */
    private Integer status;
    /** 借方合计 */
    private BigDecimal debitTotal;
    /** 贷方合计 */
    private BigDecimal creditTotal;
    /** 来源类型（payment/return/manual） */
    private String sourceType;
    /** 来源单据ID */
    private Long sourceId;

}
