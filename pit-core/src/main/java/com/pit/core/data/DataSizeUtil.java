package com.pit.core.data;

import java.text.DecimalFormat;

/**
 * @author gy
 * @version 1.0
 * @date 2020/9/18.
 * @description:
 */
public class DataSizeUtil {
    /**
     * 可读的文件大小<br>
     * 参考 http://stackoverflow.com/questions/3263892/format-file-size-as-mb-gb-etc
     *
     * @param size Long类型大小
     * @return 大小
     */
    public static String prettyLook(long size) {
        if (size <= 0) {
            return "0";
        }
        int digitGroups = Math.min(DataUnit.UNIT_NAMES.length - 1, (int) (Math.log10(size) / Math.log10(1024)));
        return new DecimalFormat("#,##0.##")
                .format(size / Math.pow(1024, digitGroups)) + " " + DataUnit.UNIT_NAMES[digitGroups];
    }
}
