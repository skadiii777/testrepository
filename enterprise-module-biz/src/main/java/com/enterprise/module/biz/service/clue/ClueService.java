package com.enterprise.module.biz.service.clue;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.module.biz.controller.admin.clue.vo.clue.ClueConvertReqVO;
import com.enterprise.module.biz.controller.admin.clue.vo.clue.CluePageReqVO;
import com.enterprise.module.biz.controller.admin.clue.vo.clue.ClueSaveReqVO;
import com.enterprise.module.biz.dal.dataobject.clue.ClueDO;

/**
 * 销售线索 Service 接口
 *
 * @author 企业管理平台
 */
public interface ClueService {

    /**
     * 创建线索（负责人=当前登录人，状态=待跟进）
     */
    Long createClue(ClueSaveReqVO createReqVO);

    /**
     * 更新线索（已转化/已无效为终态，不可更新）
     */
    void updateClue(ClueSaveReqVO updateReqVO);

    /**
     * 删除线索
     */
    void deleteClue(Long id);

    /**
     * 获得线索
     */
    ClueDO getClue(Long id);

    /**
     * 获得线索分页
     */
    PageResult<ClueDO> getCluePage(CluePageReqVO pageReqVO);

    /**
     * 线索转商机：创建客户（按名称不存在则新建）+ 商机，线索状态置为已转化，返回商机 id
     */
    Long convertClue(Long id, ClueConvertReqVO convertReqVO);

}
