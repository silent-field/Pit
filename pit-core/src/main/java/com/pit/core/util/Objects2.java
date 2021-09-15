package com.pit.core.util;

import java.util.*;

/**
 * @Description:
 * @Author: gy
 * @Date: 2021/9/14
 */
public class Objects2 {
    public static <T> List<T> or(List<T> list){
        return isEmpty(list) ? list : new ArrayList<>();
    }

    public static <K,V> Map<K,V> or(Map<K,V> map){
        return isEmpty(map) ? map : new HashMap<>(0);
    }

    public static <T> Set<T> or(Set<T> set){
        return isEmpty(set) ? set : new HashSet<>();
    }

    public static String or(String s) {
        return isEmpty(s) ? s : "";
    }

    public static <T> T or(T t, T defaultValueIfNull) {
        return isEmpty(t) ? t : defaultValueIfNull;
    }

    private static  <T> boolean isEmpty(T t){
        if (t instanceof List){
            return ((List) t).size() > 0;
        }else if (t instanceof Map){
            return ((Map) t).size() >0 ;
        }else if (t instanceof Set){
            return ((Set) t).size() >0 ;
        }else if (t instanceof String){
            return ((String) t).length() > 0;
        } else {
            return t != null;
        }
    }
}
