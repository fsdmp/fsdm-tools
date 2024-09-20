package com.fsdm.tools.util;

import com.google.common.base.Charsets;

/**
 * Created by @author liushouyun on 2022/2/16 11:24 上午.
 */
public class Crc32Utils {

    private static final long W_CPOLY = 0xEDB88320L;

    public static long crc32(String key) {
        byte[] bytes = key.getBytes(Charsets.UTF_8);
        return crc32(bytes, 0, bytes.length);
    }

    public static long crc32(byte[] source, int offset, int length) {
        long wCrcIn = 0xFFFFFFFFL;
        for (int i = offset, cnt = offset + length; i < cnt; i++) {
            wCrcIn ^= ((long) source[i] & 0x000000FFL);
            int byteCount = 8;
            for (int j = 0; j < byteCount; j++) {
                if ((wCrcIn & 0x00000001L) != 0) {
                    wCrcIn >>= 1;
                    wCrcIn ^= W_CPOLY;
                } else {
                    wCrcIn >>= 1;
                }
            }
        }
        return wCrcIn ^= 0xFFFFFFFFL;
    }
}