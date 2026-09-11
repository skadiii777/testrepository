package com.enterprise.module.biz.service.support;

import java.util.UUID;

/** Globally random document identifiers; database unique indexes provide the final guarantee. */
public final class BizDocumentNo {
    private BizDocumentNo() {}
    public static String next(String prefix) {
        return prefix + UUID.randomUUID().toString().replace("-", "");
    }
}
