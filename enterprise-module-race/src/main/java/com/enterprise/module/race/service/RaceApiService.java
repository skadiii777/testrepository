package com.enterprise.module.race.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * race-api（Python FastAPI :49000）客户端：指标库 / RAG 问答的统一出口。
 * 竞赛架构分工：本模块只做业务编排与权限，AI/检索脏活全在 Python 侧。
 */
@Service
public class RaceApiService {

    private final RestClient restClient;

    public RaceApiService(@Value("${enterprise.race.api-url:http://127.0.0.1:49000}") String apiUrl) {
        this.restClient = RestClient.builder().baseUrl(apiUrl).build();
    }

    /** 公司列表（25 家银行样本）。 */
    @SuppressWarnings("unchecked")
    public java.util.List<Map<String, Object>> listCompanies() {
        Map<String, Object> body = restClient.get().uri("/indicators/companies").retrieve()
                .body(Map.class);
        return body == null ? java.util.List.of() : (java.util.List<Map<String, Object>>) body.get("data");
    }

    /** 某年度全样本指标（对比分析/看板数据源；status=pending 的值前端标“待确认”）。 */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getIndicators(String year, String code) {
        String uri = "/indicators/" + year + (code != null ? "?code=" + code : "");
        Map<String, Object> body = restClient.get().uri(uri).retrieve().body(Map.class);
        return body == null ? Map.of() : (Map<String, Object>) body.getOrDefault("data", Map.of());
    }

    /** RAG 问答（Python 侧双路：先查指标库，未命中走混合检索）。 */
    @SuppressWarnings("unchecked")
    public Map<String, Object> ragAsk(String question, String company, String year) {
        Map<String, Object> req = new java.util.HashMap<>();
        req.put("question", question);
        if (company != null) req.put("company", company);
        if (year != null) req.put("year", year);
        Map<String, Object> body = restClient.post().uri("/rag/ask")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(req).retrieve().body(Map.class);
        return body == null ? Map.of() : body;
    }
}
