package com.enterprise.module.biz.dal.mysql.contact;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.mybatis.core.mapper.BaseMapperX;
import com.enterprise.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.enterprise.module.biz.controller.admin.contact.vo.contact.ContactPageReqVO;
import com.enterprise.module.biz.dal.dataobject.contact.ContactDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 客户联系人 Mapper
 *
 * @author 企业管理平台
 */
@Mapper
public interface ContactMapper extends BaseMapperX<ContactDO> {

    default PageResult<ContactDO> selectPage(ContactPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ContactDO>()
                .eqIfPresent(ContactDO::getCustomerId, reqVO.getCustomerId())
                .likeIfPresent(ContactDO::getCustomerName, reqVO.getCustomerName())
                .likeIfPresent(ContactDO::getName, reqVO.getName())
                .likeIfPresent(ContactDO::getMobile, reqVO.getMobile())
                .orderByDesc(ContactDO::getId));
    }

    /**
     * 查询指定客户的全部联系人
     */
    default List<ContactDO> selectListByCustomer(Long customerId) {
        return selectList(new LambdaQueryWrapperX<ContactDO>()
                .eq(ContactDO::getCustomerId, customerId)
                .orderByAsc(ContactDO::getId));
    }

}
