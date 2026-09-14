package com.enterprise.module.biz.service.support;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Globally random document identifiers; database unique indexes provide the final guarantee. */
public final class BizDocumentNo {
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyMMddHHmmss");
    private static final char[] ALPHANUM = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();

    private BizDocumentNo() {}
    public static String next(String prefix) {
        return prefix + UUID.randomUUID().toString().replace("-", "");
    }

    /** 人类可读短单号：前缀 + yyMMddHHmmss + 2 位随机字母数字（撞号由库内唯一键兜底） */
    public static String nextShort(String prefix) {
        char[] rand = new char[2];
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < rand.length; i++) {
            rand[i] = ALPHANUM[random.nextInt(ALPHANUM.length)];
        }
        return prefix + LocalDateTime.now().format(TS) + new String(rand);
    }
}
