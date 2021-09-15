package com.pit.core.util;


import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 环境工具类
 * @author gy
 * @version 1.0
 * @date 2020/7/23.
 */
@Slf4j
@UtilityClass
public class EnvUtil {
    /** 缓存初始化过的环境标记 */
    private static Environment iEnv = null;
    /** 启动参数环境标记 */
    private final static String SPRING_PROFILES_ACTIVE_KEY = "spring.profiles.active";

    public static final String APPLICATION_PROPERTIES_CLASSPATH = "application.properties";

    public static final String APPLICATION_YML_CLASSPATH = "application.yml";

    private static final Properties applicationProperties;

    private static Map applicationYamlMap = new HashMap();

    static {
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(APPLICATION_PROPERTIES_CLASSPATH);
        applicationProperties = new Properties();
        if (in != null) {
            try {
                applicationProperties.load(in);
            } catch (IOException e) {
            }finally {
                try {
                    in.close();
                } catch (IOException e) {
                    //
                }
            }
        }

        InputStream in2 = Thread.currentThread().getContextClassLoader().getResourceAsStream(APPLICATION_YML_CLASSPATH);
        if (in2 != null) {
            try{
                Yaml applicationYaml = new Yaml();
                applicationYamlMap = applicationYaml.loadAs(in2, Map.class);
            }finally {
                try {
                    in2.close();
                } catch (IOException e) {
                    //
                }
            }
        }
    }

    /**
     * 获取当前运行环境
     *
     * @return 环境
     */
    public static Environment getEnv() {
        if (null == iEnv) {
            initEnv();
        }
        return iEnv;
    }

    /**
     * 初始化环境
     * <p>
     * 目前仅支持通过启动参数设置
     */
    private static synchronized void initEnv() {
        if (null != iEnv) {
            return;
        }

        String env = System.getProperty(SPRING_PROFILES_ACTIVE_KEY);
        if (StringUtils.isEmpty(env)) {
            env = applicationProperties.getProperty("spring.profiles.active");
        }
        if (StringUtils.isEmpty(env)) {
            env = applicationProperties.getProperty("spring.profiles.active");
        }
        if (StringUtils.isEmpty(env)) {
            Map springMap = (Map) applicationYamlMap.get("spring");
            if (springMap != null) {
                Map profilesMap = (Map) springMap.get("profiles");
                if (profilesMap != null) {
                    env = MapUtils.getString(profilesMap, "active", null);
                }
            }
        }
        // 未设置环境参数的默认为DEV
        if (StringUtils.isEmpty(env)) {
            log.info("未找到环境配置,默认为dev环境");
            iEnv = Environment.DEV;
            return;
        }

        // 忽略大小写
        env = env.trim().toLowerCase();
        switch (env) {
            case "prod":
            case "pro":
                iEnv = Environment.PROD;
                break;
            case "gray":
                iEnv = Environment.GRAY;
                break;
            case "test":
                iEnv = Environment.TEST;
                break;
            case "dev":
                iEnv = Environment.DEV;
                break;
            default:
                log.warn("无法匹配环境配置,设为dev环境 - wrong env:{}", env);
                iEnv = Environment.DEV;
        }
    }

    /**
     * 判断是否开发环境
     *
     * @return 返回是否开发环境
     */
    public static boolean isDevEnv() {
        if (null == iEnv) {
            initEnv();
        }
        return Environment.DEV.equals(iEnv);
    }

    /**
     * 判断是否正式环境
     *
     * @return 返回是否正式环境
     */
    public static boolean isProdEnv() {
        if (null == iEnv) {
            initEnv();
        }
        return Environment.PROD.equals(iEnv);
    }

    /**
     * 判断是否灰度环境
     *
     * @return 返回是否灰度环境
     */
    public boolean isGrayEnv() {
        if (null == iEnv) {
            initEnv();
        }
        return Environment.GRAY.equals(iEnv);
    }

    /**
     * 判断是否测试环境
     *
     * @return 返回是否灰度环境
     */
    public boolean isTestEnv() {
        if (null == iEnv) {
            initEnv();
        }
        return Environment.TEST.equals(iEnv);
    }

    /**
     * 环境定义
     *
     * @author chenyesheng
     * @since 2019-07-04
     */
    public enum Environment {
        /** 开发环境 */
        DEV,
        /** 灰度环境 */
        GRAY,
        /** 测试环境 */
        TEST,
        /** 生产环境 */
        PROD
    }
}
