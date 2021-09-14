package com.pit.core.exception;

import lombok.Data;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import java.io.Serializable;

/**
 * @author gy
 *
 * 返回码
 */
public class ResultCodes {
    public ResultCodes() {
    }

    @Data
    public final static class ResultCode implements Serializable {
        private static final long serialVersionUID = -7547924594875275954L;
        private String group;
        private Integer code;
        private String message;

        public ResultCode(String group, String code, String message) {
            this.group = group;
            this.code = Integer.valueOf(code);
            this.message = message;
        }

        public ResultCode getRealResultCode(Object... args) {
            if (args != null && args.length != 0) {
                ResultCode resultCode = new ResultCode(this.group, this.code.toString(), this.message);
                FormattingTuple ft = MessageFormatter.arrayFormat(resultCode.getMessage(), args);
                resultCode.setMessage(ft.getMessage());
                return resultCode;
            } else {
                return this;
            }
        }
    }

    public static final class CommonResultCode {
        public static final ResultCode SUCCESS = new ResultCode("0", "0", "操作成功");
        public static final ResultCode NOT_EMPTY = new ResultCode("40", "400000", "{}不能为空");
        public static final ResultCode NO_SUPPORT_FILE_TYPE = new ResultCode("40", "400000", "{}文件后缀只能是{}. 当前文件名:{}");
    }
}
