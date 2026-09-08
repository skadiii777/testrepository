package com.enterprise.module.biz.service.returnorder;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.module.biz.controller.admin.returnorder.vo.returnorder.ReturnPageReqVO;
import com.enterprise.module.biz.controller.admin.returnorder.vo.returnorder.ReturnSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.returnorder.ReturnDO;

/**
 * 退货单 Service 接口
 *
 * @author 企业管理平台
 */
public interface ReturnService {

    /**
     * 创建退货单（校验原单已完成、累计退货不超原单数量）
     */
    Long createReturn(ReturnSaveReqVO createReqVO);

    /**
     * 更新退货单（仅待退货状态）
     */
    void updateReturn(ReturnSaveReqVO updateReqVO);

    /**
     * 删除退货单（已退货单删除不回补库存，需人工调整）
     */
    void deleteReturn(Long id);

    /**
     * 获得退货单
     */
    ReturnDO getReturn(Long id);

    /**
     * 获得退货单分页
     */
    PageResult<ReturnDO> getReturnPage(ReturnPageReqVO pageReqVO);

    /**
     * 执行退货：销售退货入库 / 采购退货出库（联动库存），0 → 1
     */
    void executeReturn(Long id);

    /**
     * 作废退货单（仅待退货状态），0 → 3
     */
    void voidReturn(Long id);

    /**
     * 汇总指定原单的已退数量（不含已作废）
     */
    Long getReturnedSumByOrder(String returnType, Long orderId);

}
