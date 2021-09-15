package com.pit.core.util;

import com.pit.core.exception.CommonException;
import com.pit.core.exception.ResultCodes;
import org.apache.commons.lang3.StringUtils;

/**
 * @Description: 内部断言工具, 用于快速抛出内部规范异常
 * @Author: gy
 * @Date: 2021/9/15
 */
public class Asserts {
    public static void assertTrue(final boolean flag, final ResultCodes.ResultCode resultCode) {
        if (flag) {
            throw new CommonException(resultCode);
        }
    }

    public static void assertNotNull(final Object value, final ResultCodes.ResultCode resultCode) {
        if (null == value) {
            throw new CommonException(resultCode);
        }
    }

    public static void assertNotBlank(final String check, final ResultCodes.ResultCode resultCode) {
        if (StringUtils.isEmpty(check)) {
            throw new CommonException(resultCode);
        }
    }

    public static void assertNotEmpty(final String check, final ResultCodes.ResultCode resultCode) {
        if (StringUtils.isBlank(check)) {
            throw new CommonException(resultCode);
        }
    }
}
