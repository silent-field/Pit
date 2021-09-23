package com.pit.rocketmq;

import com.pit.core.dict.Dict;

/**
 * @Description:
 * @Author: gy
 * @Date: 2021/9/23
 */
public enum DelayLevelEnum {
    /**
     * 注意，一定要按顺序，时间短的在前，时间长的在后
     * messageDelayLevel=1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
     */
    SECONDS_1(1, 1),
    SECONDS_5(2, 5),
    SECONDS_10(3, 10),
    SECONDS_30(4, 30),
    MINUTES_1(5, 60),
    MINUTES_2(6, 120),
    MINUTES_3(7, 180),
    MINUTES_4(8, 240),
    MINUTES_5(9, 300),
    MINUTES_6(10, 360),
    MINUTES_7(11, 420),
    MINUTES_8(12, 480),
    MINUTES_9(13, 540),
    MINUTES_10(14, 600),
    MINUTES_20(15, 1200),
    MINUTES_30(16, 1800),
    HOURS_1(17, 3600),
    HOURS_2(18, 7200),
    ;

    private int level;
    private int seconds;

    DelayLevelEnum(int level, int seconds) {
        this.level = level;
        this.seconds = seconds;
    }

    public Integer getLevel() {
        return level;
    }

    public Integer getSeconds() {
        return seconds;
    }

    public static DelayLevelEnum get(int level) {
        for (DelayLevelEnum delayLevelEnum : DelayLevelEnum.values()) {
            if (delayLevelEnum.getLevel() == level) {
                return delayLevelEnum;
            }
        }
        return DelayLevelEnum.SECONDS_1;
    }

    /**
     * 获取延时等级，向上求值
     * @param delaySeconds
     * @return
     */
    public static DelayLevelEnum getLevelUpBySeconds(int delaySeconds) {
        if (delaySeconds > 0) {
            DelayLevelEnum[] delayLevelEnums = DelayLevelEnum.values();
            for (int i = delayLevelEnums.length - 1; i >= 0; i--) {
                if (delaySeconds == delayLevelEnums[i].getSeconds()) {
                    return delayLevelEnums[i];
                } else if (delaySeconds > delayLevelEnums[i].getSeconds()) {
                    int idx = i == delayLevelEnums.length - 1 ? i : i+1;
                    return delayLevelEnums[idx];
                }
            }
        }
        return DelayLevelEnum.SECONDS_1;
    }

    /**
     * 获取延时等级，向下求值
     * @param delaySeconds
     * @return
     */
    public static DelayLevelEnum getLevelDownBySeconds(int delaySeconds) {
        if (delaySeconds > 0) {
            DelayLevelEnum[] delayLevelEnums = DelayLevelEnum.values();
            for (int i = delayLevelEnums.length - 1; i >= 0; i--) {
                if (delaySeconds >= delayLevelEnums[i].getSeconds()) {
                    return delayLevelEnums[i];
                }
            }
        }
        return DelayLevelEnum.SECONDS_1;
    }

    /**
     * 查找最匹配的延时等级
     * @param delaySeconds
     * @return
     */
    public static DelayLevelEnum getLevelMatchBySeconds(int delaySeconds) {
        if (delaySeconds > 0) {
            DelayLevelEnum[] delayLevelEnums = DelayLevelEnum.values();
            for (int i = delayLevelEnums.length - 1; i >= 0; i--) {
                if (delaySeconds >= delayLevelEnums[i].getSeconds()) {
                    if (i < delayLevelEnums.length - 1) {
                        DelayLevelEnum upTime = delayLevelEnums[i + 1];
                        if (upTime.getSeconds() - delaySeconds < delaySeconds - delayLevelEnums[i].getSeconds()) {
                            return upTime;
                        }
                    }
                    return delayLevelEnums[i];
                }
            }
        }
        return DelayLevelEnum.SECONDS_1;
    }
}
