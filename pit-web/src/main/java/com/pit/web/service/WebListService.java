package com.pit.web.service;


import java.util.Set;

/**
 * 用于获取黑名单
 * @author gy
 * @version 1.0
 * @date 2020/7/4.
 */
public interface WebListService {
    Set<String> ipBlackList();
}
