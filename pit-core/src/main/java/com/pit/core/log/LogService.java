package com.pit.core.log;

import com.pit.core.exception.ExceptionUtils;
import com.pit.core.json.GsonUtils;
import com.pit.core.thread.PitThreadLocalHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;

@Slf4j
public class LogService implements ILogService {
    private LogDicHelper logDicHelper = new LogDicHelper() {
        @Override
        public Set<String> notLogStartKeys() {
            return new HashSet<>();
        }
    };

    public LogService() {

    }

    public LogService(LogDicHelper logDicHelper) {
        this.logDicHelper = logDicHelper;
    }

    public void sendInfoLog(Object... objs) {
        if (!isNeedInfoLog()) {
            return;
        }
        String msg = chainLog(objs);
        if (null == msg) {
            return;
        }
        log.info("info_log: " + ra() + msg);
    }

    public void sendWarnLog(Object... objs) {
        if (!isNeedWarnLog()) {
            return;
        }
        String msg = chainLog(objs);
        if (null == msg) {
            return;
        }
        log.warn("warn_log: " + ra() + msg);
    }

    public void sendErrorLog(Object... objs) {
        if (!isNeedErrorLog()) {
            return;
        }
        String msg = chainLog(objs);
        if (null == msg) {
            return;
        }
        log.error("error_account_log: " + ra() + msg);
    }

    private String chainLog(Object... objs) {
        StringBuilder msg = new StringBuilder(1000);
        for (Object obj : objs) {
            msg.append(obj2Str(obj) + "|");
        }
        String logStr = msg.toString();
        // 获取不需要打log的set
        Set<String> notLogSet = logDicHelper.notLogStartKeys();
        for (String notLogStr : notLogSet) {
            // 不打日志的字符串如果不为空，且日志又是以它开头则不打印
            if (StringUtils.isNotBlank(notLogStr) && logStr.startsWith(notLogStr)) {
                return null;
            }
        }
        return logStr;
    }

    /**
     * @param obj
     */
    public void sendRequestLog(Object obj) {
        if (!isNeedInfoLog()) {
            return;
        }
        log.info("request_log: " + ra() + obj2Str(obj));
    }

    /**
     * @param obj
     */
    @Override
    public void sendBeginRequestLog(Object obj) {
        if (!isNeedRequestLog()) {
            return;
        }
        log.info("request_begin_log: " + ra() + obj2Str(obj));
    }

    /**
     * @param obj
     */
    @Override
    public void sendEndRequestLog(Object obj) {
        if (!isNeedRequestLog()) {
            return;
        }
        log.info("request_end_log: " + ra() + obj2Str(obj));
    }

    @Override
    public boolean isNeedLog() {
        if (null == PitThreadLocalHolder.getRequestLogFlag()) {
            return false;
        }
        return isNeedInfoLog();
    }

    /**
     * 0,1 表示需要error log，2表示需要warning log, 3表示需要info log, 4表示需求入参出参,5表示需要RA log
     *
     * @return
     */
    private boolean isNeedRequestLog() {
        if (null == PitThreadLocalHolder.getRequestLogFlag()) {
            return false;
        }
        return PitThreadLocalHolder.getRequestLogFlag() >= 4;
    }

    /**
     * 0,1 表示需要error log，2表示需要warning log, 3表示需要info log, 4表示需求入参出参,5表示需要RA log
     *
     * @return
     */
    private boolean isNeedRALog() {
        if (null == PitThreadLocalHolder.getRequestLogFlag()) {
            return false;
        }
        return PitThreadLocalHolder.getRequestLogFlag() >= 5;
    }

    /**
     * 0,1 表示需要error log，2表示需要warning log, 3表示需要info log
     *
     * @return
     */
    private boolean isNeedInfoLog() {
        if (null == PitThreadLocalHolder.getRequestLogFlag()) {
            return false;
        }
        if (PitThreadLocalHolder.getRequestLogFlag() >= 3) {
            return true;
        }

        return false;
    }

    /**
     * 0,1 表示需要error log，2表示需要warning log, 3表示需要info log
     *
     * @return
     */
    private boolean isNeedWarnLog() {
        if (null == PitThreadLocalHolder.getRequestLogFlag()) {
            return false;
        }
        return PitThreadLocalHolder.getRequestLogFlag() >= 2;
    }

    /**
     * 0,1 表示需要error log，2表示需要warning log, 3表示需要info log
     *
     * @return
     */
    private boolean isNeedErrorLog() {
        if (null == PitThreadLocalHolder.getRequestLogFlag()) {
            return false;
        }
        return PitThreadLocalHolder.getRequestLogFlag() >= 1;
    }

    /**
     * @return 返回一个 uuid
     */
    private String ra() {
        return "ra:" + getRa() + ", ";
    }

    public String getRa() {
        return PitThreadLocalHolder.getRunningAccount();
    }

    public String objToStr(Object income) {
        return obj2Str(income);
    }

    private static String obj2Str(Object income) {
        if (null == income) {
            return "<null>";
        }

        String className = income.getClass().getName();

        if (income instanceof Class) {
            return ((Class) income).getName();
        }

        if (income instanceof String) {
            return income.toString();
        }

        if (income instanceof Throwable) {
            return ExceptionUtils.getStackTrace((Throwable) income);
        }

        String jsonStr = "";
        try {
            jsonStr = GsonUtils.toJson(income);
        } catch (Exception e) {
            jsonStr = income.toString();
        }
        return jsonStr;
    }

    public static void main(String[] args) {
        try {
            int x = 1 / 0;
        } catch (Exception t) {
            System.out.println(obj2Str(t));
        }
    }
}
