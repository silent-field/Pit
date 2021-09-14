package com.pit.core.id;

import com.pit.core.time.CachingSystemTimer2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author gy
 * @date 2020/3/20
 * <p>
 * 用于生成分布式ID
 */
public class IdUtils {
    /**
     * 2020-1-1 0点 开始，根据需求改动
     */
    private final static long startTime = 1577808000000L;

    /**
     * 间隔，3年
     */
    private final static long gapLimit = 100000000000L;

    private final static long maxIndex = 9999L;

    private String machNo;

    private int partition;

    private int remain;

    public IdUtils(String machNo, int partition) {
        init(machNo, partition);

        CachingSystemTimer2.getNow();
    }

    public static boolean isLegal(String id) {
        if (StringUtils.isBlank(id)) {
            return false;
        }
        if (id.length() != 19) {
            return false;
        }
        for (int i = 0; i < id.length(); i++) {
            if (!Character.isDigit(id.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    public String getID() {
        return getID(0L);
    }

    public long getPartition(long id) {
        return id % partition;
    }

    /**
     * 获取ID (19位)
     * <p>
     * 11-------2---------4--------2
     * <p>
     * time + machineNo + index + mod20(uid)
     * <p>
     * 3年为期自动回收ID,因此从startTime开始计算3年后的id可能会重复
     *
     * @param id
     * @return
     */
    public String getID(long id) {
        StringBuilder idPre = new StringBuilder(32);
        long now = CachingSystemTimer2.getNow();
        long time = now - startTime;
        while (time > gapLimit) {
            time = now % startTime;
        }
        long index = CachingSystemTimer2.getNowIndex();
        while (index > maxIndex) {
            long tmpNow = 0L;
            do {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    // nothing to do
                }
                tmpNow = CachingSystemTimer2.getNow();
            } while (tmpNow == now);
            time = tmpNow - startTime;
            index = CachingSystemTimer2.getNowIndex();
        }
        idPre.append(time);
        idPre.append(machNo);
        if (index < 10L) {
            idPre.append("000");
        } else if (index < 100) {
            idPre.append("00");
        } else if (index < 1000) {
            idPre.append("0");
        }
        idPre.append(index);
        long mod = 0L;
        // uid!=0时候0- partition - 1
        if (0 != id) {
            mod = getPartition(id);
            // 确保2位数
            if (mod < 10) {
                idPre.append("0");
            }
        } else {
            // uid==0时候20-99
            int random = ThreadLocalRandom.current().nextInt(remain);
            mod = (long) partition + random;
        }
        idPre.append(mod);
        return idPre.toString();
    }

    public synchronized void init(String machNo, int partition) {
        if (null != this.machNo) {
            return;
        }

        if (StringUtils.isEmpty(machNo) || machNo.length() != 2 || !NumberUtils.isCreatable(machNo)) {
            throw new IllegalArgumentException("machNo is invalid");
        }

        this.machNo = machNo;

        if (partition <= 0 || partition >= 100) {
            throw new IllegalArgumentException("partition is invalid");
        }
        this.partition = partition;

        this.remain = 100 - partition;
    }
}
