package com.enterprise.module.biz.dal.mysql.fms;

import com.enterprise.framework.mybatis.core.mapper.BaseMapperX;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.enterprise.module.biz.dal.dataobject.fms.FmsVoucherEntryDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FmsVoucherEntryMapper extends BaseMapperX<FmsVoucherEntryDO> {

    default List<FmsVoucherEntryDO> selectListByVoucherId(Long voucherId) {
        return selectList(new LambdaQueryWrapperX<FmsVoucherEntryDO>()
                .eq(FmsVoucherEntryDO::getVoucherId, voucherId)
                .orderByAsc(FmsVoucherEntryDO::getSort));
    }

    default void deleteByVoucherId(Long voucherId) {
        delete(new LambdaQueryWrapperX<FmsVoucherEntryDO>()
                .eq(FmsVoucherEntryDO::getVoucherId, voucherId));
    }

}
