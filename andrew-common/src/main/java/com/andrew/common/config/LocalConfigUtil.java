package com.andrew.common.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * 读取本地配置
 *
 * @Author Andrew
 * @Date 2019-06-15 12:06
 */
@Slf4j
public class LocalConfigUtil {
	private static final String CONFIG_BASE_DIR = "/data/service/config_dir";

	private static JSONObject configJson = null;

	public synchronized static JSONObject loadConfig(String applicationName, String key) {
		if (null != configJson) {
			return configJson;
		}

		String finalConfigFilePath =
				CONFIG_BASE_DIR + File.separator + applicationName + File.separator + "config.json";
		File file = new File(finalConfigFilePath);
		if (!file.exists()) {
			throw new IllegalArgumentException(applicationName + " config.json not exist!!");
		}

		String content;
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			StringBuilder sb = new StringBuilder(1024);
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			content = sb.toString();
		} catch (Exception e) {
			log.error("read " + applicationName + " config.json failed", e);
			throw new IllegalArgumentException(
					"read " + applicationName + " config.json failed,error:" + e.getMessage());
		}

		if (content.isEmpty()) {
			throw new IllegalArgumentException("read " + applicationName + " config.json empty");
		}

		JSONObject jsonObject;
		try {
			jsonObject = JSON.parseObject(content);
		} catch (Exception e) {
			log.error(applicationName + " config.json format invalid,to json failed", e);
			throw new IllegalArgumentException(applicationName + " config.json format invalid,to json failed");
		}

		configJson = jsonObject.getJSONObject("config");
		if (configJson == null) {
			throw new IllegalArgumentException("config.json content config is null or empty");
		}

		return configJson;
	}
}
