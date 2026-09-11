package com.enterprise.module.biz.dal.mysql.returnorder;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.mybatis.core.mapper.BaseMapperX;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.enterprise.module.biz.controller.admin.returnorder.vo.returnorder.ReturnPageReqVO;
import com.enterprise.module.biz.dal.dataobject.returnorder.ReturnDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 退货单 Mapper
 *
 * @author 企业管理平台
 */
@Mapper
public interface ReturnMapper extends BaseMapperX<ReturnDO> {
    /** Current read; caller must hold a transaction. Tenant and logical-delete filters still apply. */
    default ReturnDO selectForUpdate(Long id) {
        return selectOne(new LambdaQueryWrapperX<ReturnDO>().eq(ReturnDO::getId, id).last("FOR UPDATE"));
    }


    default PageResult<ReturnDO> selectPage(ReturnPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ReturnDO>()
                .eqIfPresent(ReturnDO::getProductId, reqVO.getProductId())
                .eqIfPresent(ReturnDO::getWarehouseId, reqVO.getWarehouseId())
                .eqIfPresent(ReturnDO::getReturnType, reqVO.getReturnType())
                .eqIfPresent(ReturnDO::getStatus, reqVO.getStatus())
                .likeIfPresent(ReturnDO::getOrderCode, reqVO.getOrderCode())
                .likeIfPresent(ReturnDO::getPartyName, reqVO.getPartyName())
                .likeIfPresent(ReturnDO::getProductName, reqVO.getProductName())
                .betweenIfPresent(ReturnDO::getReturnDate, reqVO.getReturnDateRange())
                .orderByDesc(ReturnDO::getId));
    }

    /**
     * 查询指定原单的有效退货（不含已作废）
     */
    default List<ReturnDO> selectValidListByOrder(String returnType, Long orderId) {
        return selectList(new LambdaQueryWrapperX<ReturnDO>()
                .eq(ReturnDO::getReturnType, returnType)
                .eq(ReturnDO::getOrderId, orderId)
                .ne(ReturnDO::getStatus, "3").last("FOR UPDATE"));
    }

    /**
     * 汇总指定原单的已退数量（不含已作废）
     */
    default Long selectReturnedSumByOrder(String returnType, Long orderId) {
        return selectValidListByOrder(returnType, orderId).stream()
                .mapToLong(ReturnDO::getQuantity)
                .sum();
    }

}
