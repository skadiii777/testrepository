package com.enterprise.module.biz.dal.mysql.payment;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.enterprise.framework.mybatis.core.mapper.BaseMapperX;
import com.enterprise.module.biz.controller.admin.payment.vo.payment.PaymentPageReqVO;
import com.enterprise.module.biz.dal.dataobject.payment.PaymentDO;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.List;

/**
 * 收付款流水 Mapper
 *
 * @author 企业管理平台
 */
@Mapper
public interface PaymentMapper extends BaseMapperX<PaymentDO> {
    default PaymentDO selectForUpdate(Long id) {
        return selectOne(new LambdaQueryWrapperX<PaymentDO>().eq(PaymentDO::getId, id).last("FOR UPDATE"));
    }
    default PaymentDO selectByRequestId(String requestId) {
        return selectOne(new LambdaQueryWrapperX<PaymentDO>().eq(PaymentDO::getRequestId, requestId).last("FOR UPDATE"));
    }
    default List<PaymentDO> selectCurrentByOrder(String type, Long orderId) {
        return selectList(new LambdaQueryWrapperX<PaymentDO>().eq(PaymentDO::getBizType, type)
                .eq(PaymentDO::getOrderId, orderId).orderByAsc(PaymentDO::getId).last("FOR UPDATE"));
    }
    default List<PaymentDO> selectCurrentByContract(Long contractId) {
        return selectList(new LambdaQueryWrapperX<PaymentDO>().eq(PaymentDO::getContractId, contractId)
                .orderByAsc(PaymentDO::getId).last("FOR UPDATE"));
    }


    default PageResult<PaymentDO> selectPage(PaymentPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PaymentDO>()
                .eqIfPresent(PaymentDO::getPaymentType, reqVO.getPaymentType())
                .eqIfPresent(PaymentDO::getBizType, reqVO.getBizType())
                .likeIfPresent(PaymentDO::getOrderCode, reqVO.getOrderCode())
                .likeIfPresent(PaymentDO::getPartyName, reqVO.getPartyName())
                .betweenIfPresent(PaymentDO::getPaymentDate, reqVO.getPaymentDateRange())
                .orderByDesc(PaymentDO::getId));
    }

    default List<PaymentDO> selectListByOrder(String bizType, Long orderId) {
        return selectList(new LambdaQueryWrapperX<PaymentDO>()
                .eq(PaymentDO::getBizType, bizType)
                .eq(PaymentDO::getOrderId, orderId));
    }

    default BigDecimal selectPaidSumByOrder(String bizType, Long orderId) {
        return selectListByOrder(bizType, orderId).stream()
                .map(PaymentDO::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 汇总挂到指定合同的收款金额（合同回款进度）
     */
    default List<PaymentDO> selectListByContract(Long contractId) {
        return selectList(new LambdaQueryWrapperX<PaymentDO>()
                .eq(PaymentDO::getContractId, contractId));
    }

}
