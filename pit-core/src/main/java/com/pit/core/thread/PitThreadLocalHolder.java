package com.pit.core.thread;

import com.pit.core.id.UUIDUtils;
import com.pit.core.time.CachingSystemTimer2;

/**
 * @author gy
 */
public class PitThreadLocalHolder {
    private static ThreadLocal<PitThreadInfo> contextHolder = new ThreadLocal<PitThreadInfo>();

    /**
     * 初始化 pitThreadLocal 或者 更新 runningId
     */
    public static void initRunningAccount() {
        PitThreadInfo th = contextHolder.get();
        if (null == th) {
            th = new PitThreadInfo();
            contextHolder.set(th);
        }
        String runningId = UUIDUtils.getUUID();
        th.setRunningId(runningId);
        th.setStatus(0);
        th.setBegin(CachingSystemTimer2.getNow());
    }

    /**
     * pitThreadInfo 状态
     *
     * @return
     */
    public static int getStatus() {
        if (null == contextHolder.get()) {
            initRunningAccount();
        }
        return contextHolder.get().getStatus();
    }

    public static void setStatus(int status) {
        if (null == contextHolder.get()) {
            initRunningAccount();
        }
        contextHolder.get().setStatus(status);
    }

    public static long getBegin() {
        if (null == contextHolder.get()) {
            initRunningAccount();
        }
        return contextHolder.get().getBegin();
    }

    public static String getRunningAccount() {
        if (null == contextHolder.get()) {
            initRunningAccount();
        }
        return contextHolder.get().getRunningId();
    }

    public static Integer getRunningFlag() {
        if (null == contextHolder.get()) {
            initRunningAccount();
        }
        return contextHolder.get().getRunningFlag();
    }

    public static void setRunningFlag(Integer RunningFlag) {
        if (null == contextHolder.get()) {
            initRunningAccount();
        }
        contextHolder.get().setRunningFlag(RunningFlag);
    }

    public static Integer getRequestLogFlag() {
        if (null == contextHolder.get()) {
            initRunningAccount();
        }
        if (null == contextHolder.get().getRequestLogFlag()) {
            return 0;
        }
        return contextHolder.get().getRequestLogFlag();
    }

    public static void setRequestLogFlag(Integer requestLogFlag) {
        if (null == contextHolder.get()) {
            initRunningAccount();
        }
        contextHolder.get().setRequestLogFlag(requestLogFlag);
    }

    public static PitThreadInfo getThreadHolder() {
        if (null == contextHolder.get()) {
            initRunningAccount();
        }
        return contextHolder.get();
    }

    public static void setThreadHolder(PitThreadInfo threadHolder) {
        contextHolder.set(threadHolder);
    }

    public static String getRemark() {
        if (null == contextHolder.get()) {
            initRunningAccount();
        }
        return contextHolder.get().getRemark();
    }

    public static void setRemark(String remark) {
        if (null == contextHolder.get()) {
            initRunningAccount();
        }
        contextHolder.get().setRemark(remark);
    }

    public static void resetRemark() {
        if (null == contextHolder.get()) {
            initRunningAccount();
        }
        contextHolder.get().setRemark("");
    }

    public static void appendRemark(String remark) {
        if (null == contextHolder.get()) {
            initRunningAccount();
        }
        String oldRemark = contextHolder.get().getRemark();
        contextHolder.get().setRemark(oldRemark + remark);
    }

}
