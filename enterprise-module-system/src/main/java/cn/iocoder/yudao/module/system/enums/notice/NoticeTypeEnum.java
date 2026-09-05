package com.enterprise.module.system.enums.notice;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通知类型
 *
 * @author 企业管理平台源码
 */
@Getter
@AllArgsConstructor
public enum NoticeTypeEnum {

    NOTICE(1),
    ANNOUNCEMENT(2);

    /**
     * 类型
     */
    private final Integer type;

}
