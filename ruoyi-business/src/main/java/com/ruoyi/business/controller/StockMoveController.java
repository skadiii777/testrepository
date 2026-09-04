package com.ruoyi.business.controller;

import java.util.List;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.ruoyi.business.domain.StockMove;
import com.ruoyi.business.service.IStockMoveService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;

/**
 * 库存流水 信息操作处理
 *
 * @author biz
 */
@Controller
@RequestMapping("/biz/stockMove")
public class StockMoveController extends BaseController
{
    private String prefix = "biz/stockMove";

    @Autowired
    private IStockMoveService stockMoveService;

    @RequiresPermissions("biz:stockMove:view")
    @GetMapping()
    public String stockMove()
    {
        return prefix + "/stockMove";
    }

    @RequiresPermissions("biz:stockMove:list")
    @PostMapping("/list")
    @ResponseBody
    public TableDataInfo list(StockMove stockMove)
    {
        startPage();
        List<StockMove> list = stockMoveService.selectStockMoveList(stockMove);
        return getDataTable(list);
    }

    @RequiresPermissions("biz:stockMove:export")
    @Log(title = "库存流水", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    @ResponseBody
    public AjaxResult export(StockMove stockMove)
    {
        List<StockMove> list = stockMoveService.selectStockMoveList(stockMove);
        ExcelUtil<StockMove> util = new ExcelUtil<StockMove>(StockMove.class);
        return util.exportExcel(list, "库存流水数据");
    }
}
