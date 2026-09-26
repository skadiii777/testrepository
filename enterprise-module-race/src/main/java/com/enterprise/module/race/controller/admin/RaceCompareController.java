package com.enterprise.module.race.controller.admin;

import com.enterprise.framework.common.pojo.CommonResult;
import com.enterprise.module.race.service.RaceApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "竞赛分析 - 同业对比")
@RestController
@RequestMapping("/race/compare")
public class RaceCompareController {

    @Resource
    private RaceApiService raceApiService;

    @GetMapping("/companies")
    @Operation(summary = "样本公司列表")
    @PreAuthorize("@ss.hasPermission('race:compare:query')")
    public CommonResult<?> companies() {
        return CommonResult.success(raceApiService.listCompanies());
    }

    @GetMapping("/indicators")
    @Operation(summary = "年度全样本指标（对比分析数据源）")
    @PreAuthorize("@ss.hasPermission('race:compare:query')")
    public CommonResult<Map<String, Object>> indicators(@RequestParam("year") String year,
                                                        @RequestParam(value = "code", required = false) String code) {
        return CommonResult.success(raceApiService.getIndicators(year, code));
    }

    @PostMapping("/ask")
    @Operation(summary = "年报智能问答（RAG，带页码引用与拒答）")
    @PreAuthorize("@ss.hasPermission('race:compare:query')")
    public CommonResult<Map<String, Object>> ask(@RequestParam("question") String question,
                                                 @RequestParam(value = "company", required = false) String company,
                                                 @RequestParam(value = "year", required = false) String year) {
        return CommonResult.success(raceApiService.ragAsk(question, company, year));
    }
}
