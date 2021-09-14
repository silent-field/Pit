package com.pit.core.thread;

import lombok.Data;

/**
 * @author gy
 *
 */
@Data
public class PitThreadInfo {
    /** 0 begin, -1 end */
    private int status;

    /** 开始时间 */
    private long begin;

    private String runningId;

    private Integer runningFlag;

    private Integer requestLogFlag;

    private String remark;


    public void copy(PitThreadInfo threadHolder) {
        this.status = threadHolder.getStatus();
        this.begin = threadHolder.getBegin();
        this.runningId = threadHolder.getRunningId();
        this.runningFlag = threadHolder.getRunningFlag();
        this.requestLogFlag = threadHolder.getRequestLogFlag();
        this.remark = threadHolder.getRemark();
    }

    @Override
    public PitThreadInfo clone() {
        PitThreadInfo th = new PitThreadInfo();
        th.setStatus(this.getStatus());
        th.setBegin(this.begin);
        th.setRunningId(this.runningId);
        th.setRunningFlag(this.runningFlag);
        th.setRequestLogFlag(this.requestLogFlag);
        th.setRemark(this.remark);
        return th;
    }

    public void append(PitThreadInfo threadHolder) {
        this.remark = this.remark + threadHolder.getRemark();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(128);
        builder.append("pitThreadHolder [status=");
        builder.append(status);
        builder.append(", begin=");
        builder.append(begin);
        builder.append(", runningId=");
        builder.append(runningId);
        builder.append(", runningFlag=");
        builder.append(runningFlag);
        builder.append(", requestLogFlag=");
        builder.append(requestLogFlag);
        builder.append(", remark=");
        builder.append(remark);
        builder.append("]");
        return builder.toString();
    }

}
