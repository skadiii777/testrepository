package com.enterprise.module.biz.service.stockcheck;

import com.enterprise.framework.common.pojo.PageResult;
import com.enterprise.framework.common.util.object.BeanUtils;
import com.enterprise.module.biz.controller.admin.stockcheck.vo.stockcheck.StockCheckCreateReqVO;
import com.enterprise.module.biz.controller.admin.stockcheck.vo.stockcheck.StockCheckPageReqVO;
import com.enterprise.module.biz.dal.dataobject.stockcheck.StockCheckDO;
import com.enterprise.module.biz.dal.mysql.stockcheck.StockCheckMapper;
import com.enterprise.module.biz.service.stock.StockService;
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.enterprise.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.enterprise.module.biz.enums.ErrorCodeConstants.*;

/**
 * 库存盘点 Service 实现类
 *
 * @author 企业管理平台
 */
@Slf4j
@Service
@Validated
public class StockCheckServiceImpl implements StockCheckService {

    /** 状态：待确认 */
    private static final String STATUS_PENDING = "0";
    /** 状态：已确认 */
    private static final String STATUS_CONFIRMED = "1";

    @Resource
    private StockCheckMapper stockCheckMapper;
    @Resource
    private StockService stockService;

    @Override
    public Long createStockCheck(StockCheckCreateReqVO createReqVO) {
        StockCheckDO check = BeanUtils.toBean(createReqVO, StockCheckDO.class);
        check.setCheckNo(generateCheckNo());
        check.setStatus(STATUS_PENDING);
        check.setWarehouse(createReqVO.getWarehouse() == null || createReqVO.getWarehouse().isEmpty()
                ? "默认仓库" : createReqVO.getWarehouse());
        // 快照创建时的账面数量（仅作参考，确认时以当下库存为准）
        check.setBookQuantity(stockService.findQuantity(check.getProductName(), check.getWarehouse()));
        stockCheckMapper.insert(check);
        return check.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmStockCheck(Long id) {
        StockCheckDO check = validateStockCheckExists(id);
        if (!STATUS_PENDING.equals(check.getStatus())) {
            throw exception(STOCKCHECK_ALREADY_CONFIRMED);
        }
        // 确认时重新读取当下库存作为账面（创建后库存可能已变动）
        Long bookQuantity = stockService.findQuantity(check.getProductName(), check.getWarehouse());
        Long diff = check.getActualQuantity() - bookQuantity;
        if (diff != 0) {
            boolean ok = stockService.changeStock(check.getProductName(), check.getWarehouse(),
                    diff, "stockcheck", check.getCheckNo());
            if (!ok) {
                throw exception(STOCKCHECK_CONFIRM_FAILED);
            }
        }
        StockCheckDO update = new StockCheckDO();
        update.setId(id);
        update.setBookQuantity(bookQuantity);
        update.setDiffQuantity(diff);
        update.setStatus(STATUS_CONFIRMED);
        stockCheckMapper.updateById(update);
        log.info("[confirmStockCheck][盘点单({}) 确认：产品({}) 账面 {} → 实盘 {}，差异 {}]",
                id, check.getProductName(), bookQuantity, check.getActualQuantity(), diff);
    }

    @Override
    public void deleteStockCheck(Long id) {
        StockCheckDO check = validateStockCheckExists(id);
        if (STATUS_CONFIRMED.equals(check.getStatus())) {
            throw exception(STOCKCHECK_CONFIRMED_CANNOT_DELETE);
        }
        stockCheckMapper.deleteById(id);
    }

    @Override
    public PageResult<StockCheckDO> getStockCheckPage(StockCheckPageReqVO pageReqVO) {
        return stockCheckMapper.selectPage(pageReqVO);
    }

    private StockCheckDO validateStockCheckExists(Long id) {
        StockCheckDO check = stockCheckMapper.selectById(id);
        if (check == null) {
            throw exception(STOCKCHECK_NOT_EXISTS);
        }
        return check;
    }

    /**
     * 生成盘点单号：PD + yyyyMMddHHmmss
     */
    private String generateCheckNo() {
        return "PD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

}
