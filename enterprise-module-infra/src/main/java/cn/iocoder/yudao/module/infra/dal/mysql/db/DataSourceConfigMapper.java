package com.enterprise.module.infra.dal.mysql.db;

import com.enterprise.framework.mybatis.core.mapper.BaseMapperX;
import com.enterprise.module.infra.dal.dataobject.db.DataSourceConfigDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据源配置 Mapper
 *
 * @author 企业管理平台源码
 */
@Mapper
public interface DataSourceConfigMapper extends BaseMapperX<DataSourceConfigDO> {
}
