package com.enterprise.module.biz.dal.dataobject.returnorder;

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

import java.math.BigDecimal;

/**
 * 退货单 DO
 *
 * 对齐 yudao ERP 销售退货/采购退货概念：销售单（已完成）可发起销售退货（货物入库），
 * 采购单（已完成）可发起采购退货（货物退回供应商、出库）；
 * 同一原单累计退货数量不可超过原单数量；执行退货时联动库存。
 *
 * @author 企业管理平台
 */
@TableName("biz_return")
@KeySequence("biz_return_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnDO extends TenantBaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 退货单号（SR/PR+时间戳）
     */
    private String returnNo;
    /**
     * 退货类型（1=销售退货 2=采购退货）
     */
    private String returnType;
    /**
     * 关联单据 id
     */
    private Long orderId;
    /**
     * 关联单据编号
     */
    private String orderCode;
    /**
     * 对方名称（客户/供应商）
     */
    private String partyName;
    /**
     * 产品名称
     */
    private String productName;
    /**
     * 仓库
     */
    private String warehouse;
    /**
     * 退货数量
     */
    private Long quantity;
    /**
     * 退货单价
     */
    private BigDecimal price;
    /**
     * 总金额
     */
    private BigDecimal totalAmount;
    /**
     * 退货日期
     */
    private String returnDate;
    /**
     * 退货原因
     */
    private String reason;
    /**
     * 状态（0=待退货 1=已退货 3=已作废）
     */
    private String status;
    /**
     * 备注
     */
    private String remark;

}
