package com.enterprise.module.biz.dal.dataobject.fms;

import com.enterprise.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;

/**
 * 凭证分录 DO（明细行，借方合计必须等于贷方合计）
 */
@TableName("biz_fms_voucher_entry")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FmsVoucherEntryDO extends TenantBaseDO {

    @TableId
    private Long id;
    /** 凭证ID */
    private Long voucherId;
    /** 科目ID */
    private Long accountId;
    /** 科目编码 */
    private String accountCode;
    /** 科目名称 */
    private String accountName;
    /** 分录摘要 */
    private String summary;
    /** 借方金额（借方有值时贷方为 0） */
    private BigDecimal debitAmount;
    /** 贷方金额 */
    private BigDecimal creditAmount;
    /** 排序 */
    private Integer sort;

}
